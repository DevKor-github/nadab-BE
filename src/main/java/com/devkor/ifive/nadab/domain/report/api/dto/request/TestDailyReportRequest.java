package com.devkor.ifive.nadab.domain.report.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "테스트용 오늘의 리포트 생성 요청")
public record TestDailyReportRequest(
        @Schema(example = "프롬프트")
        @NotBlank(message = "prompt는 필수입니다")
        String prompt,

        @Schema(example = "0.7")
        @NotBlank(message = "temperature는 필수입니다")
        Double temperature,

        @Schema(example = "질문")
        @NotBlank(message = "question은 필수입니다")
        String question,

        @Schema(example = "답변")
        @NotBlank(message = "answer는 필수입니다")
        String answer
) {
}
