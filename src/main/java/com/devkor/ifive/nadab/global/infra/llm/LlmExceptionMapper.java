package com.devkor.ifive.nadab.global.infra.llm;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public final class LlmExceptionMapper {

    private LlmExceptionMapper() {
    }

    public static AiServiceUnavailableException toUnavailable(ErrorCode errorCode, Exception exception) {
        ExternalError externalError = extractExternalError(exception);
        return new AiServiceUnavailableException(
                errorCode,
                externalError == null ? null : externalError.httpStatus(),
                externalError == null ? null : externalError.externalErrorCode(),
                exception
        );
    }

    private static ExternalError extractExternalError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof WebClientResponseException webClientException) {
                int status = webClientException.getStatusCode().value();
                return new ExternalError(status, toExternalErrorCode(status));
            }
            if (current instanceof HttpStatusCodeException httpStatusException) {
                HttpStatusCode statusCode = httpStatusException.getStatusCode();
                int status = statusCode.value();
                return new ExternalError(status, toExternalErrorCode(status));
            }
            if (current instanceof RestClientResponseException restClientException) {
                int status = restClientException.getStatusCode().value();
                return new ExternalError(status, toExternalErrorCode(status));
            }
            current = current.getCause();
        }
        return null;
    }

    private static String toExternalErrorCode(int httpStatus) {
        return "HTTP_" + httpStatus;
    }

    private record ExternalError(
            Integer httpStatus,
            String externalErrorCode
    ) {
    }
}
