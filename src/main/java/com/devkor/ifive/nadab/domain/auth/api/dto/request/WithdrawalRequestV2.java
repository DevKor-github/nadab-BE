package com.devkor.ifive.nadab.domain.auth.api.dto.request;

import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalReasonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WithdrawalRequestV2(
        @Schema(
                description = "탈퇴 사유 목록 (다중 선택 가능)",
                example = "[\"DAILY_LOGGING_BURDEN\", \"OTHER\"]"
        )
        @NotEmpty(message = "탈퇴 사유는 최소 1개 이상 선택해야 합니다.")
        List<WithdrawalReasonType> reasons,

        @Schema(
                description = "기타 사유 직접 입력 (reasons에 OTHER가 포함된 경우 필수)",
                example = "앱이 저에게 맞지 않았어요."
        )
        @Size(max = 200, message = "기타 사유는 최대 200자까지 입력할 수 있습니다.")
        String customReason
) {
}
