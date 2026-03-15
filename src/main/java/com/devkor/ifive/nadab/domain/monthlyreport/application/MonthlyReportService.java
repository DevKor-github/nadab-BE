package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportStartResponse;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReserveResultDto;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.CompletedCountResponse;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.report.MonthlyReportNotEligibleException;
import com.devkor.ifive.nadab.global.shared.util.MonthRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonthlyReportService {

    private final UserRepository userRepository;
    private final DailyReportRepository dailyReportRepository;

    private final MonthlyReportTxService monthlyReportTxService;

    /**
     * 비동기 시작 API: 즉시 reportId 반환
     */
    public MonthlyReportStartResponse startMonthlyReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 월간 리포트 작성 자격 확인 (저번 달에 15회 이상 완료)
        MonthRangeDto range = MonthRangeCalculator.getLastMonthRange();

        long completedCount = dailyReportRepository.countCompletedInMonth(userId, range.monthStartDate(), range.monthEndDate());
        boolean eligible = completedCount >= 15;

        if (!eligible) {
            CompletedCountResponse response = new CompletedCountResponse(completedCount);
            throw new MonthlyReportNotEligibleException(ErrorCode.MONTHLY_REPORT_NOT_ENOUGH_REPORTS, response);
        }

        // (Tx) Report(PENDING) + reserve consume + log(PENDING)
        MonthlyReserveResultDto reserve = monthlyReportTxService.reserveMonthlyAndPublish(user);

        return new MonthlyReportStartResponse(reserve.reportId(), "PENDING", reserve.balanceAfter());
    }
}
