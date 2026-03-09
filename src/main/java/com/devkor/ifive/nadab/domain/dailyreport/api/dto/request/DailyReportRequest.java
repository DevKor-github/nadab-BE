package com.devkor.ifive.nadab.domain.dailyreport.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "오늘의 리포트 생성 요청")
public record DailyReportRequest(
        @Schema(description = "질문 ID", example = "1")
        @NotNull(message = "questionId는 필수입니다")
        Long questionId,

        @Schema(description = "유저의 답변 내용")
        @Size(max = 200, message = "answer는 최대 200자까지 입력 가능합니다")
        @Size(min = 1, message = "answer는 최소 1자 이상 입력해야 합니다")
        @NotBlank(message = "answer는 필수입니다")
        String answer
) {
}
