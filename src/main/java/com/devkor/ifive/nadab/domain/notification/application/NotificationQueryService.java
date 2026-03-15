package com.devkor.ifive.nadab.domain.notification.application;

import com.devkor.ifive.nadab.domain.notification.core.entity.Notification;
import com.devkor.ifive.nadab.domain.notification.core.repository.NotificationRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림 Query 서비스
 * - 알림 목록 조회 (커서 기반 페이지네이션)
 * - 미읽음 개수 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private static final int PAGE_SIZE = 20;

    public record PageResult(
        List<Notification> content,
        Long nextCursor,
        boolean hasNext
    ) {}

    /**
     * 사용자 알림 목록 조회 (커서 기반 페이지네이션, 20개씩)
     */
    public PageResult getUserNotifications(Long userId, Long cursor) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 21개 조회 (다음 페이지 존재 여부 판단용)
        List<Notification> notifications = notificationRepository.findByUserWithCursor(
            user, cursor, PAGE_SIZE + 1
        );

        // 21개가 왔으면 다음 페이지 있음
        boolean hasNext = notifications.size() > PAGE_SIZE;

        // 20개만 반환
        List<Notification> content = hasNext
            ? notifications.subList(0, PAGE_SIZE)
            : notifications;

        // 다음 커서는 마지막 알림의 ID
        Long nextCursor = hasNext && !content.isEmpty()
            ? content.get(content.size() - 1).getId()
            : null;

        return new PageResult(content, nextCursor, hasNext);
    }

    /**
     * 미읽음 알림 개수 조회
     */
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        return notificationRepository.countUnreadByUser(user);
    }
}