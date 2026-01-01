package com.devkor.ifive.nadab.domain.weeklyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.service.AnswerEntryService;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReserveResult;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyReportRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.core.service.PendingWeeklyReportService;
import com.devkor.ifive.nadab.global.exception.NotEnoughCrystalException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WeeklyReportTxService {

    private final WeeklyReportRepository weeklyReportRepository;
    private final AnswerEntryService answerEntryService;
    private final PendingWeeklyReportService pendingWeeklyReportService;

    private final UserWalletRepository userWalletRepository;
    private final CrystalLogRepository crystalLogRepository;

    private static final long WEEKLY_REPORT_COST = 30L;

    /**
     * (Tx) WeeklyReport(PENDING) + reserve consume + CrystalLog(PENDING)
     * 커밋되면 리포트 생성 작업을 시작할 준비가 완료됨
     */
    public WeeklyReserveResult reserveWeekly(User user) {

        // Report: 있으면 기존 사용, 없으면 새로 PENDING 생성
        WeeklyReport report = pendingWeeklyReportService.getOrCreatePendingWeeklyReport(user);

        // 선차감(원자적) + balanceAfter 확보
        int updated = userWalletRepository.tryConsume(user.getId(), WEEKLY_REPORT_COST);
        if (updated == 0) {
            throw new NotEnoughCrystalException("크리스탈이 부족합니다.");
        }

        UserWallet wallet = userWalletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("지갑을 찾을 수 없습니다. userId: " + user.getId()));
        long balanceAfter = wallet.getCrystalBalance();


        // 로그(PENDING)
        CrystalLog log = crystalLogRepository.save(
                CrystalLog.createPending(
                        user,
                        -WEEKLY_REPORT_COST,
                        balanceAfter,
                        CrystalLogReason.REPORT_GENERATE_WEEKLY,
                        "WEEKLY_REPORT",
                        report.getId()
                )
        );

        return new WeeklyReserveResult(report.getId(), log.getId(), user.getId());
    }

    public void confirmWeekly(Long reportId, Long logId, String discovered, String good, String improve) {
        // 네 weekly_report schema에 맞춰 markCompleted 파라미터 변경
        weeklyReportRepository.markCompleted(reportId, WeeklyReportStatus.COMPLETED, discovered, good, improve);

        // log를 CONFIRMED로
        crystalLogRepository.markConfirmed(logId);
    }

    public void failAndRefundWeekly(Long userId, Long reportId, Long logId, String errorMsg) {
        weeklyReportRepository.markFailed(reportId, WeeklyReportStatus.FAILED);

        // 환불(+cost)
        int updated = userWalletRepository.refund(userId, WEEKLY_REPORT_COST);
        if (updated == 0) {
            // wallet이 없을 수 있는 상황
            throw new NotFoundException("지갑을 찾을 수 없습니다. userId: " + userId);
        }

        // log를 REFUNDED로
        crystalLogRepository.markRefunded(logId);
    }
}
