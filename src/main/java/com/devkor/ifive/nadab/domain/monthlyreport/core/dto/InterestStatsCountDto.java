package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

public record InterestStatsCountDto(
        InterestCode interestCode,
        String interestName,
        Long count
) {
}
