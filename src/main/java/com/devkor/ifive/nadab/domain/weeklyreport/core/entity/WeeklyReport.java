package com.devkor.ifive.nadab.domain.weeklyreport.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContent;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContentFactory;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(
        name = "weekly_reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_weekly_reports_user_id_week_start_date",
                        columnNames = {"user_id", "week_start_date"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyReport extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

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
    private WeeklyReportStatus status;

    @Column(name = "analyzed_at")
    private OffsetDateTime analyzedAt;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    public static WeeklyReport create(User user, LocalDate weekStartDate, LocalDate weekEndDate,
                                      ReportContent content,
                                      LocalDate date, WeeklyReportStatus status) {
        WeeklyReport wr = new WeeklyReport();
        wr.user = user;
        wr.weekStartDate = weekStartDate;
        wr.weekEndDate = weekEndDate;

        ReportContent normalized = (content == null) ? ReportContentFactory.empty() : content.normalized();
        wr.content = normalized;

        // plain 캐시
        wr.discovered = normalized.discovered().plainText();
        wr.improve = normalized.improve().plainText();
        wr.summary = normalized.summary();

        wr.date = date;
        wr.status = status;
        return wr;
    }

    public static WeeklyReport createPending(User user, LocalDate weekStartDate, LocalDate weekEndDate, LocalDate date) {
        return create(user, weekStartDate, weekEndDate, ReportContentFactory.empty(), date, WeeklyReportStatus.PENDING);
    }
}

