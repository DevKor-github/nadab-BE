package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.GetMonthlyCalendarRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.SearchAnswerEntryRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.AnswerEntrySummaryResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CalendarEntryResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CalendarRecentsResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.MonthlyCalendarResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.SearchAnswerEntryResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.MonthlyCalendarDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.SearchAnswerEntryDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryQueryRepository;
import com.devkor.ifive.nadab.domain.dailyreport.application.helper.CursorParser;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.shared.util.MonthRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        // Pageable 생성 (PAGE_SIZE + 1개 조회)
        Pageable pageable = PageRequest.of(0, PAGE_SIZE + 1);

        // 검색 실행 - 조건별 분기
        List<SearchAnswerEntryDto> results;
        if (cursorDate == null) {
            // 첫 페이지 조회
            results = answerEntryQueryRepository.searchAnswerEntriesFirstPage(
                    userId,
                    keyword,
                    emotionCode,
                    pageable
            );
        } else {
            // 다음 페이지 조회 (cursor 있음)
            results = answerEntryQueryRepository.searchAnswerEntriesWithCursor(
                    userId,
                    keyword,
                    emotionCode,
                    cursorDate,
                    pageable
            );
        }

        // hasNext 판단
        boolean hasNext = results.size() > PAGE_SIZE;
        List<SearchAnswerEntryDto> items = hasNext
                ? results.subList(0, PAGE_SIZE)
                : results;

        // DTO 변환
        List<AnswerEntrySummaryResponse> responseItems = items.stream()
                .map(dto -> AnswerEntrySummaryResponse.from(dto, request.keyword()))
                .toList();

        // nextCursor 생성
        String nextCursor = null;
        if (hasNext && !items.isEmpty()) {
            SearchAnswerEntryDto last = items.get(items.size() - 1);
            nextCursor = CursorParser.encode(last.answerDate());
        }

        return new SearchAnswerEntryResponse(responseItems, nextCursor, hasNext);
    }

    public MonthlyCalendarResponse getMonthlyCalendar(Long userId, GetMonthlyCalendarRequest request) {
        // 월의 시작/종료 날짜 계산
        LocalDate anyDayInMonth = LocalDate.of(request.year(), request.month(), 1);
        MonthRangeDto range = MonthRangeCalculator.monthRangeOf(anyDayInMonth);

        // 월별 데이터 조회
        List<MonthlyCalendarDto> results = answerEntryQueryRepository.findCalendarEntriesInMonth(
                userId,
                range.monthStartDate(),
                range.monthEndDate()
        );

        // DTO 변환
        List<CalendarEntryResponse> calendarEntries = results.stream()
                .map(CalendarEntryResponse::from)
                .toList();

        return new MonthlyCalendarResponse(calendarEntries);
    }

    public CalendarRecentsResponse getRecentAnswers(Long userId) {
        // 최근 6개만 조회 (페이지 번호 0, 크기 6)
        Pageable pageable = PageRequest.of(0, 6);

        List<SearchAnswerEntryDto> results = answerEntryQueryRepository.findRecentAnswers(
                userId,
                pageable
        );

        // DTO 변환
        List<AnswerEntrySummaryResponse> items = results.stream()
                .map(AnswerEntrySummaryResponse::from)
                .toList();

        return CalendarRecentsResponse.from(items);
    }

    public AnswerEntrySummaryResponse getAnswerByDate(Long userId, LocalDate date) {
        SearchAnswerEntryDto dto = answerEntryQueryRepository.findByUserAndDate(userId, date)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ANSWER_NOT_FOUND));

        return AnswerEntrySummaryResponse.from(dto);
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