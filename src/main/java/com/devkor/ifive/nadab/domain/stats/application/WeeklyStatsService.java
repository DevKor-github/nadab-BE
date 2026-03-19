package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.weekly.WeeklyStatsViewModel;
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

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter WEEK_LABEL_FMT =
            DateTimeFormatter.ofPattern("MM-dd");

    public WeeklyStatsViewModel getWeeklyStatsLast7Weeks() {
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        LocalDate currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startWeekStart = currentWeekStart.minusWeeks(6);
        LocalDate endDateInclusive = currentWeekStart.plusDays(6);

        List<LocalDate> weekStarts = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
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

        List<Long> signupCounts = weekStarts.stream().map(d -> signupMap.getOrDefault(d, 0L)).toList();
        List<Long> assignedCounts = weekStarts.stream().map(d -> assignedMap.getOrDefault(d, 0L)).toList();
        List<Long> completedCounts = weekStarts.stream().map(d -> completedMap.getOrDefault(d, 0L)).toList();

        long inProgressNow = repo.countInProgressWeeklyReportsNow();

        return new WeeklyStatsViewModel(
                labels,
                signupCounts,
                assignedCounts,
                completedCounts,
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
}
