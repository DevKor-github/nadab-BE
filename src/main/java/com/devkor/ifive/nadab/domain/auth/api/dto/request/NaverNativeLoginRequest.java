package com.devkor.ifive.nadab.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 네이버 Native SDK 로그인 요청 DTO
 * - Android/iOS 앱에서 네이버 SDK로 받은 Access Token 전달
 */
@Schema(description = "네이버 Native SDK 로그인 요청")
public record NaverNativeLoginRequest(
        @Schema(description = "네이버 SDK로부터 받은 Access Token", example = "AAAANv1...")
        @NotBlank(message = "네이버 Access Token은 필수입니다")
        String naverAccessToken
) {
}