package com.devkor.ifive.nadab.global.core.notification.message;

/**
 * 알림 메시지 템플릿 로더
 * - Local: YAML 파일에서 로드
 * - Secret: 환경 변수에서 로드
 */
public interface NotificationMessageLoader {

    /**
     * 알림 메시지 템플릿을 YAML 문자열로 반환
     */
    String loadMessages();
}