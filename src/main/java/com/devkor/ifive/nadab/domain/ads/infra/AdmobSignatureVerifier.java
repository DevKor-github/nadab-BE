package com.devkor.ifive.nadab.domain.ads.infra;

import com.google.crypto.tink.apps.rewardedads.RewardedAdsVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

// AdMob SSV 서명 검증(Tink). 공개키는 Tink가 gstatic에서 자동 fetch/캐시
@Slf4j
@Component
public class AdmobSignatureVerifier {

    private final RewardedAdsVerifier verifier;

    public AdmobSignatureVerifier() throws GeneralSecurityException {
        this(new RewardedAdsVerifier.Builder()
                .fetchVerifyingPublicKeysWith(RewardedAdsVerifier.KEYS_DOWNLOADER_INSTANCE_PROD)
                .build());
    }

    // 테스트에서 검증기(키 소스) 주입용
    AdmobSignatureVerifier(RewardedAdsVerifier verifier) {
        this.verifier = verifier;
    }

    // 콜백을 받은 원본 쿼리스트링 그대로 검증(디코드/재정렬 금지)
    public AdRewardVerifyResult verify(HttpServletRequest request) {
        String rewardUrl = request.getRequestURL().toString()
                + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
        try {
            verifier.verify(rewardUrl);
            return AdRewardVerifyResult.VALID;
        } catch (Exception e) {
            // 키 fetch/네트워크(IOException) 계열은 일시 오류 — 서명이 유효할 수도 있으니 재시도 유도
            if (hasCause(e, IOException.class)) {
                log.warn("SSV 검증 일시 오류(재시도 유도): {}", e.toString());
                return AdRewardVerifyResult.TRANSIENT_ERROR;
            }
            // 그 외는 서명 확정 무효 — 재시도 무의미
            log.warn("SSV 서명 무효: {}", e.getMessage());
            return AdRewardVerifyResult.INVALID_SIGNATURE;
        }
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        for (Throwable c = throwable; c != null; c = c.getCause()) {
            if (type.isInstance(c)) {
                return true;
            }
        }
        return false;
    }
}