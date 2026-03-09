package com.devkor.ifive.nadab.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 카카오 Native SDK 로그인 요청 DTO
 * - Android/iOS 앱에서 카카오 SDK로 받은 Access Token 전달
 */
@Schema(description = "카카오 Native SDK 로그인 요청")
public record KakaoNativeLoginRequest(
        @Schema(description = "카카오 SDK로부터 받은 Access Token", example = "AAAANv1...")
        @NotBlank(message = "카카오 Access Token은 필수입니다")
        String kakaoAccessToken
) {
}