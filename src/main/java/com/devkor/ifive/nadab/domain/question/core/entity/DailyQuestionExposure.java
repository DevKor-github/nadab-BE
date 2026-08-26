package com.devkor.ifive.nadab.domain.question.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "daily_question_exposures",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_daily_question_exposures_assignment_sequence",
                        columnNames = {"user_daily_question_id", "sequence"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyQuestionExposure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_daily_question_id")
    private UserDailyQuestion userDailyQuestion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_question_revision_id", nullable = false)
    private DailyQuestionRevision dailyQuestionRevision;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private DailyQuestionExposureSource source;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "rerolled_at")
    private OffsetDateTime rerolledAt;

    @Column(name = "answered_at")
    private OffsetDateTime answeredAt;

    public static DailyQuestionExposure create(
            UserDailyQuestion userDailyQuestion,
            DailyQuestionRevision dailyQuestionRevision,
            LocalDate assignmentDate,
            int sequence,
            DailyQuestionExposureSource source
    ) {
        DailyQuestionExposure exposure = new DailyQuestionExposure();
        exposure.userDailyQuestion = userDailyQuestion;
        exposure.dailyQuestionRevision = dailyQuestionRevision;
        exposure.assignmentDate = assignmentDate;
        exposure.sequence = sequence;
        exposure.source = source;
        exposure.assignedAt = OffsetDateTime.now();
        return exposure;
    }
}
