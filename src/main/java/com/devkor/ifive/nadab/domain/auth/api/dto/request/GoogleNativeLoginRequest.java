package com.devkor.ifive.nadab.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 구글 Native SDK 로그인 요청 DTO
 * - Android/iOS 앱에서 구글 SDK로 받은 ID Token 전달
 */
@Schema(description = "구글 Native SDK 로그인 요청")
public record GoogleNativeLoginRequest(
        @Schema(description = "구글 SDK로부터 받은 ID Token (JWT)", example = "eyJhbGciOiJSUzI1NiIs...")
        @NotBlank(message = "구글 ID Token은 필수입니다")
        String googleIdToken
) {
}