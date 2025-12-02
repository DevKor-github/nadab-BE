package com.devkor.ifive.nadab.domain.user.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "interests",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_interest_code", columnNames = "code")
        }
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
}