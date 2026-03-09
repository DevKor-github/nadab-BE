package com.devkor.ifive.nadab.domain.notification.application.scheduler.report;

import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.weeklyreport.application.event.WeeklyReportAvailableEvent;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.WeekRangeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 주간 리포트 제작 가능 알림 스케줄러
 * - 매주 월요일 오전 8시 실행
 * - 직전주에 답변 3건 이상 작성한 사용자에게 알림
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportAvailableScheduler {

    private final AnswerEntryRepository answerEntryRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final long MIN_WEEKLY_ANSWERS = 3;

    /**
     * 주간 리포트 제작 가능 알림
     * - cron: 매주 월요일 오전 8시 (0 0 8 * * MON)
     */
    @Scheduled(cron = "0 0 8 * * MON", zone = "Asia/Seoul")
    public void notifyWeeklyReportAvailable() {
        log.debug("Starting weekly report available notification scheduler");

        try {
            // 직전주 범위 계산
            WeekRangeDto lastWeek = WeekRangeCalculator.getLastWeekRange();

            // 직전주에 답변 3건 이상 작성한 사용자 조회
            List<User> eligibleUsers = answerEntryRepository.findUsersWithMinAnswers(
                lastWeek.weekStartDate(),
                lastWeek.weekEndDate(),
                MIN_WEEKLY_ANSWERS
            );

            // 각 사용자에게 이벤트 발행
            int notificationCount = 0;
            for (User user : eligibleUsers) {
                eventPublisher.publishEvent(new WeeklyReportAvailableEvent(user.getId()));
                notificationCount++;
            }

            log.info("Weekly report available notification scheduler completed: {} users notified", notificationCount);

        } catch (Exception e) {
            log.error("Failed to execute weekly report available notification scheduler", e);
        }
    }
}