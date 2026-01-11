package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportResponse;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.shared.util.MonthRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyReportQueryService {

    private final MonthlyReportRepository monthlyReportRepository;
    private final UserRepository userRepository;

    public MonthlyReportResponse getLastMonthMonthlyReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        MonthRangeDto range = MonthRangeCalculator.getLastMonthRange();

        MonthlyReport report = monthlyReportRepository.findByUserIdAndMonthStartDate(user.getId(), range.monthStartDate())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MONTHLY_REPORT_NOT_FOUND));

        if (report.getStatus() != MonthlyReportStatus.COMPLETED) {
            throw new NotFoundException(ErrorCode.MONTHLY_REPORT_NOT_COMPLETED);
        }

        return new MonthlyReportResponse(
                range.monthStartDate().getMonthValue(),
                report.getDiscovered(),
                report.getGood(),
                report.getImprove(),
                report.getStatus().name()
        );
    }

    public MonthlyReportResponse getMonthlyReportById(Long id) {
        MonthlyReport report = monthlyReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MONTHLY_REPORT_NOT_FOUND));

        MonthRangeDto range = MonthRangeCalculator.monthRangeOf(report.getMonthStartDate());

        return new MonthlyReportResponse(
                range.monthStartDate().getMonthValue(),
                report.getDiscovered(),
                report.getGood(),
                report.getImprove(),
                report.getStatus().name()
        );
    }
}

