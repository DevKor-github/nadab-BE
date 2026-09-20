package com.devkor.ifive.nadab.domain.ads.application;

import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardEvent;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardLog;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardSession;
import com.devkor.ifive.nadab.domain.ads.core.repository.AdRewardLogRepository;
import com.devkor.ifive.nadab.domain.ads.core.repository.AdRewardSessionRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

// SSV 검증 통과 후 지급 처리(멱등). 비즈니스 결과는 정상 반환(→200), 일시 오류는 예외가 전파되어 재시도 유도(→5xx).
@Slf4j
@Service
@RequiredArgsConstructor
public class AdRewardGrantService {

    private static final String REF_TYPE = "AD_REWARD_SESSION";

    private final AdRewardSessionRepository sessionRepository;
    private final AdRewardLogRepository logRepository;
    private final UserWalletRepository userWalletRepository;
    private final CrystalLogRepository crystalLogRepository;

    // 반환은 처리 결과(로깅/테스트용). null = 처리할 세션 없음(앱로그만). 어떤 결과든 재시도 유도 안 함(200).
    @Transactional
    public AdRewardEvent process(String sessionKey, String transactionId, boolean signatureValid) {
        // 서명 확정 무효 — 지급 X. 실제 세션이 있으면 위변조 흔적으로 기록
        if (!signatureValid) {
            sessionRepository.findBySessionKey(sessionKey).ifPresent(s ->
                    saveLog(s, AdRewardEvent.SSV_SIGNATURE_INVALID, transactionId, null));
            return AdRewardEvent.SSV_SIGNATURE_INVALID;
        }

        AdRewardSession session = sessionRepository.findBySessionKeyForUpdate(sessionKey).orElse(null);
        if (session == null) {
            log.warn("SSV 콜백: 세션 없음(무시) sessionKey={}", mask(sessionKey));
            return null;
        }

        // 멱등: 이미 지급된 세션
        if (session.isRewarded()) {
            saveLog(session, AdRewardEvent.DUPLICATE, transactionId, "already rewarded");
            return AdRewardEvent.DUPLICATE;
        }

        // 만료
        if (!session.isPending() || session.isExpired(OffsetDateTime.now())) {
            session.markExpired();
            saveLog(session, AdRewardEvent.SESSION_EXPIRED, transactionId, null);
            return AdRewardEvent.SESSION_EXPIRED;
        }

        // txid 누락 — 콜백 불량으로 지급 불가(재시도 무의미)
        if (transactionId == null || transactionId.isBlank()) {
            saveLog(session, AdRewardEvent.GRANT_FAILED, transactionId, "missing transaction_id");
            return AdRewardEvent.GRANT_FAILED;
        }

        // 멱등: 이미 처리된 txid(중복 콜백)
        if (sessionRepository.existsByTransactionId(transactionId)) {
            saveLog(session, AdRewardEvent.DUPLICATE, transactionId, "duplicate transaction_id");
            return AdRewardEvent.DUPLICATE;
        }

        int rows = userWalletRepository.charge(session.getUserId(), session.getRewardAmount());
        // charge(clearAutomatically)가 영속성 컨텍스트를 비워 기존 session은 detached → 재로드해 이후 변경이 flush되게
        AdRewardSession managed = sessionRepository.findById(session.getId())
                .orElseThrow(() -> new IllegalStateException("locked ad reward session missing: " + session.getId()));
        if (rows == 0) {
            // 지갑 없음 등 — 영구 실패(재시도 무의미). 세션이 있는데 지갑이 없는 건 정상적으로 불가 → 이상신호
            log.error("SSV 지급 실패: 지갑 없음(데이터 이상) sessionId={}, userId={}", managed.getId(), managed.getUserId());
            saveLog(managed, AdRewardEvent.GRANT_FAILED, transactionId, "charge returned 0 (wallet missing)");
            return AdRewardEvent.GRANT_FAILED;
        }

        long balanceAfter = userWalletRepository.findByUserId(managed.getUserId())
                .map(UserWallet::getCrystalBalance)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
        crystalLogRepository.save(CrystalLog.createConfirmed(
                managed.getUser(), managed.getRewardAmount(), balanceAfter,
                CrystalLogReason.AD_REWARD_GRANT, REF_TYPE, managed.getId()));

        managed.markRewarded(transactionId);
        saveLog(managed, AdRewardEvent.GRANTED, transactionId, null);
        return AdRewardEvent.GRANTED;
    }

    // 검증 일시 오류를 durable 로그로 남김. 구글 재시도로 콜백이 반복되므로 세션당 1건만 기록.
    @Transactional
    public void logTransientError(String sessionKey, String transactionId) {
        sessionRepository.findBySessionKey(sessionKey).ifPresent(session -> {
            if (!logRepository.existsBySessionIdAndEvent(session.getId(), AdRewardEvent.SSV_TRANSIENT_ERROR)) {
                saveLog(session, AdRewardEvent.SSV_TRANSIENT_ERROR, transactionId, null);
            }
        });
    }

    private void saveLog(AdRewardSession session, AdRewardEvent event, String transactionId, String detail) {
        logRepository.save(AdRewardLog.create(session.getUser(), session, event, detail, blankToNull(transactionId)));
    }

    private String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }

    private String mask(String v) {
        if (v == null || v.length() <= 6) {
            return "***";
        }
        return v.substring(0, 3) + "***" + v.substring(v.length() - 3);
    }
}