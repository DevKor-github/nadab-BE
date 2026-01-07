package com.devkor.ifive.nadab.domain.test.api;

import com.devkor.ifive.nadab.domain.test.api.dto.request.TestDailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CreateDailyReportResponse;
import com.devkor.ifive.nadab.domain.test.api.dto.response.TestDailyReportResponse;
import com.devkor.ifive.nadab.domain.test.application.TestDailyReportService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "테스트 API", description = "테스트와 관련된 API들")
@RestController
@RequestMapping("${api_prefix}/test")
@RequiredArgsConstructor
public class TestController {

    private final TestDailyReportService testDailyReportService;

    @PostMapping("/generate/daily-report")
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
                    @ApiResponse(
                            responseCode = "502",
                            description = "- ErrorCode: AI_RESPONSE_PARSE_FAILED - AI 응답 형식을 해석할 수 없음",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "503",
                            description = "- ErrorCode: AI_NO_RESPONSE - AI 서비스로부터 응답을 받지 못함",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<TestDailyReportResponse>> generateDailyReport(
            @Valid @RequestBody TestDailyReportRequest request
    ) {
        TestDailyReportResponse response = testDailyReportService.generateTestDailyReport(request);
        return ApiResponseEntity.ok(response);
    }

}
