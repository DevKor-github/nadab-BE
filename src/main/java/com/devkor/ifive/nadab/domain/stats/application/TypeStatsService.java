package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportDateInterestCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportInterestCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeReportInterestSeriesViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.TypeStatsRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TypeStatsService {

    private final TypeStatsRepository repo;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int RECENT_DAYS = 7;

    public TypeStatsViewModel getTypeStats() {
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        LocalDate startDate = today.minusDays(RECENT_DAYS - 1L);

        long inProgressTypeReportCount = repo.countInProgressTypeReportsNow();
        List<TypeReportInterestCountDto> completedByInterest = repo.countCompletedTypeReportsByInterest();
        List<TypeReportDateInterestCountDto> completedByDateAndInterest =
                repo.countCompletedTypeReportsByDateAndInterest(startDate, today);

        Map<InterestCode, Long> completedCountMap = new EnumMap<>(InterestCode.class);
        for (TypeReportInterestCountDto dto : completedByInterest) {
            completedCountMap.put(dto.interestCode(), dto.count());
        }

        List<InterestCode> interests = List.of(InterestCode.values());
        List<String> interestLabels = interests.stream()
                .map(InterestCode::displayNameKo)
                .toList();
        List<Long> completedTypeReportCounts = interests.stream()
                .map(interest -> completedCountMap.getOrDefault(interest, 0L))
                .toList();

        List<LocalDate> recentDates = new ArrayList<>(RECENT_DAYS);
        for (int i = 0; i < RECENT_DAYS; i++) {
            recentDates.add(startDate.plusDays(i));
        }

        Map<LocalDate, Map<InterestCode, Long>> dailyCountMap = new HashMap<>();
        for (TypeReportDateInterestCountDto dto : completedByDateAndInterest) {
            dailyCountMap.computeIfAbsent(dto.date(), ignored -> new EnumMap<>(InterestCode.class))
                    .put(dto.interestCode(), dto.count());
        }

        List<TypeReportInterestSeriesViewModel> completedTypeReportSeries = interests.stream()
                .map(interest -> new TypeReportInterestSeriesViewModel(
                        interest.name(),
                        interest.displayNameKo(),
                        recentDates.stream()
                                .map(date -> dailyCountMap.getOrDefault(date, Map.of())
                                        .getOrDefault(interest, 0L))
                                .toList()
                ))
                .toList();

        return new TypeStatsViewModel(
                inProgressTypeReportCount,
                interestLabels,
                completedTypeReportCounts,
                recentDates.stream().map(LocalDate::toString).toList(),
                completedTypeReportSeries,
                OffsetDateTime.now(SEOUL).format(FMT)
        );
    }
}
