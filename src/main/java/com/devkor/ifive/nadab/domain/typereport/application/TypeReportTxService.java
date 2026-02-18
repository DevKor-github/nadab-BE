package com.devkor.ifive.nadab.domain.typereport.application;

import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeReserveResultDto;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus;
import com.devkor.ifive.nadab.domain.typereport.core.repository.TypeReportRepository;
import com.devkor.ifive.nadab.domain.typereport.core.service.PendingTypeReportService;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotEnoughCrystalException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeReportTxService {

    private final PendingTypeReportService pendingTypeReportService;

    private final TypeReportRepository typeReportRepository;
    private final UserWalletRepository userWalletRepository;
    private final CrystalLogRepository crystalLogRepository;

    private final ApplicationEventPublisher eventPublisher;

    private static final long TYPE_REPORT_COST = 100L;

    public TypeReserveResultDto reserveType(User user, InterestCode interestCode) {

        TypeReport report = pendingTypeReportService.getOrCreatePendingTypeReport(user, interestCode);

        int updated = userWalletRepository.tryConsume(user.getId(), TYPE_REPORT_COST);
        if (updated == 0) {
            throw new NotEnoughCrystalException(ErrorCode.WALLET_INSUFFICIENT_BALANCE);
        }

        UserWallet wallet = userWalletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
        long balanceAfter = wallet.getCrystalBalance();

        CrystalLog log = crystalLogRepository.save(
                CrystalLog.createPending(
                        user,
                        -TYPE_REPORT_COST,
                        balanceAfter,
                        CrystalLogReason.REPORT_GENERATE_TYPE,
                        "TYPE_REPORT: " + interestCode.name(),
                        report.getId()
                )
        );

        return new TypeReserveResultDto(report.getId(), log.getId(), user.getId(), balanceAfter);
    }

    public TypeReserveResultDto reserveTypeAndPublish(User user, InterestCode interestCode) {
        TypeReserveResultDto reserve = this.reserveType(user, interestCode);

        typeReportRepository.updateStatus(reserve.reportId(), TypeReportStatus.IN_PROGRESS);

        eventPublisher.publishEvent(new TypeReportGenerationRequestedEventDto(
                reserve.reportId(),
                user.getId(),
                reserve.crystalLogId()
        ));

        return reserve;
    }

    public void confirmType(
            Long reportId,
            Long logId,
            Long analysisTypeId,
            String typeAnalysis,
            String persona1Title,
            String persona1Content,
            String persona2Title,
            String persona2Content
    ) {
        typeReportRepository.markCompleted(
                reportId,
                TypeReportStatus.COMPLETED,
                analysisTypeId,
                typeAnalysis,
                persona1Title,
                persona1Content,
                persona2Title,
                persona2Content
        );

        crystalLogRepository.markConfirmed(logId);
    }

    public void failAndRefundType(Long userId, Long reportId, Long logId) {
        typeReportRepository.markFailed(reportId, TypeReportStatus.FAILED);

        int updated = userWalletRepository.refund(userId, TYPE_REPORT_COST);
        if (updated == 0) {
            throw new NotFoundException(ErrorCode.WALLET_NOT_FOUND);
        }

        crystalLogRepository.markRefunded(logId);
    }
}