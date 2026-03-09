package com.devkor.ifive.nadab.domain.typereport.core.dto;

import java.util.List;

public record TypeReportContentDto(
        String analysisTypeCode,
        String typeAnalysis,
        List<PersonaDto> personas
) {
    public record PersonaDto(
            String title,
            String content
    ) {}
}