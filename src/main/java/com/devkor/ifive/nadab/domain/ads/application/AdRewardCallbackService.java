package com.devkor.ifive.nadab.domain.ads.application;

import com.devkor.ifive.nadab.domain.ads.infra.AdRewardVerifyResult;
import com.devkor.ifive.nadab.domain.ads.infra.AdmobSignatureVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// SSV 콜백 오케스트레이션: 검증(트랜잭션 밖) → 지급 처리. 검증 자체가 외부 I/O라 tx 밖에서 수행.
@Slf4j
@Service
@RequiredArgsConstructor
public class AdRewardCallbackService {

    private final AdmobSignatureVerifier verifier;
    private final AdRewardGrantService grantService;

    // 반환 true = 구글 재시도 유도(5xx), false = 처리 완료(200).
    public boolean handle(HttpServletRequest request) {
        // getParameter는 URL 디코드된 값(파싱용). 서명 검증은 verifier가 원본 getQueryString 사용.
        String sessionKey = nullSafe(request.getParameter("custom_data"));
        String transactionId = nullSafe(request.getParameter("transaction_id"));
        String signature = nullSafe(request.getParameter("signature"));
        String keyId = nullSafe(request.getParameter("key_id"));

        // probe(콘솔 Verify, custom_data 없음)·서명 메타 없음 → 무시하고 200
        if (sessionKey.isBlank() || signature.isBlank() || keyId.isBlank()) {
            log.info("SSV 콜백 무시(probe 또는 서명 메타 없음)");
            return false;
        }

        AdRewardVerifyResult verifyResult = verifier.verify(request);
        if (verifyResult == AdRewardVerifyResult.TRANSIENT_ERROR) {
            // 키 fetch 등 일시 오류 — 유효할 수도 있으니 구글이 재시도하게(리워드 살림)
            try {
                grantService.logTransientError(sessionKey, transactionId);
            } catch (Exception e) {
                // 로그 저장 실패(DB 문제 등)해도 재시도는 유도해야 하므로 결과는 그대로 5xx
                log.error("SSV 일시 오류 로그 저장 실패", e);
            }
            return true;
        }
        boolean signatureValid = verifyResult == AdRewardVerifyResult.VALID;

        try {
            grantService.process(sessionKey, transactionId, signatureValid);
            return false;
        } catch (Exception e) {
            // 지급 처리 중 일시 오류(DB 등) — 재시도 유도(리워드 살림)
            log.error("SSV 지급 처리 중 오류(재시도 유도)", e);
            return true;
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value.trim();
    }
}