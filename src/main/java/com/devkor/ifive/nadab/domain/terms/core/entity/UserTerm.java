package com.devkor.ifive.nadab.domain.terms.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_terms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_terms_user_term", columnNames = {"user_id", "terms_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTerm extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_terms_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "terms_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_terms_term")
    )
    private Term term;

    @Column(nullable = false)
    private Boolean agreed;

    public static UserTerm create(User user, Term term, boolean agreed) {
        UserTerm userTerm = new UserTerm();
        userTerm.user = user;
        userTerm.term = term;
        userTerm.agreed = agreed;
        return userTerm;
    }

    public void withdraw() {
        this.agreed = false;
    }

    public void reAgree() {
        this.agreed = true;
    }
}