package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyWeeklySummaryInputDto;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class MonthlyWeeklySummariesAssembler {

    private static final String NA = "N/A";

    private MonthlyWeeklySummariesAssembler() {
    }

    /**
     * 프롬프트에 넣을 weeklySummaries 텍스트를 생성합니다.
     *
     * 예)
     * - W1(2026-01-01 ~ 2026-01-07)
     *   discovered: ...
     *   good: ...
     *   improve: ...
     */
    public static String assemble(List<MonthlyWeeklySummaryInputDto> inputs) {
        if (inputs == null || inputs.isEmpty()) return "";

        List<MonthlyWeeklySummaryInputDto> sorted = inputs.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(MonthlyWeeklySummaryInputDto::weekStartDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        StringBuilder sb = new StringBuilder();
        int idx = 1;

        for (MonthlyWeeklySummaryInputDto w : sorted) {
            sb.append("- W").append(idx)
                    .append("(").append(valueOrNa(w.weekStartDate() == null ? null : w.weekStartDate().toString()))
                    .append(" ~ ").append(valueOrNa(w.weekEndDate() == null ? null : w.weekEndDate().toString()))
                    .append(")\n")
                    .append("  discovered: ").append(valueOrNa(w.discovered())).append("\n")
                    .append("  good: ").append(valueOrNa(w.good())).append("\n")
                    .append("  improve: ").append(valueOrNa(w.improve())).append("\n\n");
            idx++;
        }

        return sb.toString().trim();
    }

    private static String valueOrNa(String s) {
        if (s == null) return NA;
        String t = s.trim();
        return t.isEmpty() ? NA : t;
    }
}
