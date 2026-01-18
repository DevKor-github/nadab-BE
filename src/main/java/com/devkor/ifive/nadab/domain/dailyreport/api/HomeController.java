package com.devkor.ifive.nadab.domain.dailyreport.api;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.HomeResponse;
import com.devkor.ifive.nadab.domain.dailyreport.application.HomeQueryService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "홈화면 API", description = "홈화면 관련 API")
@RestController
@RequestMapping("${api_prefix}/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeQueryService homeQueryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "홈화면 정보 조회 API",
            description = """
                    홈화면에 표시할 답변 관련 정보를 조회합니다.

                    ### 제공 정보
                    1. 주간 답변 상태: 이번 주(월~일) 답변한 날짜 목록
                    2. 연속 기록(Streak): 현재 연속 답변 일수
                    3. 총 기록 일수: 실제 답변한 날짜의 총 개수

                    ### 계산 기준
                    - 주 시작: 월요일, 주 종료: 일요일
                    - Streak: 현재까지 매일 연속 답변한 총 일수
                      * 오늘 답변 있음 → 오늘까지 포함한 연속 일수
                      * 오늘 답변 없음 → 어제까지의 연속 일수
                      * 어제도 답변 없음 → 0
                    - 총 기록 일수: 실제 답변을 작성한 날짜의 개수

                    ### 예시
                    - 첫 답변: 2025-12-27
                    - 12월 27일~31일 매일 답변 (5일)
                    - 1월 1일~3일 답변 안 함
                    - 1월 4일~15일 매일 답변 (12일)
                    - 오늘: 2026-01-15

                    응답:
                    - answeredDates: ["2026-01-12", "2026-01-13", "2026-01-14", "2026-01-15"]
                    - streakCount: 12 (1월 4일부터 15일까지 연속)
                    - totalRecordDays: 17 (5+ 12 = 총 17일 답변)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "홈화면 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = HomeResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = """
                                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<HomeResponse>> getHomeData(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        HomeResponse response = homeQueryService.getHomeData(principal.getId());
        return ApiResponseEntity.ok(response);
    }
}