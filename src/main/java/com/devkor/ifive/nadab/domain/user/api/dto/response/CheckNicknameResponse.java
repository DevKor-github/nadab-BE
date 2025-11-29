package com.devkor.ifive.nadab.domain.user.api.dto.response;

public record CheckNicknameResponse(
    boolean isAvailable,
    String reason
) {
}
