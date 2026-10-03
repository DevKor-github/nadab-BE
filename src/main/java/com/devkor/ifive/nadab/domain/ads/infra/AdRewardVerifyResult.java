package com.devkor.ifive.nadab.domain.ads.infra;

public enum AdRewardVerifyResult {
    VALID,              // 서명 유효
    INVALID_SIGNATURE,  // 서명 확정 무효 → 200(재시도 무의미)
    TRANSIENT_ERROR     // 키 fetch/네트워크 등 일시 오류 → 5xx(구글 재시도 유도, 리워드 살림)
}