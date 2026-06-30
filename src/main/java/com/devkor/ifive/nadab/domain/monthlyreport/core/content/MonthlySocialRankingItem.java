package com.devkor.ifive.nadab.domain.monthlyreport.core.content;

import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;

public record MonthlySocialRankingItem(
        int displayOrder,
        Long userId,
        String nickname,
        String profileImageKey,
        DefaultProfileType defaultProfileType,
        boolean topRank
) {
    public MonthlySocialRankingItem normalized() {
        return new MonthlySocialRankingItem(
                Math.max(1, displayOrder),
                userId,
                nickname == null ? "" : nickname.trim(),
                profileImageKey,
                defaultProfileType,
                topRank
        );
    }
}
