package com.devkor.ifive.nadab.domain.typereport.infra;

import com.devkor.ifive.nadab.global.core.prompt.type.report.TypeReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TypeReportLlmClient {

    private final TypeReportPromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final LlmRouter llmRouter;

    private final LlmProvider provider = LlmProvider.GEMINI;
    private static final LlmProvider REWRITE_PROVIDER = LlmProvider.GEMINI;

    private static final int MIN_TYPE_ANALYSIS = 220;
    private static final int MAX_TYPE_ANALYSIS = 300;

    private static final int MIN_PERSONA_CONTENT = 140;
    private static final int MAX_PERSONA_CONTENT = 220;

    private static final int REWRITE_MIN_MARGIN = 10;
    private static final int REWRITE_MAX_MARGIN = 10;

    private static final String[] BANNED_WORDS = {
            "패턴", "분석", "데이터", "기록", "습관", "장점", "단점", "모습", "결국", "보여집니다", "확인됩니다"
    };

    public JsonNode generateRaw(String selectedType, String patterns, String evidenceCards) {
        if (blank(selectedType) || blank(patterns) || blank(evidenceCards)) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_GENERATE_INPUT_EMPTY);
        }

        String prompt = promptLoader.loadPrompt()
                .replace("{selectedType}", selectedType)
                .replace("{patterns}", patterns)
                .replace("{evidenceCards}", evidenceCards);

        ChatClient client = llmRouter.route(provider);

        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model(GoogleGenAiChatModel.ChatModel.GEMINI_2_5_FLASH)
                .responseMimeType("application/json")
                .temperature(0.0)
                .build();

        String content = client.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();

        if (content == null || content.trim().isEmpty()) {
            throw new AiServiceUnavailableException(ErrorCode.AI_NO_RESPONSE);
        }

        try {
            JsonNode raw = objectMapper.readTree(content);

            return enforceLength(raw);
        } catch (AiResponseParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    public JsonNode rewriteOnly(JsonNode raw) {
        return enforceLength(raw);
    }

    private JsonNode enforceLength(JsonNode raw) {
        if (raw == null || raw.isNull() || !raw.isObject()) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_INPUT_SCHEMA_INVALID);
        }

        ObjectNode root = ((ObjectNode) raw).deepCopy();

        JsonNode taNode = root.get("typeAnalysis");
        JsonNode personasNode = root.get("personas");

        if (taNode == null || !taNode.isTextual() || personasNode == null || !personasNode.isArray()) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_INPUT_SCHEMA_INVALID);
        }

        String typeAnalysis = taNode.asText();
        ArrayNode personasArr = (ArrayNode) personasNode;

        boolean badTA = isOutOfRange(typeAnalysis.length(), MIN_TYPE_ANALYSIS, MAX_TYPE_ANALYSIS) || !validTwoParagraphs(typeAnalysis);

        boolean badP1 = false, badP2 = false;
        if (personasArr.size() >= 1) {
            badP1 = isOutOfRange(getPersonaContent(personasArr.get(0)).length(), MIN_PERSONA_CONTENT, MAX_PERSONA_CONTENT);
        }
        if (personasArr.size() >= 2) {
            badP2 = isOutOfRange(getPersonaContent(personasArr.get(1)).length(), MIN_PERSONA_CONTENT, MAX_PERSONA_CONTENT);
        }

        if (!badTA && !badP1 && !badP2) return root;

        ChatClient rewriteClient = llmRouter.route(REWRITE_PROVIDER);

        if (badTA) {
            log.debug("[TypeReportRewrite] typeAnalysis invalid. len={}, twoParagraph={}", typeAnalysis.length(), validTwoParagraphs(typeAnalysis));
            root.put("typeAnalysis", rewriteTypeAnalysis(rewriteClient, typeAnalysis));
        }

        if (personasArr.size() >= 1 && badP1 && personasArr.get(0).isObject()) {
            ObjectNode p0 = (ObjectNode) personasArr.get(0);
            String in = safeText(p0.get("content"));
            log.debug("[TypeReportRewrite] personas[0].content invalid. len={}", in.length());
            p0.put("content", rewritePersonaContent(rewriteClient, in, 1));
        }

        if (personasArr.size() >= 2 && badP2 && personasArr.get(1).isObject()) {
            ObjectNode p1 = (ObjectNode) personasArr.get(1);
            String in = safeText(p1.get("content"));
            log.debug("[TypeReportRewrite] personas[1].content invalid. len={}", in.length());
            p1.put("content", rewritePersonaContent(rewriteClient, in, 2));
        }

        // ===== rewrite 후 검증(원인별 코드로) =====
        String newTA = safeText(root.get("typeAnalysis")).trim();
        if (blank(newTA)) throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_OUTPUT_EMPTY);
        if (containsHardLineBreak(newTA)) throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_LINEBREAK_INVALID);
        if (containsBanned(newTA)) throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_BANNED_WORD_USED);
        if (!validTwoParagraphs(newTA)) throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_TYPE_ANALYSIS_PARAGRAPH_INVALID);
        if (isOutOfRange(newTA.length(), MIN_TYPE_ANALYSIS, MAX_TYPE_ANALYSIS)) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_TYPE_ANALYSIS_LENGTH_INVALID);
        }

        for (int i = 0; i < Math.min(2, personasArr.size()); i++) {
            JsonNode pNode = personasArr.get(i);
            if (!pNode.isObject()) {
                throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_INPUT_SCHEMA_INVALID);
            }
            ObjectNode p = (ObjectNode) pNode;

            String c = safeText(p.get("content")).trim();
            if (blank(c)) throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_OUTPUT_EMPTY);
            if (containsHardLineBreak(c)) throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_LINEBREAK_INVALID);
            if (containsBanned(c)) throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_BANNED_WORD_USED);

            if (isOutOfRange(c.length(), MIN_PERSONA_CONTENT, MAX_PERSONA_CONTENT)) {
                throw new AiResponseParseException(i == 0
                        ? ErrorCode.TYPE_REPORT_REWRITE_PERSONA_1_LENGTH_INVALID
                        : ErrorCode.TYPE_REPORT_REWRITE_PERSONA_2_LENGTH_INVALID
                );
            }
        }

        return root;
    }

    private String rewriteTypeAnalysis(ChatClient client, String in) {
        String prompt = """
        아래 'typeAnalysis' 본문은 의미는 유지하되 글자수 규칙을 맞춰야 해요.
        의미는 유지하면서 글자수(공백 포함)를 최소 %d자 ~ 최대 %d자로 맞춰서 다시 써줘요.

        [반드시 지킬 것]
        - 출력은 JSON 1개만: {"text":"..."}
        - 문장은 전부 해요체로 끝나요(합니다체 금지예요)
        - 금지 단어는 절대 쓰지 말아요: %s
        - 문단은 정확히 2문단이에요. 문자열 안에 "\\n\\n" 1회만 포함해요
        - 줄바꿈은 "\\n\\n" 1회 외에는 금지예요(\\r 금지예요)
        - 구체 사물/장소/브랜드/메뉴/지명 나열 금지예요
        - 위로/덕담/칭찬/미래 예측/비유/감탄 금지예요
        - 나열형 금지예요. 앞 문장 핵심어를 다음 문장 첫머리에서 이어가요
        - 접속사는 문단당 1~2회만 써요. 같은 접속사로 2문장 연속 시작 금지예요
        
        [주어 사용 규칙]
        - 1인칭 주어(나는)를 사용하지 말 것
        - 2인칭 주어(당신)는 필요할 때만 제한적으로 사용할 것
        - 기본적으로 주어 생략형 문장을 사용할 것

        [입력 typeAnalysis]
        %s
        """.formatted(
                MIN_TYPE_ANALYSIS + REWRITE_MIN_MARGIN,
                MAX_TYPE_ANALYSIS - REWRITE_MAX_MARGIN,
                String.join(", ", BANNED_WORDS),
                in
        );

        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model(GoogleGenAiChatModel.ChatModel.GEMINI_2_5_FLASH)
                .responseMimeType("application/json")
                .temperature(0.0)
                .build();

        String out = client.prompt().user(prompt).options(options).call().content();

        if (out == null || out.isBlank()) {
            throw new AiServiceUnavailableException(ErrorCode.TYPE_REPORT_REWRITE_NO_RESPONSE);
        }

        try {
            JsonNode node = objectMapper.readTree(out);
            String text = safeText(node.get("text")).trim();
            if (blank(text)) throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_OUTPUT_EMPTY);
            return text;
        } catch (AiResponseParseException e) {
            throw e;
        } catch (JsonParseException e) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_JSON_PARSE_FAILED);
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_JSON_MAPPING_FAILED);
        }
    }

    private String rewritePersonaContent(ChatClient client, String in, int personaIndex) {
        String prompt = """
        아래 'personas[%d].content' 본문은 의미는 유지하되 글자수 규칙을 맞춰야 해요.
        의미는 유지하면서 글자수(공백 포함)를 최소 %d자 ~ 최대 %d자로 맞춰서 다시 써줘요.

        [반드시 지킬 것]
        - 출력은 JSON 1개만: {"text":"..."}
        - 문장은 전부 해요체로 끝나요(합니다체 금지예요)
        - 금지 단어는 절대 쓰지 말아요: %s
        - 문장 수는 4~5문장으로 유지해요
        - 줄바꿈 금지예요(\\n, \\r 금지예요)
        - 위로/덕담/칭찬/미래 예측/비유/감탄 금지예요
        - 나열형 금지예요. (상황)->(선택)->(효과)->(유지)->(조건부 비용 1문장) 흐름을 지켜요
        - 조건부 비용은 1문장만, 입력에 없는 사실은 만들지 말아요
        
        [주어 사용 규칙]
        - 1인칭 주어(나는)를 사용하지 말 것
        - 2인칭 주어(당신)는 필요할 때만 제한적으로 사용할 것
        - 기본적으로 주어 생략형 문장을 사용할 것

        [입력 personas[%d].content]
        %s
        """.formatted(
                personaIndex,
                MIN_PERSONA_CONTENT + REWRITE_MIN_MARGIN,
                MAX_PERSONA_CONTENT - REWRITE_MAX_MARGIN,
                String.join(", ", BANNED_WORDS),
                personaIndex,
                in
        );

        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model(GoogleGenAiChatModel.ChatModel.GEMINI_2_5_FLASH)
                .responseMimeType("application/json")
                .temperature(0.0)
                .build();

        String out = client.prompt().user(prompt).options(options).call().content();

        if (out == null || out.isBlank()) {
            throw new AiServiceUnavailableException(ErrorCode.TYPE_REPORT_REWRITE_NO_RESPONSE);
        }

        try {
            JsonNode node = objectMapper.readTree(out);
            String text = safeText(node.get("text")).trim();
            if (blank(text)) throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_OUTPUT_EMPTY);
            return text;
        } catch (AiResponseParseException e) {
            throw e;
        } catch (JsonParseException e) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_JSON_PARSE_FAILED);
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_JSON_MAPPING_FAILED);
        }
    }

    private boolean validTwoParagraphs(String s) {
        if (s == null) return false;
        int idx = s.indexOf("\n\n");
        if (idx < 0) return false;
        return s.indexOf("\n\n", idx + 2) < 0;
    }

    private String getPersonaContent(JsonNode personaNode) {
        if (personaNode == null || personaNode.isNull() || !personaNode.isObject()) return "";
        return safeText(personaNode.get("content"));
    }

    private boolean containsBanned(String s) {
        if (s == null) return false;
        for (String w : BANNED_WORDS) {
            if (s.contains(w)) return true;
        }
        return false;
    }

    private boolean containsHardLineBreak(String s) {
        if (s == null) return false;
        // typeAnalysis는 "\n\n" 1회만 허용이지만, 여기서는 "하드 라인브레이크 존재"만 체크
        // typeAnalysis 상세 검증은 validTwoParagraphs + 별도 체크로 처리
        return s.contains("\r");
    }

    private boolean isOutOfRange(int len, int min, int max) {
        return len < min || len > max;
    }

    private String safeText(JsonNode node) {
        if (node == null || node.isNull() || !node.isTextual()) return "";
        return node.asText();
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
