package com.devkor.ifive.nadab.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에러 응답")
public record ErrorResponse(
        @Schema(description = "HTTP 상태 코드", example = "400")
        int status,

        @Schema(description = "에러 메시지", example = "잘못된 요청입니다")
        String message
) {
}
