package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.monthly.MonthlyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.MonthlyStatsRepository;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonthlyStatsService {

    private final MonthlyStatsRepository repo;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MONTH_LABEL_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int MONTHLY_CHART_SIZE = 5;

    public MonthlyStatsViewModel getMonthlyStatsLast5Months() {
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth startMonth = currentMonth.minusMonths(MONTHLY_CHART_SIZE - 1L);

        LocalDate startDate = startMonth.atDay(1);
        LocalDate endDateInclusive = currentMonth.atEndOfMonth();

        List<YearMonth> months = new ArrayList<>();
        for (int i = 0; i < MONTHLY_CHART_SIZE; i++) {
            months.add(startMonth.plusMonths(i));
        }

        List<String> labels = months.stream()
                .map(month -> MONTH_LABEL_FMT.format(month.atDay(1)))
                .toList();

        Map<YearMonth, Long> signupMap = aggregateByMonth(
                repo.findSignupCountsByDateBetween(startDate, endDateInclusive)
        );
        Map<YearMonth, Long> assignedMap = aggregateByMonth(
                repo.findAssignedQuestionCountsByDateBetween(startDate, endDateInclusive)
        );
        Map<YearMonth, Long> completedV1Map = aggregateByMonth(
                repo.findCompletedMonthlyReportV1CountsByDateBetween(startDate, endDateInclusive)
        );
        Map<YearMonth, Long> completedV2Map = aggregateByMonth(
                repo.findCompletedMonthlyReportV2CountsByDateBetween(startDate, endDateInclusive)
        );
        Map<YearMonth, Long> completedDailyMap = aggregateByMonth(
                repo.findCompletedDailyReportCountsByDateBetween(startDate, endDateInclusive)
        );
        Map<YearMonth, Long> mauMap = aggregateByMonth(
                repo.findMonthlyActiveUserCountsByDateBetween(startDate, endDateInclusive)
        );

        List<Long> signupCounts = months.stream().map(m -> signupMap.getOrDefault(m, 0L)).toList();
        List<Long> assignedCounts = months.stream().map(m -> assignedMap.getOrDefault(m, 0L)).toList();
        List<Long> completedDailyCounts = months.stream().map(m -> completedDailyMap.getOrDefault(m, 0L)).toList();
        List<Long> completedV1Counts = months.stream().map(m -> completedV1Map.getOrDefault(m, 0L)).toList();
        List<Long> completedV2Counts = months.stream().map(m -> completedV2Map.getOrDefault(m, 0L)).toList();
        List<Long> completedTotalCounts = sumCounts(completedV1Counts, completedV2Counts);
        List<Long> mauCounts = months.stream().map(m -> mauMap.getOrDefault(m, 0L)).toList();

        long inProgressV1Now = repo.countInProgressMonthlyReportV1Now();
        long inProgressV2Now = repo.countInProgressMonthlyReportV2Now();
        long inProgressTotalNow = inProgressV1Now + inProgressV2Now;

        return new MonthlyStatsViewModel(
                labels,
                signupCounts,
                assignedCounts,
                completedDailyCounts,
                completedV1Counts,
                completedV2Counts,
                completedTotalCounts,
                mauCounts,
                inProgressV1Now,
                inProgressV2Now,
                inProgressTotalNow,
                OffsetDateTime.now(SEOUL).format(FMT)
        );
    }

    private Map<YearMonth, Long> aggregateByMonth(List<DateCountDto> dailyCounts) {
        Map<YearMonth, Long> map = new HashMap<>();
        for (DateCountDto dto : dailyCounts) {
            YearMonth month = YearMonth.from(dto.date());
            map.merge(month, dto.count(), Long::sum);
        }
        return map;
    }

    private List<Long> sumCounts(List<Long> first, List<Long> second) {
        List<Long> totals = new ArrayList<>(first.size());
        for (int i = 0; i < first.size(); i++) {
            totals.add(first.get(i) + second.get(i));
        }
        return totals;
    }
}
