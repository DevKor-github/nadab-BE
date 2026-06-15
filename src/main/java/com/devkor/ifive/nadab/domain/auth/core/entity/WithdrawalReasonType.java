package com.devkor.ifive.nadab.domain.auth.core.entity;

public enum WithdrawalReasonType {
    DAILY_LOGGING_BURDEN,           // 매일 기록이 부담
    INSUFFICIENT_QUESTION_ANALYSIS, // 질문·분석 부족
    LOSS_OF_INTEREST_IN_WRITING,    // 글쓰기 흥미 상실
    PRIVACY_RECORD_CONCERN,         // 감정·기록 보안 우려
    APP_ERROR_OR_SLOWNESS,          // 오류·속도 문제
    OTHER                           // 기타(직접 입력)
}
