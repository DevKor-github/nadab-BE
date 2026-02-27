package com.devkor.ifive.nadab.domain.monthlyreport.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContent;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContentFactory;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "monthly_reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_monthly_reports_user_month",
                        columnNames = {"user_id", "month_start_date"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "month_start_date", nullable = false)
    private LocalDate monthStartDate;

    @Column(name = "month_end_date", nullable = false)
    private LocalDate monthEndDate;

    @Column(name = "discovered", length = 250)
    private String discovered;

    @Column(name = "improve", length = 250)
    private String improve;

    @Column(name = "summary", nullable = false, length = 80)
    private String summary;

    @Type(JsonType.class)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private ReportContent content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private MonthlyReportStatus status;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "analyzed_at")
    private OffsetDateTime analyzedAt;

    public static MonthlyReport create(User user, LocalDate monthStartDate, LocalDate monthEndDate,
                                       ReportContent content,
                                      LocalDate date, MonthlyReportStatus status) {
        MonthlyReport mr = new MonthlyReport();
        mr.user = user;
        mr.monthStartDate = monthStartDate;
        mr.monthEndDate = monthEndDate;

        ReportContent normalized = (content == null) ? ReportContentFactory.empty() : content.normalized();
        mr.content = normalized;

        // plain 캐시
        mr.discovered = normalized.discovered().plainText();
        mr.improve = normalized.improve().plainText();
        mr.summary = normalized.summary();

        mr.date = date;
        mr.status = status;
        return mr;
    }

    public static MonthlyReport createPending(User user, LocalDate monthStartDate, LocalDate monthEndDate, LocalDate date) {
        return create(user, monthStartDate, monthEndDate, ReportContentFactory.empty(), date, MonthlyReportStatus.PENDING);
    }
}
