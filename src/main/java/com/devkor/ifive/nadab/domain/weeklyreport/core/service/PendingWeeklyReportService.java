package com.devkor.ifive.nadab.domain.weeklyreport.core.service;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyReportRepository;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.WeekRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PendingWeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WeeklyReport getOrCreatePendingWeeklyReport(User user) {

        WeekRangeDto range = WeekRangeCalculator.getLastWeekRange();
        LocalDate today = TodayDateTimeProvider.getTodayDate();

        WeeklyReport report = weeklyReportRepository.findByUserAndWeekStartDate(user, range.weekStartDate())
                .orElseGet(() -> weeklyReportRepository.save(WeeklyReport.createPending(
                        user,
                        range.weekStartDate(),
                        range.weekEndDate(),
                        today
                )));

        if (report.getStatus() == WeeklyReportStatus.COMPLETED) {
            throw new ConflictException("이미 작성된 주간 리포트가 존재합니다. reportId: " + report.getId());
        }

        if (report.getStatus() == WeeklyReportStatus.IN_PROGRESS) {
            throw new ConflictException("주간 리포트를 이미 작성 중입니다. reportId: " + report.getId());
        }

        if (report.getStatus() == WeeklyReportStatus.FAILED) {
            weeklyReportRepository.updateStatus(report.getId(), WeeklyReportStatus.PENDING);
        }

        return report;
    }
}
