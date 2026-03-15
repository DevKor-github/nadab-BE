package com.devkor.ifive.nadab.domain.notification.application.event.report;

import com.devkor.ifive.nadab.domain.dailyreport.application.event.DailyReportCompletedEvent;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.application.event.MonthlyReportAvailableEvent;
import com.devkor.ifive.nadab.domain.monthlyreport.application.event.MonthlyReportCompletedEvent;
import com.devkor.ifive.nadab.domain.notification.application.NotificationCommandService;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationType;
import com.devkor.ifive.nadab.domain.question.core.repository.DailyQuestionRepository;
import com.devkor.ifive.nadab.domain.typereport.application.event.TypeReportCompletedEvent;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.application.event.WeeklyReportAvailableEvent;
import com.devkor.ifive.nadab.domain.weeklyreport.application.event.WeeklyReportCompletedEvent;
import com.devkor.ifive.nadab.global.core.notification.message.NotificationContent;
import com.devkor.ifive.nadab.global.core.notification.message.NotificationMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 리포트 관련 알림 이벤트 리스너
 * - 리포트 완성 → 사용자에게 완성 알림
 * - 리포트 제작 가능 → 사용자에게 제작 가능 알림
 * - 일일 리포트 완성 → 유형 리포트 제작 가능 여부 체크 및 알림
 * - @Async로 비동기 처리 (리포트 생성 스레드와 분리)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportNotificationEventListener {

    private final NotificationMessageFactory messageFactory;
    private final NotificationCommandService notificationCommandService;
    private final AnswerEntryRepository answerEntryRepository;
    private final DailyQuestionRepository dailyQuestionRepository;
    private final UserRepository userRepository;

    private static final int MILESTONE_30 = 30;
    private static final int MILESTONE_50 = 50;

    // ========== 리포트 완성 알림 ==========

    /**
     * 주간 리포트 완성 알림
     */
    @Async("notificationTaskExecutor")
    @EventListener
    public void handleWeeklyReportCompleted(WeeklyReportCompletedEvent event) {
        try {
            // 메시지 생성
            NotificationContent content = messageFactory.createMessage(
                NotificationType.WEEKLY_REPORT_COMPLETED,
                Map.of()
            );

            // 알림 생성
            String idempotencyKey = String.format("WEEKLY_REPORT_COMPLETED_%d", event.getReportId());
            notificationCommandService.sendNotification(
                event.getUserId(),
                NotificationType.WEEKLY_REPORT_COMPLETED,
                content.title(),
                content.body(),
                content.inboxMessage(),
                event.getReportId().toString(),
                idempotencyKey
            );

            log.debug("Weekly report completed notification created: reportId={}, userId={}",
                event.getReportId(), event.getUserId());

        } catch (Exception e) {
            log.error("Failed to handle weekly report completed event: reportId={}, error={}",
                event.getReportId(), e.getMessage(), e);
        }
    }

    /**
     * 월간 리포트 완성 알림
     */
    @Async("notificationTaskExecutor")
    @EventListener
    public void handleMonthlyReportCompleted(MonthlyReportCompletedEvent event) {
        try {
            // 메시지 생성
            NotificationContent content = messageFactory.createMessage(
                NotificationType.MONTHLY_REPORT_COMPLETED,
                Map.of()
            );

            // 알림 생성
            String idempotencyKey = String.format("MONTHLY_REPORT_COMPLETED_%d", event.getReportId());
            notificationCommandService.sendNotification(
                event.getUserId(),
                NotificationType.MONTHLY_REPORT_COMPLETED,
                content.title(),
                content.body(),
                content.inboxMessage(),
                event.getReportId().toString(),
                idempotencyKey
            );

            log.debug("Monthly report completed notification created: reportId={}, userId={}",
                event.getReportId(), event.getUserId());

        } catch (Exception e) {
            log.error("Failed to handle monthly report completed event: reportId={}, error={}",
                event.getReportId(), e.getMessage(), e);
        }
    }

    /**
     * 유형 리포트 완성 알림
     */
    @Async("notificationTaskExecutor")
    @EventListener
    public void handleTypeReportCompleted(TypeReportCompletedEvent event) {
        try {
            // 메시지 생성
            Map<String, String> params = Map.of("categoryName", event.getCategoryName());
            NotificationContent content = messageFactory.createMessage(
                NotificationType.TYPE_REPORT_COMPLETED,
                params
            );

            // 알림 생성
            String idempotencyKey = String.format("TYPE_REPORT_COMPLETED_%d", event.getReportId());
            notificationCommandService.sendNotification(
                event.getUserId(),
                NotificationType.TYPE_REPORT_COMPLETED,
                content.title(),
                content.body(),
                content.inboxMessage(),
                event.getReportId().toString(),
                idempotencyKey
            );

            log.debug("Type report completed notification created: reportId={}, userId={}, categoryName={}",
                event.getReportId(), event.getUserId(), event.getCategoryName());

        } catch (Exception e) {
            log.error("Failed to handle type report completed event: reportId={}, error={}",
                event.getReportId(), e.getMessage(), e);
        }
    }

    // ========== 리포트 제작 가능 알림 ==========

    /**
     * 주간 리포트 제작 가능 알림
     */
    @Async("notificationTaskExecutor")
    @EventListener
    public void handleWeeklyReportAvailable(WeeklyReportAvailableEvent event) {
        try {
            // 메시지 생성
            NotificationContent content = messageFactory.createMessage(
                NotificationType.WEEKLY_REPORT_AVAILABLE,
                Map.of()
            );

            // 알림 생성
            String idempotencyKey = String.format("%d_WEEKLY_AVAILABLE_%s",
                event.getUserId(), java.time.LocalDate.now());
            notificationCommandService.sendNotification(
                event.getUserId(),
                NotificationType.WEEKLY_REPORT_AVAILABLE,
                content.title(),
                content.body(),
                content.inboxMessage(),
                null,
                idempotencyKey
            );

            log.debug("Weekly report available notification created: userId={}", event.getUserId());

        } catch (Exception e) {
            log.error("Failed to handle weekly report available event: userId={}, error={}",
                event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 월간 리포트 제작 가능 알림
     */
    @Async("notificationTaskExecutor")
    @EventListener
    public void handleMonthlyReportAvailable(MonthlyReportAvailableEvent event) {
        try {
            // 메시지 생성
            Map<String, String> params = Map.of("nickname", event.getNickname());
            NotificationContent content = messageFactory.createMessage(
                NotificationType.MONTHLY_REPORT_AVAILABLE,
                params
            );

            // 알림 생성
            String idempotencyKey = String.format("%d_MONTHLY_AVAILABLE_%s",
                event.getUserId(), java.time.LocalDate.now());
            notificationCommandService.sendNotification(
                event.getUserId(),
                NotificationType.MONTHLY_REPORT_AVAILABLE,
                content.title(),
                content.body(),
                content.inboxMessage(),
                null,
                idempotencyKey
            );

            log.debug("Monthly report available notification created: userId={}", event.getUserId());

        } catch (Exception e) {
            log.error("Failed to handle monthly report available event: userId={}, error={}",
                event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 일일 리포트 완성 → 유형 리포트 제작 가능 여부 체크
     * - 30개, 50개, 전체 완료 시 TYPE_REPORT_AVAILABLE 알림 발송
     */
    @Async("notificationTaskExecutor")
    @EventListener
    public void handleDailyReportCompleted(DailyReportCompletedEvent event) {
        try {
            // 해당 InterestCode의 총 답변 개수 조회
            long answerCount = answerEntryRepository.countByUserIdAndInterestCode(
                    event.getUserId(),
                    event.getInterestCode()
            );

            // 해당 InterestCode의 전체 질문 개수 조회
            long totalQuestionCount = dailyQuestionRepository.countByInterestCode(event.getInterestCode());

            // 마일스톤 체크 및 알림 발송
            String milestone = null;
            if (answerCount == MILESTONE_30) {
                milestone = "30";
            } else if (answerCount == MILESTONE_50) {
                milestone = "50";
            } else if (answerCount == totalQuestionCount) {
                milestone = "Complete";
            }

            if (milestone != null) {
                sendTypeReportAvailableNotification(
                        event.getUserId(),
                        event.getInterestCode(),
                        milestone
                );
            }

        } catch (Exception e) {
            log.error("Failed to handle daily report completed event: userId={}, interestCode={}, error={}",
                    event.getUserId(), event.getInterestCode(), e.getMessage(), e);
        }
    }

    // ========== Helper Methods ==========

    /**
     * 유형 리포트 제작 가능 알림 발송
     */
    private void sendTypeReportAvailableNotification(Long userId, InterestCode interestCode, String milestone) {
        // 사용자 조회 (탈퇴 회원 체크)
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getDeletedAt() != null) {
            log.info("User not found or deleted, skip notification: userId={}", userId);
            return;
        }

        // InterestCode → 한글 카테고리 이름 변환
        String categoryName = getCategoryNameKorean(interestCode);

        // 메시지 생성
        Map<String, String> params = new HashMap<>();
        params.put("categoryName", categoryName);
        params.put("nickname", user.getNickname());
        params.put("milestone", milestone);

        NotificationContent content = messageFactory.createMessage(
            NotificationType.TYPE_REPORT_AVAILABLE,
            params
        );

        // 알림 발송
        String idempotencyKey = String.format("%d_TYPE_AVAILABLE_%s_%s",
            userId, categoryName, milestone);
        notificationCommandService.sendNotification(
            userId,
            NotificationType.TYPE_REPORT_AVAILABLE,
            content.title(),
            content.body(),
            content.inboxMessage(),
            null,
            idempotencyKey
        );

        log.debug("Type report available notification created: userId={}, interestCode={}, milestone={}",
            userId, interestCode, milestone);
    }

    /**
     * InterestCode를 한글 카테고리 이름으로 변환
     */
    private String getCategoryNameKorean(InterestCode code) {
        return switch (code) {
            case PREFERENCE -> "취향";
            case EMOTION -> "감정";
            case ROUTINE -> "루틴";
            case RELATIONSHIP -> "관계";
            case LOVE -> "사랑";
            case VALUES -> "가치관";
        };
    }
}