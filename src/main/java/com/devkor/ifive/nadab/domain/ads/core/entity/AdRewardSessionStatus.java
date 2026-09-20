package com.devkor.ifive.nadab.domain.ads.core.entity;

public enum AdRewardSessionStatus {
    PENDING,    // 지급 대기 — 광고 시청·SSV 콜백을 기다리는 상태
    REWARDED,   // 지급 완료(SSV 검증 성공 → 크리스탈 적립)
    EXPIRED     // TTL 만료(지급 없이 종료)
}