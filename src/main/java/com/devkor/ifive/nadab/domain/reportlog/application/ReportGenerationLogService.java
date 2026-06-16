package com.devkor.ifive.nadab.domain.reportlog.application;

import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLog;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationStep;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import com.devkor.ifive.nadab.domain.reportlog.core.repository.ReportGenerationLogRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BusinessException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportGenerationLogService {

    private final ReportGenerationLogRepository reportGenerationLogRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReportGenerationLog start(
            Long userId,
            ReportGenerationType reportType,
            Long reportId,
            ReportGenerationStep step,
            LlmProvider llmProvider,
            String llmModel,
            JsonNode metadata
    ) {
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);

        ReportGenerationLog log = ReportGenerationLog.start(
                user,
                reportType,
                reportId,
                step,
                llmProvider,
                llmModel,
                metadata
        );

        return reportGenerationLogRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void succeed(Long logId) {
        reportGenerationLogRepository.findById(logId)
                .ifPresent(ReportGenerationLog::succeed);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long logId, Exception exception) {
        Integer externalHttpStatus = extractExternalHttpStatus(exception);
        String externalErrorCode = extractExternalErrorCode(exception);
        fail(logId, exception, externalHttpStatus, externalErrorCode);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long logId, Exception exception, Integer httpStatus, String externalErrorCode) {
        reportGenerationLogRepository.findById(logId)
                .ifPresent(log -> {
                    ErrorCode errorCode = extractErrorCode(exception);
                    Integer resolvedHttpStatus = httpStatus != null ? httpStatus : extractHttpStatus(errorCode);

                    log.fail(
                            errorCode == null ? null : errorCode.getCode(),
                            exception == null ? null : cut(exception.getClass().getName(), 255),
                            resolvedHttpStatus,
                            externalErrorCode
                    );
                });
    }

    private ErrorCode extractErrorCode(Exception exception) {
        if (exception instanceof BusinessException businessException) {
            return businessException.getErrorCode();
        }
        return null;
    }

    private Integer extractHttpStatus(ErrorCode errorCode) {
        if (errorCode == null) {
            return null;
        }
        return errorCode.getHttpStatus().value();
    }

    private Integer extractExternalHttpStatus(Exception exception) {
        if (exception instanceof AiServiceUnavailableException aiException) {
            return aiException.getExternalHttpStatus();
        }
        return null;
    }

    private String extractExternalErrorCode(Exception exception) {
        if (exception instanceof AiServiceUnavailableException aiException) {
            return aiException.getExternalErrorCode();
        }
        return null;
    }

    private String cut(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
