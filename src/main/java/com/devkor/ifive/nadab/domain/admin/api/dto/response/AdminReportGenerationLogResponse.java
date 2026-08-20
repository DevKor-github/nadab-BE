package com.devkor.ifive.nadab.domain.admin.api.dto.response;

import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLog;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLogStatus;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationStep;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "관리자 리포트 생성 로그")
public record AdminReportGenerationLogResponse(
        @Schema(description = "로그 ID", example = "1001")
        Long id,

        @Schema(description = "로그 사용자")
        AdminLogUserResponse user,

        @Schema(description = "리포트 유형", example = "MONTHLY_V2")
        ReportGenerationType reportType,

        @Schema(description = "리포트 ID", example = "2001")
        Long reportId,

        @Schema(description = "생성 단계", example = "MONTHLY_V2_TEXT_CONFIRM")
        ReportGenerationStep step,

        @Schema(description = "로그 상태", example = "SUCCEEDED")
        ReportGenerationLogStatus status,

        @Schema(description = "LLM 제공자", example = "OPENAI")
        LlmProvider llmProvider,

        @Schema(description = "LLM 모델", example = "GPT_4_O_MINI")
        String llmModel,

        @Schema(description = "내부 오류 코드", example = "AI_NO_RESPONSE")
        String errorCode,

        @Schema(description = "예외 클래스", example = "com.example.AiException")
        String exceptionClass,

        @Schema(description = "외부 응답 HTTP 상태", example = "503")
        Integer httpStatus,

        @Schema(description = "외부 오류 코드", example = "HTTP_503")
        String externalErrorCode,

        @Schema(description = "처리 시간(ms)", example = "1250")
        Long elapsedMs,

        @Schema(description = "입력 토큰 수", example = "1000")
        Long inputTokens,

        @Schema(description = "출력 토큰 수", example = "500")
        Long outputTokens,

        @Schema(description = "전체 토큰 수", example = "1500")
        Long totalTokens,

        @Schema(description = "추론 토큰 수", example = "300")
        Long thinkingTokens,

        @Schema(description = "생성 시작 시각")
        OffsetDateTime startedAt,

        @Schema(description = "생성 종료 시각")
        OffsetDateTime endedAt,

        @Schema(description = "로그 생성 시각")
        OffsetDateTime createdAt
) {

    public static AdminReportGenerationLogResponse from(ReportGenerationLog log) {
        return new AdminReportGenerationLogResponse(
                log.getId(),
                AdminLogUserResponse.from(log.getUser()),
                log.getReportType(),
                log.getReportId(),
                log.getStep(),
                log.getStatus(),
                log.getLlmProvider(),
                log.getLlmModel(),
                log.getErrorCode(),
                log.getExceptionClass(),
                log.getHttpStatus(),
                log.getExternalErrorCode(),
                log.getElapsedMs(),
                log.getInputTokens(),
                log.getOutputTokens(),
                log.getTotalTokens(),
                log.getThinkingTokens(),
                log.getStartedAt(),
                log.getEndedAt(),
                log.getCreatedAt()
        );
    }
}
