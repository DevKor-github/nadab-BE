package com.devkor.ifive.nadab.domain.question.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "user_daily_questions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_daily_questions_user_date", columnNames = {"user_id", "date"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDailyQuestion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_question_id", nullable = false)
    private DailyQuestion dailyQuestion;

    @Column(name = "reroll_used", nullable = false)
    private boolean rerollUsed = false;

    public static UserDailyQuestion create(User user, LocalDate date, DailyQuestion dailyQuestion) {
        UserDailyQuestion udq = new UserDailyQuestion();
        udq.user = user;
        udq.date = date;
        udq.dailyQuestion = dailyQuestion;
        udq.rerollUsed = false;
        return udq;
    }

    // 리롤 처리
    public void rerollTo(DailyQuestion newQuestion) {
        this.dailyQuestion = newQuestion;
        this.rerollUsed = true;
    }
}