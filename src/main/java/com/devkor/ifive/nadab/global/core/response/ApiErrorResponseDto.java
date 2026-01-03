package com.devkor.ifive.nadab.global.core.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(name = "ApiErrorResponseDto", description = "공통 API 에러 응답 형식")
public class ApiErrorResponseDto<T> {

    @Schema(description = "HTTP 상태 코드", example = "400")
    private final int status;

    @Schema(description = "에러 코드", example = "AUTH_EMAIL_VERIFICATION_REQUIRED")
    private final String code;

    @Schema(description = "응답 메시지", example = "이메일 인증을 먼저 완료해주세요")
    private final String message;

    @Schema(description = "응답 데이터")
    private final T data;

    // 실패 응답 (data 있는 경우)
    public static <T> ApiErrorResponseDto<T> error(int status, String code, String message, T data) {
        return ApiErrorResponseDto.<T>builder()
                .status(status)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    // 실패 응답 (data 없는 경우)
    public static <T> ApiErrorResponseDto<T> error(int status, String code, String message) {
        return ApiErrorResponseDto.<T>builder()
                .status(status)
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}