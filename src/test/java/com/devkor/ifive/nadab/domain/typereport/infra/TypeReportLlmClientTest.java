package com.devkor.ifive.nadab.domain.typereport.infra;

import com.devkor.ifive.nadab.domain.typereport.core.content.TypeContentFactory;
import com.devkor.ifive.nadab.global.core.prompt.type.report.TypeReportPromptLoader;
import com.devkor.ifive.nadab.global.infra.llm.LlmGenerationResult;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypeReportLlmClientTest {

    @Mock
    TypeReportPromptLoader promptLoader;

    @Mock
    LlmRouter llmRouter;

    @Mock
    ChatClient chatClient;

    @Mock
    ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    ChatClient.CallResponseSpec callResponseSpec;

    @Test
    void rewriteOnly_rewrites_persona_content_containing_unsupported_marks_tag() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReportLlmClient client = new TypeReportLlmClient(promptLoader, objectMapper, llmRouter);
        ObjectNode raw = reportJson(objectMapper, malformedPersonaContent());
        String rewrittenContent = "수".repeat(160);

        when(llmRouter.route(LlmProvider.GEMINI)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse(
                objectMapper.writeValueAsString(Map.of("text", rewrittenContent))
        ));

        LlmGenerationResult<JsonNode> result = client.rewriteOnly(raw);

        assertThat(result.content().path("personas").get(0).path("content").asText())
                .isEqualTo(rewrittenContent);
        assertThat(result.content().path("personas").get(1).path("content").asText())
                .isEqualTo("다".repeat(160));
    }

    private ObjectNode reportJson(ObjectMapper objectMapper, String firstPersonaContent) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("analysisTypeCode", "TYPE_A");
        String typeAnalysis = "가".repeat(110) + "\n\n" + "나".repeat(110);
        root.put("typeAnalysis", typeAnalysis);
        root.set("typeAnalysisContent", objectMapper.valueToTree(TypeContentFactory.fromPlainText(typeAnalysis)));

        ArrayNode personas = root.putArray("personas");
        personas.addObject()
                .put("title", "내면의 균형")
                .put("content", firstPersonaContent);
        personas.addObject()
                .put("title", "상황 적응 전략")
                .put("content", "다".repeat(160));
        return root;
    }

    private String malformedPersonaContent() {
        return "가".repeat(100)
                + "<marks text=\"내적 만족감을 높여요\" marks=[\"BOLD\",\"HIGHLIGHT\"]>"
                + "나".repeat(20);
    }

    private ChatResponse chatResponse(String content) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(content))));
    }
}
