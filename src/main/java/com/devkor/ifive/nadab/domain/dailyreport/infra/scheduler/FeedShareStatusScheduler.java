package com.devkor.ifive.nadab.domain.dailyreport.infra.scheduler;

import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 피드 공유 상태 초기화 스케줄러
 * - 매일 자정(00시)에 실행
 * - 모든 DailyReport의 isShared를 false로 초기화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedShareStatusScheduler {

    private final DailyReportRepository dailyReportRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 매일 자정 (서울 시간)
    @Transactional
    public void resetShareStatus() {
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        int resetCount = dailyReportRepository.resetAllShareStatus(today);

        if (resetCount > 0) {
            log.info("피드 공유 상태 초기화 완료: {}개의 리포트 초기화", resetCount);
        } else {
            log.debug("초기화할 공유 리포트가 없습니다.");
        }
    }
}
