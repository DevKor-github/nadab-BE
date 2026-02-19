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
    private static final long FREE_COST = 0L;

    public TypeReserveResultDto reserveType(User user, InterestCode interestCode) {

        PendingTypeReportService.PendingTypeReportResult pending =
                pendingTypeReportService.createPendingForRegeneration(user, interestCode);

        TypeReport report = pending.report();
        Long prevCompletedId = pending.previousCompletedReportId();

        // interest별 최초 1회 무료 판정 (과거 COMPLETED 이력 기준, deletedAt 무관)
        boolean hasEverCompleted = typeReportRepository.existsByUserIdAndInterestCodeAndStatus(
                user.getId(), interestCode, TypeReportStatus.COMPLETED
        );
        boolean isFirstFree = !hasEverCompleted;
        long cost = isFirstFree ? FREE_COST : TYPE_REPORT_COST;

        // 유료일 때만 차감
        if (cost > 0) {
            int updated = userWalletRepository.tryConsume(user.getId(), cost);
            if (updated == 0) {
                throw new NotEnoughCrystalException(ErrorCode.WALLET_INSUFFICIENT_BALANCE);
            }
        }

        UserWallet wallet = userWalletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
        long balanceAfter = wallet.getCrystalBalance();

        // 0원이어도 로그 남김(PENDING)
        CrystalLog log = crystalLogRepository.save(
                CrystalLog.createPending(
                        user,
                        -cost,
                        balanceAfter,
                        isFirstFree ? CrystalLogReason.REPORT_GENERATE_TYPE_FREE_FIRST
                                : CrystalLogReason.REPORT_GENERATE_TYPE,
                        "TYPE_REPORT: " + interestCode.name(),
                        report.getId()
                )
        );

        return new TypeReserveResultDto(report.getId(), log.getId(), user.getId(), balanceAfter, prevCompletedId);
    }

    public TypeReserveResultDto reserveTypeAndPublish(User user, InterestCode interestCode) {
        TypeReserveResultDto reserve = this.reserveType(user, interestCode);

        typeReportRepository.updateStatus(reserve.reportId(), TypeReportStatus.IN_PROGRESS);

        eventPublisher.publishEvent(new TypeReportGenerationRequestedEventDto(
                reserve.reportId(),
                user.getId(),
                reserve.crystalLogId(),
                reserve.previousCompletedReportId()
        ));

        return reserve;
    }

    public void confirmType(
            Long reportId,
            Long logId,
            Long previousCompletedReportId,
            String analysisTypeCode,
            String typeAnalysis,
            String persona1Title,
            String persona1Content,
            String persona2Title,
            String persona2Content
    ) {

        // 새 리포트가 완성된 뒤에만 이전 completed 리포트 soft-delete
        if (previousCompletedReportId != null) {
            typeReportRepository.softDeleteById(previousCompletedReportId);
        }
        // 그 다음 새 리포트 COMPLETED로 업데이트 (analysisTypeCode 유효성 검사 포함)
        int updated = typeReportRepository.markCompleted(
                reportId,
                TypeReportStatus.COMPLETED,
                analysisTypeCode,
                typeAnalysis,
                persona1Title,
                persona1Content,
                persona2Title,
                persona2Content
        );

        if (updated == 0) {
            throw new NotFoundException(ErrorCode.ANALYSIS_TYPE_NOT_FOUND);
        }

        crystalLogRepository.markConfirmed(logId);
    }

    public void failAndRefundType(Long userId, Long reportId, Long logId) {
        typeReportRepository.markFailed(reportId, TypeReportStatus.FAILED);

        CrystalLog log = crystalLogRepository.findById(logId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CRYSTAL_LOG_NOT_FOUND));

        // 유료였다면(delta<0) 환불
        if (log.getDelta() < 0) {
            long refundAmount = -log.getDelta(); // ex) -100 -> 100
            int updated = userWalletRepository.refund(userId, refundAmount);
            if (updated == 0) {
                throw new NotFoundException(ErrorCode.WALLET_NOT_FOUND);
            }
        }

        crystalLogRepository.markRefunded(logId);
    }
}