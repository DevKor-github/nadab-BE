package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.AllReportItemResponseV2;
import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.MonthlyReportResponseV2;
import com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response.ReportListTypeV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyContentFactory;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportRepository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeContentFactory;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyReportRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.shared.util.MonthRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyReportQueryServiceV2 {

    private final MonthlyReportRepository monthlyReportRepository;
    private final MonthlyReportV2Repository monthlyReportV2Repository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final UserRepository userRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;

    public List<AllReportItemResponseV2> getAllReports(Long userId, ReportListTypeV2 type) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        List<ReportListRow> rows = new ArrayList<>();

        if (type == ReportListTypeV2.ALL || type == ReportListTypeV2.MONTHLY) {
            List<MonthlyReport> monthlyV1 = monthlyReportRepository.findAllByUserIdAndStatus(userId, MonthlyReportStatus.COMPLETED);
            for (MonthlyReport report : monthlyV1) {
                LocalDate month = report.getMonthStartDate();
                rows.add(new ReportListRow(
                        report.getId(),
                        "MONTHLY",
                        month.getYear(),
                        month.getMonthValue(),
                        99,
                        month.getMonthValue() + "월",
                        report.getSummary(),
                        1
                ));
            }

            List<MonthlyReportV2> monthlyV2 = monthlyReportV2Repository.findAllByUserIdAndStatus(userId, MonthlyReportStatus.COMPLETED);
            for (MonthlyReportV2 report : monthlyV2) {
                LocalDate month = report.getMonthStartDate();
                rows.add(new ReportListRow(
                        report.getId(),
                        "MONTHLY",
                        month.getYear(),
                        month.getMonthValue(),
                        99,
                        month.getMonthValue() + "월",
                        report.getSummary(),
                        2
                ));
            }
        }

        if (type == ReportListTypeV2.ALL || type == ReportListTypeV2.WEEKLY) {
            List<WeeklyReport> weekly = weeklyReportRepository.findAllByUserIdAndStatus(userId, WeeklyReportStatus.COMPLETED);
            for (WeeklyReport report : weekly) {
                LocalDate weekStart = report.getWeekStartDate();
                int weekOfMonth = WeekRangeCalculator.getWeekOfMonth(WeekRangeCalculator.weekRangeOf(weekStart));
                rows.add(new ReportListRow(
                        report.getId(),
                        "WEEKLY",
                        weekStart.getYear(),
                        weekStart.getMonthValue(),
                        weekOfMonth,
                        weekStart.getMonthValue() + "월 " + weekOfMonth + "주차",
                        report.getSummary(),
                        1
                ));
            }
        }

        rows.sort(
                Comparator.comparingInt(ReportListRow::year).reversed()
                        .thenComparing(Comparator.comparingInt(ReportListRow::month).reversed())
                        .thenComparing(Comparator.comparingInt(ReportListRow::weekOrder).reversed())
                        .thenComparing(Comparator.comparingInt(ReportListRow::version).reversed())
                        .thenComparing(Comparator.comparingLong(ReportListRow::id).reversed())
        );

        return rows.stream()
                .map(r -> new AllReportItemResponseV2(r.id(), r.type(), r.period(), r.summary(), r.version()))
                .toList();
    }

    public MonthlyReportResponseV2 getMyMonthlyReport(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        MonthRangeDto range = MonthRangeCalculator.getLastMonthRange();
        return monthlyReportV2Repository.findByUserIdAndMonthStartDate(userId, range.monthStartDate())
                .map(this::toResponse)
                .orElse(null);
    }

    public MonthlyReportResponseV2 getMonthlyReportById(Long userId, Long id) {
        MonthlyReportV2 report = monthlyReportV2Repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MONTHLY_REPORT_NOT_FOUND));
        if (report.getUser() == null || report.getUser().getId() == null || !report.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.MONTHLY_REPORT_ACCESS_FORBIDDEN);
        }
        return toResponse(report);
    }

    private MonthlyReportResponseV2 toResponse(MonthlyReportV2 report) {
        MonthlyReportV2Content content = report.getContent() == null
                ? new MonthlyReportV2Content("", "", "", "", TypeContentFactory.emptyText().styledText(), TypeContentFactory.emptyText().styledText())
                : report.getContent().normalized();

        String imageUrl = report.getImageKey() == null ? null : profileImageUrlBuilder.buildUrl(report.getImageKey());

        return new MonthlyReportResponseV2(
                report.getMonthStartDate().getMonthValue(),
                report.getStatus() == null ? MonthlyReportStatus.PENDING : report.getStatus(),
                report.getComparisonType(),
                content.summary(),
                imageUrl,
                content.discovered(),
                content.dominantKeyword(),
                report.getEmotionStats() == null ? TypeContentFactory.emptyEmotionStats() : report.getEmotionStats().normalized(),
                report.getEmotionSummaryContent() == null ? TypeContentFactory.emptyText() : report.getEmotionSummaryContent().normalized(),
                content.emotionTrend(),
                content.comment(),
                content.commentSummary(),
                report.getInterestStats() == null ? MonthlyContentFactory.emptyInterestStats() : report.getInterestStats().normalized()
        );
    }

    private record ReportListRow(
            Long id,
            String type,
            int year,
            int month,
            int weekOrder,
            String period,
            String summary,
            int version
    ) {
    }
}
