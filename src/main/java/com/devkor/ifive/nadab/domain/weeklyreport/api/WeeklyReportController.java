package com.devkor.ifive.nadab.domain.weeklyreport.api;

import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.WeeklyReportStartResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.application.WeeklyReportService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "주간 리포트 API", description = "주간 리포트 생성 및 조회 관련 API")
@RestController
@RequestMapping("${api_prefix}/weekly-report")
@RequiredArgsConstructor
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "주간 리포트 생성 시작",
            description = "사용자의 주간 리포트 생성을 시작합니다. 이미 작성된 리포트가 있을 경우 충돌 예외를 발생시킵니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseDto<WeeklyReportStartResponse>> startWeeklyReport(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        WeeklyReportStartResponse response = weeklyReportService.startWeeklyReport(principal.getId());
        return ApiResponseEntity.ok(response);
    }
}
