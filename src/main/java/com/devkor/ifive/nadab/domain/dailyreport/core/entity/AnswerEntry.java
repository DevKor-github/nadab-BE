package com.devkor.ifive.nadab.domain.dailyreport.core.entity;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "answer_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerEntry extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private DailyQuestion question;

    @Column(name = "content", length = 500, nullable = false)
    private String content;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    public static AnswerEntry create(User user, DailyQuestion question, String content, LocalDate date) {
        AnswerEntry e = new AnswerEntry();
        e.user = user;
        e.question = question;
        e.content = content;
        e.date = date;
        return e;
    }

    public void updateContent(String content) {
        this.content = content;
        onUpdate();
    }
}