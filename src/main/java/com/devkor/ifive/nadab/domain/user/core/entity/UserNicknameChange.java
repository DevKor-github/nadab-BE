package com.devkor.ifive.nadab.domain.user.core.entity;

import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(
        name = "user_nickname_changes",
        indexes = {
                @Index(name = "idx_user_nickname_changes_user_created_at", columnList = "user_id, created_at DESC")
        }
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class UserNicknameChange extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "old_nickname")
    private String oldNickname;

    @Column(name = "new_nickname", nullable = false)
    private String newNickname;

    public static UserNicknameChange create(User user, String oldNickname, String newNickname) {
        UserNicknameChange h = new UserNicknameChange();
        h.user = user;
        h.oldNickname = oldNickname;
        h.newNickname = newNickname;
        return h;
    }
}