package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.application.helper.PeakStatsTracker;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DailyPeriodStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DailyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakMetric;
import com.devkor.ifive.nadab.domain.stats.core.dto.peak.PeakStatViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.DailyStatsRepository;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyStatsService {

    private final DailyStatsRepository repo;
    private final PeakStatsTracker peakStatsTracker;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DAILY_CHART_SIZE = 7;

    public DailyStatsViewModel getDailyStats(LocalDate selectedDate) {
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        LocalDate startDate = selectedDate.minusDays(DAILY_CHART_SIZE - 1L);

        // 라벨 7개 고정 생성
        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < DAILY_CHART_SIZE; i++) days.add(startDate.plusDays(i));

        List<String> labels = days.stream().map(LocalDate::toString).toList();

        // 1) 가입자 수
        Map<LocalDate, Long> signupMap = new HashMap<>();
        for (Object[] row : repo.findSignupCountsLast7Days(startDate, selectedDate)) {
            DateCountDto dto = DailyStatsRepository.toDateCountDto(row);
            signupMap.put(dto.date(), dto.count());
        }

        // 2) 할당 질문 수
        Map<LocalDate, Long> assignedMap = new HashMap<>();
        for (DateCountDto dto : repo.findAssignedQuestionCountsLast7Days(startDate, selectedDate)) {
            assignedMap.put(dto.date(), dto.count());
        }

        // 3) COMPLETED 리포트 수
        Map<LocalDate, Long> completedMap = new HashMap<>();
        for (DateCountDto dto : repo.findCompletedDailyReportCountsLast7Days(startDate, selectedDate)) {
            completedMap.put(dto.date(), dto.count());
        }

        // 빈 날짜는 0 채우기
        List<Long> signupCounts = days.stream().map(d -> signupMap.getOrDefault(d, 0L)).toList();
        List<Long> assignedCounts = days.stream().map(d -> assignedMap.getOrDefault(d, 0L)).toList();
        List<Long> completedCounts = days.stream().map(d -> completedMap.getOrDefault(d, 0L)).toList();

        PeakStatViewModel signupPeak = peakStatsTracker.updateAndGet(
                PeakMetric.DAILY_SIGNUP, days, signupCounts, today
        );
        PeakStatViewModel assignedQuestionPeak = peakStatsTracker.updateAndGet(
                PeakMetric.DAILY_ASSIGNED_QUESTION, days, assignedCounts, today
        );
        PeakStatViewModel dauPeak = peakStatsTracker.updateAndGet(
                PeakMetric.DAU, days, completedCounts, today
        );

        int selectedIndex = days.size() - 1;
        long selectedDateSharedDailyReportCount = repo.countSharedDailyReports(selectedDate);
        DailyPeriodStatsViewModel selectedPeriod = new DailyPeriodStatsViewModel(
                selectedDate.toString(),
                selectedDate.toString(),
                signupCounts.get(selectedIndex),
                assignedCounts.get(selectedIndex),
                completedCounts.get(selectedIndex),
                selectedDateSharedDailyReportCount
        );
        long sharedDailyReportCount = selectedDate.equals(today)
                ? selectedDateSharedDailyReportCount
                : repo.countSharedDailyReports(today);

        return new DailyStatsViewModel(
                labels,
                signupCounts,
                assignedCounts,
                completedCounts,
                selectedPeriod,
                signupPeak,
                assignedQuestionPeak,
                dauPeak,
                sharedDailyReportCount,
                OffsetDateTime.now(SEOUL).format(FMT)
        );
    }
}
