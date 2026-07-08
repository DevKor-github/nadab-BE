package com.devkor.ifive.nadab.domain.dailyreport.infra;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
    void generate_returns_content_with_token_usage() {
        // given
        DailyReportLlmClient client = new DailyReportLlmClient(
                dailyReportPromptLoader,
                new ObjectMapper(),
                llmRouter,
                profileImageUrlBuilder
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

        // when
        LlmGenerationResult<?> result = client.generate("question", answerEntry);

        // then
        assertThat(result.content()).isEqualTo(new com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto(
                "good",
                "ACHIEVEMENT"
        ));
        assertThat(result.tokenUsage().inputTokens()).isEqualTo(100L);
        assertThat(result.tokenUsage().outputTokens()).isEqualTo(50L);
        assertThat(result.tokenUsage().totalTokens()).isEqualTo(150L);
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
}
