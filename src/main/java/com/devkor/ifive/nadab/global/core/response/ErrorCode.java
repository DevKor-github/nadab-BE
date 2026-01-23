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
    FILE_STORAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "스토리지에서 해당 파일을 찾을 수 없습니다"), // S3 등 스토리지에 실제 파일 객체가 없는 경우

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
    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "약관을 찾을 수 없습니다"),

    // ==================== USER (유저) ====================
    // 400 Bad Request
    USER_UPDATE_NO_DATA(HttpStatus.BAD_REQUEST, "수정할 프로필 정보가 없습니다"),
    USER_INTEREST_CODE_INVALID(HttpStatus.BAD_REQUEST, "지원하지 않는 관심 주제 코드입니다"),

    // 404 Not Found
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "관심 주제를 찾을 수 없습니다"),
    USER_INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 관심 주제를 찾을 수 없습니다"),

    // ==================== IMAGE (이미지) ====================
    // 400 Bad Request
    IMAGE_UNSUPPORTED_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 타입입니다"),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지 크기가 제한을 초과했습니다 (최대 5MB)"),
    IMAGE_METADATA_INVALID(HttpStatus.BAD_REQUEST, "파일 메타데이터를 읽을 수 없습니다. 다시 시도해주세요."),

    // ==================== QUESTION (질문) ====================
    // 400 Bad Request
    DAILY_QUESTION_MISMATCH(HttpStatus.BAD_REQUEST, "요청한 질문이 사용자에게 할당된 오늘의 질문과 일치하지 않습니다"),

    // 404 Not Found
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다"),
    QUESTION_NOT_FOUND_FOR_CONDITION(HttpStatus.NOT_FOUND, "조건에 맞는 질문을 찾을 수 없습니다"),
    QUESTION_NO_ALTERNATIVE(HttpStatus.NOT_FOUND, "리롤 가능한 질문이 없습니다"),
    DAILY_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "오늘의 질문이 아직 생성되지 않았습니다"),

    // 409 Conflict
    QUESTION_REROLL_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "오늘의 질문은 하루에 한 번만 새로 받을 수 있습니다"),
    QUESTION_ALREADY_ANSWERED(HttpStatus.CONFLICT, "오늘의 질문에 이미 답변을 작성한 후에는 질문을 새로 받을 수 없습니다"),

    // ==================== WALLET (지갑) ====================
    // 400 Bad Request
    WALLET_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "보유한 크리스탈이 부족합니다"),

    //404 Not Found
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 지갑을 찾을 수 없습니다"),

    // ==================== CRYSTAL_LOG (크리스탈 로그) ====================
    // 404 Not Found
    CRYSTAL_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "크리스탈 로그를 찾을 수 없습니다"),

    // ==================== DAILY_REPORT (일간 리포트) ====================
    // 404 Not Found
    DAILY_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "일간 리포트를 찾을 수 없습니다"),

    // 409 Conflict - 일간
    DAILY_REPORT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 작성된 일간 리포트가 존재합니다"),

    // ==================== WEEKLY_REPORT (주간 리포트) ====================
    // 400 Bad Request
    WEEKLY_REPORT_NOT_ENOUGH_REPORTS(HttpStatus.BAD_REQUEST, "주간 리포트 작성 자격이 없습니다. (지난 주 4회 이상 완료 필요)"),

    // 404 Not Found
    WEEKLY_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "주간 리포트를 찾을 수 없습니다"),
    WEEKLY_REPORT_NOT_COMPLETED(HttpStatus.NOT_FOUND, "해당 주간 리포트가 아직 생성 완료되지 않았습니다"),

    // 409 Conflict
    WEEKLY_REPORT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 작성된 주간 리포트가 존재합니다"),
    WEEKLY_REPORT_IN_PROGRESS(HttpStatus.CONFLICT, "현재 주간 리포트를 생성 중입니다"),

    // ==================== MONTHLY_REPORT (월간 리포트) ====================
    // 400 Bad Request
    MONTHLY_REPORT_NOT_ENOUGH_REPORTS(HttpStatus.BAD_REQUEST, "월간 리포트 작성 자격이 없습니다. (지난 달 15회 이상 완료 필요)"),

    // 404 Not Found
    MONTHLY_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "월간 리포트를 찾을 수 없습니다"),
    MONTHLY_REPORT_NOT_COMPLETED(HttpStatus.NOT_FOUND, "해당 월간 리포트가 아직 생성 완료되지 않았습니다"),

    // 409 Conflict
    MONTHLY_REPORT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 작성된 월간 리포트가 존재합니다"),
    MONTHLY_REPORT_IN_PROGRESS(HttpStatus.CONFLICT, "현재 월간 리포트를 생성 중입니다"),

    // ==================== AI (인공지능) ====================
    // 502 Bad Gateway
    AI_RESPONSE_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "AI 응답 형식을 해석할 수 없습니다"),
    AI_RESPONSE_FORMAT_INVALID(HttpStatus.BAD_GATEWAY, "AI 응답 JSON의 필수 필드가 비어있습니다"),

    // 503 Service Unavailable
    AI_NO_RESPONSE(HttpStatus.SERVICE_UNAVAILABLE, "AI 서비스로부터 응답을 받지 못했습니다"),

    // ==================== EMOTION (감정) ====================
    // 404 Not Found
    EMOTION_NOT_FOUND(HttpStatus.NOT_FOUND, "감정 정보를 찾을 수 없습니다"),

    // ==================== ANSWER (답변) ====================
    // 403 Forbidden
    ANSWER_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 답변만 조회할 수 있습니다"),

    // 404 Not Found
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "작성된 답변 내역을 찾을 수 없습니다"),

    // ==================== SEARCH (검색) ====================
    // 403 Forbidden
    SEARCH_HISTORY_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 검색어만 삭제할 수 있습니다"),

    // 404 Not Found
    SEARCH_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "검색어를 찾을 수 없습니다"),

    // ==================== PROMPT (프롬프트) ====================
    // 400 Bad Request
    PROMPT_DAILY_FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "일간 리포트 프롬프트 파일이 존재하지 않습니다"),
    PROMPT_DAILY_FILE_READ_FAILED(HttpStatus.BAD_REQUEST, "로컬 일간 리포트 프롬프트 파일을 읽을 수 없습니다"),
    PROMPT_DAILY_ENV_VAR_NOT_SET(HttpStatus.BAD_REQUEST, "INSIGHT_PROMPT 환경 변수에 프롬프트가 설정되어 있지 않습니다"),

    PROMPT_WEEKLY_FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "주간 리포트 프롬프트 파일이 존재하지 않습니다"),
    PROMPT_WEEKLY_FILE_READ_FAILED(HttpStatus.BAD_REQUEST, "로컬 주간 리포트 프롬프트 파일을 읽을 수 없습니다"),
    PROMPT_WEEKLY_ENV_VAR_NOT_SET(HttpStatus.BAD_REQUEST, "WEEKLY_PROMPT 환경 변수에 프롬프트가 설정되어 있지 않습니다"),

    PROMPT_MONTHLY_FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "월간 리포트 프롬프트 파일이 존재하지 않습니다"),
    PROMPT_MONTHLY_FILE_READ_FAILED(HttpStatus.BAD_REQUEST, "로컬 월간 리포트 프롬프트 파일을 읽을 수 없습니다"),
    PROMPT_MONTHLY_ENV_VAR_NOT_SET(HttpStatus.BAD_REQUEST, "MONTHLY_PROMPT 환경 변수에 프롬프트가 설정되어 있지 않습니다"),

    // ==================== NICKNAME (닉네임) ====================
    // 400 Bad Request
    NICKNAME_CHANGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "닉네임 변경 가능 횟수를 초과했습니다 (14일 내 최대 2회)"),
    NICKNAME_ALREADY_TAKEN(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다"),

    // ==================== FRIEND (친구) ====================
    // 400 Bad Request
    FRIENDSHIP_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "이미 처리된 친구 요청입니다"),
    FRIENDSHIP_USER_NOT_INVOLVED(HttpStatus.BAD_REQUEST, "해당 유저는 이 친구 관계에 포함되지 않습니다");

    private final HttpStatus httpStatus;
    private final String message;

    public String getCode() {
        return this.name();
    }
}