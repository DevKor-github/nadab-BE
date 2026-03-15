package com.devkor.ifive.nadab.domain.dailyreport.core.dto;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.Emotion;

public record ConfirmDailyAndRewardDto(
        Emotion emotion,
        long balanceAfter
) {
}
