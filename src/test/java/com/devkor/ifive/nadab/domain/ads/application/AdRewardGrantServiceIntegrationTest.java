package com.devkor.ifive.nadab.domain.ads.application;

import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardEvent;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardFeature;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSession;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSessionStatus;
import com.devkor.ifive.nadab.domain.ads.core.repository.AdRewardLogRepository;
import com.devkor.ifive.nadab.domain.ads.core.repository.AdRewardSessionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SSV 지급 처리(process)·일시오류 로그의 통합 검증.
 * 특히 charge(clearAutomatically) 이후 세션이 실제로 REWARDED로 반영되는지(재로드 누락 시 이중 지급 회귀)를 본다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(AdRewardGrantService.class)
class AdRewardGrantServiceIntegrationTest extends PostgresIntegrationTestSupport {

    private static final AdRewardFeature FEATURE = AdRewardFeature.ASK_CHAT_TURN_CHARGE;
    private static final long REWARD = 100L;

    @Autowired AdRewardGrantService grantService;
    @Autowired UserRepository userRepository;
    @Autowired UserWalletRepository walletRepository;
    @Autowired AdRewardSessionRepository sessionRepository;
    @Autowired AdRewardLogRepository logRepository;
    @Autowired CrystalLogRepository crystalLogRepository;

    @Test
    void 유효_콜백이면_충전되고_세션이_REWARDED로_반영된다() {
        User user = createUser(0L);
        AdRewardSession session = pendingSession(user, REWARD, future());

        AdRewardEvent result = grantService.process(session.getSessionKey(), "tx-1", true);

        assertThat(result).isEqualTo(AdRewardEvent.GRANTED);

        AdRewardSession reloaded = sessionRepository.findBySessionKey(session.getSessionKey()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(AdRewardSessionStatus.REWARDED);
        assertThat(reloaded.getTransactionId()).isEqualTo("tx-1");
        assertThat(reloaded.getRewardedAt()).isNotNull();

        assertThat(balanceOf(user)).isEqualTo(REWARD);
        assertThat(logRepository.existsBySessionIdAndEvent(session.getId(), AdRewardEvent.GRANTED)).isTrue();

        List<CrystalLog> crystalLogs = crystalLogsOf(user);
        assertThat(crystalLogs).hasSize(1);
        assertThat(crystalLogs.get(0).getReason()).isEqualTo(CrystalLogReason.AD_REWARD_GRANT);
        assertThat(crystalLogs.get(0).getDelta()).isEqualTo(REWARD);
        assertThat(crystalLogs.get(0).getBalanceAfter()).isEqualTo(REWARD);
    }

    @Test
    void 중복_콜백이어도_한_번만_지급된다() {
        User user = createUser(0L);
        AdRewardSession session = pendingSession(user, REWARD, future());

        assertThat(grantService.process(session.getSessionKey(), "tx-1", true)).isEqualTo(AdRewardEvent.GRANTED);
        assertThat(grantService.process(session.getSessionKey(), "tx-1", true)).isEqualTo(AdRewardEvent.DUPLICATE);

        assertThat(balanceOf(user)).isEqualTo(REWARD); // 두 번째 콜백에도 잔액은 그대로
        assertThat(crystalLogsOf(user)).hasSize(1);
    }

    @Test
    void 다른_세션이_이미_쓴_txid면_DUPLICATE로_막는다() {
        User user = createUser(0L);
        AdRewardSession first = pendingSession(user, REWARD, future());
        assertThat(grantService.process(first.getSessionKey(), "tx-1", true)).isEqualTo(AdRewardEvent.GRANTED);

        AdRewardSession second = pendingSession(user, REWARD, future());
        AdRewardEvent result = grantService.process(second.getSessionKey(), "tx-1", true); // 같은 txid 재사용

        assertThat(result).isEqualTo(AdRewardEvent.DUPLICATE);
        assertThat(balanceOf(user)).isEqualTo(REWARD); // 첫 지급분만, 재충전 없음
        assertThat(sessionRepository.findBySessionKey(second.getSessionKey()).orElseThrow().getStatus())
                .isEqualTo(AdRewardSessionStatus.PENDING);
    }

    @Test
    void 만료된_세션은_지급하지_않고_EXPIRED로_남긴다() {
        User user = createUser(0L);
        AdRewardSession session = pendingSession(user, REWARD, past());

        AdRewardEvent result = grantService.process(session.getSessionKey(), "tx-1", true);

        assertThat(result).isEqualTo(AdRewardEvent.SESSION_EXPIRED);
        assertThat(balanceOf(user)).isZero();
        assertThat(sessionRepository.findBySessionKey(session.getSessionKey()).orElseThrow().getStatus())
                .isEqualTo(AdRewardSessionStatus.EXPIRED);
        assertThat(logRepository.existsBySessionIdAndEvent(session.getId(), AdRewardEvent.SESSION_EXPIRED)).isTrue();
        assertThat(crystalLogsOf(user)).isEmpty();
    }

    @Test
    void 서명이_무효면_지급하지_않고_로그만_남긴다() {
        User user = createUser(0L);
        AdRewardSession session = pendingSession(user, REWARD, future());

        AdRewardEvent result = grantService.process(session.getSessionKey(), "tx-1", false);

        assertThat(result).isEqualTo(AdRewardEvent.SSV_SIGNATURE_INVALID);
        assertThat(balanceOf(user)).isZero();
        assertThat(sessionRepository.findBySessionKey(session.getSessionKey()).orElseThrow().getStatus())
                .isEqualTo(AdRewardSessionStatus.PENDING);
        assertThat(logRepository.existsBySessionIdAndEvent(session.getId(), AdRewardEvent.SSV_SIGNATURE_INVALID)).isTrue();
        assertThat(crystalLogsOf(user)).isEmpty();
    }

    @Test
    void txid가_없으면_GRANT_FAILED이고_지급하지_않는다() {
        User user = createUser(0L);
        AdRewardSession session = pendingSession(user, REWARD, future());

        AdRewardEvent result = grantService.process(session.getSessionKey(), "  ", true);

        assertThat(result).isEqualTo(AdRewardEvent.GRANT_FAILED);
        assertThat(balanceOf(user)).isZero();
        assertThat(sessionRepository.findBySessionKey(session.getSessionKey()).orElseThrow().getStatus())
                .isEqualTo(AdRewardSessionStatus.PENDING);
        assertThat(logRepository.existsBySessionIdAndEvent(session.getId(), AdRewardEvent.GRANT_FAILED)).isTrue();
    }

    @Test
    void 존재하지_않는_세션이면_무시한다() {
        AdRewardEvent result = grantService.process("no-such-session-key", "tx-1", true);
        assertThat(result).isNull();
    }

    @Test
    void 일시오류_로그는_세션당_한_건만_남긴다() {
        User user = createUser(0L);
        AdRewardSession session = pendingSession(user, REWARD, future());

        grantService.logTransientError(session.getSessionKey(), "tx-1");
        grantService.logTransientError(session.getSessionKey(), "tx-1");

        long count = logRepository.findAll().stream()
                .filter(log -> log.getSession().getId().equals(session.getId()))
                .filter(log -> log.getEvent() == AdRewardEvent.SSV_TRANSIENT_ERROR)
                .count();
        assertThat(count).isEqualTo(1);
    }

    /* ── 픽스처·조회 ─────────────────────────────────────────────────── */

    private User createUser(long balance) {
        User user = User.createUser("ad-grant+" + System.nanoTime() + "@test.com", "hashed_password");
        user.updateNickname("ag" + System.nanoTime());
        User saved = userRepository.save(user);
        walletRepository.save(UserWallet.create(saved, balance));
        return saved;
    }

    private AdRewardSession pendingSession(User user, long rewardAmount, OffsetDateTime expiresAt) {
        return sessionRepository.save(
                AdRewardSession.create(user, "sk-" + System.nanoTime(), FEATURE, rewardAmount, expiresAt));
    }

    private long balanceOf(User user) {
        return walletRepository.findByUserId(user.getId()).orElseThrow().getCrystalBalance();
    }

    private List<CrystalLog> crystalLogsOf(User user) {
        return crystalLogRepository.findAll().stream()
                .filter(log -> log.getUser().getId().equals(user.getId()))
                .toList();
    }

    private static OffsetDateTime future() {
        return OffsetDateTime.now().plusMinutes(10);
    }

    private static OffsetDateTime past() {
        return OffsetDateTime.now().minusSeconds(1);
    }
}