package com.devkor.ifive.nadab.domain.typereport.core.service;

import com.devkor.ifive.nadab.domain.typereport.application.helper.TypeSelectionInputAssembler;
import com.devkor.ifive.nadab.domain.typereport.core.dto.AnalysisTypeCandidateDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.PatternExtractionResultDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeSelectionResultDto;
import com.devkor.ifive.nadab.domain.typereport.infra.TypeSelectLlmClient;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeSelectionService {

    private static final int BECAUSE_MIN = 2;
    private static final int BECAUSE_MAX = 3;
    private static final int EVID_MIN = 1;
    private static final int EVID_MAX = 4;
    private static final int PATTERN_MAX_LEN = 20;

    private final TypeSelectLlmClient llmClient;

    public TypeSelectionResultDto select(
            List<AnalysisTypeCandidateDto> candidates,
            PatternExtractionResultDto patternResult
    ) {
        if (candidates == null || candidates.size() != 6 || patternResult == null || patternResult.patterns() == null || patternResult.patterns().isEmpty()) {
            throw new AiResponseParseException(ErrorCode.TYPE_SELECT_INPUT_EMPTY);
        }

        Set<String> candidateCodes = candidates.stream()
                .map(AnalysisTypeCandidateDto::code)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 입력 패턴에서 사용 가능한 evidence id 집합
        Set<String> validEvidenceIds = patternResult.patterns().stream()
                .flatMap(p -> p.evidenceIds().stream())
                .collect(Collectors.toSet());

        String candidatesText = TypeSelectionInputAssembler.assembleCandidates(candidates);
        String patternsText = TypeSelectionInputAssembler.assemblePatterns(patternResult);

        JsonNode raw = llmClient.selectTypeRawJson(candidatesText, patternsText);

        TypeSelectionResultDto dto = toDto(raw);
        validate(dto, candidateCodes, validEvidenceIds);

        System.out.println("raw = " + raw);

        return dto;
    }

    private TypeSelectionResultDto toDto(JsonNode raw) {
        // {"analysisTypeCode":"...","confidence":82,"because":[{"pattern":"..","evidence_ids":["D1"]}]}
        JsonNode codeNode = raw.get("analysisTypeCode");
        JsonNode confNode = raw.get("confidence");
        JsonNode becauseNode = raw.get("because");

        if (codeNode == null || !codeNode.isTextual() ||
                confNode == null || !confNode.isInt() ||
                becauseNode == null || !becauseNode.isArray()) {
            throw new AiResponseParseException(ErrorCode.TYPE_SELECT_JSON_MISSING_FIELDS);
        }

        String code = codeNode.asText();
        int confidence = confNode.asInt();

        List<TypeSelectionResultDto.BecauseDto> because = new ArrayList<>();
        for (JsonNode b : becauseNode) {
            JsonNode patternNode = b.get("pattern");
            JsonNode evidNode = b.get("evidence_ids");

            if (patternNode == null || !patternNode.isTextual() ||
                    evidNode == null || !evidNode.isArray()) {
                throw new AiResponseParseException(ErrorCode.TYPE_SELECT_JSON_BECAUSE_ITEM_INVALID);
            }

            String pattern = patternNode.asText();

            List<String> evidenceIds = new ArrayList<>();
            for (JsonNode idNode : evidNode) {
                if (!idNode.isTextual()) {
                    throw new AiResponseParseException(ErrorCode.TYPE_SELECT_JSON_EVIDENCE_ID_NOT_TEXT);
                }
                evidenceIds.add(idNode.asText());
            }

            because.add(new TypeSelectionResultDto.BecauseDto(pattern, evidenceIds));
        }

        return new TypeSelectionResultDto(code, confidence, because);
    }

    private void validate(
            TypeSelectionResultDto dto,
            Set<String> candidateCodes,
            Set<String> validEvidenceIds
    ) {
        if (!candidateCodes.contains(dto.analysisTypeCode())) {
            throw new AiResponseParseException(ErrorCode.TYPE_SELECT_CODE_NOT_IN_CANDIDATES);
        }

        if (dto.confidence() < 0 || dto.confidence() > 100) {
            throw new AiResponseParseException(ErrorCode.TYPE_SELECT_CONFIDENCE_INVALID);
        }

        List<TypeSelectionResultDto.BecauseDto> because = dto.because();
        if (because == null) {
            throw new AiResponseParseException(ErrorCode.TYPE_SELECT_JSON_MISSING_FIELDS);
        }
        if (because.size() < BECAUSE_MIN || because.size() > BECAUSE_MAX) {
            throw new AiResponseParseException(ErrorCode.TYPE_SELECT_BECAUSE_COUNT_INVALID);
        }

        Set<String> used = new HashSet<>();
        for (TypeSelectionResultDto.BecauseDto b : because) {
            if (b.pattern() == null || b.pattern().isBlank()) {
                throw new AiResponseParseException(ErrorCode.TYPE_SELECT_JSON_BECAUSE_ITEM_INVALID);
            }
            if (b.pattern().length() > PATTERN_MAX_LEN) {
                throw new AiResponseParseException(ErrorCode.TYPE_SELECT_JSON_BECAUSE_ITEM_INVALID);
            }

            List<String> evid = b.evidenceIds();
            if (evid == null) {
                throw new AiResponseParseException(ErrorCode.TYPE_SELECT_JSON_BECAUSE_ITEM_INVALID);
            }
            if (evid.size() < EVID_MIN || evid.size() > EVID_MAX) {
                throw new AiResponseParseException(ErrorCode.TYPE_SELECT_EVIDENCE_ID_COUNT_INVALID);
            }

            for (String id : evid) {
                if (!validEvidenceIds.contains(id)) {
                    throw new AiResponseParseException(ErrorCode.TYPE_SELECT_EVIDENCE_ID_NOT_IN_INPUT);
                }
                if (!used.add(id)) {
                    throw new AiResponseParseException(ErrorCode.TYPE_SELECT_EVIDENCE_ID_DUPLICATED);
                }
            }
        }
    }
}
