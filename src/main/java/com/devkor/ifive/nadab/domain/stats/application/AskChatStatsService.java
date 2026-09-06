package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyMessageStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyRagDocumentStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyRagReferenceStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyRagStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailySessionStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyWalletStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatDailyWalletStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatMessageSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagErrorStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagSourceStatsDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatRagSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatSessionSummaryDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatWalletStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.askchat.AskChatWalletSummaryDto;
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
        List<AskChatDailyWalletStatsDto> dailyWalletStats = repository.findDailyWalletStats(
                startInclusive,
                endExclusive
        );
        AskChatWalletSummaryDto walletSummary = repository.findWalletSummary(
                startInclusive,
                endExclusive
        );
        List<AskChatDailyRagDocumentStatsDto> dailyRagDocumentStats = repository.findDailyRagDocumentStats(
                startInclusive,
                endExclusive
        );
        List<AskChatDailyRagReferenceStatsDto> dailyRagReferenceStats = repository.findDailyRagReferenceStats(
                startInclusive,
                endExclusive
        );
        AskChatRagSummaryDto ragSummary = repository.findRagSummary(startInclusive, endExclusive);
        List<AskChatRagSourceStatsDto> ragSourceStats = repository.findRagSourceStats(
                startInclusive,
                endExclusive
        );
        List<AskChatRagErrorStatsDto> ragErrorStats = repository.findRagErrorStats(
                startInclusive,
                endExclusive
        );

        Map<LocalDate, AskChatDailySessionStatsDto> sessionStatsByDate = new HashMap<>();
        dailySessionStats.forEach(stats -> sessionStatsByDate.put(stats.date(), stats));
        Map<LocalDate, AskChatDailyMessageStatsDto> messageStatsByDate = new HashMap<>();
        dailyMessageStats.forEach(stats -> messageStatsByDate.put(stats.date(), stats));
        Map<LocalDate, AskChatDailyWalletStatsDto> walletStatsByDate = new HashMap<>();
        dailyWalletStats.forEach(stats -> walletStatsByDate.put(stats.date(), stats));
        Map<LocalDate, AskChatDailyRagDocumentStatsDto> ragDocumentStatsByDate = new HashMap<>();
        dailyRagDocumentStats.forEach(stats -> ragDocumentStatsByDate.put(stats.date(), stats));
        Map<LocalDate, AskChatDailyRagReferenceStatsDto> ragReferenceStatsByDate = new HashMap<>();
        dailyRagReferenceStats.forEach(stats -> ragReferenceStatsByDate.put(stats.date(), stats));

        List<AskChatDailyStatsViewModel> dailyStats = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> toDailyViewModel(
                        date,
                        sessionStatsByDate.get(date),
                        messageStatsByDate.get(date),
                        walletStatsByDate.get(date),
                        ragDocumentStatsByDate.get(date),
                        ragReferenceStatsByDate.get(date)
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
                toWalletStatsViewModel(walletSummary),
                toRagStatsViewModel(ragSummary, ragSourceStats, ragErrorStats),
                OffsetDateTime.now(SEOUL).format(REFRESHED_AT_FORMATTER)
        );
    }

    private AskChatDailyStatsViewModel toDailyViewModel(
            LocalDate date,
            AskChatDailySessionStatsDto sessionStats,
            AskChatDailyMessageStatsDto messageStats,
            AskChatDailyWalletStatsDto walletStats,
            AskChatDailyRagDocumentStatsDto ragDocumentStats,
            AskChatDailyRagReferenceStatsDto ragReferenceStats
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
                messageStats == null ? 0L : messageStats.p95GenerationDurationMs(),
                toDailyWalletStatsViewModel(walletStats),
                toDailyRagStatsViewModel(ragDocumentStats, ragReferenceStats)
        );
    }

    private AskChatDailyWalletStatsViewModel toDailyWalletStatsViewModel(
            AskChatDailyWalletStatsDto stats
    ) {
        if (stats == null) {
            return new AskChatDailyWalletStatsViewModel(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
        }
        return new AskChatDailyWalletStatsViewModel(
                stats.totalLogCount(),
                stats.pendingLogCount(),
                stats.confirmedLogCount(),
                stats.refundedLogCount(),
                stats.freeTurnsGranted(),
                stats.freeTurnsConsumed(),
                stats.paidTurnsConsumed(),
                stats.freeTurnsRefunded(),
                stats.paidTurnsRefunded(),
                stats.paidTurnsCharged(),
                stats.netFreeTurnDelta(),
                stats.netPaidTurnDelta()
        );
    }

    private AskChatDailyRagStatsViewModel toDailyRagStatsViewModel(
            AskChatDailyRagDocumentStatsDto documentStats,
            AskChatDailyRagReferenceStatsDto referenceStats
    ) {
        return new AskChatDailyRagStatsViewModel(
                documentStats == null ? 0L : documentStats.totalDocumentCount(),
                documentStats == null ? 0L : documentStats.pendingDocumentCount(),
                documentStats == null ? 0L : documentStats.completedDocumentCount(),
                documentStats == null ? 0L : documentStats.failedDocumentCount(),
                documentStats == null ? 0L : documentStats.deadLetterDocumentCount(),
                documentStats == null ? 0.0 : documentStats.averageFailedRetryCount(),
                referenceStats == null ? 0L : referenceStats.referenceCount(),
                referenceStats == null ? 0L : referenceStats.uniqueReferencedDocumentCount()
        );
    }

    private AskChatWalletStatsViewModel toWalletStatsViewModel(AskChatWalletSummaryDto summary) {
        return new AskChatWalletStatsViewModel(
                summary.totalLogCount(),
                summary.pendingLogCount(),
                summary.confirmedLogCount(),
                summary.refundedLogCount(),
                summary.freeTurnsGranted(),
                summary.freeTurnsConsumed(),
                summary.paidTurnsConsumed(),
                summary.freeTurnsRefunded(),
                summary.paidTurnsRefunded(),
                summary.paidTurnsCharged(),
                summary.netFreeTurnDelta(),
                summary.netPaidTurnDelta()
        );
    }

    private AskChatRagStatsViewModel toRagStatsViewModel(
            AskChatRagSummaryDto summary,
            List<AskChatRagSourceStatsDto> sourceStats,
            List<AskChatRagErrorStatsDto> errorStats
    ) {
        double embeddingCompletionRatePercent = summary.totalDocumentCount() == 0
                ? 0.0
                : summary.completedDocumentCount() * 100.0 / summary.totalDocumentCount();
        return new AskChatRagStatsViewModel(
                summary.totalDocumentCount(),
                summary.pendingDocumentCount(),
                summary.completedDocumentCount(),
                summary.failedDocumentCount(),
                summary.deadLetterDocumentCount(),
                embeddingCompletionRatePercent,
                summary.averageFailedRetryCount(),
                summary.totalReferenceCount(),
                summary.uniqueReferencedDocumentCount(),
                sourceStats,
                errorStats
        );
    }
}
