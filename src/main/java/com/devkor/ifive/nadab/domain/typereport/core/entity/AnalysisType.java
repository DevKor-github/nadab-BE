package com.devkor.ifive.nadab.domain.typereport.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.global.shared.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "analysis_types",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_analysis_types_code",
                        columnNames = {"code"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisType extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_code", nullable = false, length = 50)
    private InterestCode interestCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "hashtag_1", nullable = false, length = 50)
    private String hashtag1;

    @Column(name = "hashtag_2", nullable = false, length = 50)
    private String hashtag2;

    @Column(name = "hashtag_3", nullable = false, length = 50)
    private String hashtag3;

    @Column(name = "image_key", nullable = false, length = 255)
    private String imageKey;
}