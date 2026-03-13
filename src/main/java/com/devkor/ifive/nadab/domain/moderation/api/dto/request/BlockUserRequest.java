package com.devkor.ifive.nadab.domain.moderation.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "사용자 차단 요청")
public record BlockUserRequest(
        @Schema(description = "차단할 사용자 닉네임", example = "모래")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 1, max = 255, message = "닉네임은 1자 이상 255자 이하여야 합니다.")
        String blockedNickname
) {
}
