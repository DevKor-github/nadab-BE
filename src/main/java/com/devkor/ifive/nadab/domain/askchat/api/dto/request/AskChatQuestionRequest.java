package com.devkor.ifive.nadab.domain.askchat.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "물어보기 질문 전송 요청")
public record AskChatQuestionRequest(
        @Schema(description = "사용자 질문 내용. 공백 제외 1자 이상 200자 이하로 입력해야 합니다.", example = "나는 어떤 사람이야?")
        @NotBlank
        @Size(max = 200)
        String content
) {
}
