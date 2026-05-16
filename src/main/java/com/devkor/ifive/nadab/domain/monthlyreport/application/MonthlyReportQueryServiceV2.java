package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportResponseV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyContentFactory;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeContentFactory;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
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
public class MonthlyReportQueryServiceV2 {

    private final MonthlyReportV2Repository monthlyReportV2Repository;
    private final UserRepository userRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;

    public MonthlyReportResponseV2 getMyMonthlyReport(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        MonthRangeDto range = MonthRangeCalculator.getLastMonthRange();
        return monthlyReportV2Repository.findByUserIdAndMonthStartDate(userId, range.monthStartDate())
                .map(this::toResponse)
                .orElse(null);
    }

    public MonthlyReportResponseV2 getMonthlyReportById(Long id) {
        MonthlyReportV2 report = monthlyReportV2Repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MONTHLY_REPORT_NOT_FOUND));
        return toResponse(report);
    }

    private MonthlyReportResponseV2 toResponse(MonthlyReportV2 report) {
        MonthlyReportV2Content content = report.getContent() == null
                ? new MonthlyReportV2Content("", "", "", "", TypeContentFactory.emptyText().styledText(), TypeContentFactory.emptyText().styledText())
                : report.getContent().normalized();

        String imageUrl = report.getImageKey() == null ? null : profileImageUrlBuilder.buildUrl(report.getImageKey());

        return new MonthlyReportResponseV2(
                report.getMonthStartDate().getMonthValue(),
                content.summary(),
                content.commentSummary(),
                content.dominantKeyword(),
                content.emotionTrend(),
                content.discovered(),
                content.comment(),
                report.getEmotionSummaryContent() == null ? TypeContentFactory.emptyText() : report.getEmotionSummaryContent().normalized(),
                report.getEmotionStats() == null ? TypeContentFactory.emptyEmotionStats() : report.getEmotionStats().normalized(),
                report.getInterestStats() == null ? MonthlyContentFactory.emptyInterestStats() : report.getInterestStats().normalized(),
                imageUrl,
                report.getStatus() == null ? MonthlyReportStatus.PENDING.name() : report.getStatus().name()
        );
    }
}
