-- Monthly report v2 QA seed for app 1.4.0.
-- Replace the seven qa_*_user_id values below with the user ids you create.
-- The script is idempotent for the generated QA data range.

BEGIN;

DO $$
DECLARE
    qa_01_user_id BIGINT := NULL; -- TODO: qa_01 user_id
    qa_02_user_id BIGINT := NULL; -- TODO: qa_02 user_id
    qa_03_user_id BIGINT := NULL; -- TODO: qa_03 user_id
    qa_04_user_id BIGINT := NULL; -- TODO: qa_04 user_id
    qa_05_user_id BIGINT := NULL; -- TODO: qa_05 user_id
    qa_06_user_id BIGINT := NULL; -- TODO: qa_06 user_id
    qa_07_user_id BIGINT := NULL; -- TODO: qa_07 user_id
BEGIN
    CREATE TEMP TABLE qa_seed_users ON COMMIT DROP AS
    SELECT *
    FROM (VALUES
        ('qa_01', qa_01_user_id),
        ('qa_02', qa_02_user_id),
        ('qa_03', qa_03_user_id),
        ('qa_04', qa_04_user_id),
        ('qa_05', qa_05_user_id),
        ('qa_06', qa_06_user_id),
        ('qa_07', qa_07_user_id)
    ) AS t(qa_key, user_id);

    IF EXISTS (SELECT 1 FROM qa_seed_users WHERE user_id IS NULL) THEN
        RAISE EXCEPTION 'Set every qa_*_user_id before running this seed SQL.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM qa_seed_users q
        LEFT JOIN users u ON u.id = q.user_id
        WHERE u.id IS NULL
    ) THEN
        RAISE EXCEPTION 'One or more qa_*_user_id values do not exist in users.';
    END IF;

    DELETE FROM answer_entries
    WHERE user_id IN (SELECT user_id FROM qa_seed_users)
      AND date BETWEEN DATE '2026-01-01' AND DATE '2026-06-30';

    DELETE FROM monthly_reports_v2
    WHERE user_id IN (SELECT user_id FROM qa_seed_users)
      AND month_start_date IN (DATE '2026-04-01', DATE '2026-05-01', DATE '2026-06-01');

    DELETE FROM users
    WHERE email LIKE 'qa_monthly_1_4_0_%@seed.local';

    CREATE TEMP TABLE qa_seed_answers (
        qa_key TEXT NOT NULL,
        answer_date DATE NOT NULL,
        interest_code TEXT NOT NULL,
        emotion_code TEXT NOT NULL,
        question_text TEXT NOT NULL,
        answer_content TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO qa_seed_answers VALUES
        ('qa_01', DATE '2026-06-01', 'LOVE', 'DEPRESSION', '비 올 때 듣고 싶은 노래가 있나요?', '쳇 베이커의 재즈곡들이요. 창밖으로 떨어지는 빗소리랑 트럼펫 소리가 섞이면...'),
        ('qa_01', DATE '2026-06-02', 'LOVE', 'DEPRESSION', '최근에 나의 한계를 느꼈던 적이 있나요?', '일이 너무 몰려서 제 마음 챙길 여유조차 없을 때요. 예민해진 제 모습을 보면서...'),
        ('qa_01', DATE '2026-06-03', 'LOVE', 'DEPRESSION', '세상에 오직 나만 알고 있는 내 마음은 어떤 모양인가요?', '구멍이 숭숭 뚫린 하얀 솜사탕 같아요. 달콤하고 부드럽지만 작은 바람에도...'),
        ('qa_01', DATE '2026-06-04', 'LOVE', 'DEPRESSION', '비 올 때 듣고 싶은 노래가 있나요?', '쳇 베이커의 재즈곡들이요. 창밖으로 떨어지는 빗소리랑 트럼펫 소리가 섞이면...'),
        ('qa_01', DATE '2026-06-05', 'LOVE', 'DEPRESSION', '최근에 나의 한계를 느꼈던 적이 있나요?', '일이 너무 몰려서 제 마음 챙길 여유조차 없을 때요. 예민해진 제 모습을 보면서...'),
        ('qa_01', DATE '2026-06-06', 'LOVE', 'DEPRESSION', '세상에 오직 나만 알고 있는 내 마음은 어떤 모양인가요?', '구멍이 숭숭 뚫린 하얀 솜사탕 같아요. 달콤하고 부드럽지만 작은 바람에도...'),
        ('qa_01', DATE '2026-06-07', 'LOVE', 'DEPRESSION', '비 올 때 듣고 싶은 노래가 있나요?', '쳇 베이커의 재즈곡들이요. 창밖으로 떨어지는 빗소리랑 트럼펫 소리가 섞이면...'),
        ('qa_01', DATE '2026-06-08', 'LOVE', 'DEPRESSION', '최근에 나의 한계를 느꼈던 적이 있나요?', '일이 너무 몰려서 제 마음 챙길 여유조차 없을 때요. 예민해진 제 모습을 보면서...'),
        ('qa_01', DATE '2026-06-09', 'LOVE', 'DEPRESSION', '세상에 오직 나만 알고 있는 내 마음은 어떤 모양인가요?', '구멍이 숭숭 뚫린 하얀 솜사탕 같아요. 달콤하고 부드럽지만 작은 바람에도...'),
        ('qa_01', DATE '2026-06-10', 'LOVE', 'DEPRESSION', '비 올 때 듣고 싶은 노래가 있나요?', '쳇 베이커의 재즈곡들이요. 창밖으로 떨어지는 빗소리랑 트럼펫 소리가 섞이면...'),

        ('qa_02', DATE '2026-06-01', 'PREFERENCE', 'PEACE', '요즘 자주 찾는 색깔은 무엇인가요?', '요즘은 물 빠진 듯한 연한 버터색에 자꾸 손이 가요.'),
        ('qa_02', DATE '2026-06-02', 'PREFERENCE', 'PEACE', '가장 자주 듣는 노래는 무엇인가요?', '인디 밴드 ''설(SURL)''이나 ''검정치마''의 노래를 반복해서 들어요.'),
        ('qa_02', DATE '2026-06-03', 'PREFERENCE', 'PEACE', '카페에서 항상 고르는 메뉴는 무엇인가요?', '따뜻한 바닐라 라떼요. 기분이 좋을 때나 가라앉을 때나...'),
        ('qa_02', DATE '2026-06-04', 'PREFERENCE', 'PLEASURE', '가장 좋아하는 과일은 무엇인가요?', '말랑말랑한 복숭아요. 분홍빛 색깔도 예쁘고 향기도 너무 로맨틱하잖아요.'),
        ('qa_02', DATE '2026-06-05', 'PREFERENCE', 'PEACE', '빵집에 가면 가장 먼저 집는 빵은 무엇인가요?', '고소한 소금빵요. 화려한 토핑은 없지만 씹을수록 느껴지는 버터 향과...'),
        ('qa_02', DATE '2026-06-06', 'EMOTION', 'PEACE', '지금 당장 하고 싶은 말이 있다면 무엇인가요?', '오늘 하루도 무사히 잘 버텼다. 이제 아무 생각 안 하고 푹 쉬고 싶어.'),
        ('qa_02', DATE '2026-06-07', 'EMOTION', 'WILL', '최근 나를 가장 답답하게 만든 일은 무엇인가요?', '마음은 앞서는데 몸이 잘 안 따라줄 때요. 자꾸 게을러지는 것 같은...'),

        ('qa_03', DATE '2026-06-01', 'PREFERENCE', 'PEACE', '요즘 자주 찾는 색깔은 무엇인가요?', '요즘은 물 빠진 듯한 연한 버터색에 자꾸 손이 가요.'),
        ('qa_03', DATE '2026-06-02', 'PREFERENCE', 'PEACE', '가장 자주 듣는 노래는 무엇인가요?', '인디 밴드 ''설(SURL)''이나 ''검정치마''의 노래를 반복해서 들어요.'),
        ('qa_03', DATE '2026-06-03', 'PREFERENCE', 'PEACE', '카페에서 항상 고르는 메뉴는 무엇인가요?', '따뜻한 바닐라 라떼요. 기분이 좋을 때나 가라앉을 때나...'),
        ('qa_03', DATE '2026-06-04', 'PREFERENCE', 'PLEASURE', '가장 좋아하는 과일은 무엇인가요?', '말랑말랑한 복숭아요. 분홍빛 색깔도 예쁘고 향기도 너무 로맨틱하잖아요.'),
        ('qa_03', DATE '2026-06-05', 'PREFERENCE', 'PEACE', '빵집에 가면 가장 먼저 집는 빵은 무엇인가요?', '고소한 소금빵요. 화려한 토핑은 없지만 씹을수록 느껴지는 버터 향과...'),
        ('qa_03', DATE '2026-06-06', 'EMOTION', 'PEACE', '지금 당장 하고 싶은 말이 있다면 무엇인가요?', '오늘 하루도 무사히 잘 버텼다. 이제 아무 생각 안 하고 푹 쉬고 싶어.'),
        ('qa_03', DATE '2026-06-07', 'EMOTION', 'WILL', '최근 나를 가장 답답하게 만든 일은 무엇인가요?', '마음은 앞서는데 몸이 잘 안 따라줄 때요. 자꾸 게을러지는 것 같은...'),
        ('qa_03', DATE '2026-06-08', 'EMOTION', 'REGRET', '질투라는 감정을 느낀 적이 있나요?', 'SNS에서 저랑 비슷한 고민을 하던 친구가 저보다 먼저 행복을 찾은 것 같을 때...'),
        ('qa_03', DATE '2026-06-09', 'ROUTINE', 'PEACE', '주말 아침에 가장 하고 싶은 활동은 무엇인가요?', '알람 없이 눈을 떠서 침대 속에서 30분 정도 뒹굴거리는 거요.'),

        ('qa_04', DATE '2026-06-01', 'PREFERENCE', 'PEACE', '요즘 자주 찾는 색깔은 무엇인가요?', '요즘은 물 빠진 듯한 연한 버터색에 자꾸 손이 가요.'),
        ('qa_04', DATE '2026-06-02', 'PREFERENCE', 'PEACE', '가장 자주 듣는 노래는 무엇인가요?', '인디 밴드 ''설(SURL)''이나 ''검정치마''의 노래를 반복해서 들어요.'),
        ('qa_04', DATE '2026-06-03', 'PREFERENCE', 'PEACE', '카페에서 항상 고르는 메뉴는 무엇인가요?', '따뜻한 바닐라 라떼요.'),
        ('qa_04', DATE '2026-06-04', 'PREFERENCE', 'PLEASURE', '가장 좋아하는 과일은 무엇인가요?', '말랑말랑한 복숭아요.'),
        ('qa_04', DATE '2026-06-05', 'PREFERENCE', 'PEACE', '빵집에 가면 가장 먼저 집는 빵은 무엇인가요?', '고소한 소금빵요.'),
        ('qa_04', DATE '2026-06-06', 'EMOTION', 'PEACE', '지금 당장 하고 싶은 말이 있다면 무엇인가요?', '오늘 하루도 무사히 잘 버텼다.'),
        ('qa_04', DATE '2026-06-07', 'EMOTION', 'WILL', '최근 나를 가장 답답하게 만든 일은 무엇인가요?', '마음은 앞서는데 몸이 잘 안 따라줄 때요.'),
        ('qa_04', DATE '2026-06-08', 'EMOTION', 'REGRET', '질투라는 감정을 느낀 적이 있나요?', 'SNS에서 저랑 비슷한 고민을 하던 친구가...'),
        ('qa_04', DATE '2026-06-09', 'EMOTION', 'ETC', '최근에 느낀 감정 중 이름 붙이기 힘든 게 있었나요?', '어제 저녁에 노을 보는데 괜히 마음이 벅차면서도...'),
        ('qa_04', DATE '2026-06-10', 'EMOTION', 'PEACE', '감정을 솔직하게 표현해서 후련했던 적이 있나요?', '친구한테 서운했던 거 용기 내서 말했을 때요.'),

        ('qa_05', DATE '2026-06-01', 'PREFERENCE', 'PEACE', '요즘 자주 찾는 색깔은 무엇인가요?', '요즘은 물 빠진 듯한 연한 버터색에 자꾸 손이 가요.'),
        ('qa_05', DATE '2026-06-02', 'PREFERENCE', 'PEACE', '가장 자주 듣는 노래는 무엇인가요?', '인디 밴드 ''설(SURL)''이나 ''검정치마''의 노래를 반복해서 들어요.'),
        ('qa_05', DATE '2026-06-03', 'PREFERENCE', 'PEACE', '카페에서 항상 고르는 메뉴는 무엇인가요?', '따뜻한 바닐라 라떼요.'),
        ('qa_05', DATE '2026-06-04', 'PREFERENCE', 'PLEASURE', '가장 좋아하는 과일은 무엇인가요?', '말랑말랑한 복숭아요.'),
        ('qa_05', DATE '2026-06-05', 'PREFERENCE', 'PEACE', '빵집에 가면 가장 먼저 집는 빵은 무엇인가요?', '고소한 소금빵요.'),
        ('qa_05', DATE '2026-06-06', 'EMOTION', 'PEACE', '지금 당장 하고 싶은 말이 있다면 무엇인가요?', '오늘 하루도 무사히 잘 버텼다.'),
        ('qa_05', DATE '2026-06-07', 'EMOTION', 'WILL', '최근 나를 가장 답답하게 만든 일은 무엇인가요?', '마음은 앞서는데 몸이 잘 안 따라줄 때요.'),
        ('qa_05', DATE '2026-06-08', 'ROUTINE', 'PEACE', '주말 아침에 가장 하고 싶은 활동은 무엇인가요?', '알람 없이 눈을 떠서 침대 속에서 30분 정도 뒹굴거리는 거요.'),
        ('qa_05', DATE '2026-06-09', 'ROUTINE', 'PEACE', '내 감정을 다스리는 나만의 루틴이 있나요?', '방에 불 끄고 좋아하는 인디 음악 틀어놓고 일기 써요.'),

        ('qa_06', DATE '2026-06-01', 'RELATIONSHIP', 'PEACE', '매력을 느끼는 목소리나 말투가 있나요?', '낮고 차분하면서도 끝음이 다정한 목소리요.'),
        ('qa_06', DATE '2026-06-02', 'RELATIONSHIP', 'WILL', '누군가에게 선물할 때 가장 고려하는 것은 무엇인가요?', '그 사람의 취향이나 요즘 하는 고민요.'),
        ('qa_06', DATE '2026-06-03', 'RELATIONSHIP', 'PEACE', '내가 좋아하는 사람들의 공통점은 무엇인가요?', '다정하고 조용한 사람들이요.'),
        ('qa_06', DATE '2026-06-04', 'RELATIONSHIP', 'PLEASURE', '누군가에게 깊은 감동을 받았던 순간은 언제인가요?', '비 오는 날 퇴근하는데 직장 동료가 말없이...'),
        ('qa_06', DATE '2026-06-05', 'ROUTINE', 'PEACE', '주말 아침에 가장 하고 싶은 활동은 무엇인가요?', '알람 없이 눈을 떠서 침대 속에서...'),
        ('qa_06', DATE '2026-06-06', 'ROUTINE', 'PEACE', '내 감정을 다스리는 나만의 루틴이 있나요?', '방에 불 끄고 좋아하는 인디 음악...'),
        ('qa_06', DATE '2026-06-07', 'ROUTINE', 'PEACE', '오늘 하루 중 가장 차분했던 때는 언제인가요?', '퇴근하고 집에 돌아와서 조명 다 끄고...'),
        ('qa_06', DATE '2026-06-08', 'ROUTINE', 'ACHIEVEMENT', '최근에 가장 뿌듯했던 순간은 언제인가요?', '한 달 동안 매일 빠짐없이 일기 쓰기 성공했을 때요.'),
        ('qa_06', DATE '2026-06-09', 'LOVE', 'WILL', '사랑이라는 감정을 어떻게 정의하고 싶나요?', '서로의 빈틈을 기꺼이 채워주고 싶어지는 마음요.'),
        ('qa_06', DATE '2026-06-10', 'LOVE', 'PLEASURE', '내가 가장 아름답다고 느끼는 사람의 모습은 무엇인가요?', '길 잃은 사람한테 먼저 다가가서...'),
        ('qa_06', DATE '2026-06-11', 'LOVE', 'PEACE', '최근에 누군가를 용서한 적이 있나요?', '약속을 자꾸 어기는 친구를 이해해 보려고...'),
        ('qa_06', DATE '2026-06-12', 'LOVE', 'ETC', '지금 가장 보고 싶은 사람은 누구인가요?', '멀리 살아서 자주 못 보는 대학교 때 제일 친했던 친구요.'),

        ('qa_07', DATE '2026-06-01', 'PREFERENCE', 'PEACE', '요즘 자주 찾는 색깔은 무엇인가요?', '요즘은 물 빠진 듯한 연한 버터색에 자꾸 손이 가요.'),
        ('qa_07', DATE '2026-06-02', 'PREFERENCE', 'PLEASURE', '가장 좋아하는 과일은 무엇인가요?', '말랑말랑한 복숭아요.'),
        ('qa_07', DATE '2026-06-03', 'PREFERENCE', 'INTEREST', '가방 속에 늘 챙기는 물건은 무엇인가요?', '작고 가벼운 메모장과 펜이요.'),
        ('qa_07', DATE '2026-06-04', 'PREFERENCE', 'INTEREST', '여행지에서 꼭 들르는 곳은 어디인가요?', '현지의 작은 소품샵이나 빈티지 마켓요.'),
        ('qa_07', DATE '2026-06-05', 'PREFERENCE', 'ACHIEVEMENT', '오늘 나를 기운 나게 한 말은 무엇인가요?', '팀장님이 지나가면서 이번 마케팅 문구...'),
        ('qa_07', DATE '2026-06-06', 'EMOTION', 'PEACE', '지금 당장 하고 싶은 말이 있다면 무엇인가요?', '오늘 하루도 무사히 잘 버텼다.'),
        ('qa_07', DATE '2026-06-07', 'EMOTION', 'PLEASURE', '오늘 나를 웃게 만든 장면은 무엇인가요?', '출근길에 길가 담장 밑에서 낮잠 자는 고양이를...'),
        ('qa_07', DATE '2026-06-08', 'EMOTION', 'ACHIEVEMENT', '두려움을 극복하고 무언가 해낸 적이 있나요?', '혼자서 처음으로 해외여행 갔던 거요!'),
        ('qa_07', DATE '2026-06-09', 'EMOTION', 'ACHIEVEMENT', '고통스러운 순간이 나에게 준 가르침이 있다면 무엇인가요?', '비 온 뒤에 땅이 굳는 것처럼...'),
        ('qa_07', DATE '2026-06-10', 'EMOTION', 'INTEREST', '누군가 나에 대해 쓴다면 어떤 제목의 책일까요?', '여전히 꿈을 꾸는 아이라는 제목일 것 같아요.'),
        ('qa_07', DATE '2026-06-11', 'ROUTINE', 'PEACE', '주말 아침에 가장 하고 싶은 활동은 무엇인가요?', '알람 없이 눈을 떠서...'),
        ('qa_07', DATE '2026-06-12', 'ROUTINE', 'PLEASURE', '최근에 가장 크게 웃었던 일은 무엇인가요?', '친구랑 카페에서 수다 떨다가 서로 말도 안 되는...');

    INSERT INTO daily_questions (
        interest_id,
        question_text,
        question_level,
        empathy_guide,
        hint_guide,
        leading_question_guide,
        created_at,
        updated_at
    )
    SELECT i.id, q.question_text, 1, NULL, NULL, NULL, NOW(), NOW()
    FROM (
        SELECT DISTINCT interest_code, question_text FROM qa_seed_answers
        UNION ALL
        SELECT 'PREFERENCE', 'QA 소셜 집계용 보조 질문'
    ) q
    JOIN interests i ON i.code = q.interest_code
    WHERE NOT EXISTS (
        SELECT 1
        FROM daily_questions dq
        WHERE dq.interest_id = i.id
          AND dq.question_text = q.question_text
          AND dq.deleted_at IS NULL
    );

    INSERT INTO answer_entries (
        user_id,
        question_id,
        content,
        date,
        image_key,
        created_at,
        updated_at
    )
    SELECT
        qsu.user_id,
        dq.id,
        a.answer_content,
        a.answer_date,
        NULL,
        a.answer_date::timestamp AT TIME ZONE 'Asia/Seoul',
        a.answer_date::timestamp AT TIME ZONE 'Asia/Seoul'
    FROM qa_seed_answers a
    JOIN qa_seed_users qsu ON qsu.qa_key = a.qa_key
    JOIN interests i ON i.code = a.interest_code
    JOIN LATERAL (
        SELECT id
        FROM daily_questions
        WHERE interest_id = i.id
          AND question_text = a.question_text
          AND deleted_at IS NULL
        ORDER BY id DESC
        LIMIT 1
    ) dq ON TRUE;

    INSERT INTO daily_reports (
        answer_entry_id,
        emotion_id,
        content,
        status,
        analyzed_at,
        date,
        is_shared,
        created_at
    )
    SELECT
        ae.id,
        e.id,
        'QA 월간 리포트 테스트용 일일 리포트입니다.',
        'COMPLETED',
        ae.date::timestamp AT TIME ZONE 'Asia/Seoul',
        ae.date,
        TRUE,
        ae.date::timestamp AT TIME ZONE 'Asia/Seoul'
    FROM answer_entries ae
    JOIN qa_seed_users qsu ON qsu.user_id = ae.user_id
    JOIN qa_seed_answers a ON a.qa_key = qsu.qa_key AND a.answer_date = ae.date
    JOIN emotions e ON e.code = a.emotion_code;

    INSERT INTO monthly_reports_v2 (
        user_id,
        month_start_date,
        month_end_date,
        date,
        image_key,
        image_status,
        content,
        emotion_summary_content,
        emotion_stats,
        interest_stats,
        emotion_comparison,
        social_summary,
        summary,
        comment_summary,
        dominant_keyword,
        comparison_type,
        status,
        analyzed_at,
        created_at
    )
    SELECT
        qsu.user_id,
        p.month_start_date,
        p.month_end_date,
        p.month_end_date,
        'qa/monthly/previous-placeholder.webp',
        'COMPLETED',
        '{
          "summary": "직전 월간 리포트",
          "commentSummary": "이전 달의 감정 흐름을 비교하기 위한 QA 데이터입니다.",
          "dominantKeyword": "평온",
          "emotionTrend": "지난달은 전반적으로 평온함 속에서 일상을 차분히 채워나간 시간이었습니다.",
          "discovered": {"segments": [{"text": "지난달은 전반적으로 평온함 속에서 일상을 차분히 채워나간 시간이었습니다.", "marks": []}]},
          "comment": {"segments": [{"text": "작은 도전과 회복의 흐름을 이어간 점이 인상적입니다.", "marks": []}]}
        }'::jsonb,
        '{
          "styledText": {"segments": [{"text": "지난달은 평온함을 중심으로 긍정 감정 30%가 기록되었습니다.", "marks": []}]}
        }'::jsonb,
        '{
          "totalCount": 20,
          "dominantEmotionCode": "PEACE",
          "positivePercent": 30,
          "emotions": [
            {"emotionCode": "PEACE", "emotionName": "평온", "count": 7, "percent": 35},
            {"emotionCode": "PLEASURE", "emotionName": "즐거움", "count": 4, "percent": 20},
            {"emotionCode": "ACHIEVEMENT", "emotionName": "성취", "count": 3, "percent": 15},
            {"emotionCode": "INTEREST", "emotionName": "흥미", "count": 2, "percent": 10},
            {"emotionCode": "WILL", "emotionName": "의지", "count": 2, "percent": 8},
            {"emotionCode": "DEPRESSION", "emotionName": "우울", "count": 1, "percent": 5},
            {"emotionCode": "REGRET", "emotionName": "후회", "count": 1, "percent": 4},
            {"emotionCode": "ETC", "emotionName": "기타", "count": 0, "percent": 3}
          ]
        }'::jsonb,
        '{"interests": []}'::jsonb,
        NULL,
        '{"visible": false, "month": 1, "likeRanking": [], "commentRanking": []}'::jsonb,
        '직전 월간 리포트',
        '이전 달의 감정 흐름을 비교하기 위한 QA 데이터입니다.',
        '평온',
        'BASELINE',
        'COMPLETED',
        p.month_end_date::timestamp AT TIME ZONE 'Asia/Seoul',
        p.month_end_date::timestamp AT TIME ZONE 'Asia/Seoul'
    FROM (
        VALUES
            ('qa_03', DATE '2026-05-01', DATE '2026-05-31'),
            ('qa_04', DATE '2026-05-01', DATE '2026-05-31'),
            ('qa_05', DATE '2026-05-01', DATE '2026-05-31'),
            ('qa_06', DATE '2026-04-01', DATE '2026-04-30'),
            ('qa_07', DATE '2026-05-01', DATE '2026-05-31')
    ) p(qa_key, month_start_date, month_end_date)
    JOIN qa_seed_users qsu ON qsu.qa_key = p.qa_key;

    CREATE TEMP TABLE qa_seed_helper_defs ON COMMIT DROP AS
    SELECT *
    FROM (VALUES
        ('active_minjun', '민준', 'ACTIVE'),
        ('active_garam', '가람', 'ACTIVE'),
        ('active_nayeon', '나연', 'ACTIVE'),
        ('active_dabin', '다빈', 'ACTIVE'),
        ('active_seoyeon', '서연', 'ACTIVE'),
        ('active_jiwoo', '지우', 'ACTIVE'),
        ('active_hyunwoo', '현우', 'ACTIVE'),
        ('blocked_kim', '김차단', 'BLOCKED'),
        ('deleted_na', '나삭제', 'DELETED'),
        ('inactive_lee', '이비활', 'INACTIVE'),
        ('active_normal', '정상친구', 'ACTIVE')
    ) AS t(helper_key, nickname, helper_status);

    INSERT INTO users (
        email,
        password_hash,
        nickname,
        profile_image_key,
        default_profile_type,
        signup_status,
        registered_at,
        created_at,
        updated_at,
        deleted_at
    )
    SELECT
        'qa_monthly_1_4_0_' || helper_key || '@seed.local',
        NULL,
        nickname,
        NULL,
        'DEFAULT',
        CASE WHEN helper_status = 'DELETED' THEN 'WITHDRAWN' ELSE 'COMPLETED' END,
        NOW(),
        NOW(),
        NOW(),
        CASE WHEN helper_status = 'DELETED' THEN NOW() ELSE NULL END
    FROM qa_seed_helper_defs;

    CREATE TEMP TABLE qa_seed_helpers ON COMMIT DROP AS
    SELECT d.helper_key, d.nickname, d.helper_status, u.id AS user_id
    FROM qa_seed_helper_defs d
    JOIN users u ON u.email = 'qa_monthly_1_4_0_' || d.helper_key || '@seed.local';

    CREATE TEMP TABLE qa_seed_social_rows ON COMMIT DROP AS
    SELECT *
    FROM (VALUES
        ('qa_02', 'active_minjun', 15, 15),
        ('qa_03', 'active_garam', 15, 15),
        ('qa_03', 'active_nayeon', 10, 10),
        ('qa_03', 'active_dabin', 5, 5),
        ('qa_04', 'active_seoyeon', 10, 10),
        ('qa_04', 'active_jiwoo', 10, 10),
        ('qa_05', 'active_minjun', 15, 15),
        ('qa_05', 'active_seoyeon', 5, 5),
        ('qa_05', 'active_hyunwoo', 5, 5),
        ('qa_05', 'active_jiwoo', 5, 5),
        ('qa_06', 'active_garam', 5, 5),
        ('qa_06', 'active_nayeon', 5, 5),
        ('qa_06', 'active_dabin', 5, 5),
        ('qa_06', 'blocked_kim', 20, 50),
        ('qa_06', 'deleted_na', 20, 100),
        ('qa_06', 'inactive_lee', 50, 50),
        ('qa_07', 'active_normal', 10, 10)
    ) AS t(qa_key, helper_key, like_count, comment_count);

    INSERT INTO friendships (
        user_id_1,
        user_id_2,
        status,
        requester_id,
        created_at,
        updated_at
    )
    SELECT DISTINCT
        LEAST(qsu.user_id, h.user_id),
        GREATEST(qsu.user_id, h.user_id),
        'ACCEPTED',
        h.user_id,
        NOW(),
        NOW()
    FROM qa_seed_social_rows s
    JOIN qa_seed_users qsu ON qsu.qa_key = s.qa_key
    JOIN qa_seed_helpers h ON h.helper_key = s.helper_key
    WHERE h.helper_status <> 'INACTIVE';

    INSERT INTO user_blocks (
        blocker_id,
        blocked_id,
        created_at,
        updated_at
    )
    SELECT qsu.user_id, h.user_id, NOW(), NOW()
    FROM qa_seed_social_rows s
    JOIN qa_seed_users qsu ON qsu.qa_key = s.qa_key
    JOIN qa_seed_helpers h ON h.helper_key = s.helper_key
    WHERE h.helper_status = 'BLOCKED';

    CREATE TEMP TABLE qa_seed_social_need ON COMMIT DROP AS
    SELECT *
    FROM (VALUES
        ('qa_02', 15),
        ('qa_03', 15),
        ('qa_04', 10),
        ('qa_05', 15),
        ('qa_06', 50),
        ('qa_07', 10)
    ) AS t(qa_key, report_count);

    INSERT INTO answer_entries (
        user_id,
        question_id,
        content,
        date,
        image_key,
        created_at,
        updated_at
    )
    SELECT
        qsu.user_id,
        dq.id,
        '소셜 집계용 보조 답변입니다.',
        DATE '2026-01-01' + (gs.n - 1),
        NULL,
        (DATE '2026-01-01' + (gs.n - 1))::timestamp AT TIME ZONE 'Asia/Seoul',
        (DATE '2026-01-01' + (gs.n - 1))::timestamp AT TIME ZONE 'Asia/Seoul'
    FROM qa_seed_social_need n
    JOIN qa_seed_users qsu ON qsu.qa_key = n.qa_key
    CROSS JOIN LATERAL generate_series(1, n.report_count) AS gs(n)
    JOIN interests i ON i.code = 'PREFERENCE'
    JOIN LATERAL (
        SELECT id
        FROM daily_questions
        WHERE interest_id = i.id
          AND question_text = 'QA 소셜 집계용 보조 질문'
          AND deleted_at IS NULL
        ORDER BY id DESC
        LIMIT 1
    ) dq ON TRUE;

    INSERT INTO daily_reports (
        answer_entry_id,
        emotion_id,
        content,
        status,
        analyzed_at,
        date,
        is_shared,
        created_at
    )
    SELECT
        ae.id,
        e.id,
        'QA 소셜 집계용 보조 일일 리포트입니다.',
        'COMPLETED',
        ae.date::timestamp AT TIME ZONE 'Asia/Seoul',
        ae.date,
        TRUE,
        ae.date::timestamp AT TIME ZONE 'Asia/Seoul'
    FROM answer_entries ae
    JOIN qa_seed_users qsu ON qsu.user_id = ae.user_id
    JOIN qa_seed_social_need n ON n.qa_key = qsu.qa_key
    JOIN emotions e ON e.code = 'PEACE'
    WHERE ae.date BETWEEN DATE '2026-01-01' AND DATE '2026-02-19';

    INSERT INTO daily_report_likes (
        user_id,
        daily_report_id,
        created_at
    )
    SELECT
        h.user_id,
        ranked.daily_report_id,
        TIMESTAMPTZ '2026-06-15 12:00:00+09'
    FROM qa_seed_social_rows s
    JOIN qa_seed_users qsu ON qsu.qa_key = s.qa_key
    JOIN qa_seed_helpers h ON h.helper_key = s.helper_key
    JOIN LATERAL (
        SELECT dr.id AS daily_report_id,
               ROW_NUMBER() OVER (ORDER BY dr.date ASC, dr.id ASC) AS rn
        FROM daily_reports dr
        JOIN answer_entries ae ON ae.id = dr.answer_entry_id
        WHERE ae.user_id = qsu.user_id
        ORDER BY dr.date ASC, dr.id ASC
    ) ranked ON ranked.rn <= s.like_count;

    INSERT INTO comments (
        daily_report_id,
        author_id,
        parent_comment_id,
        content,
        is_secret,
        created_at,
        updated_at,
        deleted_at
    )
    SELECT
        target.daily_report_id,
        h.user_id,
        NULL,
        'QA 소셜 집계용 댓글 ' || gs.n,
        FALSE,
        TIMESTAMPTZ '2026-06-15 12:00:00+09',
        TIMESTAMPTZ '2026-06-15 12:00:00+09',
        NULL
    FROM qa_seed_social_rows s
    JOIN qa_seed_users qsu ON qsu.qa_key = s.qa_key
    JOIN qa_seed_helpers h ON h.helper_key = s.helper_key
    CROSS JOIN LATERAL generate_series(1, s.comment_count) AS gs(n)
    JOIN LATERAL (
        SELECT dr.id AS daily_report_id
        FROM daily_reports dr
        JOIN answer_entries ae ON ae.id = dr.answer_entry_id
        WHERE ae.user_id = qsu.user_id
        ORDER BY dr.date ASC, dr.id ASC
        LIMIT 1
    ) target ON TRUE;
END $$;

COMMIT;
