package com.devkor.ifive.nadab.domain.report.api;

import com.devkor.ifive.nadab.domain.report.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.report.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.domain.report.application.ReportService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리포트 API", description = "리포트 생성 및 조회 관련 API")
@RestController
@RequestMapping("${api_prefix}/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/daily/generate")
    @PermitAll
    @Operation(
            summary = "일간 리포트 생성 API",
            description = "사용자의 일간 리포트를 생성합니다."
    )
    public ResponseEntity<ApiResponseDto<DailyReportResponse>> generateDailyReport(
            @Valid @RequestBody DailyReportRequest request
    ) {
        DailyReportResponse response = reportService.generateDailyReport(request);
        return ApiResponseEntity.ok(response);
    }
}
