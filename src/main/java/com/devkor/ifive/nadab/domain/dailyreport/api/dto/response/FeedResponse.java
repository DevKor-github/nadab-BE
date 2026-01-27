package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "피드 응답")
public record FeedResponse(

        @Schema(description = "친구 닉네임", example = "모래")
        String friendNickname,

        @Schema(description = "친구 프로필 이미지 URL", example = "https://cdn.example.com/profiles/abc123.png")
        String friendProfileImageUrl,

        @Schema(description = "관심분야 코드", example = "EMOTION")
        String interestCode,

        @Schema(description = "질문 내용", example = "오늘 가장 기뻤던 순간은?")
        String questionText,

        @Schema(description = "답변 내용", example = "집에 갈 때")
        String answer,

        @Schema(description = "감정 코드", example = "ACHIEVEMENT")
        String emotionCode
) {
}
