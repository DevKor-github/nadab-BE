package com.devkor.ifive.nadab.global.core.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(name = "ApiResponseDto", description = "공통 API 성공 응답 형식")
public class ApiResponseDto<T> {

    @Schema(description = "HTTP 상태 코드", example = "200")
    private final int status;

    @Schema(description = "응답 메시지", example = "OK")
    private final String message;

    @Schema(description = "응답 데이터")
    private final T data;

    // 성공 응답 (data 있는 경우)
    public static <T> ApiResponseDto<T> success(int status, String message, T data) {
        return ApiResponseDto.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    // 성공 응답 (data 없는 경우)
    public static <T> ApiResponseDto<T> success(int status, String message) {
        return ApiResponseDto.<T>builder()
                .status(status)
                .message(message)
                .data(null)
                .build();
    }
}
