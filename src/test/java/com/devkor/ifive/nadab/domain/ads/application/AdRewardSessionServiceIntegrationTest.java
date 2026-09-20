package com.devkor.ifive.nadab.domain.ads.application;

import com.devkor.ifive.nadab.domain.ads.api.dto.response.AdRewardQuoteResponse;
import com.devkor.ifive.nadab.domain.ads.api.dto.response.AdRewardSessionCreateResponse;
import com.devkor.ifive.nadab.domain.ads.api.dto.response.AdRewardSessionStatusResponse;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardEvent;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardFeature;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSession;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSessionStatus;
import com.devkor.ifive.nadab.domain.ads.core.repository.AdRewardLogRepository;
import com.devkor.ifive.nadab.domain.ads.core.repository.AdRewardSessionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.security.util.SecureRandomBytesGenerator;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 세션 발급(quote/createSession/getStatus)의 통합 검증.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({AdRewardSessionService.class, AdRewardPricing.class, SecureRandomBytesGenerator.class})
class AdRewardSessionServiceIntegrationTest extends PostgresIntegrationTestSupport {

    private static final AdRewardFeature FEATURE = AdRewardFeature.ASK_CHAT_TURN_CHARGE;

    @Autowired AdRewardSessionService sessionService;
    @Autowired AdRewardPricing pricing;
    @Autowired UserRepository userRepository;
    @Autowired UserWalletRepository walletRepository;
    @Autowired AdRewardSessionRepository sessionRepository;
    @Autowired AdRewardLogRepository logRepository;

    @Test
    void createSession_부족분으로_PENDING_세션과_로그를_만든다() {
        long cost = pricing.crystalCost(FEATURE);
        User user = createUser(0L);

        AdRewardSessionCreateResponse response = sessionService.createSession(user.getId(), FEATURE);

        assertThat(response.sessionKey()).isNotBlank();
        assertThat(response.rewardAmount()).isEqualTo(cost); // N = 비용 - 보유(0)
        assertThat(response.expiresAt()).isAfter(OffsetDateTime.now());

        AdRewardSession session = sessionRepository.findBySessionKey(response.sessionKey()).orElseThrow();
        assertThat(session.getStatus()).isEqualTo(AdRewardSessionStatus.PENDING);
        assertThat(session.getRewardAmount()).isEqualTo(cost);
        assertThat(session.getFeature()).isEqualTo(FEATURE);
        assertThat(logRepository.existsBySessionIdAndEvent(session.getId(), AdRewardEvent.SESSION_CREATED)).isTrue();
    }

    @Test
    void createSession_이미_충분하면_거부한다() {
        long cost = pricing.crystalCost(FEATURE);
        User user = createUser(cost); // 보유 = 비용 → 부족분 0

        assertThatThrownBy(() -> sessionService.createSession(user.getId(), FEATURE))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createSession_기존_PENDING_세션을_만료시킨다() {
        User user = createUser(0L);

        AdRewardSessionCreateResponse first = sessionService.createSession(user.getId(), FEATURE);
        AdRewardSessionCreateResponse second = sessionService.createSession(user.getId(), FEATURE);

        assertThat(sessionRepository.findBySessionKey(first.sessionKey()).orElseThrow().getStatus())
                .isEqualTo(AdRewardSessionStatus.EXPIRED);
        assertThat(sessionRepository.findBySessionKey(second.sessionKey()).orElseThrow().getStatus())
                .isEqualTo(AdRewardSessionStatus.PENDING);
    }

    @Test
    void quote_비용_보유_부족분을_반환한다() {
        long cost = pricing.crystalCost(FEATURE);
        User user = createUser(30L);

        AdRewardQuoteResponse response = sessionService.quote(user.getId(), FEATURE);

        assertThat(response.crystalCost()).isEqualTo(cost);
        assertThat(response.balance()).isEqualTo(30L);
        assertThat(response.requiredCrystal()).isEqualTo(cost - 30L);
    }

    @Test
    void quote_보유가_충분하면_부족분은_0이다() {
        long cost = pricing.crystalCost(FEATURE);
        User user = createUser(cost + 50L);

        AdRewardQuoteResponse response = sessionService.quote(user.getId(), FEATURE);

        assertThat(response.requiredCrystal()).isZero();
    }

    @Test
    void getStatus_본인_세션이_아니면_거부한다() {
        User owner = createUser(0L);
        User other = createUser(0L);
        AdRewardSession session = pendingSession(owner, 100L, future());

        assertThatThrownBy(() -> sessionService.getStatus(other.getId(), session.getSessionKey()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getStatus_없는_세션이면_NotFound() {
        User user = createUser(0L);

        assertThatThrownBy(() -> sessionService.getStatus(user.getId(), "no-such-session-key"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getStatus_지급완료면_REWARDED를_반환한다() {
        User user = createUser(0L);
        AdRewardSession session = AdRewardSession.create(user, "sk-" + System.nanoTime(), FEATURE, 100L, future());
        session.markRewarded("tx-1");
        sessionRepository.save(session);

        AdRewardSessionStatusResponse response = sessionService.getStatus(user.getId(), session.getSessionKey());

        assertThat(response.status()).isEqualTo(AdRewardSessionStatus.REWARDED);
        assertThat(response.rewardAmount()).isEqualTo(100L);
    }

    @Test
    void getStatus_대기중이고_만료전이면_PENDING을_반환한다() {
        User user = createUser(0L);
        AdRewardSession session = pendingSession(user, 100L, future());

        AdRewardSessionStatusResponse response = sessionService.getStatus(user.getId(), session.getSessionKey());

        assertThat(response.status()).isEqualTo(AdRewardSessionStatus.PENDING);
    }

    @Test
    void getStatus_PENDING이라도_TTL이_지나면_EXPIRED로_반환한다() {
        User user = createUser(0L);
        AdRewardSession session = pendingSession(user, 100L, past());

        AdRewardSessionStatusResponse response = sessionService.getStatus(user.getId(), session.getSessionKey());

        assertThat(response.status()).isEqualTo(AdRewardSessionStatus.EXPIRED);
        assertThat(response.rewardAmount()).isEqualTo(100L);
        // 계산값일 뿐 DB 상태는 그대로 PENDING
        assertThat(sessionRepository.findBySessionKey(session.getSessionKey()).orElseThrow().getStatus())
                .isEqualTo(AdRewardSessionStatus.PENDING);
    }

    /* ── 픽스처 ─────────────────────────────────────────────────────── */

    private User createUser(long balance) {
        User user = User.createUser("ad-session+" + System.nanoTime() + "@test.com", "hashed_password");
        user.updateNickname("as" + System.nanoTime());
        User saved = userRepository.save(user);
        walletRepository.save(UserWallet.create(saved, balance));
        return saved;
    }

    private AdRewardSession pendingSession(User user, long rewardAmount, OffsetDateTime expiresAt) {
        return sessionRepository.save(
                AdRewardSession.create(user, "sk-" + System.nanoTime(), FEATURE, rewardAmount, expiresAt));
    }

    private static OffsetDateTime future() {
        return OffsetDateTime.now().plusMinutes(10);
    }

    private static OffsetDateTime past() {
        return OffsetDateTime.now().minusSeconds(1);
    }
}