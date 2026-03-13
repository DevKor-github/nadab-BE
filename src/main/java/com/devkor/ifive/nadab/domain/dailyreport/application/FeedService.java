package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.ShareStartResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.ShareStartStatus;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.moderation.application.SharingSuspensionService;
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
    private final SharingSuspensionService sharingSuspensionService;

    public ShareStartResponse startSharing(Long userId) {
        // 1. 공유 활동 중지 확인
        if (sharingSuspensionService.isSharingSuspended(userId)) {
            return new ShareStartResponse(ShareStartStatus.SUSPENDED);
        }

        // 2. 당일 DailyReport 조회
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        DailyReport report = dailyReportRepository.findByUserIdAndDate(userId, today)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));

        // 3. 공유 시작
        report.startSharing();
        return new ShareStartResponse(ShareStartStatus.SHARED);
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
