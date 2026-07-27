package com.devkor.ifive.nadab.domain.askchat.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ask_chat_sample_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AskChatSampleQuestion extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_code", nullable = false, length = 50)
    private InterestCode interestCode;

    @Column(name = "question", nullable = false, length = 200)
    private String question;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "active", nullable = false)
    private boolean active;

    public static AskChatSampleQuestion create(InterestCode interestCode, String question, int displayOrder) {
        AskChatSampleQuestion sampleQuestion = new AskChatSampleQuestion();
        sampleQuestion.interestCode = interestCode;
        sampleQuestion.question = question;
        sampleQuestion.displayOrder = displayOrder;
        sampleQuestion.active = true;
        return sampleQuestion;
    }
}
