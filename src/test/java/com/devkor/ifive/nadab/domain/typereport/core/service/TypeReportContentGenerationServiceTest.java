package com.devkor.ifive.nadab.domain.typereport.core.service;

import com.devkor.ifive.nadab.domain.typereport.core.content.TypeContentFactory;
import com.devkor.ifive.nadab.domain.typereport.core.dto.AnalysisTypeCandidateDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.EvidenceCardDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.PatternExtractionResultDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeReportContentDto;
import com.devkor.ifive.nadab.domain.typereport.infra.TypeReportLlmClient;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.infra.llm.LlmGenerationResult;
import com.devkor.ifive.nadab.global.infra.llm.LlmTokenUsage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypeReportContentGenerationServiceTest {

    @Mock
    TypeReportLlmClient llmClient;

    ObjectMapper objectMapper;
    TypeReportContentGenerationService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new TypeReportContentGenerationService(llmClient, objectMapper);
    }

    @Test
    void generate_rewrites_persona_content_containing_unsupported_marks_tag() {
        JsonNode malformed = reportJson(malformedPersonaContent());
        JsonNode repaired = reportJson("수".repeat(160));

        when(llmClient.generateRaw(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new LlmGenerationResult<>(malformed, LlmTokenUsage.empty()));
        when(llmClient.rewriteOnly(malformed))
                .thenReturn(new LlmGenerationResult<>(repaired, LlmTokenUsage.empty()));

        LlmGenerationResult<TypeReportContentDto> result = service.generate(
                selectedType(),
                patterns(),
                evidenceCards(),
                TypeContentFactory.emptyEmotionStats(),
                "TYPE_A"
        );

        assertThat(result.content().personas().get(0).content()).isEqualTo("수".repeat(160));
        verify(llmClient).rewriteOnly(malformed);
    }

    @Test
    void generate_rejects_persona_content_when_rewrite_keeps_unsupported_marks_tag() {
        JsonNode malformed = reportJson(malformedPersonaContent());

        when(llmClient.generateRaw(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new LlmGenerationResult<>(malformed, LlmTokenUsage.empty()));
        when(llmClient.rewriteOnly(malformed))
                .thenReturn(new LlmGenerationResult<>(malformed, LlmTokenUsage.empty()));

        assertThatThrownBy(() -> service.generate(
                selectedType(),
                patterns(),
                evidenceCards(),
                TypeContentFactory.emptyEmotionStats(),
                "TYPE_A"
        )).isInstanceOfSatisfying(AiResponseParseException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TYPE_REPORT_PERSONAS_INVALID));
    }

    private JsonNode reportJson(String firstPersonaContent) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("analysisTypeCode", "TYPE_A");
        root.put("typeAnalysis", "가".repeat(110) + "\n\n" + "나".repeat(110));

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

    private AnalysisTypeCandidateDto selectedType() {
        return new AnalysisTypeCandidateDto("TYPE_A", "유형", "설명", "#1", "#2", "#3");
    }

    private PatternExtractionResultDto patterns() {
        return new PatternExtractionResultDto(List.of(
                new PatternExtractionResultDto.PatternDto("패턴", List.of("D1"), "근거")
        ));
    }

    private List<EvidenceCardDto> evidenceCards() {
        return List.of(new EvidenceCardDto("D1", LocalDate.of(2026, 8, 19), "근거 카드"));
    }
}
