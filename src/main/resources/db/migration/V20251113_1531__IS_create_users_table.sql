CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,

                       email VARCHAR(255),
                       password_hash VARCHAR(255),
                       nickname VARCHAR(255),

                       provider VARCHAR(255),
                       provider_id VARCHAR(255),

                       signup_status VARCHAR(50) NOT NULL,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       deleted_at TIMESTAMPTZ
);
