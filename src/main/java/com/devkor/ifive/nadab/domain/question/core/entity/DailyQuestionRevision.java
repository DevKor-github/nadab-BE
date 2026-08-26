package com.devkor.ifive.nadab.domain.question.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.Interest;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "daily_question_revisions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_daily_question_revisions_question_revision",
                        columnNames = {"daily_question_id", "revision_no"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyQuestionRevision extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_question_id", nullable = false)
    private DailyQuestion dailyQuestion;

    @Column(name = "revision_no", nullable = false)
    private int revisionNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id")
    private Interest interest;

    @Column(name = "question_text", nullable = false, length = 100)
    private String questionText;

    @Column(name = "question_level", nullable = false)
    private int questionLevel;

    @Column(name = "empathy_guide", length = 100)
    private String empathyGuide;

    @Column(name = "hint_guide", length = 100)
    private String hintGuide;

    @Column(name = "leading_question_guide", length = 100)
    private String leadingQuestionGuide;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "effective_from", nullable = false)
    private OffsetDateTime effectiveFrom;

    @Column(name = "source_migration", nullable = false, length = 100)
    private String sourceMigration;
}
