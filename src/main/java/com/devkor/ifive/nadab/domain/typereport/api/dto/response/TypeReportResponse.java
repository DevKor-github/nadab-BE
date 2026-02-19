package com.devkor.ifive.nadab.domain.typereport.api.dto.response;

public record TypeReportResponse(
        String status,
        String analysisTypeName,
        String hashTag1,
        String hashTag2,
        String hashTag3,
        String typeAnalysis,
        String personaTitle1,
        String personaContent1,
        String personaTitle2,
        String personaContent2,
        String typeImageUrl
) {
}
