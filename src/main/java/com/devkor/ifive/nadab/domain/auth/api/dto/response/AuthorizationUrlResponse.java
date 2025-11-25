package com.devkor.ifive.nadab.domain.auth.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Authorization URL 응답 DTO
 * - 프론트엔드로 전달할 OAuth2 Authorization URL
 */
@Schema(description = "OAuth2 Authorization URL 응답")
public record AuthorizationUrlResponse(
        @Schema(description = "OAuth2 제공자의 로그인 페이지 URL (프론트엔드에서 리다이렉트)", example = "https://nid.naver.com/oauth2.0/authorize?client_id=...")
        String authorizationUrl
) {
}
