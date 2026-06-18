package com.devkor.ifive.nadab.domain.monthlyreport.infra;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportComparisonInputDto;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.global.core.prompt.monthly.MonthlyReportPromptLoader;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.devkor.ifive.nadab.global.shared.reportcontent.Mark;
import com.devkor.ifive.nadab.global.shared.reportcontent.Segment;
import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportLlmClientV2Test {

    @Mock
    private MonthlyReportPromptLoader promptLoader;
    @Mock
    private LlmRouter llmRouter;

    private MonthlyReportLlmClientV2 client;

    @BeforeEach
    void setUp() {
        client = new MonthlyReportLlmClientV2(promptLoader, new ObjectMapper(), llmRouter);
    }

    @Test
    void 비교_입력이_없으면_BASELINE_프롬프트를_사용한다() {
        when(promptLoader.loadV2BaselinePrompt()).thenReturn(
                "baseline {monthStartDate} {monthEndDate} {weeklySummaries} {representativeEntries} {emotionStats}"
        );

        String prompt = client.buildPrompt(
                "2026-05-01",
                "2026-05-31",
                "weekly",
                "entries",
                emptyEmotionStats(),
                null
        );

        assertThat(prompt).startsWith("baseline 2026-05-01 2026-05-31 weekly entries");
        verify(promptLoader, never()).loadV2ComparisonPrompt();
    }

    @Test
    void 비교_입력이_있으면_COMPARISON_프롬프트에_JSON을_주입한다() {
        when(promptLoader.loadV2ComparisonPrompt()).thenReturn("comparison {comparisonInput}");
        MonthlyReportComparisonInputDto comparisonInput = new MonthlyReportComparisonInputDto(
                10L,
                null,
                null,
                null,
                null,
                null,
                71,
                57,
                14,
                List.of()
        );

        String prompt = client.buildPrompt(
                "2026-05-01",
                "2026-05-31",
                "weekly",
                "entries",
                emptyEmotionStats(),
                comparisonInput
        );

        assertThat(prompt)
                .startsWith("comparison {")
                .contains("\"previousReportId\":10")
                .contains("\"positivePercentPointChange\":14")
                .doesNotContain("{comparisonInput}");
        verify(promptLoader, never()).loadV2BaselinePrompt();
    }

    @Test
    void COMPARISON_emotionTrend는_길이와_dominantKeyword를_검증한다() {
        client.validateComparisonEmotionTrend(
                "도전을 중심으로 성취와 의지가 함께 증가했어요",
                "도전"
        );

        assertThatThrownBy(() -> client.validateComparisonEmotionTrend("감정이 변했어요", "도전"))
                .isInstanceOf(AiResponseParseException.class);
    }

    @Test
    void COMPARISON_감정_분석문은_BOLD만_최대_3개까지_허용한다() {
        StyledText valid = new StyledText(List.of(
                new Segment("직전 기간보다 선택을 망설이는 시간이 줄었고, ", List.of()),
                new Segment("도전", List.of(Mark.BOLD)),
                new Segment("을 행동으로 옮기는 흐름이 여러 상황에서 더 분명하게 이어졌어요.", List.of())
        ));
        client.validateComparisonEmotionSummary(valid, "도전");

        StyledText invalid = new StyledText(List.of(
                new Segment("도전", List.of(Mark.BOLD, Mark.HIGHLIGHT)),
                new Segment("을 행동으로 옮기는 흐름이 더 분명해졌어요.", List.of())
        ));
        assertThatThrownBy(() -> client.validateComparisonEmotionSummary(invalid, "도전"))
                .isInstanceOf(AiResponseParseException.class);
    }

    @Test
    void COMPARISON_감정_분석문은_100자를_초과할_수_없다() {
        assertThatThrownBy(() -> client.validateLength(
                "가".repeat(150),
                "나".repeat(150),
                "다".repeat(101),
                true
        )).isInstanceOf(AiResponseParseException.class);
    }

    private TypeEmotionStatsContent emptyEmotionStats() {
        return new TypeEmotionStatsContent(0, null, 0, List.of());
    }
}
