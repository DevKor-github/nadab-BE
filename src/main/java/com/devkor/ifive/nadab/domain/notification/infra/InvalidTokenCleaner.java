package com.devkor.ifive.nadab.domain.notification.infra;

import com.devkor.ifive.nadab.domain.notification.core.repository.UserDeviceRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Invalid FCM 토큰 정리 Component
 * - 호출자의 트랜잭션에 참여
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InvalidTokenCleaner {

    private final UserDeviceRepository userDeviceRepository;

    /**
     * Invalid Token 자동 정리
     * - DB에서 Invalid Token 삭제
     * - 호출자의 트랜잭션에 참여 (FCM 발송 처리의 일부)
     */
    public int cleanupInvalidTokens(List<String> fcmTokens, BatchResponse response) {
        List<String> invalidTokens = collectInvalidTokens(fcmTokens, response);

        if (invalidTokens.isEmpty()) {
            return 0;
        }

        try {
            int deletedCount = userDeviceRepository.deleteByFcmTokenIn(invalidTokens);
            log.debug("Invalid tokens cleanup completed: deleted={}", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.warn("Invalid FCM tokens cleanup failed ({} tokens): {}",
                invalidTokens.size(), e.getMessage());
            return 0;
        }
    }

    /**
     * FCM 응답에서 Invalid Token 수집
     */
    private List<String> collectInvalidTokens(List<String> fcmTokens, BatchResponse response) {
        List<String> invalidTokens = new ArrayList<>();
        List<SendResponse> responses = response.getResponses();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (!sendResponse.isSuccessful()) {
                Exception exception = sendResponse.getException();
                if (exception instanceof FirebaseMessagingException) {
                    FirebaseMessagingException fme = (FirebaseMessagingException) exception;
                    if (isInvalidTokenError(fme.getMessagingErrorCode())) {
                        String invalidToken = fcmTokens.get(i);
                        invalidTokens.add(invalidToken);
                        log.debug("Invalid token detected: token={}", maskToken(invalidToken));
                    }
                }
            }
        }

        return invalidTokens;
    }

    /**
     * FCM 에러가 Invalid Token인지 확인
     */
    private boolean isInvalidTokenError(MessagingErrorCode errorCode) {
        if (errorCode == null) {
            return false;
        }
        return errorCode == MessagingErrorCode.UNREGISTERED
            || errorCode == MessagingErrorCode.INVALID_ARGUMENT
            || errorCode == MessagingErrorCode.SENDER_ID_MISMATCH;
    }

    /**
     * 토큰 마스킹 (로그용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 20) {
            return "***";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }
}