package com.devkor.ifive.nadab.domain.typereport.api;

import com.devkor.ifive.nadab.domain.typereport.api.dto.response.MyTypeReportResponse;
import com.devkor.ifive.nadab.domain.typereport.api.dto.response.TypeReportStartResponse;
import com.devkor.ifive.nadab.domain.typereport.application.TypeReportQueryService;
import com.devkor.ifive.nadab.domain.typereport.application.TypeReportService;
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

@Tag(name = "유형 리포트 API", description = "유형 리포트 생성 및 조회 관련 API")
@RestController
@RequestMapping("${api_prefix}/type-report")
@RequiredArgsConstructor
public class TypeReportController {

    private final TypeReportService typeReportService;
    private final TypeReportQueryService typeReportQueryService;

    @PostMapping("/start/{interestCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "유형 리포트 생성 시작",
            description = """
                    사용자의 유형 리포트 생성을 시작합니다. </br>
                    비동기로 처리되기 때문에, id로 월간 리포트 조회 API를 폴링하여 상태를 확인할 수 있습니다.
                    
                    선택 가능한 관심 주제 코드는 다음과 같습니다.
                    
                    - **PREFERENCE** : 취향
                    - **EMOTION** : 감정
                    - **ROUTINE** : 루틴
                    - **RELATIONSHIP** : 인간관계
                    - **LOVE** : 사랑
                    - **VALUES** : 가치관
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "유형 리포트 생성 시작 성공",
                            content = @Content(schema = @Schema(implementation = TypeReportStartResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: TYPE_REPORT_NOT_ENOUGH_REPORTS - 유형 리포트 작성 자격 미달 **(이 경우 data의 completedCount 필드에 현재까지 작성된 해당 유형 일간 리포트 수가 포함됩니다.)**
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
                                    - ErrorCode: TYPE_REPORT_ALREADY_COMPLETED - 이미 작성된 유형 리포트가 존재함
                                    - ErrorCode: TYPE_REPORT_IN_PROGRESS - 현재 유형 리포트를 생성 중임
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<TypeReportStartResponse>> startTypeReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String interestCode
    ) {
        TypeReportStartResponse response = typeReportService.startTypeReport(principal.getId(), interestCode);
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/{interestCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "나의 유형 리포트 단일 조회",
            description = """
                    유형 하나에 대한 사용자의 단일 유형 리포트를 조회합니다. </br>
                    이때 ```report``` 필드가 ```null```인 경우 해당 유형 리포트가 존재하지 않음을 의미합니다. </br>
                    **<```report```의 state>** </br>
                    생성 대기 중인 경우 ```status = "PENDING"``` 으로 반환됩니다. </br>
                    생성 진행 중인 경우 ```status = "IN_PROGRESS"``` 로 반환됩니다. </br>
                    생성에 성공한 경우 ```status = "COMPLETED"``` 로 반환됩니다. </br>
                    생성에 실패한 경우 ```status = "FAILED"``` 로 반환됩니다. 이때 크리스탈이 환불되기 때문에 잔액 조회를 해야합니다.
                    
                    선택 가능한 관심 주제 코드는 다음과 같습니다.
                    
                    - **PREFERENCE** : 취향
                    - **EMOTION** : 감정
                    - **ROUTINE** : 루틴
                    - **RELATIONSHIP** : 인간관계
                    - **LOVE** : 사랑
                    - **VALUES** : 가치관
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "나의 유형 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = MyTypeReportResponse.class), mediaType = "application/json")
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
    public ResponseEntity<ApiResponseDto<MyTypeReportResponse>> getMyTypeReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String interestCode
    ) {
        MyTypeReportResponse response = typeReportQueryService.getMyTypeReport(principal.getId(), interestCode);
        return ApiResponseEntity.ok(response);
    }
}
