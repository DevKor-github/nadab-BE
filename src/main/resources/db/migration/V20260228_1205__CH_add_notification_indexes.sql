-- 알림 인덱스 최적화
-- RetryScheduler 조회 최적화 (PENDING, FAILED 상태 + id)
-- PENDING 재시도, FAILED Exponential Backoff에서 사용
CREATE INDEX idx_notifications_status_id
    ON notifications(status, id ASC);

-- RecoveryScheduler 타임아웃 조회 최적화 (status + updated_at)
CREATE INDEX idx_notifications_status_updated_at
    ON notifications(status, updated_at)
    WHERE status = 'SENDING';

-- 읽지 않은 알림 개수 조회 최적화 (user_id + is_read + deleted_at)
CREATE INDEX idx_notifications_user_unread
    ON notifications(user_id, is_read, deleted_at)
    WHERE is_read = false AND deleted_at IS NULL;

-- Exponential Backoff 재시도 최적화 (status + retry_count + updated_at)
CREATE INDEX idx_notifications_failed_retry
    ON notifications(status, retry_count, updated_at)
    WHERE status = 'FAILED';