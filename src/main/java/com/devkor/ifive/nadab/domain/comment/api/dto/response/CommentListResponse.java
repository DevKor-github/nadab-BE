package com.devkor.ifive.nadab.domain.comment.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "댓글/대댓글 목록 응답")
public record CommentListResponse(

        @Schema(description = "댓글 목록")
        List<CommentResponse> comments,

        @Schema(description = "다음 페이지 커서 (없으면 null)")
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {
}