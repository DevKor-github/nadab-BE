package com.devkor.ifive.nadab.domain.dailyreport.api;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CreateDailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.application.DailyReportQueryService;
import com.devkor.ifive.nadab.domain.dailyreport.application.DailyReportService;
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


@Tag(name = "오늘의 리포트 API", description = "오늘의 리포트 생성 및 조회 관련 API")
@RestController
@RequestMapping("${api_prefix}/daily-report")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;
    private final DailyReportQueryService dailyReportQueryService;


    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "오늘의 리포트 생성 API",
            description = """
                    유저의 오늘의 리포트를 생성합니다. <br/>
                    생성 실패 시에도 이 API를 다시 호출하면 됩니다. <br/>
                    이 때 유저의 답변은 기존의 답변으로 자동으로 사용됩니다. <br/>
                    소요 시간이 최대 3~4초밖에 안 되어 동기처리로 구현했습니다. <br/>
                    
                    | 응답의 emotion | 해당 감정 |
                    | :--- | :--- |
                    | `ACHIEVEMENT` | 성취 |
                    | `INTEREST` | 흥미 |
                    | `PEACE` | 평온 |
                    | `PLEASURE` | 즐거움 |
                    | `WILL` | 의지 |
                    | `DEPRESSION` | 우울 |
                    | `REGRET` | 후회 |
                    | `ETC` | 기타 |
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "오늘의 리포트 생성 성공",
                            content = @Content(schema = @Schema(implementation = CreateDailyReportResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: DAILY_QUESTION_MISMATCH - 요청한 질문이 사용자에게 할당된 오늘의 질문과 일치하지 않음
                                    """,
                            content = @Content
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
                                    - ErrorCode: QUESTION_NOT_FOUND - 질문이 존재하지 않음
                                    - ErrorCode: DAILY_QUESTION_NOT_FOUND - 오늘의 질문이 아직 생성되지 않음
                                    - ErrorCode: EMOTION_NOT_FOUND - 감정 정보를 찾을 수 없음
                                    - ErrorCode: WALLET_NOT_FOUND - 사용자의 지갑을 찾을 수 없음
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = """
                                    - ErrorCode: DAILY_REPORT_ALREADY_COMPLETED - 이미 작성된 일간 리포트가 존재함
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description = """
                                    - ErrorCode: AI_RESPONSE_PARSE_FAILED - AI 응답 형식을 해석할 수 없음
                                    - ErrorCode: AI_RESPONSE_FORMAT_INVALID - AI 응답 JSON의 필수 필드가 비어있음
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "503",
                            description = """
                                    - ErrorCode: AI_NO_RESPONSE - AI 서비스로부터 응답을 받지 못함
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<CreateDailyReportResponse>> generateDailyReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DailyReportRequest request
    ) {
        CreateDailyReportResponse response = dailyReportService.generateDailyReport(principal.getId(), request);
        return ApiResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "오늘의 리포트 조회 API",
            description = "유저의 오늘의 리포트를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "오늘의 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = DailyReportResponse.class), mediaType = "application/json")
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
                                    - ErrorCode: ANSWER_NOT_FOUND - 작성된 답변 내역을 찾을 수 없음
                                    - ErrorCode: DAILY_REPORT_NOT_FOUND - 일간 리포트를 찾을 수 없음
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<DailyReportResponse>> getDailyReport(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DailyReportResponse response = dailyReportQueryService.getDailyReport(principal.getId());
        return ApiResponseEntity.ok(response);
    }
}
