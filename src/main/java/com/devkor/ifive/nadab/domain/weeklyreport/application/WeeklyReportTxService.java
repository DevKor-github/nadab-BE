package com.devkor.ifive.nadab.domain.weeklyreport.application;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReserveResultDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyReportRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.core.service.PendingWeeklyReportService;
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
public class WeeklyReportTxService {

    private final PendingWeeklyReportService pendingWeeklyReportService;

    private final WeeklyReportRepository weeklyReportRepository;
    private final UserWalletRepository userWalletRepository;
    private final CrystalLogRepository crystalLogRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    private static final long WEEKLY_REPORT_COST = 20L;

    /**
     * (Tx) WeeklyReport(PENDING) + reserve consume + CrystalLog(PENDING)
     * 커밋되면 리포트 생성 작업을 시작할 준비가 완료됨
     */
    public WeeklyReserveResultDto reserveWeekly(User user) {

        // Report: 있으면 기존 사용, 없으면 새로 PENDING 생성
        WeeklyReport report = pendingWeeklyReportService.getOrCreatePendingWeeklyReport(user);

        // 선차감(원자적) + balanceAfter 확보
        int updated = userWalletRepository.tryConsume(user.getId(), WEEKLY_REPORT_COST);
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
                        -WEEKLY_REPORT_COST,
                        balanceAfter,
                        CrystalLogReason.REPORT_GENERATE_WEEKLY,
                        "WEEKLY_REPORT",
                        report.getId()
                )
        );

        return new WeeklyReserveResultDto(report.getId(), log.getId(), user.getId(), balanceAfter);
    }

    public WeeklyReserveResultDto reserveWeeklyAndPublish(User user) {
        WeeklyReserveResultDto reserve = this.reserveWeekly(user);

        weeklyReportRepository.updateStatus(reserve.reportId(), WeeklyReportStatus.IN_PROGRESS);

        // 트랜잭션 안에서 publish (AFTER_COMMIT 트리거 보장)
        eventPublisher.publishEvent(new WeeklyReportGenerationRequestedEventDto(
                reserve.reportId(),
                user.getId(),
                reserve.crystalLogId()
        ));

        return reserve;
    }

    public void confirmWeekly(Long reportId, Long logId, ReportContent content) {
        ReportContent normalized = content.normalized();
        String discovered = normalized.discovered().plainText();
        String improve = normalized.improve().plainText();

        // report를 COMPLETED로
        String contentJson;
        try {
            contentJson = objectMapper.writeValueAsString(normalized);
        } catch (Exception e) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }

        weeklyReportRepository.markCompleted(
                reportId,
                WeeklyReportStatus.COMPLETED.name(),
                contentJson,
                discovered,
                improve
        );

        // log를 CONFIRMED로
        crystalLogRepository.markConfirmed(logId);
    }

    public void failAndRefundWeekly(Long userId, Long reportId, Long logId) {
        weeklyReportRepository.markFailed(reportId, WeeklyReportStatus.FAILED);

        // 환불(+cost)
        int updated = userWalletRepository.refund(userId, WEEKLY_REPORT_COST);
        if (updated == 0) {
            // wallet이 없을 수 있는 상황
            throw new NotFoundException(ErrorCode.WALLET_NOT_FOUND);
        }

        // log를 REFUNDED로
        crystalLogRepository.markRefunded(logId);
    }
}
