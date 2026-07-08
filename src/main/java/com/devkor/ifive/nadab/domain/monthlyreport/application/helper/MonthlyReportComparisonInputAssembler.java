package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyReportV2ContentFactory;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportComparisonInputDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeContentFactory;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MonthlyReportComparisonInputAssembler {

    private MonthlyReportComparisonInputAssembler() {
    }

    public static MonthlyReportComparisonInputDto assemble(
            MonthlyReportV2 previousReport,
            TypeEmotionStatsContent currentEmotionStats
    ) {
        MonthlyReportV2Content previousContent = previousReport.getContent() == null
                ? MonthlyReportV2ContentFactory.empty()
                : previousReport.getContent().normalized();
        TypeTextContent previousEmotionSummaryContent = previousReport.getEmotionSummaryContent() == null
                ? TypeContentFactory.emptyText()
                : previousReport.getEmotionSummaryContent().normalized();
        TypeEmotionStatsContent previousEmotionStats = previousReport.getEmotionStats() == null
                ? TypeContentFactory.emptyEmotionStats()
                : previousReport.getEmotionStats().normalized();
        TypeEmotionStatsContent normalizedCurrentEmotionStats = currentEmotionStats == null
                ? TypeContentFactory.emptyEmotionStats()
                : currentEmotionStats.normalized();

        int currentPositivePercent = normalizedCurrentEmotionStats.positivePercent();
        int previousPositivePercent = previousEmotionStats.positivePercent();

        return new MonthlyReportComparisonInputDto(
                previousReport.getId(),
                previousReport.getMonthStartDate(),
                previousReport.getMonthEndDate(),
                previousContent,
                previousEmotionSummaryContent,
                previousEmotionStats,
                currentPositivePercent,
                previousPositivePercent,
                currentPositivePercent - previousPositivePercent,
                assembleEmotionChanges(normalizedCurrentEmotionStats, previousEmotionStats)
        );
    }

    private static List<MonthlyReportComparisonInputDto.EmotionChange> assembleEmotionChanges(
            TypeEmotionStatsContent currentEmotionStats,
            TypeEmotionStatsContent previousEmotionStats
    ) {
        Map<String, TypeEmotionStatsContent.EmotionStat> currentByCode = byEmotionCode(currentEmotionStats);
        Map<String, TypeEmotionStatsContent.EmotionStat> previousByCode = byEmotionCode(previousEmotionStats);

        return Arrays.stream(EmotionCode.values())
                .map(code -> toEmotionChange(code, currentByCode.get(code.name()), previousByCode.get(code.name())))
                .filter(change -> change.changePercentPoint() != 0)
                .sorted((left, right) -> Integer.compare(
                        Math.abs(right.changePercentPoint()),
                        Math.abs(left.changePercentPoint())
                ))
                .limit(3)
                .toList();
    }

    private static Map<String, TypeEmotionStatsContent.EmotionStat> byEmotionCode(
            TypeEmotionStatsContent emotionStats
    ) {
        return emotionStats.emotions().stream()
                .filter(stat -> stat.emotionCode() != null)
                .collect(Collectors.toMap(
                        TypeEmotionStatsContent.EmotionStat::emotionCode,
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    private static MonthlyReportComparisonInputDto.EmotionChange toEmotionChange(
            EmotionCode code,
            TypeEmotionStatsContent.EmotionStat current,
            TypeEmotionStatsContent.EmotionStat previous
    ) {
        int currentPercent = current == null ? 0 : current.percent();
        int previousPercent = previous == null ? 0 : previous.percent();
        String emotionName = current != null ? current.emotionName()
                : previous == null ? null : previous.emotionName();

        return new MonthlyReportComparisonInputDto.EmotionChange(
                code.name(),
                emotionName,
                currentPercent,
                previousPercent,
                currentPercent - previousPercent
        );
    }
}
