package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyMessageStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailySessionStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatMessageSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatSessionSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.AskChatStatsRepository;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AskChatStatsService {

    public static final int DEFAULT_CHART_DAYS = 7;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter REFRESHED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AskChatStatsRepository repository;

    public AskChatStatsViewModel getAskChatStats() {
        LocalDate endDate = TodayDateTimeProvider.getTodayDate();
        return getAskChatStats(endDate.minusDays(DEFAULT_CHART_DAYS - 1L), endDate);
    }

    public AskChatStatsViewModel getAskChatStats(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be on or before endDate");
        }

        OffsetDateTime startInclusive = startDate.atStartOfDay(SEOUL).toOffsetDateTime();
        OffsetDateTime endExclusive = endDate.plusDays(1).atStartOfDay(SEOUL).toOffsetDateTime();

        List<AskChatDailySessionStatsDto> dailySessionStats = repository.findDailySessionStats(
                startInclusive,
                endExclusive
        );
        List<AskChatDailyMessageStatsDto> dailyMessageStats = repository.findDailyMessageStats(
                startInclusive,
                endExclusive
        );
        AskChatSessionSummaryDto sessionSummary = repository.findSessionSummary(
                startInclusive,
                endExclusive
        );
        AskChatMessageSummaryDto messageSummary = repository.findMessageSummary(
                startInclusive,
                endExclusive
        );
        List<AskChatErrorStatsDto> errorStats = repository.findAssistantErrorStats(
                startInclusive,
                endExclusive
        );

        Map<LocalDate, AskChatDailySessionStatsDto> sessionStatsByDate = new HashMap<>();
        dailySessionStats.forEach(stats -> sessionStatsByDate.put(stats.date(), stats));
        Map<LocalDate, AskChatDailyMessageStatsDto> messageStatsByDate = new HashMap<>();
        dailyMessageStats.forEach(stats -> messageStatsByDate.put(stats.date(), stats));

        List<AskChatDailyStatsViewModel> dailyStats = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> toDailyViewModel(
                        date,
                        sessionStatsByDate.get(date),
                        messageStatsByDate.get(date)
                ))
                .toList();

        long assistantResponseCount = messageSummary.totalCompletedAssistantMessageCount()
                + messageSummary.totalFailedAssistantMessageCount();
        double assistantSuccessRatePercent = assistantResponseCount == 0
                ? 0.0
                : messageSummary.totalCompletedAssistantMessageCount() * 100.0 / assistantResponseCount;

        return new AskChatStatsViewModel(
                startDate,
                endDate,
                dailyStats,
                sessionSummary.totalSessionCount(),
                sessionSummary.totalUniqueUserCount(),
                sessionSummary.totalActiveSessionCount(),
                sessionSummary.totalEndedSessionCount(),
                messageSummary.totalUserMessageCount(),
                messageSummary.totalCompletedAssistantMessageCount(),
                messageSummary.totalFailedAssistantMessageCount(),
                assistantSuccessRatePercent,
                messageSummary.averageGenerationDurationMs(),
                messageSummary.p95GenerationDurationMs(),
                errorStats,
                OffsetDateTime.now(SEOUL).format(REFRESHED_AT_FORMATTER)
        );
    }

    private AskChatDailyStatsViewModel toDailyViewModel(
            LocalDate date,
            AskChatDailySessionStatsDto sessionStats,
            AskChatDailyMessageStatsDto messageStats
    ) {
        return new AskChatDailyStatsViewModel(
                date,
                sessionStats == null ? 0L : sessionStats.sessionCount(),
                sessionStats == null ? 0L : sessionStats.uniqueUserCount(),
                sessionStats == null ? 0L : sessionStats.activeSessionCount(),
                sessionStats == null ? 0L : sessionStats.endedSessionCount(),
                messageStats == null ? 0L : messageStats.userMessageCount(),
                messageStats == null ? 0L : messageStats.completedAssistantMessageCount(),
                messageStats == null ? 0L : messageStats.failedAssistantMessageCount(),
                messageStats == null ? 0.0 : messageStats.averageGenerationDurationMs(),
                messageStats == null ? 0L : messageStats.p95GenerationDurationMs()
        );
    }
}
