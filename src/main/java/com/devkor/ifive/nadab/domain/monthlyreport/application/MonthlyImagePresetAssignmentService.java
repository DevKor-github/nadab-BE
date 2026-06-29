package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.application.helper.MonthlyImagePresetSelector;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MonthlyImagePresetAssignmentService {

    private static final int RECENT_PRESET_LIMIT = 3;

    private final MonthlyReportV2Repository monthlyReportV2Repository;

    public MonthlyImageStylePreset getOrAssign(Long userId, Long reportId) {
        MonthlyReportV2 report = monthlyReportV2Repository.findById(reportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MONTHLY_REPORT_NOT_FOUND));
        if (report.getUser() == null || report.getUser().getId() == null
                || !report.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.MONTHLY_REPORT_ACCESS_FORBIDDEN);
        }
        if (report.getImagePromptVariant() != null) {
            return report.getImagePromptVariant();
        }

        List<MonthlyImageStylePreset> recentPresets =
                monthlyReportV2Repository.findRecentCompletedImagePromptVariants(
                        userId,
                        reportId,
                        PageRequest.of(0, RECENT_PRESET_LIMIT)
                );
        MonthlyImageStylePreset selected = MonthlyImagePresetSelector.select(
                userId,
                reportId,
                report.getMonthStartDate(),
                recentPresets
        );
        report.assignImagePromptVariant(selected);
        return selected;
    }
}
