package com.devkor.ifive.nadab.domain.typereport.core.service;

import com.devkor.ifive.nadab.domain.typereport.application.helper.EvidenceEntriesAssembler;
import com.devkor.ifive.nadab.domain.typereport.core.dto.DailyEntryWithIdDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.EvidenceCardDto;
import com.devkor.ifive.nadab.domain.typereport.infra.TypeEvidenceCardLlmClient;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvidenceCardGenerationService {

    private static final int MIN_LEN = 50;
    private static final int MAX_LEN = 400;

    private final TypeEvidenceCardLlmClient llmClient;

    public List<EvidenceCardDto> generate(List<DailyEntryDto> recentEntries) {
        List<DailyEntryWithIdDto> withIds = EvidenceEntriesAssembler.attachIds(recentEntries);
        if (withIds.isEmpty()) return List.of();

        String entriesText = EvidenceEntriesAssembler.assembleForPrompt(withIds);

        List<Map<String, String>> raw = llmClient.generateRawCardsJsonArray(entriesText);

        List<EvidenceCardDto> cards = mapAndValidate(withIds, raw);

        return cards;
    }

    private List<EvidenceCardDto> mapAndValidate(List<DailyEntryWithIdDto> withIds, List<Map<String, String>> raw) {
        Map<String, DailyEntryWithIdDto> idToEntry = withIds.stream()
                .collect(Collectors.toMap(DailyEntryWithIdDto::id, e -> e));

        if (raw == null || raw.isEmpty()) {
            throw new AiResponseParseException(ErrorCode.AI_NO_RESPONSE);
        }

        // id 중복/누락 체크
        Set<String> seen = new HashSet<>();
        List<EvidenceCardDto> result = new ArrayList<>(withIds.size());

        for (Map<String, String> obj : raw) {
            String id = obj.get("id");
            String card = obj.get("card");

            // 필수 필드 체크
            if (id == null || card == null) {
                throw new AiResponseParseException(ErrorCode.EVIDENCE_CARD_JSON_MISSING_ID_OR_CARD);
            }

            // 입력 id 내에 있는지 체크
            if (!idToEntry.containsKey(id)) {
                throw new AiResponseParseException(ErrorCode.EVIDENCE_CARD_ID_NOT_IN_INPUT);
            }

            // 중복 체크
            if (!seen.add(id)) {
                throw new AiResponseParseException(ErrorCode.EVIDENCE_CARD_DUPLICATE_ID);
            }

            // 길이 체크
            int len = card.length(); // 한글 기준 대체로 char length == 글자수(공백 포함)
            if (len < MIN_LEN || len > MAX_LEN) {
                throw new AiResponseParseException(ErrorCode.EVIDENCE_CARD_LENGTH_INVALID);
            }

            DailyEntryWithIdDto entry = idToEntry.get(id);
            result.add(new EvidenceCardDto(id, entry.date(), card));
        }

        // 입력 id 전부 나왔는지 체크
        if (seen.size() != withIds.size()) {
            throw new AiResponseParseException(ErrorCode.EVIDENCE_CARD_ID_MISSING);
        }

        // 순서를 유지하기 위한 정렬
        result.sort(Comparator.comparingInt(e -> parseIndex(e.id())));
        return result;
    }

    private int parseIndex(String id) {
        // "D12" -> 12
        try {
            return Integer.parseInt(id.substring(1));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }
}
