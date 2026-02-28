package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.total.LabelCountDto;
import com.devkor.ifive.nadab.domain.stats.core.dto.total.TotalStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.TotalStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TotalStatsService {

    private final TotalStatsRepository repo;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TotalStatsViewModel getTotalStats() {
        long totalUserCount = repo.countTotalUsers();

        // provider
        Map<String, Long> providerMap = new HashMap<>();
        for (Object[] row : repo.countUsersByProvider()) {
            String provider = String.valueOf(row[0]); // GOOGLE/KAKAO/NAVER
            long cnt = (Long) row[1];
            providerMap.put(provider, cnt);
        }
        long normal = repo.countNormalUsers();
        providerMap.put("NORMAL", normal);

        // 표시 순서 고정
        List<String> providerLabels = List.of("GOOGLE", "KAKAO", "NAVER", "NORMAL");
        List<Long> providerCounts = providerLabels.stream()
                .map(l -> providerMap.getOrDefault(l, 0L))
                .toList();

        // interest pie
        List<LabelCountDto> selected = repo.countInterestSelections();
        List<String> interestLabels = selected.stream().map(LabelCountDto::label).toList();
        List<Long> interestSelectedCounts = selected.stream().map(LabelCountDto::count).toList();

        // interest bar (completed daily reports)
        Map<String, Long> reportMap = new HashMap<>();
        for (LabelCountDto dto : repo.countCompletedDailyReportsByInterest()) {
            reportMap.put(dto.label(), dto.count());
        }
        List<Long> interestDailyReportCounts = interestLabels.stream()
                .map(l -> reportMap.getOrDefault(l, 0L))
                .toList();

        String refreshedAt = OffsetDateTime.now(SEOUL).format(FMT);

        return new TotalStatsViewModel(
                totalUserCount,
                providerLabels,
                providerCounts,
                interestLabels,
                interestSelectedCounts,
                interestDailyReportCounts,
                refreshedAt
        );
    }
}
