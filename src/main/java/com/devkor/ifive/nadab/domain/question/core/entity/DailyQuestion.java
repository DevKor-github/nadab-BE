package com.devkor.ifive.nadab.domain.question.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.Interest;
import com.devkor.ifive.nadab.global.shared.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "daily_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyQuestion extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id")
    private Interest interest;

    @Column(name = "question_text", nullable = false, length = 100)
    private String questionText;

    @Column(name = "question_level", nullable = false)
    private Integer questionLevel;

    @Column(name = "guide_empathy", length = 100)
    private String empathyGuide;

    @Column(name = "guide_hint", length = 100)
    private String hintGuide;

    @Column(name = "leading_question_hint", length = 100)
    private String leadingQuestionHint;
}
