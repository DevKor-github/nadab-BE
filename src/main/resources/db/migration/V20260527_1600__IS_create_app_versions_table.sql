CREATE TABLE app_versions (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(20) NOT NULL,
    version VARCHAR(30) NOT NULL,
    is_latest BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_app_versions_platform_version UNIQUE (platform, version)
);

CREATE UNIQUE INDEX uk_app_versions_platform_latest
    ON app_versions (platform)
    WHERE is_latest = true;

INSERT INTO app_versions (platform, version, is_latest) VALUES
    ('IOS', '1.2.0', true),
    ('ANDROID', '1.2.0', true),
    ('WEB', '1.2.0', true);
