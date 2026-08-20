package com.devkor.ifive.nadab.domain.stats.core.dto.type;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

import java.time.LocalDate;

public record TypeReportDateInterestCountDto(
        LocalDate date,
        InterestCode interestCode,
        long count
) {}
