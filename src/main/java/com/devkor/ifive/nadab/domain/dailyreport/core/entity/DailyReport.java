package com.devkor.ifive.nadab.domain.dailyreport.core.entity;

import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "daily_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyReport extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_entry_id")
    private AnswerEntry answerEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_id")
    private Emotion emotion;

    @Column(length = 500)
    private String content;

    @Column(name = "analyzed_at")
    private OffsetDateTime analyzedAt;

    public static DailyReport create(AnswerEntry answerEntry, Emotion emotion, String content, OffsetDateTime analyzedAt) {
        DailyReport dr = new DailyReport();
        dr.answerEntry = answerEntry;
        dr.emotion = emotion;
        dr.content = content;
        dr.analyzedAt = analyzedAt;
        return dr;
    }
}
