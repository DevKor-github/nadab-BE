package com.devkor.ifive.nadab.domain.weeklyreport.api;

import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.CompletedCountResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.MyWeeklyReportResponse;
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
                    사용자의 (지난 주에 대한) 주간 리포트 생성을 시작합니다. </br>
                    비동기로 처리되기 때문에, id로 주간 리포트 조회 API를 폴링하여 상태를 확인할 수 있습니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "주간 리포트 생성 시작 성공",
                            content = @Content(schema = @Schema(implementation = WeeklyReportStartResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: WEEKLY_REPORT_NOT_ENOUGH_REPORTS - 주간 리포트 작성 자격 미달 **(이 경우 data의 completedCount 필드에 지난 주에 작성된 오늘의 리포트 수가 포함됩니다.)**
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
                                    - ErrorCode: WEEKLY_REPORT_ALREADY_COMPLETED - 이미 작성된 주간 리포트가 존재함
                                    - ErrorCode: WEEKLY_REPORT_IN_PROGRESS - 현재 주간 리포트를 생성 중임
                                    """,
                            content = @Content
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
            description = """
                    사용자의 (지난 주에 대한) 주간 리포트와 이전 주간 리포트를 조회합니다. </br>
                    이때 ```report```혹은 ```previousReport```가 ```null```인 경우 해당 주간 리포트가 존재하지 않음을 의미합니다. </br>
                    ```previousReport```가 ```null```이 아닌 경우 ```status```필드는 항상 ```COMPLETED```입니다. </br>
                    **<```report```의 state>** </br>
                    생성 대기 중인 경우 ```status = "PENDING"``` 으로 반환됩니다. </br>
                    생성 진행 중인 경우 ```status = "IN_PROGRESS"``` 로 반환됩니다. </br>
                    생성에 성공한 경우 ```status = "COMPLETED"``` 로 반환됩니다. </br>
                    생성에 실패한 경우 ```status = "FAILED"``` 로 반환됩니다. 이때 크리스탈이 환불되기 때문에 잔액 조회를 해야합니다.
                    
                    **<텍스트 스타일(styled) 지원>** </br>
                    주간 리포트 본문은 강조 표현을 위해 ```content``` 필드에 구조화된 형태로 함께 제공됩니다. </br>
                    ```content```는 ```discovered```/```improve``` 각각에 대해 ```segments``` 배열을 가지며, </br>
                    각 segment는 ```text```와 ```marks```를 포함합니다. </br>
                    ```marks```에는 ```BOLD```, ```HIGHLIGHT```만 포함될 수 있습니다. </br>
                    클라이언트는 ```segments```를 순서대로 이어 붙여 렌더링하고, ```marks```에 따라 볼드/하이라이트를 적용하면 됩니다. </br>
                    기존 plain 텍스트(```discovered```, ```improve```)는 ```content```로부터 파생된 캐시 값이며, </br>
                    styled 렌더링이 가능하다면 ```content``` 사용을 권장합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "나의 주간 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = MyWeeklyReportResponse.class), mediaType = "application/json")
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
    public ResponseEntity<ApiResponseDto<MyWeeklyReportResponse>> getMyWeeklyReport(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MyWeeklyReportResponse response = weeklyReportQueryService.getMyWeeklyReport(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "id로 주간 리포트 조회",
            description = """
                    주간 리포트를 id로 조회합니다. </br>
                    생성 대기 중인 경우 ```status = "PENDING"``` 으로 반환됩니다. </br>
                    생성 진행 중인 경우 ```status = "IN_PROGRESS"``` 로 반환됩니다. </br>
                    생성에 성공한 경우 ```status = "COMPLETED"``` 로 반환됩니다. </br>
                    생성에 실패한 경우 ```status = "FAILED"``` 로 반환됩니다. 이때 크리스탈이 환불되기 때문에 잔액 조회를 해야합니다.
                    
                    **<텍스트 스타일(styled) 지원>** </br>
                    주간 리포트 본문은 강조 표현을 위해 ```content``` 필드에 구조화된 형태로 함께 제공됩니다. </br>
                    ```content```는 ```discovered```/```improve``` 각각에 대해 ```segments``` 배열을 가지며, </br>
                    각 segment는 ```text```와 ```marks```를 포함합니다. </br>
                    ```marks```에는 ```BOLD```, ```HIGHLIGHT```만 포함될 수 있습니다. </br>
                    클라이언트는 ```segments```를 순서대로 이어 붙여 렌더링하고, ```marks```에 따라 볼드/하이라이트를 적용하면 됩니다. </br>
                    기존 plain 텍스트(```discovered```, ```improve```)는 ```content```로부터 파생된 캐시 값이며, </br>
                    styled 렌더링이 가능하다면 ```content``` 사용을 권장합니다.
                    
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "주간 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = WeeklyReportResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "- ErrorCode: WEEKLY_REPORT_NOT_FOUND - 주간 리포트를 찾을 수 없음",
                            content = @Content
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
