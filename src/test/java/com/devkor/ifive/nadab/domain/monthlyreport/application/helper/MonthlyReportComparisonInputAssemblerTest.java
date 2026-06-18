package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportComparisonInputDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonthlyReportComparisonInputAssemblerTest {

    @Test
    void 직전_리포트와_현재_감정_통계로_비교_입력을_조립한다() {
        MonthlyReportV2 previousReport = mock(MonthlyReportV2.class);
        TypeEmotionStatsContent previousEmotionStats = emotionStats(57, List.of(
                emotion("ACHIEVEMENT", "성취", 20),
                emotion("INTEREST", "흥미", 30),
                emotion("DEPRESSION", "우울", 25)
        ));
        TypeEmotionStatsContent currentEmotionStats = emotionStats(71, List.of(
                emotion("ACHIEVEMENT", "성취", 35),
                emotion("INTEREST", "흥미", 22),
                emotion("DEPRESSION", "우울", 15)
        ));
        when(previousReport.getId()).thenReturn(10L);
        when(previousReport.getMonthStartDate()).thenReturn(LocalDate.of(2026, 4, 1));
        when(previousReport.getMonthEndDate()).thenReturn(LocalDate.of(2026, 4, 30));
        when(previousReport.getEmotionStats()).thenReturn(previousEmotionStats);

        MonthlyReportComparisonInputDto result = MonthlyReportComparisonInputAssembler.assemble(
                previousReport,
                currentEmotionStats
        );

        assertThat(result.previousReportId()).isEqualTo(10L);
        assertThat(result.previousMonthStartDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(result.previousMonthEndDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(result.currentPositivePercent()).isEqualTo(71);
        assertThat(result.previousPositivePercent()).isEqualTo(57);
        assertThat(result.positivePercentPointChange()).isEqualTo(14);
        assertThat(result.previousContent()).isNotNull();
        assertThat(result.previousEmotionSummaryContent()).isNotNull();
        assertThat(result.emotionChanges())
                .extracting(
                        MonthlyReportComparisonInputDto.EmotionChange::emotionCode,
                        MonthlyReportComparisonInputDto.EmotionChange::changePercentPoint
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("ACHIEVEMENT", 15),
                        org.assertj.core.groups.Tuple.tuple("DEPRESSION", -10),
                        org.assertj.core.groups.Tuple.tuple("INTEREST", -8)
                );
    }

    private TypeEmotionStatsContent emotionStats(
            int positivePercent,
            List<TypeEmotionStatsContent.EmotionStat> emotions
    ) {
        return new TypeEmotionStatsContent(10, "ACHIEVEMENT", positivePercent, emotions);
    }

    private TypeEmotionStatsContent.EmotionStat emotion(String code, String name, int percent) {
        return new TypeEmotionStatsContent.EmotionStat(code, name, 1, percent);
    }
}
