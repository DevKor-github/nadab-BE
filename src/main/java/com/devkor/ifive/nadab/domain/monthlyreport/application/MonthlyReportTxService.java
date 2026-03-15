package com.devkor.ifive.nadab.domain.monthlyreport.application;


import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReserveResultDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.service.PendingMonthlyReportService;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotEnoughCrystalException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MonthlyReportTxService {

    private final PendingMonthlyReportService pendingMonthlyReportService;

    private final MonthlyReportRepository monthlyReportRepository;
    private final UserWalletRepository userWalletRepository;
    private final CrystalLogRepository crystalLogRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;


    private static final long MONTHLY_REPORT_COST = 40L;

    /**
     * (Tx) MonthlyReport(PENDING) + reserve consume + CrystalLog(PENDING)
     * 커밋되면 리포트 생성 작업을 시작할 준비가 완료됨
     */
    public MonthlyReserveResultDto reserveMonthly(User user) {

        // Report: 있으면 기존 사용, 없으면 새로 PENDING 생성
        MonthlyReport report = pendingMonthlyReportService.getOrCreatePendingMonthlyReport(user);

        // 선차감(원자적) + balanceAfter 확보
        int updated = userWalletRepository.tryConsume(user.getId(), MONTHLY_REPORT_COST);
        if (updated == 0) {
            throw new NotEnoughCrystalException(ErrorCode.WALLET_INSUFFICIENT_BALANCE);
        }

        UserWallet wallet = userWalletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
        long balanceAfter = wallet.getCrystalBalance();


        // 로그(PENDING)
        CrystalLog log = crystalLogRepository.save(
                CrystalLog.createPending(
                        user,
                        -MONTHLY_REPORT_COST,
                        balanceAfter,
                        CrystalLogReason.REPORT_GENERATE_MONTHLY,
                        "MONTHLY_REPORT",
                        report.getId()
                )
        );

        return new MonthlyReserveResultDto(report.getId(), log.getId(), user.getId(), balanceAfter);
    }

    public MonthlyReserveResultDto reserveMonthlyAndPublish(User user) {
        MonthlyReserveResultDto reserve = this.reserveMonthly(user);

        monthlyReportRepository.updateStatus(reserve.reportId(), MonthlyReportStatus.IN_PROGRESS);

        // 트랜잭션 안에서 publish (AFTER_COMMIT 트리거 보장)
        eventPublisher.publishEvent(new MonthlyReportGenerationRequestedEventDto(
                reserve.reportId(),
                user.getId(),
                reserve.crystalLogId()
        ));

        return reserve;
    }

    public void confirmMonthly(Long reportId, Long logId, ReportContent content) {
        ReportContent normalized = content.normalized();

        String summary = normalized.summary();
        String discovered = normalized.discovered().plainText();
        String improve = normalized.improve().plainText();

        // report를 COMPLETED로
        String contentJson;
        try {
            contentJson = objectMapper.writeValueAsString(normalized);
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
        monthlyReportRepository.markCompleted(
                reportId, MonthlyReportStatus.COMPLETED.name(), contentJson, discovered, improve, summary);

        // log를 CONFIRMED로
        crystalLogRepository.markConfirmed(logId);
    }

    public void failAndRefundMonthly(Long userId, Long reportId, Long logId) {
        monthlyReportRepository.markFailed(reportId, MonthlyReportStatus.FAILED);

        // 환불(+cost)
        int updated = userWalletRepository.refund(userId, MONTHLY_REPORT_COST);
        if (updated == 0) {
            // wallet이 없을 수 있는 상황
            throw new NotFoundException(ErrorCode.WALLET_NOT_FOUND);
        }

        // log를 REFUNDED로
        crystalLogRepository.markRefunded(logId);
    }
}
