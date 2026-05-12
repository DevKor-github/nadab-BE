package com.devkor.ifive.nadab.domain.monthlyreport.core.entity;

import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "monthly_reports_v2",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_monthly_reports_v2_user_month",
                        columnNames = {"user_id", "month_start_date"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyReportV2 extends CreatableEntity {

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

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "image_key", length = 255)
    private String imageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_status", nullable = false, length = 16)
    private MonthlyReportImageStatus imageStatus;

    @Type(JsonType.class)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private MonthlyReportV2Content content;

    @Type(JsonType.class)
    @Column(name = "emotion_summary_content", columnDefinition = "jsonb", nullable = false)
    private TypeTextContent emotionSummaryContent;

    @Type(JsonType.class)
    @Column(name = "emotion_stats", columnDefinition = "jsonb", nullable = false)
    private TypeEmotionStatsContent emotionStats;

    @Column(name = "summary", nullable = false, length = 80)
    private String summary;

    @Column(name = "comment_summary", nullable = false, length = 80)
    private String commentSummary;

    @Column(name = "dominant_keyword", nullable = false, length = 30)
    private String dominantKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "comparison_type", nullable = false, length = 20)
    private MonthlyReportComparisonType comparisonType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private MonthlyReportStatus status;

    @Column(name = "analyzed_at")
    private OffsetDateTime analyzedAt;
}
