package com.devkor.ifive.nadab.global.core.notification.message;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Dev/Prod 환경용 알림 메시지 로더
 */
@Component
@Profile({"dev", "prod"})
@Slf4j
public class SecretNotificationMessageLoader implements NotificationMessageLoader {

    @Value("${NOTIFICATION_MESSAGES}")
    private String messagesYaml;

    @Override
    public String loadMessages() {
        if (messagesYaml == null || messagesYaml.isBlank()) {
            log.error("환경 변수 NOTIFICATION_MESSAGES가 비어있습니다.");
            throw new BadRequestException(ErrorCode.NOTIFICATION_MESSAGES_ENV_VAR_NOT_SET);
        }

        log.debug("알림 메시지 템플릿 로드 완료 (환경 변수)");
        return messagesYaml;
    }
}