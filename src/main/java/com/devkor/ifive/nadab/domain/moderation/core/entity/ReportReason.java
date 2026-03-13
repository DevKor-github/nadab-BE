package com.devkor.ifive.nadab.domain.moderation.core.entity;

public enum ReportReason {
    PROFANITY_HATE_SPEECH, // 욕설 / 혐오 표현
    SEXUAL_CONTENT, // 성적으로 부적절한 언행
    SELF_HARM, // 자해 / 자살 조장
    OTHER // 기타 (customReason 필수)
}