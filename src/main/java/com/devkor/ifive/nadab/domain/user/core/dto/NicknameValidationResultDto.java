package com.devkor.ifive.nadab.domain.user.core.dto;

public record NicknameValidationResultDto(
        boolean isValid,
        String reason
) {
    public static NicknameValidationResultDto ok() {
        return new NicknameValidationResultDto(true, null);
    }

    public static NicknameValidationResultDto fail(String reason) {
        return new NicknameValidationResultDto(false, reason);
    }
}
