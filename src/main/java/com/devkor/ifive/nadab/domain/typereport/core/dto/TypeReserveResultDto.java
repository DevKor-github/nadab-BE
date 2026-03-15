package com.devkor.ifive.nadab.domain.typereport.core.dto;

public record TypeReserveResultDto(
        Long reportId,
        Long crystalLogId,
        Long userId,
        long balanceAfter,
        Long previousCompletedReportId
) {}