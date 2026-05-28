CREATE TABLE app_versions (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(20) NOT NULL,
    latest_version VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_app_versions_platform UNIQUE (platform)
);

INSERT INTO app_versions (platform, latest_version) VALUES
    ('IOS', '1.2.0'),
    ('ANDROID', '1.2.0'),
    ('WEB', '1.2.0');
