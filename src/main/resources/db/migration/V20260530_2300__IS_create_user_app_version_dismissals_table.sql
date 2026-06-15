CREATE TABLE user_app_version_dismissals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    app_version_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_uavd_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_uavd_app_version FOREIGN KEY (app_version_id)
        REFERENCES app_versions(id) ON DELETE CASCADE,
    CONSTRAINT uk_uavd_user_app_version UNIQUE (user_id, app_version_id)
);
