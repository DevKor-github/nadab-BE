package com.devkor.ifive.nadab.domain.moderation.api.dto.request;

import com.devkor.ifive.nadab.domain.moderation.core.entity.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "공유글 신고 요청")
public record ReportContentRequest(

        @NotNull(message = "신고할 공유글 ID는 필수입니다")
        @Schema(description = "신고할 DailyReport ID", example = "123")
        Long dailyReportId,

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