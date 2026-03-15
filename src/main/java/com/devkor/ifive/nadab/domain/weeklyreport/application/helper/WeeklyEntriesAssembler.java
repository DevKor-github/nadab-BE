package com.devkor.ifive.nadab.domain.weeklyreport.application.helper;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionName;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class WeeklyEntriesAssembler {

    private static final String NA = "N/A";

    private WeeklyEntriesAssembler() {
    }

    /**
     * 프롬프트에 넣을 entries 문자열을 생성합니다.
     * - date 오름차순 정렬
     * - D1, D2 ... 번호 부여
     * - null/blank 값은 N/A로 대체
     */
    public static String assemble(List<DailyEntryDto> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return "";
        }

        List<DailyEntryDto> sorted = inputs.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(DailyEntryDto::date, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        StringBuilder sb = new StringBuilder();
        int idx = 1;

        for (DailyEntryDto e : sorted) {
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
