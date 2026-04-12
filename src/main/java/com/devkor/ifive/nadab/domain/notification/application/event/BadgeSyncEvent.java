package com.devkor.ifive.nadab.domain.notification.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 뱃지 동기화 이벤트
 * - 알림 읽음/삭제 후 다른 기기로 뱃지 개수를 동기화할 때 사용
 * - 트랜잭션 커밋 후 처리 (AFTER_COMMIT)
 */
@Getter
@RequiredArgsConstructor
public class BadgeSyncEvent {

    private final Long userId;
}