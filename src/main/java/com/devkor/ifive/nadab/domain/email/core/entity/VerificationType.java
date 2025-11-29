package com.devkor.ifive.nadab.domain.email.core.entity;

/**
 * 이메일 인증 타입
 * - 회원가입 시 이메일 인증
 * - 비밀번호 찾기나 비밀번호 변경 시 이메일 인증
 */
public enum VerificationType {
    SIGNUP,
    PASSWORD_RESET
}