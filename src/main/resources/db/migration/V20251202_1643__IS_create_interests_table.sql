CREATE TABLE interests (
                                id          SERIAL PRIMARY KEY,
                                code        VARCHAR(50) NOT NULL UNIQUE,   -- 변경되지 않는 외부 공개용 식별자
                                name        VARCHAR(100) NOT NULL,         -- 화면에 표시될 이름
                                description TEXT NOT NULL                  -- 설명 문구
    );