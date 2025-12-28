package com.devkor.ifive.nadab.domain.dailyreport.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "오늘의 리포트 생성 요청")
public record DailyReportRequest(
        @Schema(example = "질문ID")
        @NotNull(message = "questionId는 필수입니다")
        Long questionId,

        @Schema(example = "답변")
        @NotBlank(message = "answer는 필수입니다")
        String answer
) {
}
