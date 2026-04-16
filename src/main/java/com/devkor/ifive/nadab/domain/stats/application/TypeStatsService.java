package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.total.LabelCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.TypeStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeStatsService {

    private final TypeStatsRepository repo;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TypeStatsViewModel getTypeStats() {
        long inProgressTypeReportCount = repo.countInProgressTypeReportsNow();
        List<LabelCountDto> completedByInterest = repo.countCompletedTypeReportsByInterest();

        List<String> interestLabels = completedByInterest.stream()
                .map(LabelCountDto::label)
                .toList();
        List<Long> completedTypeReportCounts = completedByInterest.stream()
                .map(LabelCountDto::count)
                .toList();

        return new TypeStatsViewModel(
                inProgressTypeReportCount,
                interestLabels,
                completedTypeReportCounts,
                OffsetDateTime.now(SEOUL).format(FMT)
        );
    }
}
