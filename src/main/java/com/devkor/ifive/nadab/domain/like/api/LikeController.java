package com.devkor.ifive.nadab.domain.like.api;

import com.devkor.ifive.nadab.domain.like.api.dto.response.LikeListResponse;
import com.devkor.ifive.nadab.domain.like.application.LikeCommandService;
import com.devkor.ifive.nadab.domain.like.application.LikeQueryService;
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

@Tag(name = "좋아요 API", description = "게시글 및 댓글 좋아요 관련 API")
@RestController
@RequestMapping("${api_prefix}")
@RequiredArgsConstructor
public class LikeController {

    private final LikeCommandService likeCommandService;
    private final LikeQueryService likeQueryService;

    @PostMapping("/feed/{dailyReportId}/likes")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시글 좋아요",
            description = """
                    친구의 공유 게시글에 좋아요를 누릅니다.

                    - 본인의 게시글에는 좋아요 불가 (400)
                    - 이미 좋아요한 경우 204 반환 (멱등)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "좋아요 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = "ErrorCode: CANNOT_LIKE_OWN_CONTENT", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "ErrorCode: AUTH_ACCESS_DENIED - 공유되지 않은 게시글이거나 친구가 아닌 경우", content = @Content),
                    @ApiResponse(responseCode = "404", description = "ErrorCode: DAILY_REPORT_NOT_FOUND", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> likeReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long dailyReportId
    ) {
        likeCommandService.likeReport(dailyReportId, principal.getId());
        return ApiResponseEntity.noContent();
    }

    @DeleteMapping("/feed/{dailyReportId}/likes")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시글 좋아요 취소",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "취소 성공", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "ErrorCode: LIKE_NOT_FOUND", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> unlikeReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long dailyReportId
    ) {
        likeCommandService.unlikeReport(dailyReportId, principal.getId());
        return ApiResponseEntity.noContent();
    }

    @GetMapping("/feed/{dailyReportId}/likes")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시글 좋아요 리스트",
            description = """
                    게시글에 좋아요를 누른 사용자 목록을 조회합니다.

                    - 리포트 당사자(본인 게시글)만 조회 가능합니다.
                    - 최신순 정렬, 차단 관계 양방향 제외합니다.
                    - isFriend=true인 경우 친구 삭제·차단 버튼 제공, false인 경우 친구 신청 버튼 제공합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = LikeListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "ErrorCode: DAILY_REPORT_LIKE_LIST_FORBIDDEN - 본인 게시글 아님", content = @Content),
                    @ApiResponse(responseCode = "404", description = "ErrorCode: DAILY_REPORT_NOT_FOUND", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<LikeListResponse>> getReportLikers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long dailyReportId
    ) {
        LikeListResponse response = likeQueryService.getReportLikers(dailyReportId, principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @PostMapping("/comments/{commentId}/likes")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "댓글/대댓글 좋아요",
            description = """
                    댓글 또는 대댓글에 좋아요를 누릅니다.

                    - 본인의 댓글에는 좋아요 불가 (400)
                    - 열람 권한 없는 비밀 댓글에는 좋아요 불가 (403)
                    - 이미 좋아요한 경우 204 반환 (멱등)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "좋아요 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = "ErrorCode: CANNOT_LIKE_OWN_CONTENT", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "ErrorCode: AUTH_ACCESS_DENIED", content = @Content),
                    @ApiResponse(responseCode = "404", description = "ErrorCode: COMMENT_NOT_FOUND", content = @Content),
                    @ApiResponse(responseCode = "409", description = "ErrorCode: COMMENT_DELETED", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> likeComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId
    ) {
        likeCommandService.likeComment(commentId, principal.getId());
        return ApiResponseEntity.noContent();
    }

    @DeleteMapping("/comments/{commentId}/likes")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "댓글/대댓글 좋아요 취소",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "취소 성공", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "ErrorCode: LIKE_NOT_FOUND", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> unlikeComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId
    ) {
        likeCommandService.unlikeComment(commentId, principal.getId());
        return ApiResponseEntity.noContent();
    }

    @GetMapping("/comments/{commentId}/likes")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "댓글/대댓글 좋아요 리스트",
            description = """
                    댓글 또는 대댓글에 좋아요를 누른 사용자 목록을 조회합니다.

                    - 최신순 정렬, 차단 관계 양방향 제외합니다.
                    - isFriend=true인 경우 친구 삭제·차단 버튼 제공, false인 경우 친구 신청 버튼 제공합니다.
                    - 비밀 댓글은 열람 권한자(작성자·리포트 당사자)만 조회 가능합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = LikeListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "ErrorCode: AUTH_ACCESS_DENIED - 비밀 댓글 열람 권한 없음 또는 피드 접근 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "ErrorCode: COMMENT_NOT_FOUND", content = @Content),
                    @ApiResponse(responseCode = "409", description = "ErrorCode: COMMENT_DELETED", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<LikeListResponse>> getCommentLikers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId
    ) {
        LikeListResponse response = likeQueryService.getCommentLikers(commentId, principal.getId());
        return ApiResponseEntity.ok(response);
    }
}