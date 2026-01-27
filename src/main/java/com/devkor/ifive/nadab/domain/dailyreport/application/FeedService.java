package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedService {

    private final DailyReportRepository dailyReportRepository;

    public void startSharing(Long userId) {
        // 1. 당일 DailyReport 조회
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        DailyReport report = dailyReportRepository.findByUserIdAndDate(userId, today)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));

        // 2. 공유 시작
        report.startSharing();
    }

    public void stopSharing(Long userId) {
        // 1. 당일 DailyReport 조회
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        DailyReport report = dailyReportRepository.findByUserIdAndDate(userId, today)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));

        // 2. 공유 중단
        report.stopSharing();
    }
}
