-- 대소문자 구분 없는 닉네임 검색을 위한 함수 기반 인덱스
CREATE INDEX idx_users_nickname_lower ON users (LOWER(nickname));
