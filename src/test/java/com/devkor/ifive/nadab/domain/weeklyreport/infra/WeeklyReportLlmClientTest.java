package com.devkor.ifive.nadab.domain.weeklyreport.infra;

import com.devkor.ifive.nadab.global.core.prompt.weekly.WeeklyReportPromptLoader;
import com.devkor.ifive.nadab.global.infra.llm.LlmGenerationResult;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.devkor.ifive.nadab.global.shared.reportcontent.AiReportResultDto;
import com.devkor.ifive.nadab.global.shared.reportcontent.LlmResultDto;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContent;
import com.devkor.ifive.nadab.global.shared.reportcontent.Segment;
import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyReportLlmClientTest {

    @Mock
    WeeklyReportPromptLoader weeklyReportPromptLoader;

    @Mock
    LlmRouter llmRouter;

    @Mock
    ChatClient chatClient;

    @Mock
    ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    ChatClient.CallResponseSpec callResponseSpec;

    @Test
    void generate_returns_content_with_token_usage() throws Exception {
        WeeklyReportLlmClient client = new WeeklyReportLlmClient(
                weeklyReportPromptLoader,
                new ObjectMapper(),
                llmRouter
        );

        when(weeklyReportPromptLoader.loadPrompt())
                .thenReturn("start={weekStartDate}, end={weekEndDate}, entries={entries}");
        when(llmRouter.route(LlmProvider.GEMINI)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse(
                reportJson("steady growth", "a".repeat(150), "b".repeat(80)),
                new DefaultUsage(100, 50, 150)
        ));

        LlmGenerationResult<AiReportResultDto> result =
                client.generate("2026-01-01", "2026-01-07", "entries");

        assertThat(result.content().content().summary()).isEqualTo("steady growth");
        assertThat(result.content().discovered()).hasSize(150);
        assertThat(result.content().improve()).hasSize(80);
        assertThat(result.tokenUsage().inputTokens()).isEqualTo(100L);
        assertThat(result.tokenUsage().outputTokens()).isEqualTo(50L);
        assertThat(result.tokenUsage().totalTokens()).isEqualTo(150L);
    }

    @Test
    void generate_adds_rewrite_token_usage() throws Exception {
        WeeklyReportLlmClient client = new WeeklyReportLlmClient(
                weeklyReportPromptLoader,
                new ObjectMapper(),
                llmRouter
        );

        when(weeklyReportPromptLoader.loadPrompt())
                .thenReturn("start={weekStartDate}, end={weekEndDate}, entries={entries}");
        when(llmRouter.route(LlmProvider.GEMINI)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(
                chatResponse(
                        reportJson("steady growth", "a".repeat(20), "b".repeat(80)),
                        new DefaultUsage(100, 50, 150)
                ),
                chatResponse(
                        styledTextJson("c".repeat(150)),
                        new DefaultUsage(20, 10, 30)
                )
        );

        LlmGenerationResult<AiReportResultDto> result =
                client.generate("2026-01-01", "2026-01-07", "entries");

        assertThat(result.content().discovered()).hasSize(150);
        assertThat(result.tokenUsage().inputTokens()).isEqualTo(120L);
        assertThat(result.tokenUsage().outputTokens()).isEqualTo(60L);
        assertThat(result.tokenUsage().totalTokens()).isEqualTo(180L);
    }

    private ChatResponse chatResponse(String content, DefaultUsage usage) {
        return new ChatResponse(
                List.of(new Generation(new AssistantMessage(content))),
                ChatResponseMetadata.builder()
                        .usage(usage)
                        .build()
        );
    }

    private String reportJson(String summary, String discovered, String improve) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(new LlmResultDto(
                summary,
                new StyledText(List.of(new Segment(discovered, List.of()))),
                new StyledText(List.of(new Segment(improve, List.of())))
        ));
    }

    private String styledTextJson(String text) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(new ReportContent(
                "",
                new StyledText(List.of(new Segment(text, List.of()))),
                null
        ).discovered());
    }
}
