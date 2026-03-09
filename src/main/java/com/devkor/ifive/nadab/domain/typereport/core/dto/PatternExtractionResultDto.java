package com.devkor.ifive.nadab.domain.typereport.core.dto;

import java.util.List;

public record PatternExtractionResultDto(
        List<PatternDto> patterns
) {
    public record PatternDto(
            String label,
            List<String> evidenceIds,
            String note
    ) {}
}