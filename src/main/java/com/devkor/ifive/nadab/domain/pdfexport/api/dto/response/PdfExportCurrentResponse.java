package com.devkor.ifive.nadab.domain.pdfexport.api.dto.response;

import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 지금 생성 중인(PENDING/IN_PROGRESS) PDF 작업. 유저당 최대 1개라 단건이다.
 * PDF 탭 진입 시 진행 중 작업을 감지해 생성 화면으로 이동하는 데 쓰고,
 * 생성 중인데 다른 조건으로 또 요청해 거부(409)될 때 그 진행 중 작업 정보로도 실린다.
 * 포함 개수 3종은 생성 화면(로딩 화면)의 "포함 내용" 표시용으로 조회 시점에 즉석 계산한다.
 */
public record PdfExportCurrentResponse(
        @Schema(description = "작업 id", example = "1")
        Long jobId,

        @Schema(description = "내보내기 유형 (REPORT_ONLY/ANSWER_ONLY/REPORT_AND_ANSWER)", example = "REPORT_AND_ANSWER")
        String type,

        @Schema(description = "기간 시작일", example = "2025-11-01")
        LocalDate startDate,

        @Schema(description = "기간 종료일", example = "2025-11-30")
        LocalDate endDate,

        @Schema(description = "작업 상태 (PENDING/IN_PROGRESS)", example = "IN_PROGRESS")
        String status,

        @Schema(description = "기간 내 답변 수", example = "30")
        long answerCount,

        @Schema(description = "기간과 겹치는 완료 주간 리포트 수", example = "4")
        long weeklyCount,

        @Schema(description = "기간과 겹치는 완료 월간 리포트 수", example = "1")
        long monthlyCount
) {
    public static PdfExportCurrentResponse of(PdfExportJob job,
                                              long answerCount, long weeklyCount, long monthlyCount) {
        return new PdfExportCurrentResponse(
                job.getId(),
                job.getType().name(),
                job.getStartDate(),
                job.getEndDate(),
                job.getStatus().name(),
                answerCount,
                weeklyCount,
                monthlyCount);
    }
}