package com.devkor.ifive.nadab.domain.dailyreport.api;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.CreateAnswerImageUploadUrlRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.*;
import com.devkor.ifive.nadab.domain.dailyreport.application.DailyReportQueryService;
import com.devkor.ifive.nadab.domain.dailyreport.application.DailyReportService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final DailyReportService dailyReportService;
    private final DailyReportQueryService dailyReportQueryService;


    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "오늘의 리포트 생성 API",
            description = """
                    유저의 오늘의 리포트를 생성합니다. <br/>
                    생성 실패 시에도 이 API를 다시 호출하면 됩니다. <br/>
                    이 때 유저의 답변은 기존의 답변으로 자동으로 사용됩니다. <br/>
                    소요 시간이 최대 3~4초밖에 안 되어 동기처리로 구현했습니다. <br/>
                    
                    이미지 미포함의 경우 objectKey와 webpKey는 null로 보내주시면 됩니다. <br/>
                    
                    <이미지가 포함된 경우> <br/>
                    **5MB 이하의 이미지 파일만 허용됩니다.** <br/>
                    POST /daily-report/image/upload-url 엔드포인트로
                    미리 발급받은 PresignedURL을 통해 이미지를 업로드한 후,
                    해당 엔드포인트에서 반환된 objectKey와 webpKey를 이 요청에 포함시켜야 합니다. <br/>
                    또한 GET /daily-report/image/status 엔드포인트를 통해 이미지 업로드 후 webp 변환이 완료되었는지 확인한 후, <br/>
                    webp 변환이 완료된 경우에만 요청에 포함합니다.<br/>
                    <br/>
                  
                    
                    | 응답의 emotion | 해당 감정 |
                    | :--- | :--- |
                    | `ACHIEVEMENT` | 성취 |
                    | `INTEREST` | 흥미 |
                    | `PEACE` | 평온 |
                    | `PLEASURE` | 즐거움 |
                    | `WILL` | 의지 |
                    | `DEPRESSION` | 우울 |
                    | `REGRET` | 후회 |
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
                            responseCode = "400",
                            description = """
                                    - ErrorCode: DAILY_QUESTION_MISMATCH - 요청한 질문이 사용자에게 할당된 오늘의 질문과 일치하지 않음
                                    - ErrorCode: IMAGE_INVALID_KEY - 유효하지 않은 이미지 키
                                    - ErrorCode: IMAGE_WEBP_KEY_REQUIRED - objectKey는 존재하지만 webpKey가 누락됨
                                    """,
                            content = @Content
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
                                    - ErrorCode: QUESTION_NOT_FOUND - 질문이 존재하지 않음
                                    - ErrorCode: DAILY_QUESTION_NOT_FOUND - 오늘의 질문이 아직 생성되지 않음
                                    - ErrorCode: EMOTION_NOT_FOUND - 감정 정보를 찾을 수 없음
                                    - ErrorCode: WALLET_NOT_FOUND - 사용자의 지갑을 찾을 수 없음
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = """
                                    - ErrorCode: DAILY_REPORT_ALREADY_COMPLETED - 이미 작성된 일간 리포트가 존재함
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description = """
                                    - ErrorCode: AI_RESPONSE_PARSE_FAILED - AI 응답 형식을 해석할 수 없음
                                    - ErrorCode: AI_RESPONSE_FORMAT_INVALID - AI 응답 JSON의 필수 필드가 비어있음
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "503",
                            description = """
                                    - ErrorCode: AI_NO_RESPONSE - AI 서비스로부터 응답을 받지 못함
                                    """,
                            content = @Content
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
                            description = """
                                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음
                                    - ErrorCode: ANSWER_NOT_FOUND - 작성된 답변 내역을 찾을 수 없음
                                    - ErrorCode: DAILY_REPORT_NOT_FOUND - 일간 리포트를 찾을 수 없음
                                    """,
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

    @GetMapping("{reportId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "ID로 오늘의 리포트 조회 API",
            description = "리포트 ID로 오늘의 리포트를 조회합니다. COMPLETED 상태의 리포트만 조회 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "오늘의 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = AnswerDetailResponse.class), mediaType = "application/json")
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
                                    - ErrorCode: ANSWER_NOT_FOUND - 작성된 답변 내역을 찾을 수 없음
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<AnswerDetailResponse>> getDailyReportById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        AnswerDetailResponse response = dailyReportQueryService.getDailyReportById(principal.getId(), reportId);
        return ApiResponseEntity.ok(response);
    }

    @PostMapping("/image/upload-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "답변 이미지 업로드 PresignedURL 생성",
            description = """
                    답변에 포함되는 이미지를 업로드할 수 있는 PresignedURL을 생성합니다.
                    
                    - HTTP Method: PUT
                    - Headers:
                        - Content-Type(필수): image/jpeg, image/png만 허용
                    - Body: 이미지 파일
                    - URL 만료 시간: 5분
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = CreateAnswerImageUploadUrlResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: IMAGE_UNSUPPORTED_TYPE - 지원하지 않는 이미지 타입
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (JWT 토큰 관련)",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<CreateAnswerImageUploadUrlResponse>> createAnswerImageUploadUrl(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateAnswerImageUploadUrlRequest request) {
        CreateAnswerImageUploadUrlResponse response =
                dailyReportService.createUploadUrl(principal.getId(), request);
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/image/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "답변 이미지(webp) 상태 조회",
            description = """
                    답변에 포함되는 이미지의 webp 변환 상태를 조회합니다. <br/>
                    POST /daily-report/image/upload-url 엔드포인트로 이미지를 업로드한 후, 해당 엔드포인트에서 반환된 webpKey를 이 API의 key 파라미터로 전달하여 이미지 상태를 조회할 수 있습니다. <br/>
                    
                    프론트엔드에서는 이 API를 주기적으로 호출하여 이미지 업로드 후 webp 변환이 완료되었는지 확인해야 합니다. <br/>
                    최대 7초 동안 이 API를 호출하여 status가 READY로 변경되었는지 확인하고, <br/>
                    7초가 지나면 실패로 간주하고 사용자에게 이미지 업로드 실패 메시지를 보여주면 됩니다. <br/>
                    
                    - status가 READY인 경우: 이미지 업로드 및 webp 변환이 모두 완료되어 이미지 URL을 사용할 수 있음
                    - status가 PROCESSING인 경우: 이미지 업로드는 완료되었으나 webp 변환이 아직 완료되지 않음. 잠시 후 다시 확인 필요
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = ImageStatusResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: IMAGE_INVALID_KEY - 유효하지 않은 이미지 키
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (JWT 토큰 관련)",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<ImageStatusResponse>> getImageStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String key
    ) {
        ImageStatusResponse response = dailyReportService.getImageStatus(key, principal.getId());

        return ApiResponseEntity.ok(response);
    }

}
