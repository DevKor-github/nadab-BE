package com.devkor.ifive.nadab.global.core.notification.message;

import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationType;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 알림 메시지 생성 팩토리
 * - 템플릿 선택, 파라미터 치환, 랜덤/마일스톤 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationMessageFactory {

    private final NotificationMessageLoader loader;
    private Map<String, Object> messageTemplates;

    @PostConstruct
    public void init() {
        String yaml = loader.loadMessages();

        Yaml yamlParser = new Yaml();
        messageTemplates = yamlParser.load(yaml);

        log.debug("알림 메시지 템플릿 파싱 완료: {} 타입", messageTemplates.size());

        // 모든 NotificationType 템플릿 검증
        validateAllTemplates();
        log.debug("알림 메시지 템플릿 검증 완료");
    }

    /**
     * 모든 NotificationType에 대한 템플릿 검증
     * - 앱 시작 시 모든 템플릿이 올바른 구조인지 확인
     * - 누락된 타입, 필드, 구조 오류 등을 즉시 감지
     */
    private void validateAllTemplates() {
        Map<String, String> dummyParams = Map.of(
            "nickname", "테스트",
            "senderName", "테스트",
            "categoryName", "테스트",
            "daysInactive", "5",
            "milestone", "30"
        );

        for (NotificationType type : NotificationType.values()) {
            try {
                // 각 타입에 대해 메시지 생성 시도
                createMessage(type, dummyParams);
            } catch (Exception e) {
                log.error("알림 메시지 템플릿 검증 실패: {}", type, e);
                throw new IllegalStateException(
                    "알림 메시지 템플릿 검증 실패: " + type + " - " + e.getMessage(),
                    e
                );
            }
        }
    }

    /**
     * 알림 메시지 생성
     */
    public NotificationContent createMessage(NotificationType type, Map<String, String> params) {
        // null 방어: null이면 빈 Map 사용
        Map<String, String> safeParams = params != null ? params : Map.of();

        Map<String, Object> template = getTemplate(type, safeParams);

        String title = renderTemplate((String) template.get("title"), safeParams);
        String body = renderTemplate((String) template.get("body"), safeParams);
        String inboxMessage = renderTemplate((String) template.get("inboxMessage"), safeParams);

        return new NotificationContent(title, body, inboxMessage);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getTemplate(NotificationType type, Map<String, String> params) {
        Object rawTemplate = messageTemplates.get(type.name());

        if (rawTemplate == null) {
            log.error("알림 메시지 템플릿을 찾을 수 없습니다: {}", type);
            throw new BadRequestException(ErrorCode.NOTIFICATION_MESSAGE_TEMPLATE_NOT_FOUND);
        }

        // 특수 케이스 처리
        return switch (type) {
            case DAILY_WRITE_REMINDER -> getRandomMessageFromList((List<Map<String, Object>>) rawTemplate);
            case INACTIVE_USER_REMINDER -> getRandomInactivityMessage((Map<String, Object>) rawTemplate, params);
            case TYPE_REPORT_AVAILABLE -> getMilestoneMessage((Map<String, Object>) rawTemplate, params);
            default -> (Map<String, Object>) rawTemplate;
        };
    }

    /**
     * 랜덤 메시지 선택 (배열 형태의 템플릿)
     * - DAILY_WRITE_REMINDER 등에서 사용
     */
    private Map<String, Object> getRandomMessageFromList(List<Map<String, Object>> messages) {
        if (messages == null || messages.isEmpty()) {
            log.error("랜덤 메시지 템플릿이 비어있습니다");
            throw new BadRequestException(ErrorCode.NOTIFICATION_MESSAGE_TEMPLATE_NOT_FOUND);
        }
        return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
    }

    /**
     * 미답변 알림 랜덤 메시지 선택
     * - 7일 미만: under7Days
     * - 7일 이상: over7Days
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getRandomInactivityMessage(
        Map<String, Object> template,
        Map<String, String> params
    ) {
        int daysInactive = Integer.parseInt(params.getOrDefault("daysInactive", "0"));
        String key = daysInactive >= 7 ? "over7Days" : "under7Days";

        List<Map<String, Object>> messages = (List<Map<String, Object>>) template.get(key);
        if (messages == null || messages.isEmpty()) {
            log.error("미답변 알림 메시지 템플릿을 찾을 수 없습니다: {}", key);
            throw new BadRequestException(ErrorCode.NOTIFICATION_MESSAGE_TEMPLATE_NOT_FOUND);
        }

        return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
    }

    /**
     * 유형 리포트 제작 가능 알림 마일스톤별 메시지
     * - 30개: milestone30
     * - 50개: milestone50
     * - 전체: milestoneComplete
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMilestoneMessage(
        Map<String, Object> template,
        Map<String, String> params
    ) {
        String milestone = params.getOrDefault("milestone", "30");
        String key = "milestone" + milestone;

        Map<String, Object> message = (Map<String, Object>) template.get(key);
        if (message == null) {
            log.error("유형 리포트 마일스톤 메시지 템플릿을 찾을 수 없습니다: {}", key);
            throw new BadRequestException(ErrorCode.NOTIFICATION_MESSAGE_TEMPLATE_NOT_FOUND);
        }

        return message;
    }

    /**
     * 템플릿 문자열의 파라미터 치환
     */
    private String renderTemplate(String template, Map<String, String> params) {
        if (template == null) {
            return "";
        }

        // null이거나 비어있으면 원본 반환 (이중 방어)
        if (params == null || params.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return result;
    }
}