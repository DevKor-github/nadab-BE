package com.devkor.ifive.nadab.domain.auth.api.dto.response;

/**
 * Authorization URL 응답 DTO
 * - 프론트엔드로 전달할 OAuth2 Authorization URL
 */
public record AuthorizationUrlResponse(
        String authorizationUrl
) {
}
