package com.devkor.ifive.nadab.domain.ads.core.entity;

public enum AdRewardEvent {
    SESSION_CREATED,        // 세션 발급(광고 보기 확정 시점)
    SSV_SIGNATURE_INVALID,  // 서명 확정 무효(위·변조) → 지급X, 재시도 무의미
    SSV_TRANSIENT_ERROR,    // 공개키 fetch 네트워크 오류로 검증 못함 → 5xx 재시도 유도
    GRANTED,                // 지급 완료(charge + CrystalLog)
    GRANT_FAILED,           // 서명 유효하나 지급 못 함(지갑 없음·txid 누락 등)
    DUPLICATE,              // 이미 지급됨 / txid 중복 콜백 → 멱등 무시
    SESSION_EXPIRED         // TTL 만료 세션에 콜백 도착 → 지급X
}