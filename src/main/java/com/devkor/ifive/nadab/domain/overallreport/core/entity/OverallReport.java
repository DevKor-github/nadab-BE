package com.devkor.ifive.nadab.domain.overallreport.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(
        name = "overall_reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_overall_reports_user_snapshot",
                        columnNames = {"user_id", "snapshot_date"}
                )
        },
        indexes = {
                @Index(name = "idx_overall_reports_user_created", columnList = "user_id, created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OverallReport extends CreatableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_type_id")
    private AnalysisType analysisType;

    /**
     * 이 날짜까지의 기록을 기반으로 생성한 "스냅샷 기준일"
     */
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "type_analysis", length = 400)
    private String typeAnalysis;

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
    private OverallReportStatus status;

    @Column(name = "analyzed_at")
    private OffsetDateTime analyzedAt;

    public static OverallReport create(
            User user,
            AnalysisType analysisType,
            String typeAnalysis,
            String persona1Title,
            String persona1Content,
            String persona2Title,
            String persona2Content,
            LocalDate date,
            OverallReportStatus status
    ) {
        OverallReport report = new OverallReport();
        report.user = user;
        report.analysisType = analysisType;
        report.typeAnalysis = typeAnalysis;
        report.persona1Title = persona1Title;
        report.persona1Content = persona1Content;
        report.persona2Title = persona2Title;
        report.persona2Content = persona2Content;
        report.snapshotDate = date;
        report.status = status;
        return report;
    }

    public static OverallReport createPending(User user, LocalDate rangeStart, LocalDate rangeEnd, LocalDate date) {
        return create(
                user,
                null,
                null,
                null,
                null,
                null,
                null,
                date,
                OverallReportStatus.PENDING
        );
    }
}
