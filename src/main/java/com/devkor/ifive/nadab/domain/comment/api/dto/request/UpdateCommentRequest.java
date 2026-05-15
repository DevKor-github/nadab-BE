package com.devkor.ifive.nadab.domain.comment.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 수정 요청")
public record UpdateCommentRequest(

        @Schema(description = "수정할 댓글 내용 (1~500자)", example = "수정된 내용이에요")
        @NotBlank(message = "댓글 내용을 입력해주세요")
        @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요")
        String content
) {
}