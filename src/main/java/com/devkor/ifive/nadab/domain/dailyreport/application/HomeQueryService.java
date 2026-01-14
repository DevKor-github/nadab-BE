package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.HomeResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryQueryRepository;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.WeekRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeQueryService {

    private final AnswerEntryQueryRepository answerEntryQueryRepository;

    public HomeResponse getHomeData(Long userId) {
        LocalDate today = TodayDateTimeProvider.getTodayDate();

        // 1. 이번 주 범위 계산
        WeekRangeDto weekRange = WeekRangeCalculator.weekRangeOf(today);

        // 2. 첫 답변 날짜 조회
        LocalDate firstAnswerDate = answerEntryQueryRepository
                .findFirstAnswerDateByUserId(userId)
                .orElse(null);

        // 3. 첫 답변 날짜 ~ 오늘까지 전체 답변 날짜 조회
        List<LocalDate> allAnswerDates;
        if (firstAnswerDate == null) {
            allAnswerDates = List.of();  // 신규 사용자
        } else {
            allAnswerDates = answerEntryQueryRepository
                    .findAnswerDatesByUserIdAndDateBetween(userId, firstAnswerDate, today);
        }

        // 4. 주간 답변 필터링
        List<LocalDate> weeklyAnsweredDates = allAnswerDates.stream()
                .filter(date -> !date.isBefore(weekRange.weekStartDate())
                        && !date.isAfter(weekRange.weekEndDate()))
                .sorted()
                .toList();

        // 5. Streak 계산
        long currentStreak = calculateCurrentStreak(today, allAnswerDates);

        // 6. 총 기록 일수 계산
        long totalDaysSinceStart = calculateTotalDaysSinceStart(today, firstAnswerDate);

        // 7. 응답 생성
        return new HomeResponse(
                weeklyAnsweredDates,
                currentStreak,
                totalDaysSinceStart
        );
    }

    private long calculateCurrentStreak(LocalDate today, List<LocalDate> answerDates) {
        if (answerDates == null || answerDates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> dateSet = new HashSet<>(answerDates);

        // 시작 날짜 결정: 오늘 답변 있으면 오늘부터, 없으면 어제부터
        LocalDate checkDate = dateSet.contains(today) ? today : today.minusDays(1);

        // 역순 연속 계산
        long streak = 0;

        while (dateSet.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    private long calculateTotalDaysSinceStart(LocalDate today, LocalDate firstAnswerDate) {
        if (firstAnswerDate == null || firstAnswerDate.isAfter(today)) {
            return 0;
        }

        return ChronoUnit.DAYS.between(firstAnswerDate, today) + 1;
    }
}