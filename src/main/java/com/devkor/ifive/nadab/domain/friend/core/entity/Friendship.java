package com.devkor.ifive.nadab.domain.friend.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friendships")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id_1", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id_2", nullable = false)
    private User user2;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private FriendshipStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    public static Friendship createPending(User requester, User receiver) {
        Friendship friendship = new Friendship();

        // user_id_1 < user_id_2 보장
        if (requester.getId() < receiver.getId()) {
            friendship.user1 = requester;
            friendship.user2 = receiver;
        } else {
            friendship.user1 = receiver;
            friendship.user2 = requester;
        }

        friendship.requester = requester;
        friendship.status = FriendshipStatus.PENDING;

        return friendship;
    }

    public void accept() {
        if (this.status != FriendshipStatus.PENDING) {
            throw new BadRequestException(ErrorCode.FRIENDSHIP_ALREADY_PROCESSED);
        }
        this.status = FriendshipStatus.ACCEPTED;
    }

    public boolean involves(Long userId) {
        return user1.getId().equals(userId) || user2.getId().equals(userId);
    }

    public boolean isReceiver(Long userId) {
        return !requester.getId().equals(userId) && involves(userId);
    }

    public boolean isRequester(Long userId) {
        return requester.getId().equals(userId);
    }

    public Long getOtherUserId(Long userId) {
        if (user1.getId().equals(userId)) {
            return user2.getId();
        } else if (user2.getId().equals(userId)) {
            return user1.getId();
        }
        throw new BadRequestException(ErrorCode.FRIENDSHIP_USER_NOT_INVOLVED);
    }
}
