package com.devkor.ifive.nadab.domain.typereport.core.entity;

import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.SoftDeletableEntity;
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
@Table(name = "type_reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TypeReport extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_type_id")
    private AnalysisType analysisType;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_code", nullable = false, length = 50)
    private InterestCode interestCode;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "type_analysis", length = 400)
    private String typeAnalysis;

    @Type(JsonType.class)
    @Column(name = "type_analysis_content", columnDefinition = "jsonb")
    private TypeTextContent typeAnalysisContent;

    @Type(JsonType.class)
    @Column(name = "emotion_summary_content", columnDefinition = "jsonb")
    private TypeTextContent emotionSummaryContent;

    @Type(JsonType.class)
    @Column(name = "emotion_stats", columnDefinition = "jsonb")
    private TypeEmotionStatsContent emotionStats;

    @Column(name = "persona1_title", length = 15)
    private String persona1Title;

    @Column(name = "persona1_content", length = 300)
    private String persona1Content;

    @Column(name = "persona2_title", length = 15)
    private String persona2Title;

    @Column(name = "persona2_content", length = 300)
    private String persona2Content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TypeReportStatus status;

    @Column(name = "analyzed_at")
    private OffsetDateTime analyzedAt;

    public static TypeReport create(
            User user,
            AnalysisType analysisType,
            InterestCode interestCode,
            String typeAnalysis,
            String persona1Title,
            String persona1Content,
            String persona2Title,
            String persona2Content,
            LocalDate date,
            TypeReportStatus status
    ) {
        TypeReport report = new TypeReport();
        report.user = user;
        report.interestCode = interestCode;
        report.analysisType = analysisType;
        report.typeAnalysis = typeAnalysis;
        report.persona1Title = persona1Title;
        report.persona1Content = persona1Content;
        report.persona2Title = persona2Title;
        report.persona2Content = persona2Content;
        report.date = date;
        report.status = status;
        return report;
    }

    public static TypeReport createPending(User user, LocalDate date, InterestCode interestCode) {
        return create(
                user,
                null,
                interestCode,
                null,
                null,
                null,
                null,
                null,
                date,
                TypeReportStatus.PENDING
        );
    }
}
