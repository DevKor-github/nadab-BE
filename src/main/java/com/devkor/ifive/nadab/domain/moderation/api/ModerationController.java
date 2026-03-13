package com.devkor.ifive.nadab.domain.moderation.api;

import com.devkor.ifive.nadab.domain.moderation.api.dto.request.ReportContentRequest;
import com.devkor.ifive.nadab.domain.moderation.application.ContentReportCommandService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "신고 및 차단 API", description = "공유글 신고, 사용자 차단 관련 API")
@RestController
@RequestMapping("${api_prefix}/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ContentReportCommandService contentReportCommandService;

    @PostMapping("/reports")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "공유글 신고 API",
            description = """
                    공유된 DailyReport를 신고합니다.

                    요청 필드:
                    - dailyReportId (필수): 신고할 공유글의 DailyReport ID(GET /api/v1/feed 호출 시 필드에서 확인 가능)
                    - reason (필수): 신고 사유
                      - PROFANITY_HATE_SPEECH: 욕설 / 혐오 표현
                      - SEXUAL_CONTENT: 성적으로 부적절한 언행
                      - SELF_HARM: 자해 / 자살 조장
                      - OTHER: 기타 (customReason 필수)
                    - customReason: reason이 OTHER일 때만 필수, 200자 이하

                    신고 후 동작:
                    - 동일 공유글 중복 신고 불가
                    - 신고한 공유글은 신고자의 피드에서 숨겨짐
                    - 누적 신고 10건 이상 & 신고자 2명 이상 시 작성자의 소셜 활동 자동 중지(공유하기 시도 시 status로 SUSPENDED 반환)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "신고 성공",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "ErrorCode: CONTENT_REPORT_INVALID - 잘못된 신고 요청 (기타 사유 미입력 또는 200자 초과)",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ErrorCode: DAILY_REPORT_NOT_FOUND - 공유글을 찾을 수 없음",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "ErrorCode: CONTENT_REPORT_ALREADY_EXISTS - 이미 신고한 공유글",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> reportContent(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportContentRequest request
    ) {
        contentReportCommandService.reportContent(
                principal.getId(),
                request.dailyReportId(),
                request.reason(),
                request.customReason()
        );
        return ApiResponseEntity.noContent();
    }
}