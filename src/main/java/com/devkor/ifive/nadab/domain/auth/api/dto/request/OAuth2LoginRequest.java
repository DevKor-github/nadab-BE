package com.devkor.ifive.nadab.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * OAuth2 로그인 요청 DTO
 * - 프론트엔드가 Authorization Code와 State를 전달
 */
@Schema(description = "OAuth2 로그인 요청")
public record OAuth2LoginRequest(
        @Schema(description = "OAuth2 제공자로부터 받은 Authorization Code", example = "MsabWEWdhBgKrZk", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Authorization code는 필수입니다")
        String code,

        @Schema(description = "CSRF 방지를 위한 State 파라미터", example = "0328973a-f474f-413a-be5d", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "State는 필수입니다")
        String state
) {
}