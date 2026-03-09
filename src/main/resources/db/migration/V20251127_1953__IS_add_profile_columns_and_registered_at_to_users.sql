ALTER TABLE users
    ADD COLUMN profile_image_key VARCHAR(255),
    ADD COLUMN default_profile_type VARCHAR(50),
    ADD COLUMN registered_at TIMESTAMPTZ;
