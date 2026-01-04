package com.devkor.ifive.nadab.domain.dailyreport.api;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.application.DailyReportQueryService;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "답변 API", description = "답변 검색 및 조회 관련 API")
@RestController
@RequestMapping("${api_prefix}/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final DailyReportQueryService dailyReportQueryService;

    @GetMapping("/{answerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "답변 상세 조회",
            description = """
                    답변 ID로 해당 답변의 상세 정보와 리포트를 조회합니다.

                    - 답변 내용 (answer)
                    - 리포트 내용 (content)
                    - 감정 상태 (emotion)

                    COMPLETED 상태의 리포트만 조회 가능합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = DailyReportResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = """
                                    - ErrorCode: ANSWER_ACCESS_FORBIDDEN - 본인의 답변이 아님
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = """
                                    - ErrorCode: ANSWER_NOT_FOUND - 답변을 찾을 수 없음
                                    - ErrorCode: DAILY_REPORT_NOT_FOUND - 리포트가 생성되지 않았음
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<DailyReportResponse>> getDailyReportByAnswerId(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long answerId
    ) {
        DailyReportResponse response = dailyReportQueryService.getDailyReportByAnswerId(principal.getId(), answerId);
        return ApiResponseEntity.ok(response);
    }
}