package com.devkor.ifive.nadab.domain.weeklyreport.application;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.WeeklyReportResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyReportRepository;
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

    public WeeklyReportResponse getLastWeekWeeklyReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        WeekRangeDto range = WeekRangeCalculator.getLastWeekRange();

        WeeklyReport report = weeklyReportRepository.findByUserAndWeekStartDate(user, range.weekStartDate())
                .orElseThrow(() -> new NotFoundException("지난 주간 리포트를 찾을 수 없습니다. userId: " + userId));

        if (report.getStatus() != WeeklyReportStatus.COMPLETED) {
            throw new NotFoundException("지난 주간 리포트가 작성되지 않았습니다. userId: " + userId);
        }

        return new WeeklyReportResponse(
                range.weekStartDate().getMonthValue(),
                WeekRangeCalculator.getWeekOfMonth(range),
                report.getDiscovered(),
                report.getGood(),
                report.getImprove(),
                report.getStatus().name()
        );
    }

    public WeeklyReportResponse getWeeklyReportById(Long id) {
        WeeklyReport report = weeklyReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("주간 리포트를 찾을 수 없습니다. id: " + id));

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
