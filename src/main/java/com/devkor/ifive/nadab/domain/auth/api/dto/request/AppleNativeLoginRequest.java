package com.devkor.ifive.nadab.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 애플 Native SDK 로그인 요청 DTO
 * - iOS 앱에서 Sign in with Apple SDK로 받은 Authorization Code 전달
 */
@Schema(description = "애플 Native SDK 로그인 요청")
public record AppleNativeLoginRequest(
        @Schema(description = "애플 SDK로부터 받은 Authorization Code", example = "c1234567890abcdef")
        @NotBlank(message = "애플 Authorization Code는 필수입니다")
        String code
) {
}