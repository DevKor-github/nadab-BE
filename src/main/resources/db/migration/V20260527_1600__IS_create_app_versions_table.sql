CREATE TABLE app_versions (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(20) NOT NULL,
    version VARCHAR(30) NOT NULL,
    is_latest BOOLEAN NOT NULL,
    summary VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_app_versions_platform_version UNIQUE (platform, version)
);

CREATE TABLE app_version_items (
    id BIGSERIAL PRIMARY KEY,
    app_version_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_app_version_items_app_version FOREIGN KEY (app_version_id)
        REFERENCES app_versions(id) ON DELETE CASCADE,
    CONSTRAINT uk_app_version_items_order UNIQUE (app_version_id, display_order)
);

CREATE INDEX idx_app_version_items_app_version_id
    ON app_version_items (app_version_id);

CREATE UNIQUE INDEX uk_app_versions_platform_latest
    ON app_versions (platform)
    WHERE is_latest = true;

INSERT INTO app_versions (platform, version, is_latest, summary) VALUES
    ('IOS', '1.2.0', true, ''),
    ('ANDROID', '1.2.0', true, '');
