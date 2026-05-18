package com.devkor.ifive.nadab.domain.comment.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "댓글/대댓글 응답")
public record CommentResponse(

        @Schema(description = "댓글 ID")
        Long commentId,

        @Schema(description = "작성자 프로필 이미지 URL (canViewContent=false이면 null)")
        String authorProfileImageUrl,

        @Schema(description = "작성자 닉네임 (canViewContent=false이면 null)")
        String authorNickname,

        @Schema(description = "댓글 내용 (canViewContent=false이면 null)")
        String content,

        @Schema(description = "작성 시각 (ISO 8601, 예: 2024-05-11T10:30:00+09:00) — 프론트에서 현재 시각 기준으로 '3분 전' 등으로 변환하여 표시")
        OffsetDateTime createdAt,

        @Schema(description = "내가 좋아요 눌렀는지 여부")
        boolean isLiked,

        @Schema(description = "좋아요가 1개 이상인지 여부")
        boolean hasLikes,

        @Schema(description = "보이는 대댓글 수 (canViewContent=false이거나 대댓글에서는 null)")
        Integer visibleSubCommentCount,

        @Schema(description = "비밀 댓글 여부")
        boolean isSecret,

        @Schema(description = "비밀 댓글 열람 권한 여부 (false이면 authorProfileImageUrl·authorNickname·content가 null)")
        boolean canViewContent,

        @Schema(description = "내 댓글 여부")
        boolean isMine,

        @Schema(description = "삭제 가능 여부 (본인 또는 리포트 당사자)")
        boolean canDelete
) {
    public static CommentResponse from(
            Long commentId,
            String authorProfileImageUrl,
            String authorNickname,
            String content,
            OffsetDateTime createdAt,
            boolean isLiked,
            boolean hasLikes,
            Integer visibleSubCommentCount,
            boolean isSecret,
            boolean canViewContent,
            boolean isMine,
            boolean canDelete
    ) {
        return new CommentResponse(
                commentId,
                authorProfileImageUrl,
                authorNickname,
                content,
                createdAt,
                isLiked,
                hasLikes,
                visibleSubCommentCount,
                isSecret,
                canViewContent,
                isMine,
                canDelete
        );
    }
}