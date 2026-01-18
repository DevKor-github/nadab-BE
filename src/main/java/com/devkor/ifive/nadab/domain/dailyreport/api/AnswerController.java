package com.devkor.ifive.nadab.domain.dailyreport.api;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.GetMonthlyCalendarRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.SearchAnswerEntryRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.AnswerDetailResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CalendarRecentsResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.MonthlyCalendarResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.SearchAnswerEntryResponse;
import com.devkor.ifive.nadab.domain.dailyreport.application.AnswerQueryService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "답변 API", description = "답변 검색, 조회 및 캘린더 관련 API")
@RestController
@RequestMapping("${api_prefix}/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerQueryService answerQueryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "답변 검색 및 전체 조회",
            description = """
                    키워드 또는 감정 코드로 답변을 검색하거나, 전체 답변 목록을 조회합니다. (Cursor-based Pagination)

                    ### 검색 모드
                    - 전체 조회: 모든 파라미터 생략 시 전체 답변 목록 반환
                    - 키워드 검색: 질문 또는 답변 내용에서 검색
                    - 감정 필터링: 특정 감정 코드로 필터링
                    - 복합 검색: 키워드 + 감정 동시 검색
                    - 정렬: 최신순 (date DESC)
                    - 페이지 크기: 20개 고정

                    ### Request Parameters (모두 선택)
                    - keyword (선택): 검색어 (1~100자, 생략 시 키워드 검색 안 함)
                    - emotionCode (선택): 감정 코드 (대문자, 생략 시 감정 필터링 안 함)
                    - cursor (선택): 이전 응답의 nextCursor 값을 그대로 사용 (첫 페이지 요청 시에는 생략)

                    ### Response
                    - items: 검색 결과 리스트 (최대 20개)
                    - nextCursor: 다음 페이지 요청 시 사용할 커서 (마지막 페이지면 null)
                    - hasNext: 다음 페이지 존재 여부

                    ### 무한 스크롤 구현 (쿼리 파라미터 형식)
                    1. 첫 페이지: GET /api/v1/answers?keyword=안녕
                    2. 다음 페이지: GET /api/v1/answers?keyword=안녕&cursor=2025-12-06
                       (response의 nextCursor 값을 cursor 파라미터로 전달)
                    3. hasNext=false가 될 때까지 반복

                    예시) emotionCode와 함께 사용:
                    - GET /api/v1/answers?keyword=행복&emotionCode=JOY
                    - GET /api/v1/answers?keyword=행복&emotionCode=JOY&cursor=2025-12-06

                    ### 참고사항
                    - keyword와 emotionCode는 동시에 사용 가능합니다.
                    - emotionCode만 사용 시 keyword는 생략 가능합니다.
                    - 검색어 저장은 POST /api/v1/search/histories 엔드포인트를 통해 별도로 수행할 수 있습니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "검색 성공",
                            content = @Content(schema = @Schema(implementation = SearchAnswerEntryResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "ErrorCode: VALIDATION_FAILED - 잘못된 요청 (cursor 형식 오류 등)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<SearchAnswerEntryResponse>> searchAnswers(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute SearchAnswerEntryRequest request
    ) {
        SearchAnswerEntryResponse response = answerQueryService.searchAnswers(principal.getId(), request);
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/{answerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "답변 상세 조회",
            description = """
                    답변 ID로 해당 답변의 상세 정보와 리포트를 조회합니다.

                    - 질문 내용 (questionText)
                    - 질문 카테고리 (interestCode)
                    - 답변 작성일 (answerDate)
                    - 답변 내용 (answer)
                    - 리포트 내용 (content)
                    - 감정 상태 (emotion)

                    COMPLETED 상태의 리포트만 조회 가능합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = AnswerDetailResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = """
                                    - ErrorCode: ANSWER_NOT_FOUND - 답변을 찾을 수 없음 (또는 본인의 답변이 아님)
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<AnswerDetailResponse>> getAnswerDetailById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long answerId
    ) {
        AnswerDetailResponse response = answerQueryService.getAnswerDetailById(principal.getId(), answerId);
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/calendar")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "월별 캘린더 조회",
            description = """
                    특정 월의 답변이 있는 날짜와 감정 코드를 조회합니다.

                    - 답변이 없는 날짜는 결과에 포함되지 않습니다.
                    - emotionCode는 리포트가 COMPLETED 상태일 때만 제공됩니다.
                    - 리포트가 없거나 PENDING/FAILED 상태면 null입니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = MonthlyCalendarResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "ErrorCode: VALIDATION_FAILED - 잘못된 요청 (연도/월 범위 오류)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<MonthlyCalendarResponse>> getMonthlyCalendar(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute GetMonthlyCalendarRequest request
    ) {
        MonthlyCalendarResponse response = answerQueryService.getMonthlyCalendar(
                principal.getId(),
                request
        );
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/calendar/recents")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "캘린더 최근 기록 미리보기",
            description = """
                    최근 답변을 최대 6개까지 조회합니다. (날짜 내림차순)

                    - 답변이 6개 미만인 경우 전체 답변을 반환합니다.
                    - 답변이 없는 경우 빈 배열을 반환합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CalendarRecentsResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<CalendarRecentsResponse>> getCalendarRecents(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CalendarRecentsResponse response = answerQueryService.getRecentAnswers(principal.getId());
        return ApiResponseEntity.ok(response);
    }


    @GetMapping("/calendar/{date}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "특정 날짜 답변 상세 조회",
            description = """
                    특정 날짜의 답변 전체 정보를 조회합니다.

                    응답 데이터:
                    - 질문 내용 (questionText)
                    - 질문 카테고리 (interestCode)
                    - 답변 작성일 (answerDate)
                    - 답변 내용 (answer)
                    - 리포트 내용 (content)
                    - 감정 상태 (emotion)

                    - 해당 날짜에 답변이 없으면 404 에러를 반환합니다.
                    - COMPLETED 상태의 리포트만 조회 가능합니다.
                    - 날짜 형식: yyyy-MM-dd (예: 2026-01-30)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = AnswerDetailResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "ErrorCode: VALIDATION_FAILED - 잘못된 날짜 형식", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ErrorCode: ANSWER_NOT_FOUND - 해당 날짜에 답변이 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<AnswerDetailResponse>> getAnswerDetailByDate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable LocalDate date
    ) {
        AnswerDetailResponse response = answerQueryService.getAnswerDetailByDate(
                principal.getId(),
                date
        );
        return ApiResponseEntity.ok(response);
    }
}