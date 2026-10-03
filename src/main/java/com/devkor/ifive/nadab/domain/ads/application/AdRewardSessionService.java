package com.devkor.ifive.nadab.domain.ads.application;

import com.devkor.ifive.nadab.domain.ads.api.dto.response.AdRewardQuoteResponse;
import com.devkor.ifive.nadab.domain.ads.api.dto.response.AdRewardSessionCreateResponse;
import com.devkor.ifive.nadab.domain.ads.api.dto.response.AdRewardSessionStatusResponse;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardEvent;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardFeature;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardLog;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSession;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSessionStatus;
import com.devkor.ifive.nadab.domain.ads.core.repository.AdRewardLogRepository;
import com.devkor.ifive.nadab.domain.ads.core.repository.AdRewardSessionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.security.util.SecureRandomBytesGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AdRewardSessionService {

    private static final int SESSION_KEY_BYTES = 16; // 128비트
    private static final long SESSION_TTL_SECONDS = 600; // 10분

    private final AdRewardSessionRepository sessionRepository;
    private final AdRewardLogRepository logRepository;
    private final UserWalletRepository userWalletRepository;
    private final UserRepository userRepository;
    private final AdRewardPricing pricing;
    private final SecureRandomBytesGenerator secureRandomBytesGenerator;

    // preview: 부족분 조회
    @Transactional(readOnly = true)
    public AdRewardQuoteResponse quote(Long userId, AdRewardFeature feature) {
        long crystalCost = pricing.crystalCost(feature);
        long balance = balanceOf(userId);
        long requiredCrystal = Math.max(0, crystalCost - balance);
        return new AdRewardQuoteResponse(feature, crystalCost, balance, requiredCrystal);
    }

    // 세션 발급: 활성 세션 1개 + fresh N 확정 + SESSION_CREATED 로그
    @Transactional
    public AdRewardSessionCreateResponse createSession(Long userId, AdRewardFeature feature) {
        long crystalCost = pricing.crystalCost(feature);
        long balance = balanceOf(userId);
        long n = crystalCost - balance;
        if (n <= 0) {
            // 이미 충분 → 광고 불필요
            throw new BadRequestException(ErrorCode.AD_REWARD_NOT_NEEDED);
        }

        sessionRepository.expirePendingSessions(userId);

        String sessionKey = generateSessionKey();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(SESSION_TTL_SECONDS);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        AdRewardSession session = sessionRepository.save(
                AdRewardSession.create(user, sessionKey, feature, n, expiresAt));
        logRepository.save(AdRewardLog.create(user, session, AdRewardEvent.SESSION_CREATED, null, null));

        return new AdRewardSessionCreateResponse(sessionKey, n, expiresAt);
    }

    // poll: 상태 조회(본인 세션만). status는 계산값 — TTL 지나면 EXPIRED
    @Transactional(readOnly = true)
    public AdRewardSessionStatusResponse getStatus(Long userId, String sessionKey) {
        AdRewardSession session = sessionRepository.findBySessionKey(sessionKey)
                .orElseThrow(() -> new NotFoundException(ErrorCode.AD_REWARD_SESSION_NOT_FOUND));
        if (!session.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.AD_REWARD_SESSION_ACCESS_FORBIDDEN);
        }
        return new AdRewardSessionStatusResponse(effectiveStatus(session), session.getRewardAmount());
    }

    private AdRewardSessionStatus effectiveStatus(AdRewardSession session) {
        if (session.isRewarded()) {
            return AdRewardSessionStatus.REWARDED;
        }
        if (session.isPending() && session.isExpired(OffsetDateTime.now())) {
            return AdRewardSessionStatus.EXPIRED;
        }
        return session.getStatus();
    }

    private long balanceOf(Long userId) {
        return userWalletRepository.findByUserId(userId)
                .map(UserWallet::getCrystalBalance)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
    }

    private String generateSessionKey() {
        byte[] bytes = secureRandomBytesGenerator.generate(SESSION_KEY_BYTES);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}