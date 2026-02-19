package com.devkor.ifive.nadab.domain.typereport.core.service;

import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus;
import com.devkor.ifive.nadab.domain.typereport.core.repository.TypeReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PendingTypeReportService {

    private final TypeReportRepository typeReportRepository;

    @Transactional
    public PendingTypeReportResult createPendingForRegeneration(User user, InterestCode interestCode) {

        // 1) 현재 활성 COMPLETED id 기억 (없을 수도 있음)
        Long prevCompletedId = typeReportRepository.findActiveCompletedId(user.getId(), interestCode)
                .orElse(null);

        // 2) 동시에 2개 돌리는 건 막기 (중복 생성 방지)
        boolean inProgress = typeReportRepository.existsByUserIdAndInterestCodeAndStatusAndDeletedAtIsNull(
                user.getId(), interestCode, TypeReportStatus.IN_PROGRESS
        );
        if (inProgress) {
            throw new ConflictException(ErrorCode.TYPE_REPORT_IN_PROGRESS);
        }

        // 3) 새 PENDING 생성
        TypeReport newReport = typeReportRepository.save(
                TypeReport.createPending(user, LocalDate.now(), interestCode)
        );

        return new PendingTypeReportResult(newReport, prevCompletedId);
    }

    public record PendingTypeReportResult(TypeReport report, Long previousCompletedReportId) {}

}