CREATE TABLE app_versions (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(20) NOT NULL,
    version VARCHAR(30) NOT NULL,
    is_latest BOOLEAN NOT NULL,
    summary VARCHAR(120) NOT NULL,
    items JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_app_versions_platform_version UNIQUE (platform, version)
);

CREATE UNIQUE INDEX uk_app_versions_platform_latest
    ON app_versions (platform)
    WHERE is_latest = true;

INSERT INTO app_versions (platform, version, is_latest, summary, items) VALUES
    ('IOS', '1.2.0', true, '', '[]'::jsonb),
    ('ANDROID', '1.2.0', true, '', '[]'::jsonb);
