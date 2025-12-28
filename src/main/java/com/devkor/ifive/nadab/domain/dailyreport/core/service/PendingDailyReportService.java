package com.devkor.ifive.nadab.domain.dailyreport.core.service;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PendingDailyReportService {

    private final DailyReportRepository dailyReportRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DailyReport getOrCreatePendingDailyReport(AnswerEntry entry) {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        OffsetDateTime startOfToday =
                today.atStartOfDay(ZoneId.of("Asia/Seoul"))
                        .toOffsetDateTime();

        OffsetDateTime startOfTomorrow =
                today.plusDays(1)
                        .atStartOfDay(ZoneId.of("Asia/Seoul"))
                        .toOffsetDateTime();

        DailyReport report = dailyReportRepository.findByAnswerEntryAndCreatedAtBetween(entry, startOfToday, startOfTomorrow)
                .orElseGet(() -> dailyReportRepository.save(DailyReport.createPending(entry)));

        if (report.getStatus() == DailyReportStatus.COMPLETED) {
            throw new ConflictException("이미 작성된 일간 리포트가 존재합니다. reportId: " + report.getId());
        }

        return report;
    }
}
