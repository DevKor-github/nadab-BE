package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;

public record MonthlySocialInteractionCountDto(
        Long userId,
        String nickname,
        String profileImageKey,
        DefaultProfileType defaultProfileType,
        long interactionCount
) {
}
