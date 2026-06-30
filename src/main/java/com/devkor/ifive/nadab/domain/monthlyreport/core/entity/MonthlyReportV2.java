package com.devkor.ifive.nadab.domain.monthlyreport.core.entity;

import com.devkor.ifive.nadab.domain.monthlyreport.core.content.InterestStatsContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyEmotionComparisonContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyContentFactory;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyReportV2ContentFactory;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialSummaryContent;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeContentFactory;
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
    @Column(name = "image_prompt_variant", length = 40)
    private MonthlyImageStylePreset imagePromptVariant;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_color_palette", length = 40)
    private MonthlyImageColorPalette imageColorPalette;

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

    @Type(JsonType.class)
    @Column(name = "interest_stats", columnDefinition = "jsonb", nullable = false)
    private InterestStatsContent interestStats;

    @Type(JsonType.class)
    @Column(name = "emotion_comparison", columnDefinition = "jsonb")
    private MonthlyEmotionComparisonContent emotionComparison;

    @Type(JsonType.class)
    @Column(name = "social_summary", columnDefinition = "jsonb", nullable = false)
    private MonthlySocialSummaryContent socialSummary;

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

    public static MonthlyReportV2 create(
            User user, LocalDate monthStartDate, LocalDate monthEndDate,
            MonthlyReportV2Content content,
            LocalDate date, MonthlyReportStatus status,
            MonthlyReportImageStatus imageStatus,
            MonthlyReportComparisonType comparisonType
    ) {
        MonthlyReportV2 mr = new MonthlyReportV2();
        mr.user = user;
        mr.monthStartDate = monthStartDate;
        mr.monthEndDate = monthEndDate;

        MonthlyReportV2Content normalized = (content == null) ? MonthlyReportV2ContentFactory.empty() : content.normalized();
        mr.content = normalized;

        mr.summary = normalized.summary();
        mr.commentSummary = normalized.commentSummary();
        mr.dominantKeyword = normalized.dominantKeyword();

        mr.emotionSummaryContent = TypeContentFactory.emptyText().normalized();
        mr.emotionStats = TypeContentFactory.emptyEmotionStats();
        mr.interestStats = MonthlyContentFactory.emptyInterestStats();
        mr.socialSummary = MonthlySocialSummaryContent.empty(monthStartDate.getMonthValue());

        mr.comparisonType = comparisonType;
        mr.date = date;
        mr.imageStatus = imageStatus;
        mr.status = status;
        return mr;
    }

    public static MonthlyReportV2 createPending(
            User user, LocalDate monthStartDate, LocalDate monthEndDate, LocalDate date, MonthlyReportComparisonType comparisonType
    ) {
        return create(user, monthStartDate, monthEndDate, MonthlyReportV2ContentFactory.empty(), date,
                MonthlyReportStatus.PENDING, MonthlyReportImageStatus.PENDING, comparisonType);
    }

    public void updateSocialSummary(MonthlySocialSummaryContent socialSummary) {
        this.socialSummary = socialSummary == null
                ? MonthlySocialSummaryContent.empty(monthStartDate.getMonthValue())
                : socialSummary.normalized();
    }

    public void assignImagePromptVariant(MonthlyImageStylePreset imagePromptVariant) {
        if (this.imagePromptVariant == null) {
            this.imagePromptVariant = imagePromptVariant;
        }
    }

    public void assignImageColorPalette(MonthlyImageColorPalette imageColorPalette) {
        if (this.imageColorPalette == null) {
            this.imageColorPalette = imageColorPalette;
        }
    }
}
