package com.devkor.ifive.nadab.domain.notification.application.scheduler.report;

import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.application.event.MonthlyReportAvailableEvent;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 월간 리포트 제작 가능 알림 스케줄러
 * - 매월 1일 오전 8시 실행
 * - 지난달에 답변 15건 이상 작성한 사용자에게 알림
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyReportAvailableScheduler {

    private final AnswerEntryRepository answerEntryRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final long MIN_MONTHLY_ANSWERS = 15;

    /**
     * 월간 리포트 제작 가능 알림
     * - cron: 매월 1일 오전 8시 (0 0 8 1 * *)
     */
    @Scheduled(cron = "0 0 8 1 * *", zone = "Asia/Seoul")
    public void notifyMonthlyReportAvailable() {
        log.debug("Starting monthly report available notification scheduler");

        try {
            // 지난달 범위 계산
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            LocalDate startDate = lastMonth.atDay(1);
            LocalDate endDate = lastMonth.atEndOfMonth();

            // 지난달에 답변 15건 이상 작성한 사용자 조회
            List<User> eligibleUsers = answerEntryRepository.findUsersWithMinAnswers(
                startDate,
                endDate,
                MIN_MONTHLY_ANSWERS
            );

            // 각 사용자에게 이벤트 발행
            int notificationCount = 0;
            for (User user : eligibleUsers) {
                eventPublisher.publishEvent(
                    new MonthlyReportAvailableEvent(user.getId(), user.getNickname())
                );
                notificationCount++;
            }

            log.info("Monthly report available notification scheduler completed: {} users notified", notificationCount);

        } catch (Exception e) {
            log.error("Failed to execute monthly report available notification scheduler", e);
        }
    }
}