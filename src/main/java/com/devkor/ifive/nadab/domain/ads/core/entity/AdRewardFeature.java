package com.devkor.ifive.nadab.domain.ads.core.entity;

// 광고 보상 대상 기능 식별자. 가격은 여기 두지 않고 AdRewardPricing이 기존 소스로 위임.
public enum AdRewardFeature {
    PDF_REPORT_ONLY,
    PDF_ANSWER_ONLY,
    PDF_REPORT_AND_ANSWER,
    ASK_CHAT_TURN_CHARGE
}