package com.devkor.ifive.nadab.global.core.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ==================== 공통 ====================
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),

    // ==================== AUTH (인증) ====================
    // 400 Bad Request
    AUTH_EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다"),
    AUTH_PASSWORD_REUSE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "이전 비밀번호와 동일한 비밀번호는 사용할 수 없습니다"),
    AUTH_SOCIAL_ACCOUNT_PASSWORD_CHANGE_FORBIDDEN(HttpStatus.BAD_REQUEST, "소셜 로그인 계정은 비밀번호를 변경할 수 없습니다"),
    AUTH_WITHDRAWN_ACCOUNT_RESTORE_REQUIRED(HttpStatus.BAD_REQUEST, "탈퇴한 계정입니다. 원래 로그인 방식으로 복구를 진행해주세요."),
    AUTH_ACCOUNT_WITHDRAWN(HttpStatus.BAD_REQUEST, "탈퇴한 계정입니다. 계정 복구를 진행해주세요"),
    AUTH_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "이미 탈퇴한 계정입니다"),
    AUTH_NOT_WITHDRAWN(HttpStatus.BAD_REQUEST, "탈퇴하지 않은 계정입니다"),
    AUTH_RESTORE_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "복구 가능 기간(14일)이 지났습니다"),
    AUTH_SOCIAL_ACCOUNT_RESTORE_FORBIDDEN(HttpStatus.BAD_REQUEST, "소셜 로그인 계정은 일반 계정 복구를 사용할 수 없습니다"),
    AUTH_UNSUPPORTED_OAUTH2_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth2 제공자입니다"),

    // 401 Unauthorized
    AUTH_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다"),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 Refresh Token입니다"),
    AUTH_REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh Token이 없습니다"),
    AUTH_INVALID_STATE(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 state입니다"),
    AUTH_OAUTH2_TOKEN_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인 토큰 발급에 실패했습니다"),
    AUTH_OAUTH2_USERINFO_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인 사용자 정보 조회에 실패했습니다"),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다"),
    AUTH_TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "토큰 서명 검증에 실패했습니다"),
    AUTH_TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "토큰 형식이 올바르지 않습니다"),
    AUTH_TOKEN_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "토큰 검증에 실패했습니다"),
    AUTH_TOKEN_USERID_INVALID(HttpStatus.UNAUTHORIZED, "토큰의 유저 ID 형식이 올바르지 않습니다"),
    AUTH_TOKEN_ROLES_MISSING(HttpStatus.UNAUTHORIZED, "토큰에 권한 정보가 없습니다"),

    // 403 Forbidden
    AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),

    // 404 Not Found
    AUTH_DUMMY_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "더미 유저(ID: 11111)가 존재하지 않습니다"),

    // 409 Conflict
    AUTH_EMAIL_ALREADY_REGISTERED_WITH_DIFFERENT_METHOD(HttpStatus.CONFLICT, "이미 가입된 이메일입니다. 다른 로그인 방식을 사용해주세요."),

    // ==================== EMAIL (이메일 인증) ====================
    // 400 Bad Request
    EMAIL_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "이미 인증이 완료되었습니다"),
    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다. 재발송을 요청해주세요."),
    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다"),
    EMAIL_INVALID_VERIFICATION_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 인증 타입입니다"),
    EMAIL_SOCIAL_ACCOUNT_PASSWORD_RESET_FORBIDDEN(HttpStatus.BAD_REQUEST, "소셜 로그인 계정은 비밀번호 찾기를 사용할 수 없습니다"),
    EMAIL_WITHDRAWN_ACCOUNT_PASSWORD_RESET_FORBIDDEN(HttpStatus.BAD_REQUEST, "탈퇴한 계정입니다. 계정 복구는 비밀번호를 기억하는 경우에만 가능합니다."),
    EMAIL_WITHDRAWN_ACCOUNT_SIGNUP_FORBIDDEN(HttpStatus.BAD_REQUEST, "탈퇴한 계정입니다. 로그인 후 계정 복구를 진행해주세요."),

    // 404 Not Found
    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "인증 요청을 찾을 수 없습니다"),
    EMAIL_NOT_REGISTERED(HttpStatus.NOT_FOUND, "등록되지 않은 이메일입니다"),

    // ==================== TERMS (약관) ====================
    // 400 Bad Request
    TERMS_SERVICE_AGREEMENT_REQUIRED(HttpStatus.BAD_REQUEST, "서비스 이용약관에 동의해야 합니다"),
    TERMS_PRIVACY_POLICY_REQUIRED(HttpStatus.BAD_REQUEST, "개인정보 처리방침에 동의해야 합니다"),
    TERMS_AGE_VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "만 14세 이상 확인에 동의해야 합니다"),

    // 404 Not Found
    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "약관을 찾을 수 없습니다");

    private final HttpStatus httpStatus;
    private final String message;

    public String getCode() {
        return this.name();
    }
}