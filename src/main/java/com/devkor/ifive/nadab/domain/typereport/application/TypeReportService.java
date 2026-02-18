package com.devkor.ifive.nadab.domain.typereport.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.typereport.api.dto.response.TypeReportStartResponse;
import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeReserveResultDto;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.CompletedCountResponse;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.report.TypeReportNotEligibleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TypeReportService {

    private final UserRepository userRepository;
    private final DailyReportRepository dailyReportRepository;

    private final TypeReportTxService typeReportTxService;

    /**
     * 비동기 시작 API: 즉시 reportId 반환
     */
    public TypeReportStartResponse startTypeReport(Long userId, InterestCode interestCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 유형 리포트 작성 자격 확인 (해당 유형 30회 이상 완료)
        long completedCount = dailyReportRepository.countByUserIdAndInterestCodeAndStatus(userId, interestCode, DailyReportStatus.COMPLETED);
        boolean eligible = completedCount >= 30;

        if (!eligible) {
            CompletedCountResponse response = new CompletedCountResponse(completedCount);
            throw new TypeReportNotEligibleException(ErrorCode.TYPE_REPORT_NOT_ENOUGH_REPORTS, response);
        }

        // (Tx) Report(PENDING) + reserve consume + log(PENDING)
        TypeReserveResultDto reserve = typeReportTxService.reserveTypeAndPublish(user, interestCode);

        return new TypeReportStartResponse(reserve.reportId(), "PENDING", reserve.balanceAfter());
    }
}
