package com.devkor.ifive.nadab.domain.typereport.core.dto;

public record TypeReportGenerationRequestedEventDto(
        Long reportId,
        Long userId,
        Long crystalLogId
) {}
