package com.devkor.ifive.nadab.domain.askchat.infra;

import com.devkor.ifive.nadab.domain.askchat.application.helper.AskChatAnswerPromptAugmenter;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPrompt;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPromptContext;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerReferenceDocument;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatGeneratedAnswer;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatAnswerProperties;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatAnswerLlmClientTest {

    @Mock
    private AskChatAnswerPromptAugmenter promptAugmenter;

    @Mock
    private LlmRouter llmRouter;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private AskChatAnswerProperties properties;
    private AskChatAnswerLlmClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        properties = new AskChatAnswerProperties();
        properties.setProvider(LlmProvider.OPENAI);
        properties.setModel("gpt-5.6-luna");
        properties.setReasoningEffort("low");
        properties.setTemperature(1.0);
        properties.setMaxTokens(900);
        properties.setFollowUpQuestionCount(2);
        objectMapper = new ObjectMapper();
        client = new AskChatAnswerLlmClient(promptAugmenter, properties, llmRouter, objectMapper);
    }

    @Test
    void generate_returns_answer_with_token_usage_and_reference_document_ids() throws Exception {
        AskChatAnswerPromptContext context = context();
        when(promptAugmenter.augment(context)).thenReturn(new AskChatAnswerPrompt("system", "user"));
        when(llmRouter.route(LlmProvider.OPENAI)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("system")).thenReturn(requestSpec);
        when(requestSpec.user("user")).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse(
                objectMapper.writeValueAsString(new AskChatGeneratedAnswer(
                        "꾸준함이 강점으로 보여요.",
                        List.of("언제 꾸준함이 가장 잘 드러났나요?", "요즘 지키고 싶은 루틴은 무엇인가요?")
                )),
                new DefaultUsage(100, 50, 150)
        ));

        var result = client.generate(context);

        ArgumentCaptor<OpenAiChatOptions> optionsCaptor = ArgumentCaptor.forClass(OpenAiChatOptions.class);
        verify(requestSpec).options(optionsCaptor.capture());

        assertThat(result.answer().answer()).isEqualTo("꾸준함이 강점으로 보여요.");
        assertThat(result.answer().followUpQuestions()).hasSize(2);
        assertThat(result.provider()).isEqualTo(LlmProvider.OPENAI);
        assertThat(result.model()).isEqualTo("gpt-5.6-luna");
        assertThat(result.tokenUsage().inputTokens()).isEqualTo(100L);
        assertThat(result.tokenUsage().outputTokens()).isEqualTo(50L);
        assertThat(result.tokenUsage().totalTokens()).isEqualTo(150L);
        assertThat(result.referenceDocumentIds()).containsExactly(100L);
        assertThat(optionsCaptor.getValue().getTemperature()).isEqualTo(1.0);
        assertThat(optionsCaptor.getValue().getReasoningEffort()).isEqualTo("low");
        assertThat(optionsCaptor.getValue().getMaxTokens()).isNull();
        assertThat(optionsCaptor.getValue().getMaxCompletionTokens()).isEqualTo(900);
    }

    @Test
    void generate_throws_parse_exception_when_response_is_not_json() {
        AskChatAnswerPromptContext context = context();
        when(promptAugmenter.augment(context)).thenReturn(new AskChatAnswerPrompt("system", "user"));
        when(llmRouter.route(LlmProvider.OPENAI)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("system")).thenReturn(requestSpec);
        when(requestSpec.user("user")).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse("not-json", new DefaultUsage(1, 1, 2)));

        assertThatThrownBy(() -> client.generate(context))
                .isInstanceOfSatisfying(AiResponseParseException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AI_RESPONSE_PARSE_FAILED));
    }

    @Test
    void generate_throws_format_exception_when_answer_is_blank() throws Exception {
        AskChatAnswerPromptContext context = context();
        when(promptAugmenter.augment(context)).thenReturn(new AskChatAnswerPrompt("system", "user"));
        when(llmRouter.route(LlmProvider.OPENAI)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("system")).thenReturn(requestSpec);
        when(requestSpec.user("user")).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse(
                objectMapper.writeValueAsString(new AskChatGeneratedAnswer(" ", List.of())),
                new DefaultUsage(1, 1, 2)
        ));

        assertThatThrownBy(() -> client.generate(context))
                .isInstanceOfSatisfying(AiResponseParseException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AI_RESPONSE_FORMAT_INVALID));
    }

    @Test
    void generate_throws_format_exception_when_answer_contains_unsupported_script() throws Exception {
        AskChatAnswerPromptContext context = context();
        when(promptAugmenter.augment(context)).thenReturn(new AskChatAnswerPrompt("system", "user"));
        when(llmRouter.route(LlmProvider.OPENAI)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("system")).thenReturn(requestSpec);
        when(requestSpec.user("user")).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse(
                objectMapper.writeValueAsString(new AskChatGeneratedAnswer(
                        "필요성이 अस्पष्ट한 소비는 보류하는 편이 나아요.",
                        List.of()
                )),
                new DefaultUsage(1, 1, 2)
        ));

        assertThatThrownBy(() -> client.generate(context))
                .isInstanceOfSatisfying(AiResponseParseException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AI_RESPONSE_UNSUPPORTED_SCRIPT));
    }

    @Test
    void generate_throws_format_exception_when_follow_up_question_exceeds_30_characters() throws Exception {
        AskChatAnswerPromptContext context = context();
        when(promptAugmenter.augment(context)).thenReturn(new AskChatAnswerPrompt("system", "user"));
        when(llmRouter.route(LlmProvider.OPENAI)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("system")).thenReturn(requestSpec);
        when(requestSpec.user("user")).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse(
                objectMapper.writeValueAsString(new AskChatGeneratedAnswer(
                        "꾸준함이 강점으로 보여요.",
                        List.of("가".repeat(31))
                )),
                new DefaultUsage(1, 1, 2)
        ));

        assertThatThrownBy(() -> client.generate(context))
                .isInstanceOfSatisfying(AiResponseParseException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AI_RESPONSE_FORMAT_INVALID));
    }

    @Test
    void generate_accepts_follow_up_question_with_30_characters() throws Exception {
        AskChatAnswerPromptContext context = context();
        when(promptAugmenter.augment(context)).thenReturn(new AskChatAnswerPrompt("system", "user"));
        when(llmRouter.route(LlmProvider.OPENAI)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("system")).thenReturn(requestSpec);
        when(requestSpec.user("user")).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse(
                objectMapper.writeValueAsString(new AskChatGeneratedAnswer(
                        "꾸준함이 강점으로 보여요.",
                        List.of("가".repeat(30))
                )),
                new DefaultUsage(1, 1, 2)
        ));

        var result = client.generate(context);

        assertThat(result.answer().followUpQuestions()).containsExactly("가".repeat(30));
    }

    @Test
    void generate_throws_unavailable_exception_when_response_is_empty() {
        AskChatAnswerPromptContext context = context();
        when(promptAugmenter.augment(context)).thenReturn(new AskChatAnswerPrompt("system", "user"));
        when(llmRouter.route(LlmProvider.OPENAI)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("system")).thenReturn(requestSpec);
        when(requestSpec.user("user")).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse("", new DefaultUsage(1, 1, 2)));

        assertThatThrownBy(() -> client.generate(context))
                .isInstanceOfSatisfying(AiServiceUnavailableException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AI_NO_RESPONSE));
    }

    private AskChatAnswerPromptContext context() {
        return new AskChatAnswerPromptContext(
                1L,
                10L,
                "나는 어떤 사람에 가까워?",
                List.of(),
                List.of(new AskChatAnswerReferenceDocument(
                        100L,
                        AskChatRagDocumentSourceType.ANSWER_ENTRY,
                        200L,
                        InterestCode.VALUES,
                        "사용자는 꾸준함을 중요하게 말했다.",
                        0.1
                ))
        );
    }

    private ChatResponse chatResponse(String content, DefaultUsage usage) {
        return new ChatResponse(
                List.of(new Generation(new AssistantMessage(content))),
                ChatResponseMetadata.builder()
                        .usage(usage)
                        .build()
        );
    }
}
