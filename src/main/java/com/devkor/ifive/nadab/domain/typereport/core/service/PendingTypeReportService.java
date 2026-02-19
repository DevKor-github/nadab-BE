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
    public TypeReport getOrCreatePendingTypeReport(User user, InterestCode interestCode) {
        TypeReport report = typeReportRepository.findByUserIdAndInterestCodeAndDeletedAtIsNull(user.getId(), interestCode)
                .orElseGet(() -> typeReportRepository.save(
                        TypeReport.createPending(user, LocalDate.now(), interestCode)
                ));

        if (report.getStatus() == TypeReportStatus.COMPLETED) {
            throw new ConflictException(ErrorCode.TYPE_REPORT_ALREADY_COMPLETED);
        }

        if (report.getStatus() == TypeReportStatus.IN_PROGRESS) {
            throw new ConflictException(ErrorCode.TYPE_REPORT_IN_PROGRESS);
        }

        if (report.getStatus() == TypeReportStatus.FAILED) {
            typeReportRepository.updateStatus(report.getId(), TypeReportStatus.PENDING);
        }

        return report;
    }
}