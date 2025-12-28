package com.devkor.ifive.nadab.domain.dailyreport.core.entity;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answer_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerEntry extends SoftDeletableEntity {

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

    public static AnswerEntry create(User user, DailyQuestion question, String content) {
        AnswerEntry e = new AnswerEntry();
        e.user = user;
        e.question = question;
        e.content = content;
        return e;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}