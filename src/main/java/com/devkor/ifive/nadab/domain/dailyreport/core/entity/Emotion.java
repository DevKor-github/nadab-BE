package com.devkor.ifive.nadab.domain.dailyreport.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emotions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Emotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 50)
    private EmotionCode code;

    @Enumerated(EnumType.STRING)
    @Column(name = "name",nullable = false, length = 50)
    private EmotionName name;
}