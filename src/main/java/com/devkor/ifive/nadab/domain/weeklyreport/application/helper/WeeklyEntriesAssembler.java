package com.devkor.ifive.nadab.domain.weeklyreport.application.helper;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionName;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReportEntryInput;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class WeeklyEntriesAssembler {

    private static final String NA = "N/A";

    private WeeklyEntriesAssembler() {
    }

    public static String assemble(List<WeeklyReportEntryInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return "";
        }

        List<WeeklyReportEntryInput> sorted = inputs.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(WeeklyReportEntryInput::date, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        StringBuilder sb = new StringBuilder();
        int idx = 1;

        for (WeeklyReportEntryInput e : sorted) {
            sb.append("- D").append(idx).append("(").append(valueOrNa(e.date() == null ? null : e.date().toString())).append(")\n")
                    .append("  question: ").append(valueOrNa(e.question())).append("\n")
                    .append("  answer: ").append(valueOrNa(e.answer())).append("\n")
                    .append("  daily_report: ").append(valueOrNa(e.dailyReport())).append("\n")
                    .append("  emotion: ").append(valueOrNaEmotion(e.emotion())).append("\n\n");
            idx++;
        }

        return sb.toString().trim();
    }

    private static String valueOrNa(String s) {
        if (s == null) return NA;
        String t = s.trim();
        return t.isEmpty() ? NA : t;
    }

    private static String valueOrNaEmotion(EmotionName emotion) {
        if (emotion == null) return NA;
        return emotion.toString();
    }
}
