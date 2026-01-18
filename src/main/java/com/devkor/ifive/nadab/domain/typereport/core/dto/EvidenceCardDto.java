package com.devkor.ifive.nadab.domain.typereport.core.dto;

import java.time.LocalDate;

public record EvidenceCardDto(
        String id,          // D1, D2, ...
        LocalDate date,
        String card         // 120~180자
) {}