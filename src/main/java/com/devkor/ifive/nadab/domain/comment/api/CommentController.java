package com.devkor.ifive.nadab.domain.comment.api;

import com.devkor.ifive.nadab.domain.comment.api.dto.request.CreateCommentRequest;
import com.devkor.ifive.nadab.domain.comment.api.dto.request.CreateSubCommentRequest;
import com.devkor.ifive.nadab.domain.comment.api.dto.request.UpdateCommentRequest;
import com.devkor.ifive.nadab.domain.comment.api.dto.response.CommentListResponse;
import com.devkor.ifive.nadab.domain.comment.application.CommentCommandService;
import com.devkor.ifive.nadab.domain.comment.application.CommentQueryService;
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

@Tag(name = "댓글 API", description = "댓글 및 대댓글 관련 API")
@RestController
@RequestMapping("${api_prefix}")
@RequiredArgsConstructor
public class CommentController {

    private final CommentCommandService commentCommandService;
    private final CommentQueryService commentQueryService;

    @GetMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "댓글 목록 조회",
            description = """
                    피드 게시글의 댓글 목록을 커서 기반으로 최신순 조회합니다.

                    요청 파라미터:
                    - dailyReportId (필수): 댓글을 조회할 피드 게시글 ID
                    - cursor (선택): 이전 응답의 nextCursor, 첫 요청 시 생략

                    커서 페이지네이션 (페이지당 10개):
                    - 첫 요청: cursor 없이 호출 → GET /api/v1/comments?dailyReportId=1
                    - 다음 페이지: 응답의 nextCursor를 cursor로 전달 → GET /api/v1/comments?dailyReportId=1&cursor=42
                    - hasNext=false이면 마지막 페이지입니다.

                    비밀 댓글 응답:
                    - 권한 있음 (작성자 본인, 게시글 작성자): canViewContent=true, 모든 필드 정상 반환
                    - 권한 없음: canViewContent=false, authorProfileImageUrl·authorNickname·content·visibleSubCommentCount는 null 반환

                    응답 필드 용도:
                    - commentId: 수정(PATCH)·삭제(DELETE)·대댓글 조회(GET)·대댓글 작성(POST) API 호출 시 path variable로 사용
                    - authorProfileImageUrl·authorNickname: 댓글 작성자 프로필 표시 (canViewContent=false이면 null → 비공개 처리)
                    - content: 댓글 본문 (canViewContent=false이면 null → "비밀 댓글이에요." 등으로 대체 표시)
                    - createdAt: 작성 시각 ISO 8601 timestamp (예: 2026-05-11T10:30:00+09:00) — 프론트에서 현재 시각 기준으로 아래 규칙에 따라 변환하여 표시, 타이머로 실시간 갱신 가능
                      · 60초 미만 → N초 전
                      · 60분 미만 → N분 전
                      · 24시간 미만 → N시간 전
                      · 30일 미만 → N일 전
                      · 30일 이상 → 오래 전
                    - isLiked·hasLikes: 좋아요 아이콘 상태 제어
                      · isMine=true & hasLikes=false → 아이콘 미표시
                      · isMine=true & hasLikes=true  → 채워진 아이콘 (단순 클릭 무반응, 길게 누르면 좋아요 리스트)
                      · isMine=false & isLiked=false → 빈 아이콘
                      · isMine=false & isLiked=true  → 채워진 아이콘
                      · canViewContent=false (비밀 댓글 열람 권한 없음) → 아이콘 미표시
                    - visibleSubCommentCount: "답글 N개 더보기" 버튼 표시용, null 또는 0이면 미표시
                    - isSecret: 비밀 댓글 여부 (자물쇠 아이콘 표시, canViewContent=false이면 답글 버튼 미제공)
                    - canViewContent: false이면 authorProfileImageUrl·authorNickname·content 마스킹 처리
                    - isMine: 수정 버튼 표시 여부, 좋아요 아이콘 동작 제어 (본인 댓글은 좋아요 불가)
                    - canDelete: 삭제 버튼 표시 여부 (본인 댓글 또는 내 피드 게시글에 달린 타인 댓글)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommentListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "ErrorCode: AUTH_ACCESS_DENIED - 본인 피드 게시글이 아니거나 친구의 공유 게시글이 아닌 경우", content = @Content),
                    @ApiResponse(responseCode = "404", description = "ErrorCode: DAILY_REPORT_NOT_FOUND", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<CommentListResponse>> getComments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Long dailyReportId,
            @RequestParam(required = false) Long cursor
    ) {
        CommentListResponse response = commentQueryService.getComments(dailyReportId, principal.getId(), cursor);
        return ApiResponseEntity.ok(response);
    }

    @PostMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "댓글 작성",
            description = """
                    피드 게시글에 댓글을 작성합니다.

                    - isSecret=true로 작성 시 비밀 댓글로 설정되며, 작성자 본인과 게시글 작성자만 내용을 확인할 수 있습니다.
                    - isSecret은 작성 후 변경할 수 없습니다. (수정 API에서 내용만 변경 가능)
                    - 댓글 작성 시 게시글 작성자에게 FCM 푸시 알림이 전송됩니다. (본인 피드 게시글에 작성 시 알림 미전송)
                    - 작성 성공 후 GET /api/v1/comments?dailyReportId={id}로 목록을 재조회해야 합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "작성 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = """
                            - ErrorCode: VALIDATION_FAILED - 내용 누락 또는 500자 초과
                            - ErrorCode: SOCIAL_SUSPENDED - 소셜 정지 중
                            """, content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "ErrorCode: AUTH_ACCESS_DENIED - 본인 피드 게시글이 아니거나 친구의 공유 게시글이 아닌 경우", content = @Content),
                    @ApiResponse(responseCode = "404", description = "ErrorCode: DAILY_REPORT_NOT_FOUND", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> createComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        commentCommandService.createComment(
                request.dailyReportId(), principal.getId(), request.content(), request.isSecret());
        return ApiResponseEntity.noContent();
    }

    @GetMapping("/comments/{commentId}/sub-comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "대댓글 목록 조회",
            description = """
                    특정 댓글의 대댓글 목록을 커서 기반으로 최신순 조회합니다.

                    요청 파라미터:
                    - commentId (path, 필수): 대댓글을 조회할 부모 댓글 ID
                    - cursor (선택): 이전 응답의 nextCursor, 첫 요청 시 생략 (페이지당 10개)

                    커서 페이지네이션:
                    - 첫 요청: cursor 없이 호출 → GET /api/v1/comments/42/sub-comments
                    - 다음 페이지: 응답의 nextCursor를 cursor로 전달 → GET /api/v1/comments/42/sub-comments?cursor=99
                    - hasNext=false이면 마지막 페이지입니다.

                    비밀 대댓글 응답:
                    - 권한 있음 (작성자 본인, 게시글 작성자, 부모 댓글 작성자): canViewContent=true, 모든 필드 정상 반환
                    - 권한 없음: canViewContent=false, authorProfileImageUrl·authorNickname·content는 null 반환

                    "N개 더보기" 버튼 카운트 계산:
                    - 첫 진입 시: 댓글 목록 조회(GET /comments) 응답의 visibleSubCommentCount 값을 사용
                    - 대댓글 로드 후 hasNext=true이면: visibleSubCommentCount - 현재까지 로드한 대댓글 수 = 남은 개수로 갱신하여 표시 (프론트에서 직접 계산 및 갱신)
                    - 예) visibleSubCommentCount=12 → 10개 로드 후 "2개 더보기" 표시

                    응답 필드 용도:
                    - commentId: 수정(PATCH)·삭제(DELETE) API 호출 시 path variable로 사용
                    - authorProfileImageUrl·authorNickname: 대댓글 작성자 프로필 표시 (canViewContent=false이면 null → 비공개 처리)
                    - content: 대댓글 본문 (canViewContent=false이면 null → "비밀 댓글이에요." 등으로 대체 표시)
                    - createdAt: 작성 시각 ISO 8601 timestamp — 댓글 목록 조회와 동일한 변환 규칙 적용
                    - isLiked·hasLikes: 좋아요 아이콘 상태 제어
                      · isMine=true & hasLikes=false → 아이콘 미표시
                      · isMine=true & hasLikes=true  → 채워진 아이콘 (단순 클릭 무반응, 길게 누르면 좋아요 리스트)
                      · isMine=false & isLiked=false → 빈 아이콘
                      · isMine=false & isLiked=true  → 채워진 아이콘
                      · canViewContent=false (비밀 댓글 열람 권한 없음) → 아이콘 미표시
                    - visibleSubCommentCount: 대댓글에서는 항상 null
                    - isSecret: 비밀 댓글 여부 (자물쇠 아이콘 표시)
                    - canViewContent: false이면 authorProfileImageUrl·authorNickname·content 마스킹 처리
                      · 대댓글 열람 권한: 작성자 본인, 게시글 작성자, 부모 댓글 작성자
                    - isMine: 수정 버튼 표시 여부, 좋아요 아이콘 동작 제어 (본인 댓글은 좋아요 불가)
                    - canDelete: 삭제 버튼 표시 여부 (본인 대댓글 또는 내 피드 게시글에 달린 타인 대댓글)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommentListResponse.class))),
                    @ApiResponse(responseCode = "400", description = "ErrorCode: COMMENT_NOT_TOP_LEVEL - commentId가 대댓글 ID인 경우 (대댓글의 대댓글 불가)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = """
                            - ErrorCode: AUTH_ACCESS_DENIED - 본인 피드 게시글이 아니거나 친구의 공유 게시글이 아닌 경우
                            - ErrorCode: AUTH_ACCESS_DENIED - 비밀 댓글에 대한 열람 권한 없음
                            """, content = @Content),
                    @ApiResponse(responseCode = "404", description = "ErrorCode: COMMENT_NOT_FOUND", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<CommentListResponse>> getSubComments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId,
            @RequestParam(required = false) Long cursor
    ) {
        CommentListResponse response = commentQueryService.getSubComments(commentId, principal.getId(), cursor);
        return ApiResponseEntity.ok(response);
    }

    @PostMapping("/comments/{commentId}/sub-comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "대댓글 작성",
            description = """
                    특정 댓글에 대댓글을 작성합니다.

                    - 부모 댓글이 비밀 댓글인 경우, isSecret 값과 무관하게 대댓글도 강제로 비밀 처리됩니다. 이 경우 isSecret 토글을 사용하지 못하게 하고 isSecret=true로 고정 전송을 권장합니다.
                    - isSecret은 작성 후 변경할 수 없습니다. (수정 API에서 내용만 변경 가능)
                    - 대댓글에 대한 대댓글은 불가합니다.
                    - 대댓글 작성 시 부모 댓글 작성자에게 FCM 푸시 알림이 전송됩니다.
                    - 해당 댓글에 이미 대댓글을 단 다른 참여자들에게도 알림이 전송됩니다.
                    - 작성 성공 후 GET /api/v1/comments/{commentId}/sub-comments로 목록을 재조회해야 합니다.
                    - 대댓글 작성 성공 시 댓글 목록의 visibleSubCommentCount가 갱신되지 않습니다. "답글 N개 더보기" 카운트를 최신화하려면 성공 시 로컬에서 +1 업데이트하거나 GET /api/v1/comments?dailyReportId={id}로 댓글 목록도 재조회해야 합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "작성 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = """
                            - ErrorCode: VALIDATION_FAILED - 내용 누락 또는 500자 초과
                            - ErrorCode: COMMENT_NOT_TOP_LEVEL - 대댓글에 대댓글 시도
                            - ErrorCode: SOCIAL_SUSPENDED - 소셜 정지 중
                            """, content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "ErrorCode: AUTH_ACCESS_DENIED - 본인 피드 게시글이 아니거나 친구의 공유 게시글이 아닌 경우", content = @Content),
                    @ApiResponse(responseCode = "409", description = "ErrorCode: COMMENT_DELETED - 이미 삭제된 댓글에 대댓글 시도", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> createSubComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId,
            @Valid @RequestBody CreateSubCommentRequest request
    ) {
        commentCommandService.createSubComment(
                commentId, principal.getId(), request.content(), request.isSecret());
        return ApiResponseEntity.noContent();
    }

    @PatchMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "댓글 수정",
            description = """
                    본인이 작성한 댓글/대댓글의 내용을 수정합니다.

                    - 작성자 본인만 수정 가능합니다. (게시글 작성자도 타인 댓글 수정 불가)
                    - 내용(content)만 변경 가능하며, 비밀 여부(isSecret)는 변경할 수 없습니다.
                    - 1자 이상 500자 이하로 입력해야 합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "수정 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = """
                            - ErrorCode: VALIDATION_FAILED - 내용 누락 또는 500자 초과
                            - ErrorCode: SOCIAL_SUSPENDED - 소셜 정지 중
                            """, content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "ErrorCode: AUTH_ACCESS_DENIED - 본인 댓글만 수정 가능", content = @Content),
                    @ApiResponse(responseCode = "404", description = "ErrorCode: COMMENT_NOT_FOUND", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> updateComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        commentCommandService.updateComment(commentId, principal.getId(), request.content());
        return ApiResponseEntity.noContent();
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "댓글 삭제",
            description = """
                    댓글/대댓글을 삭제합니다.

                    삭제 권한:
                    - 본인이 작성한 댓글/대댓글: 작성자 본인
                    - 내 피드 게시글에 달린 타인의 댓글/대댓글: 게시글 작성자
                    - 타인 피드 게시글의 타인 댓글: 삭제 불가

                    - 댓글 삭제 시 하위 대댓글도 함께 삭제됩니다.
                    - 신고 이력이 있는 댓글도 삭제 가능합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = "ErrorCode: SOCIAL_SUSPENDED - 소셜 정지 중", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "ErrorCode: AUTH_ACCESS_DENIED - 삭제 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "ErrorCode: COMMENT_NOT_FOUND", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> deleteComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId
    ) {
        commentCommandService.deleteComment(commentId, principal.getId());
        return ApiResponseEntity.noContent();
    }
}