package com.devkor.ifive.nadab.domain.weeklyreport.api;

import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.WeeklyReportResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.WeeklyReportStartResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.application.WeeklyReportQueryService;
import com.devkor.ifive.nadab.domain.weeklyreport.application.WeeklyReportService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "주간 리포트 API", description = "주간 리포트 생성 및 조회 관련 API")
@RestController
@RequestMapping("${api_prefix}/weekly-report")
@RequiredArgsConstructor
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;
    private final WeeklyReportQueryService weeklyReportQueryService;

    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "주간 리포트 생성 시작",
            description = """
                    사용자의 (지난 주에 대한) 주간 리포트 생성을 시작합니다.
                    비동기로 처리되기 때문에, id로 주간 리포트 조회 API를 폴링하여 상태를 확인할 수 있습니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "주간 리포트 생성 시작 성공",
                            content = @Content(schema = @Schema(implementation = WeeklyReportStartResponse.class), mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<WeeklyReportStartResponse>> startWeeklyReport(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        WeeklyReportStartResponse response = weeklyReportService.startWeeklyReport(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "나의 주간 리포트 조회",
            description = "사용자의 (지난 주) 주간 리포트를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "나의 주간 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = WeeklyReportResponse.class), mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<WeeklyReportResponse>> getLastWeekWeeklyReport(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        WeeklyReportResponse response = weeklyReportQueryService.getLastWeekWeeklyReport(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PermitAll
    @Operation(
            summary = "id로 주간 리포트 조회",
            description = "주간 리포트를 id로 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "주간 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = WeeklyReportResponse.class), mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<WeeklyReportResponse>> getWeeklyReportById(
            @PathVariable Long id
    ) {
        WeeklyReportResponse response = weeklyReportQueryService.getWeeklyReportById(id);
        return ApiResponseEntity.ok(response);
    }
}
