package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.application.helper.PeakStatsTracker;
import com.devkor.ifive.nadab.domain.stats.application.helper.StatsPeriodResolver;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakMetric;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStatViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.weekly.WeeklyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.weekly.WeeklyPeriodStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.WeeklyStatsRepository;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeeklyStatsService {

    private final WeeklyStatsRepository repo;
    private final PeakStatsTracker peakStatsTracker;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter WEEK_LABEL_FMT =
            DateTimeFormatter.ofPattern("MM-dd");
    private static final int WEEKLY_CHART_SIZE = 5;

    public WeeklyStatsViewModel getWeeklyStats(LocalDate selectedWeek) {
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        LocalDate currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate selectedWeekStart = selectedWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startWeekStart = selectedWeekStart.minusWeeks(WEEKLY_CHART_SIZE - 1L);
        LocalDate endDateInclusive = selectedWeekStart.plusDays(6);

        List<LocalDate> weekStarts = new ArrayList<>();
        for (int i = 0; i < WEEKLY_CHART_SIZE; i++) {
            weekStarts.add(startWeekStart.plusWeeks(i));
        }

        List<String> labels = weekStarts.stream()
                .map(weekStart -> WEEK_LABEL_FMT.format(weekStart) + " ~ " + WEEK_LABEL_FMT.format(weekStart.plusDays(6)))
                .toList();

        Map<LocalDate, Long> signupMap = aggregateByWeekStart(
                repo.findSignupCountsByDateBetween(startWeekStart, endDateInclusive)
        );
        Map<LocalDate, Long> assignedMap = aggregateByWeekStart(
                repo.findAssignedQuestionCountsByDateBetween(startWeekStart, endDateInclusive)
        );

        Map<LocalDate, Long> completedMap = aggregateByWeekStart(
                repo.findCompletedWeeklyReportCountsByDateBetween(startWeekStart, endDateInclusive)
        );
        Map<LocalDate, Long> completedDailyMap = aggregateByWeekStart(
                repo.findCompletedDailyReportCountsByDateBetween(startWeekStart, endDateInclusive)
        );
        Map<LocalDate, Long> wauMap = toMapByDate(
                repo.findWeeklyActiveUserCountsByDateBetween(startWeekStart, endDateInclusive)
        );

        List<Long> signupCounts = weekStarts.stream().map(d -> signupMap.getOrDefault(d, 0L)).toList();
        List<Long> assignedCounts = weekStarts.stream().map(d -> assignedMap.getOrDefault(d, 0L)).toList();
        List<Long> completedDailyCounts = weekStarts.stream().map(d -> completedDailyMap.getOrDefault(d, 0L)).toList();
        List<Long> completedCounts = weekStarts.stream().map(d -> completedMap.getOrDefault(d, 0L)).toList();
        List<Long> wauCounts = weekStarts.stream().map(d -> wauMap.getOrDefault(d, 0L)).toList();

        PeakStatViewModel signupPeak = peakStatsTracker.updateAndGet(
                PeakMetric.WEEKLY_SIGNUP, weekStarts, signupCounts, currentWeekStart
        );
        PeakStatViewModel assignedQuestionPeak = peakStatsTracker.updateAndGet(
                PeakMetric.WEEKLY_ASSIGNED_QUESTION, weekStarts, assignedCounts, currentWeekStart
        );
        PeakStatViewModel completedDailyReportPeak = peakStatsTracker.updateAndGet(
                PeakMetric.WEEKLY_DAILY_REPORT, weekStarts, completedDailyCounts, currentWeekStart
        );
        PeakStatViewModel completedWeeklyReportPeak = peakStatsTracker.updateAndGet(
                PeakMetric.WEEKLY_REPORT, weekStarts, completedCounts, currentWeekStart
        );
        PeakStatViewModel wauPeak = peakStatsTracker.updateAndGet(
                PeakMetric.WAU, weekStarts, wauCounts, currentWeekStart
        );

        long inProgressNow = repo.countInProgressWeeklyReportsNow();
        int selectedIndex = weekStarts.size() - 1;
        WeeklyPeriodStatsViewModel selectedPeriod = new WeeklyPeriodStatsViewModel(
                StatsPeriodResolver.formatIsoWeek(selectedWeekStart),
                selectedWeekStart + " ~ " + selectedWeekStart.plusDays(6),
                signupCounts.get(selectedIndex),
                assignedCounts.get(selectedIndex),
                completedDailyCounts.get(selectedIndex),
                completedCounts.get(selectedIndex),
                wauCounts.get(selectedIndex)
        );

        return new WeeklyStatsViewModel(
                labels,
                signupCounts,
                assignedCounts,
                completedDailyCounts,
                completedCounts,
                wauCounts,
                selectedPeriod,
                signupPeak,
                assignedQuestionPeak,
                completedDailyReportPeak,
                completedWeeklyReportPeak,
                wauPeak,
                inProgressNow,
                OffsetDateTime.now(SEOUL).format(FMT)
        );
    }

    private Map<LocalDate, Long> aggregateByWeekStart(List<DateCountDto> dailyCounts) {
        Map<LocalDate, Long> map = new HashMap<>();
        for (DateCountDto dto : dailyCounts) {
            LocalDate weekStart = dto.date().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            map.merge(weekStart, dto.count(), Long::sum);
        }
        return map;
    }

    private Map<LocalDate, Long> toMapByDate(List<DateCountDto> counts) {
        Map<LocalDate, Long> map = new HashMap<>();
        for (DateCountDto dto : counts) {
            map.put(dto.date(), dto.count());
        }
        return map;
    }
}
