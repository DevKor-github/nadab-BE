package com.devkor.ifive.nadab.domain.monthlyreport.core.content;

import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;

public record MonthlySocialRankingItem(
        int rank,
        Long userId,
        String nickname,
        String profileImageKey,
        DefaultProfileType defaultProfileType,
        int interactionCount,
        boolean topRank
) {
    public MonthlySocialRankingItem normalized() {
        return new MonthlySocialRankingItem(
                Math.max(1, rank),
                userId,
                nickname == null ? "" : nickname.trim(),
                profileImageKey,
                defaultProfileType,
                Math.max(0, interactionCount),
                topRank
        );
    }
}
