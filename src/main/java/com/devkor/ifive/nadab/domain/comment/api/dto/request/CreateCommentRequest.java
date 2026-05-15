package com.devkor.ifive.nadab.domain.comment.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 작성 요청")
public record CreateCommentRequest(

        @Schema(description = "리포트 ID")
        @NotNull(message = "리포트 ID를 입력해주세요")
        Long dailyReportId,

        @Schema(description = "댓글 내용 (1~500자)", example = "공감해요!")
        @NotBlank(message = "댓글 내용을 입력해주세요")
        @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요")
        String content,

        @Schema(description = "비밀 댓글 여부", example = "false")
        boolean isSecret
) {
}