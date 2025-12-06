package com.devkor.ifive.nadab.domain.report.api;

import com.devkor.ifive.nadab.domain.report.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.report.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.domain.report.application.ReportService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @PostMapping("/daily/generate/test")
    @PermitAll
    @Operation(
            summary = "(테스트용) 오늘의 리포트 생성 API",
            description = "유저의 오늘의 리포트를 생성합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "테스트용 오늘의 리포트 생성 성공",
                            content = @Content(schema = @Schema(implementation = DailyReportResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    ),
            }
    )
    public ResponseEntity<ApiResponseDto<DailyReportResponse>> generateDailyReport(
            @Valid @RequestBody DailyReportRequest request
    ) {
        DailyReportResponse response = reportService.generateDailyReport(request);
        return ApiResponseEntity.ok(response);
    }
}
