-- USER_INTERESTS 테이블 생성

CREATE TABLE user_interests (
                                id           BIGSERIAL PRIMARY KEY,
                                user_id      BIGINT NOT NULL,
                                interest_id  BIGINT NOT NULL,
                                created_at   TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_interests_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_user_interests_interest
    FOREIGN KEY (interest_id) REFERENCES interests(id) ON DELETE CASCADE,

    CONSTRAINT uk_user_interests_user UNIQUE (user_id)
    );
