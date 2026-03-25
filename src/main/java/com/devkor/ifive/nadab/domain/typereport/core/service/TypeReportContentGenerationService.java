package com.devkor.ifive.nadab.domain.typereport.core.service;

import com.devkor.ifive.nadab.domain.typereport.application.helper.TypeReportInputAssembler;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeContentFactory;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.dto.AnalysisTypeCandidateDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.EvidenceCardDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.PatternExtractionResultDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeReportContentDto;
import com.devkor.ifive.nadab.domain.typereport.infra.TypeReportLlmClient;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeReportContentGenerationService {

    private static final int TYPE_ANALYSIS_MIN = 100;
    private static final int TYPE_ANALYSIS_MAX = 350;

    private static final int PERSONA_COUNT = 2;
    private static final int PERSONA_TITLE_MIN = 1;
    private static final int PERSONA_TITLE_MAX = 20;

    private static final int PERSONA_CONTENT_MIN = 100;
    private static final int PERSONA_CONTENT_MAX = 300;

    private final TypeReportLlmClient llmClient;

    public TypeReportContentDto generate(
            AnalysisTypeCandidateDto selectedType,
            PatternExtractionResultDto patterns,
            List<EvidenceCardDto> allCards,
            TypeEmotionStatsContent emotionStats,
            String expectedAnalysisTypeCode
    ) {
        if (selectedType == null || patterns == null || allCards == null || allCards.isEmpty()
                || emotionStats == null || expectedAnalysisTypeCode == null) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_GENERATE_INPUT_EMPTY);
        }

        // 대표 증거 카드 구성: Step2 evidence_ids 기반으로 2~3개씩 붙여서 입력 만들기
        List<EvidenceCardDto> representative = pickRepresentativeCards(patterns, allCards);

        String selectedTypeText = TypeReportInputAssembler.assembleSelectedType(selectedType);
        String patternsText = TypeReportInputAssembler.assemblePatterns(patterns);
        String evidenceCardsText = TypeReportInputAssembler.assembleEvidenceCards(representative);

        JsonNode raw = llmClient.generateRaw(selectedTypeText, patternsText, evidenceCardsText);

        // 1차 파싱/검증
        try {
            TypeReportContentDto dto = toDto(raw);
            validate(dto, expectedAnalysisTypeCode);
            return dto;
        } catch (AiResponseParseException e) {
            // 리페어 1회
            JsonNode repaired = llmClient.rewriteOnly(raw);
            TypeReportContentDto dto2 = toDto(repaired);
            validate(dto2, expectedAnalysisTypeCode);
            return dto2;
        }
    }

    private List<EvidenceCardDto> pickRepresentativeCards(PatternExtractionResultDto patterns, List<EvidenceCardDto> allCards) {
        Map<String, EvidenceCardDto> map = allCards.stream()
                .collect(Collectors.toMap(EvidenceCardDto::id, c -> c, (a, b) -> a));

        List<EvidenceCardDto> result = new ArrayList<>();
        for (PatternExtractionResultDto.PatternDto p : patterns.patterns()) {
            // 패턴당 최대 3개만
            int limit = Math.min(3, p.evidenceIds().size());
            for (int i = 0; i < limit; i++) {
                EvidenceCardDto c = map.get(p.evidenceIds().get(i));
                if (c != null) result.add(c);
            }
        }

        // 중복 제거(혹시나)
        LinkedHashMap<String, EvidenceCardDto> dedup = new LinkedHashMap<>();
        for (EvidenceCardDto c : result) {
            dedup.putIfAbsent(c.id(), c);
        }
        return new ArrayList<>(dedup.values());
    }

    private TypeReportContentDto toDto(JsonNode raw) {
        JsonNode codeNode = raw.get("analysisTypeCode");
        JsonNode typeAnalysisNode = raw.get("typeAnalysis");
        JsonNode personasNode = raw.get("personas");

        if (codeNode == null || !codeNode.isTextual() ||
                typeAnalysisNode == null || !typeAnalysisNode.isTextual() ||
                personasNode == null || !personasNode.isArray()) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_JSON_MISSING_FIELDS);
        }

        String code = codeNode.asText();
        String typeAnalysis = typeAnalysisNode.asText();

        List<TypeReportContentDto.PersonaDto> personas = new ArrayList<>();
        for (JsonNode p : personasNode) {
            JsonNode titleNode = p.get("title");
            JsonNode contentNode = p.get("content");
            if (titleNode == null || !titleNode.isTextual() || contentNode == null || !contentNode.isTextual()) {
                throw new AiResponseParseException(ErrorCode.TYPE_REPORT_PERSONAS_INVALID);
            }
            personas.add(new TypeReportContentDto.PersonaDto(titleNode.asText(), contentNode.asText()));
        }

        return new TypeReportContentDto(
                code,
                typeAnalysis,
                TypeContentFactory.fromPlainText(typeAnalysis).normalized(),
                TypeContentFactory.emptyText().normalized(),
                personas
        );
    }

    private void validate(TypeReportContentDto dto, String expectedAnalysisTypeCode) {
        if (!expectedAnalysisTypeCode.equals(dto.analysisTypeCode())) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_CODE_MISMATCH);
        }

        int typeLen = dto.typeAnalysis() == null ? 0 : dto.typeAnalysis().length();
        if (typeLen < TYPE_ANALYSIS_MIN || typeLen > TYPE_ANALYSIS_MAX) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_TYPE_ANALYSIS_LENGTH_INVALID);
        }

        List<TypeReportContentDto.PersonaDto> personas = dto.personas();
        if (personas == null || personas.size() != PERSONA_COUNT) {
            throw new AiResponseParseException(ErrorCode.TYPE_REPORT_PERSONA_COUNT_INVALID);
        }

        for (TypeReportContentDto.PersonaDto p : personas) {
            int titleLen = p.title() == null ? 0 : p.title().length();
            if (titleLen < PERSONA_TITLE_MIN || titleLen > PERSONA_TITLE_MAX) {
                throw new AiResponseParseException(ErrorCode.TYPE_REPORT_PERSONA_TITLE_INVALID);
            }

            int contentLen = p.content() == null ? 0 : p.content().length();
            if (contentLen < PERSONA_CONTENT_MIN || contentLen > PERSONA_CONTENT_MAX) {
                throw new AiResponseParseException(ErrorCode.TYPE_REPORT_PERSONA_CONTENT_LENGTH_INVALID);
            }
        }
    }
}
