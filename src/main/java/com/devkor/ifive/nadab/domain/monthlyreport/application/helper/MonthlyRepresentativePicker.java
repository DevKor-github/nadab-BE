package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionName;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 월간 리포트 프롬프트에 넣을 "대표 일일 리포트 샘플"을 선정합니다.
 *
 * 목표:
 * 1) 감정 분포 상위 감정 반영(월간 톤/패턴)
 * 2) 주차 커버리지 확보(특정 며칠에 쏠림 방지)
 * 3) 정보량(답변/일일리포트 길이) 큰 날 우선
 *
 * 출력: 날짜 오름차순 정렬된 대표 샘플 리스트
 */
public final class MonthlyRepresentativePicker {

    private MonthlyRepresentativePicker() {
    }

    public static List<DailyEntryDto> pick(List<DailyEntryDto> entries, int maxSamples) {
        if (entries == null || entries.isEmpty() || maxSamples <= 0) {
            return List.of();
        }

        // 날짜순 정렬(전환점 계산 및 결과 정렬에도 사용)
        List<DailyEntryDto> sorted = entries.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(DailyEntryDto::date, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        // 감정 빈도 집계 (null emotion은 OTHER로 취급)
        Map<EmotionName, Long> emotionCount = sorted.stream()
                .map(e -> defaultEmotion(e.emotion()))
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        Map<LocalDate, Double> scoreByDate = computeScores(sorted, emotionCount);

        // (1) 감정 분포 상위 2개에서 각각 대표 1개씩
        List<EmotionName> topEmotions = emotionCount.entrySet().stream()
                .sorted(Map.Entry.<EmotionName, Long>comparingByValue().reversed())
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        LinkedHashMap<LocalDate, DailyEntryDto> selected = new LinkedHashMap<>();

        for (EmotionName emo : topEmotions) {
            pickBestByEmotion(sorted, emo, scoreByDate)
                    .ifPresent(e -> selected.putIfAbsent(e.date(), e));
            if (selected.size() >= maxSamples) {
                return selected.values().stream()
                        .sorted(Comparator.comparing(DailyEntryDto::date))
                        .toList();
            }
        }

        // (2) 주차(week-of-month)별로 최고점 1개씩 추가해 커버리지 확보
        WeekFields wf = WeekFields.of(DayOfWeek.MONDAY, 4); // 월요일 시작(한국 기준)

        Map<Integer, List<DailyEntryDto>> byWeek = sorted.stream()
                .filter(e -> e.date() != null)
                .collect(Collectors.groupingBy(e -> e.date().get(wf.weekOfMonth())));

        List<Integer> weeks = byWeek.keySet().stream().sorted().toList();
        for (Integer w : weeks) {
            if (selected.size() >= maxSamples) break;

            Optional<DailyEntryDto> bestOfWeek = byWeek.get(w).stream()
                    .filter(e -> !selected.containsKey(e.date()))
                    .max(Comparator.comparingDouble(e -> scoreByDate.getOrDefault(e.date(), 0.0)));

            bestOfWeek.ifPresent(e -> selected.putIfAbsent(e.date(), e));
        }

        // (3) 남으면 전체 점수 높은 순으로 채우기
        if (selected.size() < maxSamples) {
            List<DailyEntryDto> rest = sorted.stream()
                    .filter(e -> e.date() != null)
                    .filter(e -> !selected.containsKey(e.date()))
                    .sorted((a, b) -> Double.compare(
                            scoreByDate.getOrDefault(b.date(), 0.0),
                            scoreByDate.getOrDefault(a.date(), 0.0)
                    ))
                    .toList();

            for (DailyEntryDto e : rest) {
                selected.putIfAbsent(e.date(), e);
                if (selected.size() >= maxSamples) break;
            }
        }

        // 결과는 날짜 오름차순으로(LLM 입력 안정성)
        return selected.values().stream()
                .sorted(Comparator.comparing(DailyEntryDto::date))
                .toList();
    }

    private static EmotionName defaultEmotion(EmotionName emotion) {
        return (emotion != null) ? emotion : EmotionName.기타;
    }

    /**
     * 점수 구성:
     * - 정보량: answer/dailyReport 길이 기반
     * - 희귀 감정 보정: 빈도 낮을수록 가산(inverse)
     * - 전환점: 전날과 감정이 다르면 가산
     */
    private static Map<LocalDate, Double> computeScores(
            List<DailyEntryDto> sorted,
            Map<EmotionName, Long> emotionCount
    ) {
        long total = emotionCount.values().stream().mapToLong(v -> v).sum();
        if (total <= 0) total = 1;

        // 희귀도: 빈도 낮을수록 가산
        Map<EmotionName, Double> rarity = new EnumMap<>(EmotionName.class);
        for (var e : emotionCount.entrySet()) {
            double freq = (double) e.getValue() / (double) total; // 0~1
            rarity.put(e.getKey(), 1.0 + (1.0 - freq) * 1.5);     // 1.0~2.5 정도
        }

        Map<LocalDate, Double> score = new HashMap<>();
        DailyEntryDto prev = null;

        for (DailyEntryDto cur : sorted) {
            if (cur.date() == null) continue;

            int answerLen = safeLen(cur.answer());
            int reportLen = safeLen(cur.dailyReport());

            // 정보량(로그로 완만하게)
            double info = Math.log1p(answerLen) * 2.0 + Math.log1p(reportLen) * 1.2;

            EmotionName emo = defaultEmotion(cur.emotion());
            double rare = rarity.getOrDefault(emo, 1.0);

            // 전환점(전날과 감정이 다르면 +)
            double change = 0.0;
            if (prev != null && prev.date() != null) {
                EmotionName prevEmo = defaultEmotion(prev.emotion());
                if (!Objects.equals(prevEmo, emo)) {
                    change = 0.8;
                }
            }

            score.put(cur.date(), info * rare + change);
            prev = cur;
        }

        return score;
    }

    private static int safeLen(String s) {
        return (s == null) ? 0 : s.trim().length();
    }

    private static Optional<DailyEntryDto> pickBestByEmotion(
            List<DailyEntryDto> sorted,
            EmotionName emotion,
            Map<LocalDate, Double> scoreByDate
    ) {
        return sorted.stream()
                .filter(e -> e.date() != null)
                .filter(e -> Objects.equals(defaultEmotion(e.emotion()), emotion))
                .max(Comparator.comparingDouble(e -> scoreByDate.getOrDefault(e.date(), 0.0)));
    }
}