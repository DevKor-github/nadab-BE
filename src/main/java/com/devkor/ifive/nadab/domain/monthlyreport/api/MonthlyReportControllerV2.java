package com.devkor.ifive.nadab.domain.monthlyreport.api;

import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.AllReportItemResponseV2;
import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportResponseV2;
import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportStartResponse;
import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.ReportListTypeV2;
import com.devkor.ifive.nadab.domain.monthlyreport.application.MonthlyReportQueryServiceV2;
import com.devkor.ifive.nadab.domain.monthlyreport.application.MonthlyReportService;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.CompletedCountResponse;
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

import java.util.List;

@Tag(name = "월간 리포트 API V2", description = "월간 리포트 생성 및 조회 관련 API V2")
@RestController
@RequestMapping("/api/v2/monthly-report")
@RequiredArgsConstructor
public class MonthlyReportControllerV2 {

    private final MonthlyReportService monthlyReportService;
    private final MonthlyReportQueryServiceV2 monthlyReportQueryServiceV2;

    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "월간 리포트 생성 시작",
            description = """
                    사용자의 (지난 달에 대한) 월간 리포트 생성을 시작합니다. </br>
                    비동기로 처리되기 때문에, id로 월간 리포트 조회 API를 폴링하여 상태를 확인할 수 있습니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "월간 리포트 생성 시작 성공",
                            content = @Content(schema = @Schema(implementation = MonthlyReportStartResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: MONTHLY_REPORT_NOT_ENOUGH_REPORTS - 월간 리포트 작성 자격 미달 **(이 경우 data의 completedCount 필드에 지난 주에 작성된 오늘의 리포트 수가 포함됩니다.)**
                                    - ErrorCode: WALLET_INSUFFICIENT_BALANCE - 크리스탈 잔액 부족
                                    """,
                            content = @Content(schema = @Schema(implementation = CompletedCountResponse.class), mediaType = "application/json")
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
                                    - ErrorCode: WALLET_NOT_FOUND - 지갑을 찾을 수 없음
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = """
                                    - ErrorCode: MONTHLY_REPORT_ALREADY_COMPLETED - 이미 작성된 월간 리포트가 존재함
                                    - ErrorCode: MONTHLY_REPORT_IN_PROGRESS - 현재 월간 리포트를 생성 중임
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<MonthlyReportStartResponse>> startMonthlyReport(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MonthlyReportStartResponse response = monthlyReportService.startMonthlyReport(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "이전 리포트 목록 조회",
            description = """
                    주간/월간 이전 리포트를 통합 조회합니다.
                    
                    type: ALL | MONTHLY | WEEKLY
                    
                    정렬 순서:
                    1) 년-월 내림차순
                    2) 동일 월에서는 월간 먼저, 그 다음 주간(주차 내림차순)
                    
                    version 규칙:
                    - weekly: 값과 상관없이 GET /api/v1/weekly-report/{id} 로 조회
                    - monthly_reports - 1인 경우 : GET /api/v1/monthly-report/{id}로 조회 (기존의 레거시 버전)
                    - monthly_reports - 2인 경우 : GET /api/v2/monthly-report/{id}로 조회 (새로운 V2 버전)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "전체 리포트 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = AllReportItemResponseV2.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "- ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<List<AllReportItemResponseV2>>> getAllReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "ALL") ReportListTypeV2 type
    ) {
        List<AllReportItemResponseV2> response = monthlyReportQueryServiceV2.getAllReports(principal.getId(), type);
        return ApiResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "나의 월간 리포트 조회 V2",
            description = """
                    사용자의 (지난 달에 대한) 월간 리포트 V2와 이전 월간 리포트 V2를 조회합니다. </br>
                    **<```report```의 state>** </br>
                    생성 대기 중인 경우 ```status = "PENDING"``` 으로 반환됩니다. </br>
                    생성 진행 중인 경우 ```status = "IN_PROGRESS"``` 로 반환됩니다. </br>
                    생성에 성공한 경우 ```status = "COMPLETED"``` 로 반환됩니다. </br>
                    생성에 실패한 경우 ```status = "FAILED"``` 로 반환됩니다. 이때 크리스탈이 환불되기 때문에 잔액 조회를 해야합니다.
                    
                    **<텍스트 스타일(styled) 지원>** </br>
                    월간 리포트 본문은 강조 표현을 위해 해당 필드에 구조화된 형태로 함께 제공됩니다. </br>
                    각 필드는 ```segments``` 배열을 가지며, </br>
                    각 segment는 ```text```와 ```marks```를 포함합니다. </br>
                    ```marks```에는 ```BOLD```, ```HIGHLIGHT```만 포함될 수 있습니다. </br>
                    클라이언트는 ```segments```를 순서대로 이어 붙여 렌더링하고, ```marks```에 따라 볼드/하이라이트를 적용하면 됩니다. </br>
                    
                    다음은 각 페이지에서 활용되는 필드의 값에 대한 설명입니다. </br>
                    comparisonType: 최초 생성인지 이전 리포트가 존재하는지 여부입니다. </br>
                    현재는 모두 최초 생성이기 때문에 "BASELINE"으로 고정되어 있고, 이전 리포트가 존재하는 경우에는 "COMPARISON"으로 반환될 예정입니다. </br>
                    
                    **<페이지 1>** </br>
                    summary : 월간 기록 요약 </br>
                    imageUrl : AI 생성 이미지 </br>
                    discovered.segments : 월간 분석 텍스트 </br>
                    
                    **<페이지 2>** </br>
                    dominantKeyword : 이번 달 요약 단어 </br>
                    emotionStats.emotions : 감정에 대한 통계가 빈도 기준 내림차순으로 정렬되어 있습니다. </br>
                    emotionSummaryContent.styledText.segments : 감정 분석 텍스트 </br>
                   
                    emotionTrend : "NOT_SUPPORTED" (현재는 고정. 변동 양상은 최초 생성 월간 리포트에서는 지원되지 않음. 이후 업데이트 예정) </br>
                    
                    **<페이지 3>** </br>
                    commentSummary : 나답의 한 마디 요약 </br>
                    comment.segments : 나답의 한 마디 텍스트 </br>
                    interestStats.interests : 카테고리(관심사)에 대한 통계가 빈도 기준 내림차순으로 정렬되어 있습니다. </br>
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "나의 월간 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = MonthlyReportResponseV2.class), mediaType = "application/json")
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
    public ResponseEntity<ApiResponseDto<MonthlyReportResponseV2>> getMyMonthlyReport(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MonthlyReportResponseV2 response = monthlyReportQueryServiceV2.getMyMonthlyReport(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "id로 월간 리포트 조회",
            description = """
                    월간 리포트 V2를 id로 조회합니다. </br>
                    생성 대기 중인 경우 ```status = "PENDING"``` 으로 반환됩니다. </br>
                    생성 진행 중인 경우 ```status = "IN_PROGRESS"``` 로 반환됩니다. </br>
                    생성에 성공한 경우 ```status = "COMPLETED"``` 로 반환됩니다. </br>
                    생성에 실패한 경우 ```status = "FAILED"``` 로 반환됩니다. 이때 크리스탈이 환불되기 때문에 잔액 조회를 해야합니다.
                    
                    **<텍스트 스타일(styled) 지원>** </br>
                    월간 리포트 본문은 강조 표현을 위해 해당 필드에 구조화된 형태로 함께 제공됩니다. </br>
                    각 필드는 ```segments``` 배열을 가지며, </br>
                    각 segment는 ```text```와 ```marks```를 포함합니다. </br>
                    ```marks```에는 ```BOLD```, ```HIGHLIGHT```만 포함될 수 있습니다. </br>
                    클라이언트는 ```segments```를 순서대로 이어 붙여 렌더링하고, ```marks```에 따라 볼드/하이라이트를 적용하면 됩니다. </br>
                    
                    다음은 각 페이지에서 활용되는 필드의 값에 대한 설명입니다. </br>
                    comparisonType: 최초 생성인지 이전 리포트가 존재하는지 여부입니다. </br>
                    현재는 모두 최초 생성이기 때문에 "BASELINE"으로 고정되어 있고, 이전 리포트가 존재하는 경우에는 "COMPARISON"으로 반환될 예정입니다. </br>
                    
                    **<페이지 1>** </br>
                    summary : 월간 기록 요약 </br>
                    imageUrl : AI 생성 이미지 </br>
                    discovered.segments : 월간 분석 텍스트 </br>
                    
                    **<페이지 2>** </br>
                    dominantKeyword : 이번 달 요약 단어 </br>
                    emotionStats.emotions : 감정에 대한 통계가 빈도 기준 내림차순으로 정렬되어 있습니다. </br>
                    emotionSummaryContent.styledText.segments : 감정 분석 텍스트 </br>
                   
                    emotionTrend : "NOT_SUPPORTED" (현재는 고정. 변동 양상은 최초 생성 월간 리포트에서는 지원되지 않음. 이후 업데이트 예정) </br>
                    
                    **<페이지 3>** </br>
                    commentSummary : 나답의 한 마디 요약 </br>
                    comment.segments : 나답의 한 마디 텍스트 </br>
                    interestStats.interests : 카테고리(관심사)에 대한 통계가 빈도 기준 내림차순으로 정렬되어 있습니다. </br>
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "월간 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = MonthlyReportResponseV2.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "- ErrorCode: MONTHLY_REPORT_NOT_FOUND - 월간 리포트를 찾을 수 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<MonthlyReportResponseV2>> getMonthlyReportById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        MonthlyReportResponseV2 response = monthlyReportQueryServiceV2.getMonthlyReportById(principal.getId(), id);
        return ApiResponseEntity.ok(response);
    }
}
