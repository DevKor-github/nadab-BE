package com.devkor.ifive.nadab.domain.user.core.entity;

import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_interests",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_interests_user", columnNames = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class UserInterest extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_interests_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "interest_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_interests_interest")
    )
    private Interest interest;

    public static UserInterest create(User user, Interest interest) {
        UserInterest userInterest = new UserInterest();
        userInterest.user = user;
        userInterest.interest = interest;
        return userInterest;
    }

    public void updateInterest(Interest interest) {
        this.interest = interest;
    }
}