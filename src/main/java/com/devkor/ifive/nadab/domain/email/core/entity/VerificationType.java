package com.devkor.ifive.nadab.domain.email.core.entity;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;

/**
 * 이메일 인증 타입
 * - 회원가입 시 이메일 인증
 * - 비밀번호 찾기나 비밀번호 변경 시 이메일 인증
 */
public enum VerificationType {
    SIGNUP,
    PASSWORD_RESET;

    public static VerificationType fromString(String type) {
        for (VerificationType vt : values()) {
            if (vt.name().equalsIgnoreCase(type)) {
                return vt;
            }
        }
        throw new BadRequestException(ErrorCode.EMAIL_INVALID_VERIFICATION_TYPE);
    }
}