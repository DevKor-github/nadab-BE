package com.devkor.ifive.nadab.domain.moderation.api.dto.request;

import com.devkor.ifive.nadab.domain.moderation.core.entity.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "신고 요청 — dailyReportId / commentId 중 하나만 필수")
public record ReportContentRequest(

        @Schema(description = "신고할 DailyReport ID (게시글 신고 시 필수)", example = "123", nullable = true)
        Long dailyReportId,

        @Schema(description = "신고할 Comment ID (댓글/대댓글 신고 시 필수)", example = "456", nullable = true)
        Long commentId,

        @NotNull(message = "신고 사유는 필수입니다")
        @Schema(
                description = "신고 사유 (PROFANITY_HATE_SPEECH, SEXUAL_CONTENT, SELF_HARM, OTHER)",
                example = "OTHER"
        )
        ReportReason reason,

        @Schema(
                description = "기타 사유 (reason이 OTHER일 때 필수, 200자 이하)",
                example = "부적절한 내용입니다"
        )
        String customReason
) {
}