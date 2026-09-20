package com.devkor.ifive.nadab.domain.ads.application;

import com.devkor.ifive.nadab.domain.ads.api.dto.response.AdRewardSessionCreateResponse;
import com.devkor.ifive.nadab.domain.ads.api.dto.response.AdRewardSessionStatusResponse;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardEvent;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardFeature;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSessionStatus;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.security.util.SecureRandomBytesGenerator;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 사용자 시퀀스: 세션 발급 → (광고 시청 가정) SSV 지급 → 폴링에서 REWARDED.
 * 서명 검증(암호 라운드트립)은 배포 시 AdMob Verify로 커버하고, 여기선 검증 통과(true)를 가정한다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({AdRewardSessionService.class, AdRewardGrantService.class, AdRewardPricing.class, SecureRandomBytesGenerator.class})
class AdRewardFlowIntegrationTest extends PostgresIntegrationTestSupport {

    private static final AdRewardFeature FEATURE = AdRewardFeature.ASK_CHAT_TURN_CHARGE;

    @Autowired AdRewardSessionService sessionService;
    @Autowired AdRewardGrantService grantService;
    @Autowired AdRewardPricing pricing;
    @Autowired UserRepository userRepository;
    @Autowired UserWalletRepository walletRepository;

    @Test
    void 세션발급부터_지급_폴링까지_실제_흐름() {
        long cost = pricing.crystalCost(FEATURE);
        User user = createUser(0L);

        // 1) 광고 보기 확정 → 세션 발급 (부족분 = 비용)
        AdRewardSessionCreateResponse created = sessionService.createSession(user.getId(), FEATURE);
        assertThat(created.rewardAmount()).isEqualTo(cost);

        // 2) 광고 시청 후 구글 SSV 콜백(서명 검증 통과 가정) → 지급
        AdRewardEvent result = grantService.process(created.sessionKey(), "tx-flow-1", true);
        assertThat(result).isEqualTo(AdRewardEvent.GRANTED);

        // 3) 잔액 충전 + 폴링에서 REWARDED 확인
        assertThat(walletRepository.findByUserId(user.getId()).orElseThrow().getCrystalBalance())
                .isEqualTo(cost);
        AdRewardSessionStatusResponse status = sessionService.getStatus(user.getId(), created.sessionKey());
        assertThat(status.status()).isEqualTo(AdRewardSessionStatus.REWARDED);
        assertThat(status.rewardAmount()).isEqualTo(cost);
    }

    private User createUser(long balance) {
        User user = User.createUser("ad-flow+" + System.nanoTime() + "@test.com", "hashed_password");
        user.updateNickname("af" + System.nanoTime());
        User saved = userRepository.save(user);
        walletRepository.save(UserWallet.create(saved, balance));
        return saved;
    }
}