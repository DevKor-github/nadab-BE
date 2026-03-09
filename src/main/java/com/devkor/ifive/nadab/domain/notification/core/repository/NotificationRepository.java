package com.devkor.ifive.nadab.domain.notification.core.repository;

import com.devkor.ifive.nadab.domain.notification.core.entity.Notification;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT n FROM Notification n JOIN FETCH n.user WHERE n.id = :id")
    Optional<Notification> findByIdWithUser(@Param("id") Long id);

    @Query("""
        select n from Notification n join fetch n.user
        where n.user = :user and n.deletedAt is null
        and (:cursor is null or n.id < :cursor)
        order by n.id desc
        limit :limit
        """)
    List<Notification> findByUserWithCursor(
        @Param("user") User user,
        @Param("cursor") Long cursor,
        @Param("limit") int limit
    );

    @Query("""
        select count(n) from Notification n
        where n.user = :user and n.isRead = false and n.deletedAt is null
        """)
    long countUnreadByUser(@Param("user") User user);

    @Query("""
        select n.user.id as userId, count(n) as unreadCount
        from Notification n
        where n.user.id in :userIds
        and n.isRead = false
        and n.deletedAt is null
        group by n.user.id
        """)
    List<UnreadCountProjection> countUnreadByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.isRead = true, n.readAt = CURRENT_TIMESTAMP
        where n.user = :user and n.isRead = false and n.deletedAt is null
        """)
    int markAllAsReadByUser(@Param("user") User user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.deletedAt = CURRENT_TIMESTAMP, n.updatedAt = CURRENT_TIMESTAMP
        where n.user = :user and n.deletedAt is null
        """)
    int softDeleteAllByUser(@Param("user") User user);

    @Query("""
        select n from Notification n join fetch n.user u
        where n.status = :status
          and u.deletedAt is null
        order by n.id asc
        limit :limit
        """)
    List<Notification> findByStatusOrderByIdAsc(
        @Param("status") NotificationStatus status,
        @Param("limit") int limit
    );

    @Query("""
        select n from Notification n join fetch n.user u
        where n.status = :status
        and n.retryCount = :retryCount
        and n.updatedAt < :threshold
        and u.deletedAt is null
        order by n.id asc
        limit :limit
        """)
    List<Notification> findByStatusAndRetryCountAndUpdatedAtBefore(
        @Param("status") NotificationStatus status,
        @Param("retryCount") int retryCount,
        @Param("threshold") OffsetDateTime threshold,
        @Param("limit") int limit
    );

    /**
     * EventListener: PENDING → SENDING & fcmSent = true
     * - 최초 알림 생성 후 즉시 발송
     * - WHERE 조건에 PENDING 상태 포함 → 중복 처리 방지
     * - 반환값 0 = 이미 PENDING이 아님 (처리 완료 또는 진행 중)
     * - 반환값 1 = 성공적으로 SENDING으로 변경
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.status = com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.SENDING,
            n.fcmSent = true,
            n.updatedAt = CURRENT_TIMESTAMP
        where n.id = :id
          and n.status = com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.PENDING
        """)
    int markAsSendingWithFcmFlag(@Param("id") Long id);

    /**
     * RetryScheduler: FAILED → SENDING & fcmSent = true
     * - FAILED 상태의 알림을 재시도할 때 사용
     * - fcmSent를 true로 낙관적 설정 (중복 발송 방지)
     * - retry_count 증가 (재시도 횟수 카운트)
     * - WHERE 조건에 FAILED 상태 포함 → 중복 처리 방지
     * - 반환값 0 = 이미 FAILED가 아님 (처리 완료 또는 진행 중)
     * - 반환값 1 = 성공적으로 SENDING으로 변경
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.status = com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.SENDING,
            n.fcmSent = true,
            n.retryCount = n.retryCount + 1,
            n.updatedAt = CURRENT_TIMESTAMP
        where n.id = :id
          and n.status = com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.FAILED
        """)
    int markFailedAsSendingWithFcmFlag(@Param("id") Long id);

    /**
     * EventListener/RetryScheduler: FCM 발송 실패 시 FAILED로 마킹
     * - 최초 즉시 발송 실패 시 사용 (retry_count 유지)
     * - 재시도 실패 시에도 사용 (retry_count는 재시도 시작 시 이미 증가됨)
     * - fcmSent를 false로 복구 (FCM 발송 안 됨)
     * - updated_at 업데이트 (Exponential Backoff 정확도)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.status = com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.FAILED,
            n.fcmSent = false,
            n.updatedAt = CURRENT_TIMESTAMP
        where n.id = :id
          and n.status = com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.SENDING
        """)
    int markAsFailedAndResetFcm(@Param("id") Long id);

    /**
     * EventListener/RetryScheduler: FCM 발송 성공 시 SENT로 변경
     * - SENDING → SENT 상태 변경
     * - WHERE 조건에 현재 상태 포함 → 중복 처리 방지
     * - 반환값 0 = 이미 다른 스레드가 처리함
     * - 반환값 1 = 현재 스레드가 처리함
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.status = :newStatus, n.updatedAt = CURRENT_TIMESTAMP
        where n.id = :id and n.status = :currentStatus
        """)
    int updateStatusConditionally(
        @Param("id") Long id,
        @Param("currentStatus") NotificationStatus currentStatus,
        @Param("newStatus") NotificationStatus newStatus
    );

    /**
     * RecoveryScheduler: FCM 발송 완료된 알림을 SENT로
     * - WHERE fcmSent=true 조건으로 중복 발송 완전 방지
     * - 반환값 0 = 이미 fcmSent가 아니거나 SENDING 아님
     * - 반환값 1 = 성공적으로 SENT로 변경
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.status = com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.SENT,
            n.updatedAt = CURRENT_TIMESTAMP
        where n.id = :id
          and n.status = com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.SENDING
          and n.fcmSent = true
        """)
    int markAsSentIfFcmSent(@Param("id") Long id);

    /**
     * RecoveryScheduler: SENDING 타임아웃 복구
     * - retry_count < MAX → PENDING으로 복구 (재시도 가능)
     * - retry_count >= MAX → DEAD_LETTER (재시도 포기)
     * - WHERE fcmSent=false 조건으로 FCM 발송 안 된 것만 복구
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.status = case
                when n.retryCount + 1 >= :maxRetryCount
                then com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.DEAD_LETTER
                else com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.PENDING
            end,
            n.retryCount = n.retryCount + 1,
            n.updatedAt = CURRENT_TIMESTAMP
        where n.id = :id
          and n.status = com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.SENDING
          and n.fcmSent = false
        """)
    int recoverStuckNotification(
        @Param("id") Long id,
        @Param("maxRetryCount") int maxRetryCount
    );

    /**
     * RecoveryScheduler: SENDING 타임아웃 조회
     */
    @Query("""
        select n from Notification n
        where n.status = :status
          and n.updatedAt < :timeout
        order by n.id asc
        limit :limit
        """)
    List<Notification> findStuckNotificationsForRecovery(
        @Param("status") NotificationStatus status,
        @Param("timeout") OffsetDateTime timeout,
        @Param("limit") int limit
    );

    /**
     * RetryScheduler: retry_count 초과 FAILED → DEAD_LETTER
     * - 최대 재시도 횟수 초과 시 재시도 포기
     * - WHERE retry_count >= MAX 조건으로 필터링
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.status = com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.DEAD_LETTER,
            n.updatedAt = CURRENT_TIMESTAMP
        where n.status = com.devkor.ifive.nadab.domain.notification.core.entity.NotificationStatus.FAILED
          and n.retryCount >= :maxRetryCount
        """)
    int moveFailedToDeadLetter(@Param("maxRetryCount") int maxRetryCount);

    interface UnreadCountProjection {
        Long getUserId();
        Long getUnreadCount();
    }
}