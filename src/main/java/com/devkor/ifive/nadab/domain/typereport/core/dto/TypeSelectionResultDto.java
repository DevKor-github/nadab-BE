package com.devkor.ifive.nadab.domain.typereport.core.dto;

import java.util.List;

public record TypeSelectionResultDto(
        String analysisTypeCode,
        int confidence,
        List<BecauseDto> because
) {
    public record BecauseDto(
            String pattern,
            List<String> evidenceIds
    ) {}
}
