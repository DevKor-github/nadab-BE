package com.devkor.ifive.nadab.domain.dailyreport.infra;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.properties.DailyReportLlmProperties;
import com.devkor.ifive.nadab.domain.dailyreport.core.properties.DailyReportLlmProperties.TokenLimitParameter;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.core.prompt.daily.DailyReportPromptLoader;
import com.devkor.ifive.nadab.global.infra.llm.LlmGenerationResult;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyReportLlmClientTest {

    @Mock
    DailyReportPromptLoader dailyReportPromptLoader;

    @Mock
    LlmRouter llmRouter;

    @Mock
    ProfileImageUrlBuilder profileImageUrlBuilder;

    @Mock
    ChatClient chatClient;

    @Mock
    ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    ChatClient.CallResponseSpec callResponseSpec;

    @Test
    void generate_uses_default_options_and_returns_content_with_token_usage() {
        // given
        DailyReportLlmProperties properties = new DailyReportLlmProperties();

        // when
        GenerationExecution execution = generateWith(properties);

        // then
        assertThat(execution.result().content()).isEqualTo(new com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto(
                "good",
                "ACHIEVEMENT"
        ));
        assertThat(execution.result().tokenUsage().inputTokens()).isEqualTo(100L);
        assertThat(execution.result().tokenUsage().outputTokens()).isEqualTo(50L);
        assertThat(execution.result().tokenUsage().totalTokens()).isEqualTo(150L);
        assertThat(execution.options().getModel()).isEqualTo("gpt-4o-mini");
        assertThat(execution.options().getTemperature()).isEqualTo(0.3);
        assertThat(execution.options().getMaxTokens()).isEqualTo(512);
        assertThat(execution.options().getMaxCompletionTokens()).isNull();
        assertThat(execution.options().getReasoningEffort()).isNull();
    }

    @Test
    void generate_uses_dev_experiment_options() {
        // given
        DailyReportLlmProperties properties = new DailyReportLlmProperties();
        properties.setModel("gpt-5.6-luna");
        properties.setTemperature(1.0);
        properties.setMaxOutputTokens(512);
        properties.setTokenLimitParameter(TokenLimitParameter.MAX_COMPLETION_TOKENS);
        properties.setReasoningEffort("none");

        // when
        GenerationExecution execution = generateWith(properties);

        // then
        assertThat(execution.options().getModel()).isEqualTo("gpt-5.6-luna");
        assertThat(execution.options().getTemperature()).isEqualTo(1.0);
        assertThat(execution.options().getMaxTokens()).isNull();
        assertThat(execution.options().getMaxCompletionTokens()).isEqualTo(512);
        assertThat(execution.options().getReasoningEffort()).isEqualTo("none");
    }

    private GenerationExecution generateWith(DailyReportLlmProperties properties) {
        DailyReportLlmClient client = new DailyReportLlmClient(
                dailyReportPromptLoader,
                new ObjectMapper(),
                llmRouter,
                profileImageUrlBuilder,
                properties
        );
        AnswerEntry answerEntry = AnswerEntry.create(
                User.createUser("test@test.com", "hashed_password"),
                dailyQuestion(1L),
                "answer",
                LocalDate.now(),
                null
        );

        when(dailyReportPromptLoader.loadPrompt()).thenReturn("question={question}, answer={answer}");
        when(dailyReportPromptLoader.loadWithImagePrompt()).thenReturn("image question={question}, answer={answer}");
        when(llmRouter.route(LlmProvider.OPENAI)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.messages(any(Message[].class))).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse(
                "{\"message\":\"good\",\"emotion\":\"ACHIEVEMENT\"}",
                new DefaultUsage(100, 50, 150)
        ));

        LlmGenerationResult<?> result = client.generate("question", answerEntry);

        ArgumentCaptor<OpenAiChatOptions> optionsCaptor = ArgumentCaptor.forClass(OpenAiChatOptions.class);
        verify(requestSpec).options(optionsCaptor.capture());

        return new GenerationExecution(result, optionsCaptor.getValue());
    }

    private ChatResponse chatResponse(String content, DefaultUsage usage) {
        return new ChatResponse(
                List.of(new Generation(new AssistantMessage(content))),
                ChatResponseMetadata.builder()
                        .usage(usage)
                        .build()
        );
    }

    private DailyQuestion dailyQuestion(Long id) {
        return mock(DailyQuestion.class);
    }

    private record GenerationExecution(LlmGenerationResult<?> result, OpenAiChatOptions options) {
    }
}
