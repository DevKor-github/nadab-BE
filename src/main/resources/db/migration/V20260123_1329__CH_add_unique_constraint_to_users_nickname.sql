-- 닉네임에 UNIQUE 제약 추가
-- 친구 기능에서 닉네임 기반 검색을 위해 필요
ALTER TABLE users ADD CONSTRAINT uq_users_nickname UNIQUE (nickname);