CREATE TABLE emotions (
                          id          BIGSERIAL PRIMARY KEY,
                          code        VARCHAR(50)  NOT NULL UNIQUE,
                          name        VARCHAR(50)  NOT NULL,
                          color_code  VARCHAR(50)
);

INSERT INTO emotions (code, name, color_code) VALUES
                                                  ('JOY', '기쁨', NULL),
                                                  ('PLEASURE', '즐거움', NULL),
                                                  ('LOVE', '사랑', NULL),
                                                  ('SADNESS', '슬픔', NULL),
                                                  ('ANGER', '분노', NULL),
                                                  ('PAIN', '고통', NULL),
                                                  ('REGRET', '후회', NULL),
                                                  ('FRUSTRATION', '좌절', NULL),
                                                  ('GROWTH', '성장', NULL),
                                                  ('ETC', '기타', NULL);