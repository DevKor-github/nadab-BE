package com.devkor.ifive.nadab.domain.dailyreport.core.dto;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

public record InterestCompletedCountDto(
        InterestCode interestCode,
        long completedCount
) {}
