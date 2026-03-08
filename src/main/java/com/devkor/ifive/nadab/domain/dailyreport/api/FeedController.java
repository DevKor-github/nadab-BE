package com.devkor.ifive.nadab.domain.dailyreport.api;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.FeedListResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.ShareStatusResponse;
import com.devkor.ifive.nadab.domain.dailyreport.application.FeedQueryService;
import com.devkor.ifive.nadab.domain.dailyreport.application.FeedService;
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

@Tag(name = "피드 API", description = "친구 피드 공유 및 조회 관련 API")
@RestController
@RequestMapping("${api_prefix}/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final FeedQueryService feedQueryService;

    @PostMapping("/share")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "공유 시작 API",
            description = "당일 DailyReport를 친구들에게 공유합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "공유 시작 성공",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ErrorCode: DAILY_REPORT_NOT_FOUND - 당일 리포트를 찾을 수 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> startSharing(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        feedService.startSharing(principal.getId());
        return ApiResponseEntity.noContent();
    }

    @PostMapping("/unshare")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "공유 중단 API",
            description = "당일 DailyReport 공유를 중단합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "공유 중단 성공",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ErrorCode: DAILY_REPORT_NOT_FOUND - 당일 리포트를 찾을 수 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> stopSharing(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        feedService.stopSharing(principal.getId());
        return ApiResponseEntity.noContent();
    }

    @GetMapping("/share/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "공유 상태 조회 API",
            description = """
                    당일 DailyReport의 공유 상태를 조회합니다.

                    이 API는 상세보기 화면에서 오늘 날짜 답변일 때 공유 버튼 상태를 확인하기 위해 사용됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = ShareStatusResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<ShareStatusResponse>> getShareStatus(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ShareStatusResponse response = feedQueryService.getShareStatus(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "피드 조회 API",
            description = "친구들의 공유된 DailyReport 목록을 조회합니다. ACCEPTED 상태의 친구만 조회됩니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "피드 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = FeedListResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<FeedListResponse>> getFeeds(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FeedListResponse response = feedQueryService.getFeeds(principal.getId());
        return ApiResponseEntity.ok(response);
    }
}
