package com.devkor.ifive.nadab.domain.stats.core.dto.type;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

public record TypeReportInterestCountDto(
        InterestCode interestCode,
        long count
) {}
