package com.devkor.ifive.nadab.domain.test.api.dto.response;

public record CreateTestUserResponse(
        Long userId,
        String email,
        String nickname,
        String signupStatus
) {
}
