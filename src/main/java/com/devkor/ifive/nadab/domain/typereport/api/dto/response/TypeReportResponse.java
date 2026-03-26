package com.devkor.ifive.nadab.domain.typereport.api.dto.response;

import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;

public record TypeReportResponse(
        String status,
        String analysisTypeName,
        String hashTag1,
        String hashTag2,
        String hashTag3,
        String typeAnalysis,

        TypeTextContent typeAnalysisContent,
        TypeTextContent emotionSummaryContent,
        TypeEmotionStatsContent emotionStats,

        String personaTitle1,
        String personaContent1,
        String personaTitle2,
        String personaContent2,
        String typeImageUrl
) {
}
