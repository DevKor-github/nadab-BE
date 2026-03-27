package com.devkor.ifive.nadab.domain.typereport.infra;

import com.devkor.ifive.nadab.global.core.prompt.type.report.TypeReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.devkor.ifive.nadab.global.shared.reportcontent.Mark;
import com.devkor.ifive.nadab.global.shared.reportcontent.Segment;
import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.EnumSet;
import java.util.List;

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

    private static final int MAX_HL_TYPE_ANALYSIS = 2;
    private static final int MAX_HL_EMOTION_SUMMARY = 2;
    private static final int MAX_HL_SEG_LEN = 32;

    private static final String[] BANNED_WORDS = {
            "패턴", "분석", "데이터", "기록", "습관", "장점", "단점", "모습", "결국", "보여집니다", "확인됩니다"
    };

    public JsonNode generateRaw(String selectedType, String patterns, String evidenceCards, String emotionStats) {
        if (blank(selectedType) || blank(patterns) || blank(evidenceCards) || blank(emotionStats)) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_GENERATE_INPUT_EMPTY);
        }

        String prompt = promptLoader.loadPrompt()
                .replace("{selectedType}", selectedType)
                .replace("{patterns}", patterns)
                .replace("{evidenceCards}", evidenceCards)
                .replace("{emotionStats}", emotionStats);

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
            if (raw.isObject()) {
                hydrateTypeAnalysisIfMissing((ObjectNode) raw);
            }
            return raw;
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

        JsonNode personasNode = root.get("personas");
        if (personasNode == null || !personasNode.isArray()) {
            return root;
        }
        validateRichContentsIfPresent(root);

        TypeTextContent sourceTypeAnalysisContent = parseTypeTextContentOrFallback(root.get("typeAnalysisContent"), "");
        String typeAnalysis = sourceTypeAnalysisContent.plainText().trim();
        if (blank(typeAnalysis)) {
            return root;
        }
        root.put("typeAnalysis", typeAnalysis);
        ArrayNode personasArr = (ArrayNode) personasNode;

        boolean badTA = blank(typeAnalysis)
                || isOutOfRange(typeAnalysis.length(), MIN_TYPE_ANALYSIS, MAX_TYPE_ANALYSIS)
                || !validTwoParagraphs(typeAnalysis);

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
            StyledText rewritten = rewriteTypeAnalysis(rewriteClient, sourceTypeAnalysisContent.styledText());
            validateStyledText(rewritten, true);
            TypeTextContent rewrittenContent = new TypeTextContent(rewritten).normalized();
            root.set("typeAnalysisContent", objectMapper.valueToTree(rewrittenContent));
            root.put("typeAnalysis", rewrittenContent.plainText());
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

            if (isOutOfRange(c.length(), MIN_PERSONA_CONTENT, MAX_PERSONA_CONTENT)) {
                throw new AiResponseParseException(i == 0
                        ? ErrorCode.TYPE_REPORT_REWRITE_PERSONA_1_LENGTH_INVALID
                        : ErrorCode.TYPE_REPORT_REWRITE_PERSONA_2_LENGTH_INVALID
                );
            }
        }

        return root;
    }

    private void validateRichContentsIfPresent(ObjectNode root) {
        validateTypeTextContentIfPresent(root.get("typeAnalysisContent"), true);
        validateTypeTextContentIfPresent(root.get("emotionSummaryContent"), false);
    }

    private void validateTypeTextContentIfPresent(JsonNode node, boolean isTypeAnalysis) {
        if (node == null || node.isNull()) {
            return;
        }
        validateTypeTextContent(node, isTypeAnalysis);
    }

    private void validateTypeTextContent(JsonNode node, boolean isTypeAnalysis) {
        if (node == null || node.isNull() || !node.isObject()) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_JSON_MISSING_FIELDS);
        }

        TypeTextContent parsed;
        try {
            parsed = objectMapper.treeToValue(node, TypeTextContent.class);
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_AI_SEGMENT_INVALID);
        }

        if (parsed == null || parsed.styledText() == null) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_JSON_MISSING_FIELDS);
        }

        validateStyledText(parsed.styledText(), isTypeAnalysis);
    }

    private void validateStyledText(StyledText styledText, boolean isTypeAnalysis) {
        if (styledText.segments() == null || styledText.segments().isEmpty()) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_JSON_MISSING_FIELDS);
        }

        int highlightCount = 0;

        for (Segment segment : styledText.segments()) {
            if (segment == null || segment.text() == null || segment.text().isBlank()) {
                throw new AiResponseParseException(ErrorCode.TYPE_REPORT_AI_SEGMENT_INVALID);
            }

            String text = segment.text();

            if (!isTypeAnalysis && text.contains("\n") || text.contains("\r")) {
                throw new AiResponseParseException(ErrorCode.TYPE_REPORT_AI_SEGMENT_INVALID);
            }

            List<Mark> marks = segment.marks() == null ? List.of() : segment.marks();
            EnumSet<Mark> markSet = EnumSet.noneOf(Mark.class);
            for (Mark mark : marks) {
                if (mark == null) {
                    throw new AiResponseParseException(ErrorCode.TYPE_REPORT_AI_SEGMENT_INVALID);
                }
                markSet.add(mark);
            }

            if (markSet.contains(Mark.HIGHLIGHT)) {
                if (!markSet.contains(Mark.BOLD)) {
                    throw new AiResponseParseException(ErrorCode.TYPE_REPORT_HIGHLIGHT_WITHOUT_BOLD);
                }
                highlightCount++;
                if (text.length() > MAX_HL_SEG_LEN) {
                    throw new AiResponseParseException(ErrorCode.TYPE_REPORT_HIGHLIGHT_SEGMENT_TOO_LONG);
                }
            }
        }

        int maxHighlightCount = isTypeAnalysis ? MAX_HL_TYPE_ANALYSIS : MAX_HL_EMOTION_SUMMARY;
        if (highlightCount > maxHighlightCount) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_HIGHLIGHT_COUNT_EXCEEDED);
        }
    }

    private StyledText rewriteTypeAnalysis(ChatClient client, StyledText in) {
        StyledText inputStyled = in == null ? new StyledText(List.of(new Segment("", List.of()))) : in.normalized();
        String jsonInput;
        try {
            jsonInput = objectMapper.writeValueAsString(inputStyled);
        } catch (JsonProcessingException e) {
            throw new AiResponseParseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        String prompt = """
        아래 JSON의 segments[].text를 이어 붙인 순수 텍스트 의미를 유지하면서,
        글자 수(공백 포함)를 최소 %d자 ~ 최대 %d자로 맞춰 다시 작성해 주세요.

        [반드시 지킬 것]
        - 출력은 JSON 1개만: {"segments":[{"text":"...","marks":[...]}]}
        - marks는 "BOLD", "HIGHLIGHT"만 허용
        - "HIGHLIGHT"가 있으면 같은 segment에 반드시 "BOLD"를 포함
        - HIGHLIGHT segment 개수는 최대 %d개
        - HIGHLIGHT segment 길이는 대략 6~25자 범위로 유지해요.
        - 줄바꿈 문자(\\n, \\r) 금지
        - 문단 구분은 정확히 1번의 "\\n\\n"만 허용
        - 금지 단어 사용 금지: %s
        - 리라이트 후 순수 텍스트는 해요체로 마무리

        [입력 JSON]
        %s
        """.formatted(
                MIN_TYPE_ANALYSIS + REWRITE_MIN_MARGIN,
                MAX_TYPE_ANALYSIS - REWRITE_MAX_MARGIN,
                MAX_HL_TYPE_ANALYSIS,
                String.join(", ", BANNED_WORDS),
                jsonInput
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
            StyledText rewritten = objectMapper.readValue(out, StyledText.class);
            if (rewritten == null || rewritten.segments() == null || rewritten.segments().isEmpty()) {
                throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_OUTPUT_EMPTY);
            }
            return rewritten;
        } catch (AiResponseParseException e) {
            throw e;
        } catch (JsonParseException e) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_JSON_PARSE_FAILED);
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_REWRITE_JSON_MAPPING_FAILED);
        }
    }

    private TypeTextContent parseTypeTextContentOrFallback(JsonNode node, String fallbackPlain) {
        String safeFallback = fallbackPlain == null ? "" : fallbackPlain;
        TypeTextContent fallback = new TypeTextContent(
                new StyledText(List.of(new Segment(safeFallback, List.of())))
        ).normalized();

        if (node == null || node.isNull()) {
            return fallback;
        }
        try {
            TypeTextContent parsed = objectMapper.treeToValue(node, TypeTextContent.class);
            if (parsed == null || parsed.styledText() == null) {
                return fallback;
            }
            return parsed.normalized();
        } catch (Exception e) {
            return fallback;
        }
    }

    private void hydrateTypeAnalysisIfMissing(ObjectNode root) {
        String current = safeText(root.get("typeAnalysis")).trim();
        if (!blank(current)) {
            return;
        }
        String fromContent = parseTypeTextContentOrFallback(root.get("typeAnalysisContent"), "").plainText().trim();
        if (!blank(fromContent)) {
            root.put("typeAnalysis", fromContent);
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
