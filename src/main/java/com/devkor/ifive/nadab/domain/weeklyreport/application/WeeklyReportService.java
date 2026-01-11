package com.devkor.ifive.nadab.domain.weeklyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.CompletedCountResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.WeeklyReportStartResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReserveResultDto;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.report.WeeklyReportNotEligibleException;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.WeekRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final UserRepository userRepository;
    private final DailyReportRepository dailyReportRepository;

    private final WeeklyReportTxService weeklyReportTxService;

    /**
     * 비동기 시작 API: 즉시 reportId 반환
     */
    public WeeklyReportStartResponse startWeeklyReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 주간 리포트 작성 자격 확인 (저번 주에 3회 이상 완료)
        WeekRangeDto range = WeekRangeCalculator.getLastWeekRange();

        long completedCount = dailyReportRepository.countCompletedInWeek(userId, range.weekStartDate(), range.weekEndDate());
        boolean eligible = completedCount >= 3;

        if (!eligible) {
            CompletedCountResponse response = new CompletedCountResponse(completedCount);
            throw new WeeklyReportNotEligibleException(ErrorCode.WEEKLY_REPORT_NOT_ENOUGH_REPORTS, response);
        }

        // (Tx) Report(PENDING) + reserve consume + log(PENDING)
        WeeklyReserveResultDto reserve = weeklyReportTxService.reserveWeeklyAndPublish(user);

        return new WeeklyReportStartResponse(reserve.reportId(), "PENDING", reserve.balanceAfter());
    }
}

