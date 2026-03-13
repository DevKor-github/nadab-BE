package com.devkor.ifive.nadab.domain.moderation.core.entity;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentReport extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private ReportReason reason;

    @Column(name = "custom_reason", length = 200)
    private String customReason;

    public static ContentReport create(
            User reporter,
            DailyReport dailyReport,
            User reportedUser,
            ReportReason reason,
            String customReason
    ) {
        ContentReport report = new ContentReport();
        report.reporter = reporter;
        report.dailyReport = dailyReport;
        report.reportedUser = reportedUser;
        report.reason = reason;
        report.customReason = customReason; // 기타 사유 (OTHER일 때만)
        return report;
    }
}