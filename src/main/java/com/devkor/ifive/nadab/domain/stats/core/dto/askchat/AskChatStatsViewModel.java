package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

import java.time.LocalDate;
import java.util.List;

public record AskChatStatsViewModel(
        LocalDate startDate,
        LocalDate endDate,
        List<AskChatDailyStatsViewModel> dailyStats,
        long totalSessionCount,
        long totalUniqueUserCount,
        long totalActiveSessionCount,
        long totalEndedSessionCount,
        long totalUserMessageCount,
        long totalCompletedAssistantMessageCount,
        long totalFailedAssistantMessageCount,
        double assistantSuccessRatePercent,
        double averageGenerationDurationMs,
        long p95GenerationDurationMs,
        List<AskChatErrorStatsDto> errorStats,
        String refreshedAt
) {
}
