package com.devkor.ifive.nadab.domain.typereport.api;

import com.devkor.ifive.nadab.domain.typereport.api.dto.response.MyAllTypeReportsResponse;
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
        특정 관심 주제(interestCode) 1개에 대한 사용자의 유형 리포트 상태를 조회합니다. </br></br>

        응답의 ```report```는 다음 3개 영역으로 구성됩니다. </br>
        - ```current```: 현재 조회 가능한(사용자가 볼 수 있는) 유형 리포트 본문 </br>
        - ```generation```: 새 리포트 생성 작업의 상태(진행 중/실패 여부 및 작업 reportId) </br>
        - ```eligibility```: 생성 자격(완료한 일간 리포트 개수, 필요 개수, 생성 가능 여부, 첫 생성 무료 여부) </br></br>

        **1) ```current```** </br>
        - 이미 생성 완료된 리포트가 있으면 ```current```에 상세 내용이 채워집니다. </br>
        - 아직 생성된 리포트가 없으면 ```current = null``` 입니다. </br>
        - ```current.status```는 “현재 조회 가능한 리포트 자체의 상태”를 의미합니다(예: COMPLETED). </br></br>

        **2) ```generation```** </br>
        - “새 유형 리포트 생성 작업”의 상태를 의미합니다. </br>
        - ```status``` 값: </br>
          - ```NONE```: 생성 작업이 없음(대기/진행/실패 상태가 아님) </br>
          - ```IN_PROGRESS```: 생성 작업 진행 중 </br>
          - ```FAILED```: 생성 작업 실패(환불/처리 등 후속 로직이 완료된 상태) </br>
        - ```reportId```는 ```IN_PROGRESS``` 또는 ```FAILED```인 생성 작업의 대상 reportId이며, 없으면 null입니다. </br></br>

        **3) ```eligibility```** </br>
        - ```dailyCompletedCount```: 해당 interest의 완료된 일간 리포트 개수 </br>
        - ```requiredCount```: 유형 리포트 생성을 위해 필요한 최소 완료 개수 (현재 30) </br>
        - ```canGenerate```: 현재 시점에 생성 조건을 충족했는지 여부 </br>
        - ```isFirstFree```: 해당 interest의 “첫 유형 리포트 생성 무료” 대상 여부 </br></br>

        선택 가능한 관심 주제 코드는 다음과 같습니다. </br>
        - **PREFERENCE** : 취향 </br>
        - **EMOTION** : 감정 </br>
        - **ROUTINE** : 루틴 </br>
        - **RELATIONSHIP** : 인간관계 </br>
        - **LOVE** : 사랑 </br>
        - **VALUES** : 가치관 </br>
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

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "나의 유형 리포트 통합 조회",
            description = """
        사용자의 유형 리포트를 관심 주제(```InterestCode```)별로 한 번에 조회합니다. </br></br>

        응답의 ```reports```는 다음 형태의 Map 입니다. </br>
        - Key: ```InterestCode``` 문자열 (PREFERENCE, EMOTION, ROUTINE, RELATIONSHIP, LOVE, VALUES) </br>
        - Value: ```TypeReportDetailResponse``` </br></br>

        Value(```TypeReportDetailResponse```)는 단일 조회 응답의 ```report```와 동일한 스키마이며, 다음 3개 영역으로 구성됩니다. </br>
        - ```current```: 현재 조회 가능한 유형 리포트 본문(없으면 null) </br>
        - ```generation```: 새 리포트 생성 작업 상태(진행 중/실패 여부 및 작업 reportId) </br>
        - ```eligibility```: 생성 자격(완료 개수/필요 개수/생성 가능/첫 생성 무료 여부) </br></br>

        **각 필드 의미** </br>
        **1) ```current```** </br>
        - 생성 완료된 리포트가 있으면 상세 내용이 채워집니다. </br>
        - 아직 생성된 리포트가 없으면 ```current = null``` 입니다. </br></br>

        **2) ```generation```** </br>
        - “새 유형 리포트 생성 작업”의 상태를 의미합니다. </br>
        - ```status``` 값: ```NONE``` / ```IN_PROGRESS``` / ```FAILED``` </br>
        - ```reportId```는 생성 작업이 존재할 때의 reportId이며, 없으면 null입니다. </br></br>

        **3) ```eligibility```** </br>
        - ```dailyCompletedCount```: 해당 interest의 완료된 일간 리포트 개수 </br>
        - ```requiredCount```: 생성에 필요한 최소 개수 (현재 30) </br>
        - ```canGenerate```: 현재 생성 조건 충족 여부 </br>
        - ```isFirstFree```: 해당 interest의 첫 생성 무료 대상 여부 </br></br>
        """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "나의 유형 리포트 통합 조회 성공",
                            content = @Content(schema = @Schema(implementation = MyAllTypeReportsResponse.class), mediaType = "application/json")
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
    public ResponseEntity<ApiResponseDto<MyAllTypeReportsResponse>> getMyAllTypeReports(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MyAllTypeReportsResponse response = typeReportQueryService.getMyAllTypeReports(principal.getId());
        return ApiResponseEntity.ok(response);
    }
}
