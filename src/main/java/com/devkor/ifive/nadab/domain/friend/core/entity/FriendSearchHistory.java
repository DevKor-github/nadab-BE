package com.devkor.ifive.nadab.domain.friend.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friend_search_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendSearchHistory extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "searched_user_id", nullable = false)
    private User searchedUser;

    public static FriendSearchHistory create(User user, User searchedUser) {
        FriendSearchHistory history = new FriendSearchHistory();
        history.user = user;
        history.searchedUser = searchedUser;
        return history;
    }
}