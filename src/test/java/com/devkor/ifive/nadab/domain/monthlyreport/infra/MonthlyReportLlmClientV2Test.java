package com.devkor.ifive.nadab.domain.monthlyreport.infra;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportComparisonInputDto;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.global.core.prompt.monthly.MonthlyReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
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
    void COMPARISON_emotionTrend는_감정_양상_문장의_길이를_검증한다() {
        client.validateComparisonEmotionTrend("성취와 의지가 증가했어요");

        assertThatThrownBy(() -> client.validateComparisonEmotionTrend("변했어요"))
                .isInstanceOf(AiResponseParseException.class);
    }

    @Test
    void 공백_전용_segment는_직전_segment에_합쳐서_정규화한다() {
        StyledText input = new StyledText(List.of(
                new Segment("높은 기준과 기대를 가지고", List.of(Mark.BOLD, Mark.HIGHLIGHT)),
                new Segment(" ", List.of()),
                new Segment("의미 있는 결과를 만들고 싶어 해요.", List.of(Mark.BOLD, Mark.HIGHLIGHT))
        ));

        StyledText result = client.normalizeStyledTextSegments(input);

        assertThat(result.segments()).hasSize(2);
        assertThat(result.segments().get(0).text()).isEqualTo("높은 기준과 기대를 가지고 ");
        assertThat(result.plainText()).isEqualTo("높은 기준과 기대를 가지고 의미 있는 결과를 만들고 싶어 해요.");
    }

    @Test
    void 빈_segment는_제거하고_null_marks는_빈_배열로_정규화한다() {
        StyledText input = new StyledText(List.of(
                new Segment("", List.of()),
                new Segment("의미 있는 문장", null)
        ));

        StyledText result = client.normalizeStyledTextSegments(input);

        assertThat(result.segments()).containsExactly(new Segment("의미 있는 문장", List.of()));
    }

    @Test
    void COMPARISON_감정_분석문은_BOLD와_HIGHLIGHT_조합을_허용한다() {
        StyledText valid = new StyledText(List.of(
                new Segment("직전 기간보다 선택을 망설이는 시간이 줄었고, ", List.of()),
                new Segment("도전", List.of(Mark.BOLD, Mark.HIGHLIGHT)),
                new Segment("을 행동으로 옮기는 흐름이 여러 상황에서 더 분명하게 이어졌어요.", List.of())
        ));
        client.validateComparisonEmotionSummary(valid, "도전");

        StyledText invalid = new StyledText(List.of(
                new Segment("도전", List.of(Mark.HIGHLIGHT)),
                new Segment("을 행동으로 옮기는 흐름이 더 분명해졌어요.", List.of())
        ));
        assertThatThrownBy(() -> client.validateComparisonEmotionSummary(invalid, "도전"))
                .isInstanceOf(AiResponseParseException.class);
    }

    @Test
    void COMPARISON_감정_분석문은_프롬프트보다_넓은_길이_범위를_허용한다() {
        client.validateLength(
                "가".repeat(150),
                "나".repeat(150),
                "다".repeat(130),
                true
        );

        assertThatThrownBy(() -> client.validateLength(
                "가".repeat(150),
                "나".repeat(150),
                "다".repeat(151),
                true
        )).isInstanceOfSatisfying(
                AiResponseParseException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.MONTHLY_REPORT_EMOTION_SUMMARY_LENGTH_INVALID)
        );
    }

    @Test
    void comment_길이_오류는_comment_전용_에러_코드를_사용한다() {
        assertThatThrownBy(() -> client.validateLength(
                "가".repeat(150),
                "나".repeat(149),
                "다".repeat(50),
                true
        )).isInstanceOfSatisfying(
                AiResponseParseException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.MONTHLY_REPORT_COMMENT_LENGTH_INVALID)
        );
    }

    private TypeEmotionStatsContent emptyEmotionStats() {
        return new TypeEmotionStatsContent(0, null, 0, List.of());
    }
}
