CREATE TABLE friendships (
    id BIGSERIAL PRIMARY KEY,

    user_id_1 BIGINT NOT NULL,
    user_id_2 BIGINT NOT NULL,

    status VARCHAR(16) NOT NULL,
    requester_id BIGINT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_friendships_users UNIQUE (user_id_1, user_id_2),
    CONSTRAINT fk_friendships_user1 FOREIGN KEY (user_id_1) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_user2 FOREIGN KEY (user_id_2) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_friendships_user_order CHECK (user_id_1 < user_id_2)
);

CREATE INDEX idx_friendships_user1_status ON friendships(user_id_1, status);
CREATE INDEX idx_friendships_user2_status ON friendships(user_id_2, status);
CREATE INDEX idx_friendships_requester ON friendships(requester_id);
