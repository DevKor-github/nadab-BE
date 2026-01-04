package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.SearchAnswerEntryRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.AnswerEntrySummaryResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.SearchAnswerEntryResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.SearchAnswerEntryDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryQueryRepository;
import com.devkor.ifive.nadab.domain.dailyreport.application.helper.CursorParser;
import com.devkor.ifive.nadab.domain.dailyreport.application.helper.MatchedSnippetExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerQueryService {

    private final AnswerEntryQueryRepository answerEntryQueryRepository;

    private static final int PAGE_SIZE = 20;

    public SearchAnswerEntryResponse searchAnswers(Long userId, SearchAnswerEntryRequest request) {
        // 파라미터 추출 및 검증
        String keyword = prepareKeywordForLike(request.keyword());
        EmotionCode emotionCode = parseEmotionCode(request.emotionCode());
        LocalDate cursorDate = CursorParser.parse(request.cursor());

        // 검색 실행 (PAGE_SIZE + 1개 조회)
        List<SearchAnswerEntryDto> results = answerEntryQueryRepository.searchAnswerEntries(
                userId,
                keyword,
                emotionCode,
                cursorDate,
                PAGE_SIZE + 1
        );

        // hasNext 판단
        boolean hasNext = results.size() > PAGE_SIZE;
        List<SearchAnswerEntryDto> items = hasNext
                ? results.subList(0, PAGE_SIZE)
                : results;

        // DTO 변환
        List<AnswerEntrySummaryResponse> responseItems = items.stream()
                .map(dto -> toSummaryResponse(dto, request.keyword()))
                .toList();

        // nextCursor 생성
        String nextCursor = null;
        if (hasNext && !items.isEmpty()) {
            SearchAnswerEntryDto last = items.get(items.size() - 1);
            nextCursor = CursorParser.encode(last.answerDate());
        }

        return new SearchAnswerEntryResponse(responseItems, nextCursor, hasNext);
    }

    /**
     * Dto → SummaryResponse 변환
     */
    private AnswerEntrySummaryResponse toSummaryResponse(SearchAnswerEntryDto dto, String keyword) {
        String snippet = MatchedSnippetExtractor.extract(dto.answerContent(), keyword);

        return new AnswerEntrySummaryResponse(
                dto.answerId(),
                dto.interestCode() != null ? dto.interestCode().name() : null,
                dto.emotionCode() != null ? dto.emotionCode().name() : null,
                dto.questionText(),
                snippet,
                dto.answerDate()
        );
    }

    /**
     * LIKE 검색용 키워드 준비 (이스케이핑 + % 추가)
     */
    private String prepareKeywordForLike(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        // 와일드카드 이스케이핑
        String escaped = keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        // 양쪽에 % 추가
        return "%" + escaped + "%";
    }

    /**
     * EmotionCode 파싱
     */
    private EmotionCode parseEmotionCode(String emotionCode) {
        if (!StringUtils.hasText(emotionCode)) {
            return null;
        }

        try {
            return EmotionCode.valueOf(emotionCode);
        } catch (IllegalArgumentException e) {
            return null; // 잘못된 코드면 null (전체 검색)
        }
    }

}