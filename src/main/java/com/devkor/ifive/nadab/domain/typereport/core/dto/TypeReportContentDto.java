package com.devkor.ifive.nadab.domain.typereport.core.dto;

import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;

import java.util.List;

public record TypeReportContentDto(
        String analysisTypeCode,
        String typeAnalysis,
        TypeTextContent typeAnalysisContent,
        TypeTextContent emotionSummaryContent,
        List<PersonaDto> personas
) {
    public record PersonaDto(
            String title,
            String content
    ) {}
}