package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import com.devkor.ifive.nadab.domain.monthlyreport.core.content.InterestStatsContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyEmotionComparisonContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportComparisonType;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;
import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "월간 리포트 V2 조회 응답")
public record MonthlyReportResponseV2(

        @Schema(description = "리포트 대상 월")
        int month,

        @Schema(description = "리포트 상태", example = "PENDING")
        MonthlyReportStatus status,

        @Schema(description = "비교 타입", example = "BASELINE")
        MonthlyReportComparisonType comparisonType,

        @Schema(description = "요약")
        String summary,

        @Schema(description = "이미지 URL", nullable = true)
        String imageUrl,

        @Schema(description = "발견한 점(styled)")
        StyledText discovered,

        @Schema(description = "핵심 키워드")
        String dominantKeyword,

        @Schema(description = "감정 흐름 요약")
        String emotionTrend,

        @Schema(description = "감정 통계")
        TypeEmotionStatsContent emotionStats,

        @Schema(description = "직전 월간 리포트 감정 비교 스냅샷. BASELINE인 경우 null", nullable = true)
        MonthlyEmotionComparisonContent emotionComparison,

        @Schema(description = "감정 요약(styled)")
        TypeTextContent emotionSummaryContent,

        @Schema(description = "코멘트(styled)")
        StyledText comment,

        @Schema(description = "코멘트 요약")
        String commentSummary,

        @Schema(description = "관심사 통계")
        InterestStatsContent interestStats,

        @Schema(description = "월간 소셜 반응 요약")
        MonthlySocialSummaryResponse socialSummary
) {
}
