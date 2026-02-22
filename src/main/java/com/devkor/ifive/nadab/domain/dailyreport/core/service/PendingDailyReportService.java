package com.devkor.ifive.nadab.domain.dailyreport.core.service;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class PendingDailyReportService {

    private final DailyReportRepository dailyReportRepository;


    @Transactional
    public DailyReport getOrCreatePendingDailyReport(AnswerEntry entry, boolean isDayPassed) {

        LocalDate targetDate =
                isDayPassed ? TodayDateTimeProvider.getTodayDate().minusDays(1) : TodayDateTimeProvider.getTodayDate();

        DailyReport report = dailyReportRepository.
                findByAnswerEntryAndDate(entry, targetDate)
                .orElseGet(() -> {
                    try {
                        return dailyReportRepository.save(DailyReport.createPending(entry, targetDate));
                    } catch (DataIntegrityViolationException e) {
                        // 동시 요청에서 이미 누가 만들었을 수 있음 -> 재조회로 멱등 처리
                        return dailyReportRepository.findByAnswerEntryAndDate(entry, targetDate)
                                .orElseThrow(() -> e);
                    }
                });

        if (report.getStatus() == DailyReportStatus.COMPLETED) {
            throw new ConflictException(ErrorCode.DAILY_REPORT_ALREADY_COMPLETED);
        }

        if (report.getStatus() == DailyReportStatus.FAILED) {
            dailyReportRepository.updateStatus(report.getId(), DailyReportStatus.PENDING);
        }

        return report;
    }
}
