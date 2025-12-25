package com.devkor.ifive.nadab.infra.builder;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class UserBuilder {
    private final TestEntityManager em;
    private String nickname;
    private String email;
    private String passwordHash;

    public UserBuilder(TestEntityManager em) {
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
