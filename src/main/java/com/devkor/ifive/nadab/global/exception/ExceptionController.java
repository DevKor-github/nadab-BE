package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBadRequestException(BadRequestException ex) {
        log.warn("BadRequestException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleForbiddenException(ForbiddenException ex) {
        log.warn("ForbiddenException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleNotFoundException(NotFoundException ex) {
        log.warn("NotFoundException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("UnauthorizedException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(OAuth2Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleOAuth2Exception(OAuth2Exception ex) {
        log.warn("OAuth2Exception: {}", ex.getMessage(), ex);

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode());
        return ApiResponseEntity.error(status, ex.getMessage());
    }

    @ExceptionHandler(NoSuchKeyException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleNoSuchKeyException(NoSuchKeyException ex) {
        log.warn("NoSuchKeyException: {}", ex.getMessage(), ex);
        // S3에서 객체를 찾을 수 없을 때 404 NOT_FOUND 응답
        return ApiResponseEntity.error(HttpStatus.NOT_FOUND, "S3에서 요청된 파일을 찾을 수 없습니다.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Internal Server Error (RuntimeException): {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 예상치 못한 오류가 발생했습니다.");
    }
}
