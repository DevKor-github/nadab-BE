package com.devkor.ifive.nadab.domain.terms.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "terms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_terms_type_version", columnNames = {"terms_type", "version"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false, length = 50)
    private TermsType termsType;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private Boolean required;

    @Column(nullable = false)
    private Boolean isActive;
}