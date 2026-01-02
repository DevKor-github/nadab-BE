package com.devkor.ifive.nadab.domain.dailyreport.api;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.TestDailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CreateDailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.TestDailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.application.DailyReportQueryService;
import com.devkor.ifive.nadab.domain.dailyreport.application.DailyReportService;
import com.devkor.ifive.nadab.domain.dailyreport.core.service.TestDailyReportService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
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

    private final TestDailyReportService testDailyReportService;
    private final DailyReportService dailyReportService;
    private final DailyReportQueryService dailyReportQueryService;

    @PostMapping("/generate/test")
    @PermitAll
    @Operation(
            summary = "(테스트용) 오늘의 리포트 생성 API",
            description = """
                    오늘의 리포트 생성 테스트입니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "테스트용 오늘의 리포트 생성 성공",
                            content = @Content(schema = @Schema(implementation = CreateDailyReportResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    ),
            }
    )
    public ResponseEntity<ApiResponseDto<TestDailyReportResponse>> generateDailyReport(
            @Valid @RequestBody TestDailyReportRequest request
    ) {
        TestDailyReportResponse response = testDailyReportService.generateTestDailyReport(request);
        return ApiResponseEntity.ok(response);
    }

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
                    | `JOY` | 기쁨 |
                    | `PLEASURE` | 즐거움 |
                    | `SADNESS` | 슬픔 |
                    | `ANGER` | 분노 |
                    | `REGRET` | 후회 |
                    | `FRUSTRATION` | 좌절 |
                    | `GROWTH` | 성장 |
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
                            responseCode = "401",
                            description = "인증 실패"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description = "AI 응답 JSON 파싱 실패"
                    ),
                    @ApiResponse(
                            responseCode = "503",
                            description = "외부 AI 서비스 연동 실패"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "오늘의 리포트가 이미 생성된 경우"
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
                            description = "오늘의 리포트가 존재하지 않는 경우",
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
