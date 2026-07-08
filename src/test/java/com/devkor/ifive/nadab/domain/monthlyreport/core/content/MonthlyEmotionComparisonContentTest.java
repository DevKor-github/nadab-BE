package com.devkor.ifive.nadab.domain.monthlyreport.core.content;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportComparisonInputDto;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyEmotionComparisonContentTest {

    @Test
    void 비교_입력으로부터_저장용_스냅샷을_생성한다() {
        TypeEmotionStatsContent previousEmotionStats = new TypeEmotionStatsContent(
                10,
                "ACHIEVEMENT",
                57,
                List.of(new TypeEmotionStatsContent.EmotionStat(
                        "ACHIEVEMENT",
                        "성취",
                        5,
                        50
                ))
        );
        MonthlyReportComparisonInputDto input = new MonthlyReportComparisonInputDto(
                10L,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                null,
                null,
                previousEmotionStats,
                71,
                57,
                14,
                List.of()
        );

        MonthlyEmotionComparisonContent result = MonthlyEmotionComparisonContent.from(input);

        assertThat(result.previousReportId()).isEqualTo(10L);
        assertThat(result.previousMonth()).isEqualTo(4);
        assertThat(result.previousEmotionStats().positivePercent()).isEqualTo(57);
        assertThat(result.positivePercentPointChange()).isEqualTo(14);
    }

    @Test
    void BASELINE_입력은_비교_스냅샷을_생성하지_않는다() {
        assertThat(MonthlyEmotionComparisonContent.from(null)).isNull();
    }
}
