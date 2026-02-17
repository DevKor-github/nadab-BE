package com.devkor.ifive.nadab.domain.typereport.core.service;

import com.devkor.ifive.nadab.domain.typereport.application.helper.EvidenceCardsAssembler;
import com.devkor.ifive.nadab.domain.typereport.core.dto.EvidenceCardDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.PatternExtractionResultDto;
import com.devkor.ifive.nadab.domain.typereport.infra.TypePatternExtractLlmClient;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatternExtractionService {

    private static final int PATTERN_MIN = 3;
    private static final int PATTERN_MAX = 6;
    private static final int EVID_MIN = 2;
    private static final int EVID_MAX = 4;
    private static final int LABEL_MAX_LEN = 25;
    private static final int NOTE_MAX_LEN = 60;

    private final TypePatternExtractLlmClient llmClient;

    public PatternExtractionResultDto extract(List<EvidenceCardDto> cards) {
        if (cards == null || cards.isEmpty()) {
            throw new AiResponseParseException(ErrorCode.PATTERN_RESULT_EMPTY);
        }

        Set<String> validIds = cards.stream()
                .map(EvidenceCardDto::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        String cardsText = EvidenceCardsAssembler.assembleForPrompt(cards);
        JsonNode raw = llmClient.extractPatternsRawJson(cardsText);

        PatternExtractionResultDto dto = toDto(raw);
        validate(dto, validIds);
        return dto;
    }

    private PatternExtractionResultDto toDto(JsonNode raw) {
        try {
            // snake_case -> camelCase 변환을 위해 직접 매핑
            // raw 예: {"patterns":[{"label":"...","evidence_ids":["D1"],"note":"..."}]}
            JsonNode patternsNode = raw.get("patterns");
            if (patternsNode == null || !patternsNode.isArray()) {
                throw new AiResponseParseException(ErrorCode.PATTERN_JSON_MISSING_PATTERNS);
            }

            List<PatternExtractionResultDto.PatternDto> patterns = new ArrayList<>();
            for (JsonNode p : patternsNode) {
                String label = text(p, "label");
                String note = text(p, "note");

                JsonNode evidNode = p.get("evidence_ids");
                if (evidNode == null || !evidNode.isArray()) {
                    throw new AiResponseParseException(ErrorCode.PATTERN_JSON_MISSING_EVIDENCE_IDS);
                }
                List<String> evidenceIds = new ArrayList<>();
                for (JsonNode idNode : evidNode) {
                    if (!idNode.isTextual()) {
                        throw new AiResponseParseException(ErrorCode.PATTERN_JSON_EVIDENCE_ID_NOT_TEXT);
                    }
                    evidenceIds.add(idNode.asText());
                }

                patterns.add(new PatternExtractionResultDto.PatternDto(label, evidenceIds, note));
            }

            return new PatternExtractionResultDto(patterns);
        } catch (AiResponseParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.PATTERN_JSON_MAPPING_FAILED);
        }
    }

    private void validate(PatternExtractionResultDto dto, Set<String> validIds) {
        List<PatternExtractionResultDto.PatternDto> patterns = dto.patterns();
        if (patterns == null) throw new AiResponseParseException(ErrorCode.PATTERN_JSON_MISSING_PATTERNS);

        int size = patterns.size();
        if (size < PATTERN_MIN || size > PATTERN_MAX) {
            throw new AiResponseParseException(ErrorCode.PATTERN_COUNT_INVALID);
        }

        Set<String> usedEvidence = new HashSet<>();

        for (PatternExtractionResultDto.PatternDto p : patterns) {
            if (p.label() == null || p.label().isBlank()) {
                throw new AiResponseParseException(ErrorCode.PATTERN_LABEL_BLANK);
            }
            if (p.label().length() > LABEL_MAX_LEN) {
                throw new AiResponseParseException(ErrorCode.PATTERN_LABEL_TOO_LONG);
            }
            if (p.note() == null || p.note().isBlank()) {
                throw new AiResponseParseException(ErrorCode.PATTERN_NOTE_BLANK);
            }
            if (p.note().length() > NOTE_MAX_LEN) {
                throw new AiResponseParseException(ErrorCode.PATTERN_NOTE_TOO_LONG);
            }

            List<String> evid = p.evidenceIds();
            if (evid == null) throw new AiResponseParseException(ErrorCode.PATTERN_JSON_MISSING_EVIDENCE_IDS);
            if (evid.size() < EVID_MIN || evid.size() > EVID_MAX) {
                throw new AiResponseParseException(ErrorCode.PATTERN_EVIDENCE_ID_COUNT_INVALID);
            }

            for (String id : evid) {
                if (!validIds.contains(id)) {
                    throw new AiResponseParseException(ErrorCode.PATTERN_EVIDENCE_ID_NOT_IN_INPUT);
                }
                if (!usedEvidence.add(id)) {
                    throw new AiResponseParseException(ErrorCode.PATTERN_EVIDENCE_ID_DUPLICATED_ACROSS_PATTERNS);
                }
            }
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || !v.isTextual()) {
            throw new AiResponseParseException(ErrorCode.PATTERN_JSON_MISSING_LABEL_OR_NOTE);
        }
        return v.asText();
    }
}
