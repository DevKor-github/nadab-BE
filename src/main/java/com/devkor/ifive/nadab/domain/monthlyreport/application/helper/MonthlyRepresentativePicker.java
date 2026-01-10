package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

public final class MonthlyRepresentativePicker {

    public record DailyEntry(
            LocalDate date,
            String question,
            String answer,
            String dailyReport,
            String emotion
    ) {}

    public List<DailyEntry> pick(List<DailyEntry> entries, int maxSamples) {
        if (entries == null || entries.isEmpty() || maxSamples <= 0) return List.of();

        // 날짜순 정렬 (변화량 계산용)
        List<DailyEntry> sorted = entries.stream()
                .sorted(Comparator.comparing(DailyEntry::date))
                .toList();

        // 감정 빈도
        Map<String, Long> emotionCount = sorted.stream()
                .collect(Collectors.groupingBy(DailyEntry::emotion, Collectors.counting()));

        // 점수 계산
        Map<LocalDate, Double> scoreByDate = computeScores(sorted, emotionCount);

        // (1) 감정 분포 상위 2개에서 대표 1개씩
        List<String> topEmotions = emotionCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        LinkedHashSet<DailyEntry> selected = new LinkedHashSet<>();

        for (String emo : topEmotions) {
            pickBestByEmotion(sorted, emo, scoreByDate).ifPresent(selected::add);
            if (selected.size() >= maxSamples) return new ArrayList<>(selected);
        }

        // (2) 주차 커버리지 확보: 주차별 최고점 1개
        WeekFields wf = WeekFields.of(DayOfWeek.MONDAY, 4); // 한국 기준 월요일 시작(대개)
        Map<Integer, List<DailyEntry>> byWeek = sorted.stream()
                .collect(Collectors.groupingBy(e -> e.date().get(wf.weekOfMonth())));

        List<Integer> weeks = byWeek.keySet().stream().sorted().toList();
        for (Integer w : weeks) {
            if (selected.size() >= maxSamples) break;

            Optional<DailyEntry> bestOfWeek = byWeek.get(w).stream()
                    .filter(e -> !containsDate(selected, e.date()))
                    .max(Comparator.comparingDouble(e -> scoreByDate.getOrDefault(e.date(), 0.0)));

            bestOfWeek.ifPresent(selected::add);
        }

        // (3) 남으면 전체 점수 순으로 채우기
        if (selected.size() < maxSamples) {
            List<DailyEntry> rest = sorted.stream()
                    .filter(e -> !containsDate(selected, e.date()))
                    .sorted((a, b) -> Double.compare(
                            scoreByDate.getOrDefault(b.date(), 0.0),
                            scoreByDate.getOrDefault(a.date(), 0.0)
                    ))
                    .toList();

            for (DailyEntry e : rest) {
                selected.add(e);
                if (selected.size() >= maxSamples) break;
            }
        }

        return new ArrayList<>(selected);
    }

    private static boolean containsDate(Set<DailyEntry> set, LocalDate date) {
        for (DailyEntry e : set) if (e.date().equals(date)) return true;
        return false;
    }

    private static Optional<DailyEntry> pickBestByEmotion(
            List<DailyEntry> sorted,
            String emotion,
            Map<LocalDate, Double> scoreByDate
    ) {
        return sorted.stream()
                .filter(e -> Objects.equals(e.emotion(), emotion))
                .max(Comparator.comparingDouble(e -> scoreByDate.getOrDefault(e.date(), 0.0)));
    }

    private static Map<LocalDate, Double> computeScores(
            List<DailyEntry> sorted,
            Map<String, Long> emotionCount
    ) {
        long total = emotionCount.values().stream().mapToLong(v -> v).sum();

        // 감정 희귀도(빈도 낮을수록 가산): inverse freq
        Map<String, Double> rarity = new HashMap<>();
        for (var e : emotionCount.entrySet()) {
            double freq = (double) e.getValue() / (double) total;
            // freq가 낮을수록 값 커짐 (최소 1.0)
            rarity.put(e.getKey(), 1.0 + (1.0 - freq) * 1.5);
        }

        Map<LocalDate, Double> score = new HashMap<>();
        DailyEntry prev = null;

        for (DailyEntry cur : sorted) {
            int answerLen = safeLen(cur.answer());
            int reportLen = safeLen(cur.dailyReport());

            // 정보량: 답변/리포트가 길수록 점수
            double info = Math.log1p(answerLen) * 2.0 + Math.log1p(reportLen) * 1.2;

            // 희귀 감정 보정
            double rare = rarity.getOrDefault(cur.emotion(), 1.0);

            // 전환점(전날과 감정이 다르면 가산)
            double change = 0.0;
            if (prev != null && !Objects.equals(prev.emotion(), cur.emotion())) {
                change = 0.8;
            }

            // 최종 점수
            double totalScore = info * rare + change;

            score.put(cur.date(), totalScore);
            prev = cur;
        }

        return score;
    }

    private static int safeLen(String s) {
        return (s == null) ? 0 : s.trim().length();
    }
}
