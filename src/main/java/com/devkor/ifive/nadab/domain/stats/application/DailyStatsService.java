package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DailyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DateCountDto;
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

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DailyStatsViewModel getDailyStatsLast7Days() {
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        LocalDate startDate = today.minusDays(6);

        // 라벨 7개 고정 생성
        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) days.add(startDate.plusDays(i));

        List<String> labels = days.stream().map(LocalDate::toString).toList();

        // 1) 가입자 수
        Map<LocalDate, Long> signupMap = new HashMap<>();
        for (Object[] row : repo.findSignupCountsLast7Days(startDate, today)) {
            DateCountDto dto = DailyStatsRepository.toDateCountDto(row);
            signupMap.put(dto.date(), dto.count());
        }

        // 2) 할당 질문 수
        Map<LocalDate, Long> assignedMap = new HashMap<>();
        for (DateCountDto dto : repo.findAssignedQuestionCountsLast7Days(startDate, today)) {
            assignedMap.put(dto.date(), dto.count());
        }

        // 3) COMPLETED 리포트 수
        Map<LocalDate, Long> completedMap = new HashMap<>();
        for (DateCountDto dto : repo.findCompletedDailyReportCountsLast7Days(startDate, today)) {
            completedMap.put(dto.date(), dto.count());
        }

        // 빈 날짜는 0 채우기
        List<Long> signupCounts = days.stream().map(d -> signupMap.getOrDefault(d, 0L)).toList();
        List<Long> assignedCounts = days.stream().map(d -> assignedMap.getOrDefault(d, 0L)).toList();
        List<Long> completedCounts = days.stream().map(d -> completedMap.getOrDefault(d, 0L)).toList();

        long sharedNow = repo.countSharedDailyReportsNow();

        return new DailyStatsViewModel(
                labels,
                signupCounts,
                assignedCounts,
                completedCounts,
                sharedNow,
                OffsetDateTime.now(SEOUL).format(FMT)
        );
    }
}