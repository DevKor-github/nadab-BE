package com.devkor.ifive.nadab.domain.test.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "테스트용 오늘의 리포트 생성 요청")
public record TestDailyReportRequest(

        @Schema(example = "질문")
        @NotBlank(message = "question은 필수입니다")
        String question,

        @Schema(example = "답변")
        @NotBlank(message = "answer는 필수입니다")
        String answer
) {
}
