package com.devkor.ifive.nadab.domain.typereport.application.helper;

import com.devkor.ifive.nadab.domain.typereport.core.dto.DailyEntryWithIdDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class EvidenceEntriesAssembler {

    private static final String NA = "N/A";

    private EvidenceEntriesAssembler() {}

    public static List<DailyEntryWithIdDto> attachIds(List<DailyEntryDto> entries) {
        if (entries == null || entries.isEmpty()) return List.of();

        // 안정성을 위해 date DESC 기준으로 정렬 후 D1.. 부여
        List<DailyEntryDto> sorted = entries.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(DailyEntryDto::date, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        int n = sorted.size();
        // D1 = 가장 최신
        return java.util.stream.IntStream.range(0, n)
                .mapToObj(i -> {
                    DailyEntryDto e = sorted.get(i);
                    String id = "D" + (i + 1);
                    return new DailyEntryWithIdDto(
                            id,
                            e.date(),
                            normalize(e.question()),
                            normalize(e.answer()),
                            normalize(e.dailyReport()),
                            e.emotion()
                    );
                })
                .toList();
    }

    public static String assembleForPrompt(List<DailyEntryWithIdDto> entriesWithId) {
        if (entriesWithId == null || entriesWithId.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (DailyEntryWithIdDto e : entriesWithId) {
            sb.append("- id: ").append(e.id()).append("\n")
                    .append("  date: ").append(e.date()).append("\n")
                    .append("  question: ").append(normalize(e.question())).append("\n")
                    .append("  answer: ").append(normalize(e.answer())).append("\n")
                    .append("  daily_report: ").append(normalize(e.dailyReport())).append("\n")
                    .append("  emotion: ").append(e.emotion() == null ? NA : e.emotion().name()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private static String normalize(String s) {
        if (s == null) return NA;
        String t = s.trim();
        return t.isBlank() ? NA : t;
    }
}
