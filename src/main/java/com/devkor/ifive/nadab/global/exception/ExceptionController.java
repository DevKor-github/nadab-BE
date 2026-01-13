package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.domain.auth.api.dto.response.WithdrawnInfoResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.CompletedCountResponse;
import com.devkor.ifive.nadab.global.core.response.ApiErrorResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.exception.report.MonthlyReportNotEligibleException;
import com.devkor.ifive.nadab.global.exception.report.WeeklyReportNotEligibleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleBadRequestException(BadRequestException ex) {
        log.warn("BadRequestException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ex.getErrorCode());
    }

    @ExceptionHandler(WithdrawnException.class)
    public ResponseEntity<ApiErrorResponseDto<WithdrawnInfoResponse>> handleWithdrawnException(WithdrawnException ex) {
        log.warn("WithdrawnException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ex.getErrorCode(), ex.getWithdrawnInfo());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("MethodArgumentNotValidException: {}", ex.getMessage(), ex);

        // 모든 validation 에러 메시지를 쉼표로 합침
        String validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        if (validationErrors.isEmpty()) {
            return ApiResponseEntity.error(ErrorCode.VALIDATION_FAILED);
        }

        // ErrorCode의 code는 사용하되, message는 validation 에러들로
        return ApiResponseEntity.error(ErrorCode.VALIDATION_FAILED, validationErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("MethodArgumentTypeMismatchException: {}", ex.getMessage(), ex);

        String parameterName = ex.getName();
        String errorMessage = String.format("%s 형식이 올바르지 않습니다. 올바른 형식으로 입력해주세요.", parameterName);

        return ApiResponseEntity.error(ErrorCode.VALIDATION_FAILED, errorMessage);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("UnauthorizedException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ex.getErrorCode());
    }

    @ExceptionHandler(OAuth2Exception.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleOAuth2Exception(OAuth2Exception ex) {
        // 로그는 OAuth2Client, SocialAuthService에서 이미 남김 (상세 정보 포함)
        return ApiResponseEntity.error(ex.getErrorCode());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleForbiddenException(ForbiddenException ex) {
        log.warn("ForbiddenException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ex.getErrorCode());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleNotFoundException(NotFoundException ex) {
        log.warn("NotFoundException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ex.getErrorCode());
    }

    @ExceptionHandler(NoSuchKeyException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleNoSuchKeyException(NoSuchKeyException ex) {
        log.warn("NoSuchKeyException: {}", ex.getMessage(), ex);
        // S3에서 객체를 찾을 수 없을 때 404 NOT_FOUND 응답
        return ApiResponseEntity.error(ErrorCode.FILE_STORAGE_NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleConflictException(ConflictException ex) {
        log.warn("ConflictException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ex.getErrorCode());
    }

    @ExceptionHandler(AiServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleAiServiceUnavailableException(AiServiceUnavailableException ex) {
        log.warn("AI Service Unavailable: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ex.getErrorCode());
    }

    @ExceptionHandler(AiResponseParseException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleAiResponseParseException(AiResponseParseException ex) {
        log.warn("AI Response Parse Error: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ex.getErrorCode());
    }

    @ExceptionHandler(NotEnoughCrystalException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleNotEnoughCrystalException(NotEnoughCrystalException ex) {
        log.warn("NotEnoughCrystalException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ex.getErrorCode());
    }

    @ExceptionHandler(WeeklyReportNotEligibleException.class)
    public ResponseEntity<ApiErrorResponseDto<CompletedCountResponse>> handleWeeklyReportNotEligibleException(WeeklyReportNotEligibleException ex) {
        log.warn("WeeklyReportNotEligibleException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ex.getErrorCode(), ex.getCompletedCountResponse());
    }

    @ExceptionHandler(MonthlyReportNotEligibleException.class)
    public ResponseEntity<ApiErrorResponseDto<CompletedCountResponse>> handleMonthlyReportNotEligibleException(MonthlyReportNotEligibleException ex) {
        log.warn("MonthlyReportNotEligibleException: {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ex.getErrorCode(), ex.getCompletedCountResponse());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponseDto<Void>> handleRuntimeException(RuntimeException ex) {
        if (ex instanceof AuthenticationException || ex instanceof AccessDeniedException) {
            throw ex; // 다시 던져서 Security FilterChain이 처리하게
        }

        log.error("Internal Server Error (RuntimeException): {}", ex.getMessage(), ex);
        return ApiResponseEntity.error(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
