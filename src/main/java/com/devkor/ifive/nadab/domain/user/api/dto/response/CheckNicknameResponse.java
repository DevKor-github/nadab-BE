package com.devkor.ifive.nadab.domain.user.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 사용 가능 여부 확인 응답")
public record CheckNicknameResponse(
        @Schema(
            description = "닉네임 사용 가능 여부",
            example = "true"
        )
        boolean isAvailable,

        @Schema(
            description = "닉네임 사용 불가 사유 (사용 가능한 경우 null)",
            example = "이미 사용 중인 닉네임입니다."
        )
        String reason
) {
}
