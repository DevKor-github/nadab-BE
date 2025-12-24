package com.devkor.ifive.nadab.infra.builder;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import jakarta.persistence.EntityManager;

public class UserBuilder {
    private final EntityManager em;
    private String nickname;
    private String email;
    private String passwordHash;

    public UserBuilder(EntityManager em) {
        this.em = em;
        this.email = "test+" + System.nanoTime() + "@test.com";
        this.nickname = "nick" + System.nanoTime();
        this.passwordHash = "hashed_password";
    }

    public User build() {
        User user = User.createUser(email, passwordHash);
        user.updateNickname(nickname);
        em.persist(user);
        return user;
    }
}
