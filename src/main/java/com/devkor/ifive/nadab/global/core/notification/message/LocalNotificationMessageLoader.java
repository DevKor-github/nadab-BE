package com.devkor.ifive.nadab.global.core.notification.message;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Profile("local")
@Slf4j
public class LocalNotificationMessageLoader implements NotificationMessageLoader {

    private static final String TEMPLATE_PATH = "secret/notification-template.yml";

    @Override
    public String loadMessages() {
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);

            if (!resource.exists()) {
                log.error("알림 메시지 템플릿 파일이 존재하지 않습니다: {}", TEMPLATE_PATH);
                throw new BadRequestException(ErrorCode.NOTIFICATION_MESSAGE_FILE_NOT_FOUND);
            }

            byte[] bytes = resource.getContentAsByteArray();
            String yaml = new String(bytes, StandardCharsets.UTF_8);

            log.debug("알림 메시지 템플릿 로드 완료: {}", TEMPLATE_PATH);
            return yaml;

        } catch (IOException e) {
            log.error("알림 메시지 템플릿 파일 읽기 실패: {}", TEMPLATE_PATH, e);
            throw new BadRequestException(ErrorCode.NOTIFICATION_MESSAGE_FILE_READ_FAILED);
        }
    }
}