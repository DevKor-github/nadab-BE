package com.devkor.ifive.nadab.global.core.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseEntity {

    // 200 OK - data 있음
    public static <T> ResponseEntity<ApiResponseDto<T>> ok(T data) {
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity
                .status(status)
                .body(ApiResponseDto.success(status.value(), status.getReasonPhrase(), data));
    }

    // 200 OK - data 없음
    public static <T> ResponseEntity<ApiResponseDto<T>> ok(String message) {
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity
                .status(status)
                .body(ApiResponseDto.success(status.value(), message));
    }

    // 201 Created
    public static <T> ResponseEntity<ApiResponseDto<T>> created(T data) {
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity
                .status(status)
                .body(ApiResponseDto.success(status.value(), status.getReasonPhrase(), data));
    }

    // 204 No Content (data 없음)
    public static <T> ResponseEntity<ApiResponseDto<T>> noContent() {
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity
                .status(status)
                .body(ApiResponseDto.success(status.value(), status.getReasonPhrase()));
    }

    // TODO: 모든 에러 응답 개선 후 아래 실패 응답 2가지 지우기

    // 에러 응답 - data 있음
    public static <T> ResponseEntity<ApiResponseDto<T>> error(HttpStatus httpStatus, String message, T data) {
        return ResponseEntity
                .status(httpStatus)
                .body(ApiResponseDto.error(httpStatus.value(), message, data));
    }

    // 에러 응답 - data 없음
    public static <T> ResponseEntity<ApiResponseDto<T>> error(HttpStatus httpStatus, String message) {
        return ResponseEntity
                .status(httpStatus)
                .body(ApiResponseDto.error(httpStatus.value(), message));
    }

    // 에러 응답 (ErrorCode) - data 있음
    public static <T> ResponseEntity<ApiErrorResponseDto<T>> error(ErrorCode errorCode, T data) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiErrorResponseDto.error(
                        errorCode.getHttpStatus().value(),
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        data));
    }

    // 에러 응답 (ErrorCode) - data 없음
    public static <T> ResponseEntity<ApiErrorResponseDto<T>> error(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiErrorResponseDto.error(
                        errorCode.getHttpStatus().value(),
                        errorCode.getCode(),
                        errorCode.getMessage()));
    }

    // 에러 응답 (ErrorCode + 커스텀 메시지) - code는 ErrorCode, message는 커스텀
    public static <T> ResponseEntity<ApiErrorResponseDto<T>> error(ErrorCode errorCode, String customMessage) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiErrorResponseDto.error(
                        errorCode.getHttpStatus().value(),
                        errorCode.getCode(),
                        customMessage));
    }
}
