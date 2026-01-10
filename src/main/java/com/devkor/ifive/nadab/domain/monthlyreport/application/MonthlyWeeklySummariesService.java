package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.application.helper.MonthlyWeeklySummariesAssembler;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyWeeklySummaryInputDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyReportRepository;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyWeeklySummariesService {

    private final WeeklyReportRepository weeklyReportRepository;

    public String buildWeeklySummaries(Long userId, MonthRangeDto monthRange) {
        List<WeeklyReport> weeklyReports =
                weeklyReportRepository.findMonthlyOverlappedWeeklyReports(
                        userId,
                        monthRange.monthStartDate(),
                        monthRange.monthEndDate(),
                        WeeklyReportStatus.COMPLETED
                );

        List<MonthlyWeeklySummaryInputDto> inputs = weeklyReports.stream()
                .map(wr -> new MonthlyWeeklySummaryInputDto(
                        wr.getWeekStartDate(),
                        wr.getWeekEndDate(),
                        wr.getDiscovered(),
                        wr.getGood(),
                        wr.getImprove()
                ))
                .toList();

        return MonthlyWeeklySummariesAssembler.assemble(inputs);
    }
}