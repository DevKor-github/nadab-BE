package com.devkor.ifive.nadab.domain.dailyreport.core.service;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeRangeProvider;
import com.devkor.ifive.nadab.global.shared.util.dto.TodayDateTimeRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;


@Service
@RequiredArgsConstructor
public class PendingDailyReportService {

    private final DailyReportRepository dailyReportRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DailyReport getOrCreatePendingDailyReport(AnswerEntry entry) {

        TodayDateTimeRangeDto range = TodayDateTimeRangeProvider.get();

        LocalDate today = LocalDate.now(KST);

        DailyReport report = dailyReportRepository.findByAnswerEntryAndCreatedAtBetween(entry, range.startOfToday(), range.startOfTomorrow())
                .orElseGet(() -> dailyReportRepository.save(DailyReport.createPending(entry, today)));

        if (report.getStatus() == DailyReportStatus.COMPLETED) {
            throw new ConflictException("이미 작성된 일간 리포트가 존재합니다. reportId: " + report.getId());
        }

        return report;
    }
}
