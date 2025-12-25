package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.domain.auth.api.dto.response.WithdrawnInfoResponse;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBadRequestException(BadRequestException ex) {
        log.warn("BadRequestException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(WithdrawnException.class)
    public ResponseEntity<ApiResponseDto<WithdrawnInfoResponse>> handleWithdrawnException(WithdrawnException ex) {
        log.warn("WithdrawnException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getWithdrawnInfo());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("MethodArgumentNotValidException: {}", ex.getMessage(), ex);

        // 모든 validation 에러 메시지를 쉼표로 합침
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        if (message.isEmpty()) {
            message = "입력값이 올바르지 않습니다";
        }

        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, message);
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

    @ExceptionHandler(NoSuchKeyException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleNoSuchKeyException(NoSuchKeyException ex) {
        log.warn("NoSuchKeyException: {}", ex.getMessage(), ex);
        // S3에서 객체를 찾을 수 없을 때 404 NOT_FOUND 응답
        return ApiResponseEntity.error(HttpStatus.NOT_FOUND, "S3에서 요청된 파일을 찾을 수 없습니다.");
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleConflictException(ConflictException ex) {
        log.warn("ConflictException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AiServiceUnavailableException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAiServiceUnavailableException(AiServiceUnavailableException ex) {
        log.warn("AI Service Unavailable: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(AiResponseParseException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAiResponseParseException(AiResponseParseException ex) {
        log.warn("AI Response Parse Error: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(NotEnoughCrystalException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleNotEnoughCrystalException(NotEnoughCrystalException ex) {
        log.warn("NotEnoughCrystalException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleRuntimeException(RuntimeException ex) {
        if (ex instanceof AuthenticationException || ex instanceof AccessDeniedException) {
            throw ex; // 다시 던져서 Security FilterChain이 처리하게
        }

        log.error("Internal Server Error (RuntimeException): {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 예상치 못한 오류가 발생했습니다.");
    }
}
