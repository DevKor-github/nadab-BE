package com.devkor.ifive.nadab.domain.weeklyreport.application;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.MyWeeklyReportResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.WeeklyReportResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.application.mapper.WeeklyReportMapper;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyReportRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.WeekRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyReportQueryService {

    private final WeeklyReportRepository weeklyReportRepository;
    private final UserRepository userRepository;

    public MyWeeklyReportResponse getMyWeeklyReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        WeekRangeDto range = WeekRangeCalculator.getLastWeekRange();
        WeeklyReportResponse reportResponse =
                weeklyReportRepository.findByUserIdAndWeekStartDateAndStatus(
                                user.getId(),
                                range.weekStartDate(),
                                WeeklyReportStatus.COMPLETED
                        ).
                        map(report -> WeeklyReportMapper.toResponse(range, report))
                        .orElse(null);


        WeekRangeDto prevRange = WeekRangeCalculator.getTwoWeeksAgoRange();
        WeeklyReportResponse prevReportResponse =
                weeklyReportRepository.findByUserIdAndWeekStartDateAndStatus(
                                user.getId(),
                                prevRange.weekStartDate(),
                                WeeklyReportStatus.COMPLETED
                        )
                        .map(report -> WeeklyReportMapper.toResponse(prevRange, report))
                        .orElse(null);

        return new MyWeeklyReportResponse(reportResponse, prevReportResponse);
    }

    public WeeklyReportResponse getWeeklyReportById(Long id) {
        WeeklyReport report = weeklyReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WEEKLY_REPORT_NOT_FOUND));

        WeekRangeDto range = WeekRangeCalculator.weekRangeOf(report.getWeekStartDate());

        return new WeeklyReportResponse(
                range.weekStartDate().getMonthValue(),
                WeekRangeCalculator.getWeekOfMonth(range),
                report.getDiscovered(),
                report.getGood(),
                report.getImprove(),
                report.getStatus().name()
        );
    }
}
