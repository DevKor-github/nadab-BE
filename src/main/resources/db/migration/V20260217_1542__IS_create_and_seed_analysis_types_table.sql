CREATE TABLE IF NOT EXISTS analysis_types (
    id BIGSERIAL PRIMARY KEY,

    code VARCHAR(50) NOT NULL,
    interest_code VARCHAR(50) NOT NULL,

    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,

    hashtag_1 VARCHAR(50) NOT NULL,
    hashtag_2 VARCHAR(50) NOT NULL,
    hashtag_3 VARCHAR(50) NOT NULL,

    image_key VARCHAR(255) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_analysis_types_code ON analysis_types(code);
CREATE INDEX IF NOT EXISTS idx_analysis_types_not_deleted ON analysis_types(deleted_at);
CREATE INDEX IF NOT EXISTS idx_analysis_types_interest_code ON analysis_types(interest_code);

-- Seed (36 rows)
INSERT INTO analysis_types (code, interest_code, name, description, hashtag_1, hashtag_2, hashtag_3, image_key) VALUES
                                                                                                                    -- PREFERENCE (6)
                                                                                                                    ('PREFERENCE_01','PREFERENCE','몽글몽글 낭만주의자','결과보다 분위기와 감각이 마음을 먼저 움직이는 편이에요. 취향이 맞는 순간을 발견하면 에너지가 빠르게 회복되고 선택 기준도 선명해져요. 다만 기분이 애매할 땐 결정을 미루고 싶어질 수 있어요.','#분위기','#취향기록','#나만의색깔','resources/analysis_type/PREFERENCE_01'),
                                                                                                                    ('PREFERENCE_02','PREFERENCE','다정한 기억술사','지나간 순간을 잘 저장해두고 필요할 때 꺼내 쓰는 타입이에요. 추억이 쌓일수록 마음이 단단해지고, 선택도 ‘내가 좋아하던 것’ 쪽으로 자연스럽게 정렬돼요. 다만 과거의 기준이 현재의 선택을 좁힐 때가 있어요.','#추억','#아날로그','#소중한물건','resources/analysis_type/PREFERENCE_02'),
                                                                                                                    ('PREFERENCE_03','PREFERENCE','포근한 일상러','낯선 자극보다 익숙한 편안함에서 안정감을 크게 느껴요. 단골 같은 확실한 선택지가 있을수록 일상이 부드럽게 굴러가고 감정 소모도 줄어요. 대신 새로운 환경에선 적응 에너지가 많이 들 수 있어요.','#익숙함','#단골메뉴','#소확행','resources/analysis_type/PREFERENCE_03'),
                                                                                                                    ('PREFERENCE_04','PREFERENCE','반짝이는 탐험가','‘처음 해보는 것’이 삶의 재미와 동력이 되는 편이에요. 새 경험을 늘릴수록 자신감이 붙고, 내 취향의 경계도 넓어져요. 다만 흥미가 꺼지면 금방 다음으로 넘어가고 싶어질 수 있어요.','#첫시도','#새로운경험','#호기심','resources/analysis_type/PREFERENCE_04'),
                                                                                                                    ('PREFERENCE_05','PREFERENCE','든든한 아지트','사람보다 공간이 먼저 안전을 만들어주는 타입이에요. 나만의 장소가 있으면 생각과 감정이 정리되고, 다시 움직일 힘이 생겨요. 다만 낯선 자리에서는 에너지가 급격히 빠질 수 있어요.','#비밀기지','#사색','#나만의공간','resources/analysis_type/PREFERENCE_05'),
                                                                                                                    ('PREFERENCE_06','PREFERENCE','싱그러운 자연주의자','자연스러운 리듬과 컨디션을 중요하게 보고 움직이는 편이에요. 날씨나 풍경 같은 작은 단서가 마음을 안정시키고, 선택을 단순하게 만들어줘요. 다만 컨디션이 흔들리면 의욕도 같이 내려갈 수 있어요.','#날씨','#풍경','#자연향','resources/analysis_type/PREFERENCE_06'),

                                                                                                                    -- EMOTION (6)
                                                                                                                    ('EMOTION_01','EMOTION','세심한 마음지기','감정이 올라오면 그냥 넘기지 않고 이유를 찾는 편이에요. 그래서 같은 상황에서도 내가 뭘 중요하게 여겼는지가 빨리 보이고, 다음 선택이 더 안정적이에요. 다만 생각이 길어지면 감정이 오래 머물 수 있어요.','#자기성찰','#감정정리','#솔직함','resources/analysis_type/EMOTION_01'),
                                                                                                                    ('EMOTION_02','EMOTION','단단한 오뚝이','힘든 일이 있어도 다시 세우는 속도가 빠른 편이에요. 문제를 감정보다 해결 과제로 바꾸는 능력이 있어서 회복이 빨라요. 다만 버티는 데 익숙해져 도움 요청을 늦출 수 있어요.','#회복탄력성','#용기','#다시시작','resources/analysis_type/EMOTION_02'),
                                                                                                                    ('EMOTION_03','EMOTION','햇살 공감러','사람의 마음을 빠르게 읽고, 따뜻한 반응을 자연스럽게 건네는 타입이에요. 그 덕분에 관계가 편안해지고 주변에서도 의지하기 쉬워요. 다만 감정을 받아주는 일이 누적되면 피로가 뒤늦게 올 수 있어요.','#다정함','#응원','#온기','resources/analysis_type/EMOTION_03'),
                                                                                                                    ('EMOTION_04','EMOTION','투명한 진심러','감정을 숨기기보다 말로 풀 때 가장 후련해지는 편이에요. 솔직함 덕분에 오해를 줄이고 관계의 속도를 맞추는 데 강점이 있어요. 다만 상대가 준비되지 않았을 땐 부담으로 느낄 수 있어요.','#솔직담백','#후련함','#있는그대로','resources/analysis_type/EMOTION_04'),
                                                                                                                    ('EMOTION_05','EMOTION','부드러운 평온주의자','큰 파도보다 잔잔한 안정감을 선호하는 편이에요. 감정이 과열되기 전에 스스로를 진정시키는 능력이 있어서 일상을 오래 유지할 수 있어요. 다만 갈등을 피하다가 핵심을 늦게 다룰 수 있어요.','#안정감','#여유','#중심잡기','resources/analysis_type/EMOTION_05'),
                                                                                                                    ('EMOTION_06','EMOTION','속 깊은 탐구자','감정을 표면에서 끝내지 않고 의미까지 파고드는 타입이에요. 이해가 쌓일수록 흔들림이 줄고, 비슷한 상황에서 더 현명하게 선택할 수 있어요. 다만 답을 찾기 전까지 마음이 무거워질 수 있어요.','#깊은생각','#배움','#내면의힘','resources/analysis_type/EMOTION_06'),

                                                                                                                    -- ROUTINE (6)
                                                                                                                    ('ROUTINE_01','ROUTINE','반짝이는 새싹','작은 습관을 붙이는 순간부터 자신감이 자라는 타입이에요. 체크리스트나 루틴이 생기면 하루가 단단해지고 성취감도 꾸준히 쌓여요. 다만 초반에 빡세게 잡으면 금방 지칠 수 있어요.','#갓생','#성실함','#습관형성','resources/analysis_type/ROUTINE_01'),
                                                                                                                    ('ROUTINE_02','ROUTINE','야무진 몰입가','해야 할 일이 정리되면 몰입이 깊어지고 성과가 잘 나는 편이에요. 목표를 쪼개 처리하면 스스로에 대한 신뢰가 빠르게 쌓여요. 다만 계획이 깨지면 리듬이 크게 흔들릴 수 있어요.','#집중','#생산성','#효율','resources/analysis_type/ROUTINE_02'),
                                                                                                                    ('ROUTINE_03','ROUTINE','충전 전문가','잘 쉬는 것이 성과를 만든다는 감각이 확실한 타입이에요. 에너지 잔고를 보고 움직여 번아웃을 예방하고, 회복 후에는 다시 속도를 낼 수 있어요. 다만 쉬는 시간이 길어지면 재시작 문턱이 높아질 수 있어요.','#재충전','#쉼표','#나만의보상','resources/analysis_type/ROUTINE_03'),
                                                                                                                    ('ROUTINE_04','ROUTINE','정갈한 정리 전문가','정리된 환경이 곧 마음의 안정으로 이어지는 편이에요. 공간과 일정을 정돈하면 불확실성이 줄고, 일상 운영이 훨씬 쉬워져요. 다만 정리가 목표가 되면 실행이 늦어질 수 있어요.','#차분함','#정리','#일상관리','resources/analysis_type/ROUTINE_04'),
                                                                                                                    ('ROUTINE_05','ROUTINE','자유로운 모험가','리듬을 딱딱하게 고정하기보다 그날의 흐름에 맞추는 편이에요. 즉흥성이 에너지로 작동해 새로운 경험을 잘 만들어내요. 다만 일정이 겹치면 우선순위가 흔들릴 수 있어요.','#즉흥적','#현재충실','#즐거움','resources/analysis_type/ROUTINE_05'),
                                                                                                                    ('ROUTINE_06','ROUTINE','차곡차곡 기록가','기록이 쌓일수록 통제감이 생기고 마음이 안정되는 타입이에요. 메모가 있으면 선택이 빨라지고, 같은 실수를 줄이는 데도 도움이 돼요. 다만 정리만 하다 실행이 늦어질 수 있어요.','#일상메모','#기록의힘','#아카이브','resources/analysis_type/ROUTINE_06'),

                                                                                                                    -- RELATIONSHIP (6)
                                                                                                                    ('RELATIONSHIP_01','RELATIONSHIP','든든한 버팀목','관계에서 가장 중요한 건 오래가는 신뢰라고 느끼는 편이에요. 말보다 행동으로 책임을 보여주고, 상대에게 안정감을 주는 타입이에요. 다만 너무 버팀목 역할을 하다 본인 감정을 뒤로 미룰 수 있어요.','#신뢰','#내사람들','#오랜인연','resources/analysis_type/RELATIONSHIP_01'),
                                                                                                                    ('RELATIONSHIP_02','RELATIONSHIP','다정한 경청자','상대의 말을 끝까지 듣는 태도가 관계의 무기가 되는 타입이에요. 공감이 빠르고 반응이 따뜻해서 주변이 편안함을 느껴요. 다만 배려가 누적되면 피로가 뒤늦게 크게 올 수 있어요.','#배려','#존중','#응원단','resources/analysis_type/RELATIONSHIP_02'),
                                                                                                                    ('RELATIONSHIP_03','RELATIONSHIP','비타민 소통가','사람을 만나면 에너지가 올라가고 분위기를 살리는 데 강점이 있어요. 가벼운 대화로 관계를 시작하고 확장하는 속도가 빠른 편이에요. 다만 깊은 감정 대화가 필요할 때는 방향을 잃을 수 있어요.','#활력','#에너지','#새로운만남','resources/analysis_type/RELATIONSHIP_03'),
                                                                                                                    ('RELATIONSHIP_04','RELATIONSHIP','단단한 홀로서기','관계가 중요해도 내 페이스를 지키는 게 우선인 타입이에요. 혼자 있는 시간이 있어야 감정이 정리되고, 그래서 관계도 오래 갈 수 있어요. 다만 필요할 때 도움을 받는 연습이 부족할 수 있어요.','#주체성','#독립적','#나만의시간','resources/analysis_type/RELATIONSHIP_04'),
                                                                                                                    ('RELATIONSHIP_05','RELATIONSHIP','함께 성장 메이트','관계를 ‘서로의 성장에 도움 되는 연결’로 바라보는 편이에요. 응원과 피드백을 잘 주고받아서 함께 있을수록 동력이 생겨요. 다만 목표가 다르면 관계를 급히 정리하고 싶어질 수 있어요.','#선한영향력','#지지','#함께자라기','resources/analysis_type/RELATIONSHIP_05'),
                                                                                                                    ('RELATIONSHIP_06','RELATIONSHIP','담백한 대화가','말을 과하게 꾸미기보다 핵심을 담백하게 전하는 편이에요. 오해가 생기면 빠르게 정리하려 하고, 관계의 온도를 현실적으로 조절해요. 다만 표현이 적어서 차가워 보일 수 있어요.','#오해풀기','#진실','#관계의온도','resources/analysis_type/RELATIONSHIP_06'),

                                                                                                                    -- LOVE (6)
                                                                                                                    ('LOVE_01','LOVE','따뜻한 동행자','사랑을 감정보다 함께 사는 방식으로 보는 편이에요. 작은 약속을 지키고 책임을 나누는 것이 애정 표현이어서 안정적인 관계를 만들어요. 다만 표현이 조용해 상대가 확신을 못 느낄 수 있어요.','#신뢰','#책임','#미래설계','resources/analysis_type/LOVE_01'),
                                                                                                                    ('LOVE_02','LOVE','성숙한 파트너','관계를 오래 가져가려면 존중과 대화가 핵심이라고 느끼는 타입이에요. 감정이 흔들려도 규칙과 합의를 세워 안정적으로 풀어가요. 다만 감정 표현이 ‘논리’로 보일 수 있어요.','#가치관','#존중','#깊은대화','resources/analysis_type/LOVE_02'),
                                                                                                                    ('LOVE_03','LOVE','반짝이는 설렘가','설렘과 새로움이 관계의 큰 연료가 되는 편이에요. 대화·데이트·이벤트처럼 변화가 있을수록 애정이 더 커져요. 다만 일상이 반복되면 감정이 빨리 무뎌질 수 있어요.','#직관','#이끌림','#두근두근','resources/analysis_type/LOVE_03'),
                                                                                                                    ('LOVE_04','LOVE','포근한 안식처','함께 있을 때 편안해지는 감각을 가장 소중히 여겨요. 사소한 일상 공유가 사랑의 증거라서, 꾸준함이 관계를 깊게 만들어요. 다만 자극이 적으면 지루하다고 느낄 수 있어요.','#일상공유','#안정감','#편안함','resources/analysis_type/LOVE_04'),
                                                                                                                    ('LOVE_05','LOVE','소중한 나 지키미','관계 속에서도 나를 잃지 않는 게 중요하다고 느껴요. 경계를 분명히 해서 건강한 관계를 만들고, 불편함을 초기에 조정하려 해요. 다만 상대가 거리감으로 받아들일 수 있어요.','#자존감','#나다움','#건강함','resources/analysis_type/LOVE_05'),
                                                                                                                    ('LOVE_06','LOVE','시너지 메이트','사랑을 ‘서로의 성장을 밀어주는 팀플’처럼 보는 편이에요. 응원과 동기부여가 애정 표현이어서 함께 있을수록 추진력이 생겨요. 다만 경쟁처럼 느껴지면 피로해질 수 있어요.','#함께성장','#응원','#변화','resources/analysis_type/LOVE_06'),

                                                                                                                    -- VALUES (6)
                                                                                                                    ('VALUES_01','VALUES','열정 꿈나무','선택의 기준이 목표와 성취 쪽으로 선명한 편이에요. 의미 있는 결과를 만들 때 자존감이 올라가고, 다음 도전을 더 크게 잡아요. 다만 성과가 늦어지면 스스로를 압박할 수 있어요.','#목표달성','#성취','#성장','resources/analysis_type/VALUES_01'),
                                                                                                                    ('VALUES_02','VALUES','소신 뚜벅이','남의 기준보다 내가 납득되는가를 먼저 보는 타입이에요. 속도는 느려도 흔들림이 적고, 결정 후 후회가 줄어드는 편이에요. 다만 타협이 필요한 상황에서 피로를 느낄 수 있어요.','#정직','#원칙','#나만의길','resources/analysis_type/VALUES_02'),
                                                                                                                    ('VALUES_03','VALUES','온기 나눔러','선택의 기준이 나 혼자보다 누군가에게 도움이 되는가에 가까워요. 그래서 신뢰를 얻고, 스스로도 삶의 의미를 크게 느껴요. 다만 손해를 감수하는 방향으로 기울 수 있어요.','#나눔','#보람','#기여','resources/analysis_type/VALUES_03'),
                                                                                                                    ('VALUES_04','VALUES','의미 탐구생','겉으로 드러난 결과보다 그 안의 의미를 찾는 편이에요. 배움과 성찰이 쌓일수록 불안이 줄고, 선택이 더 단단해져요. 다만 답을 찾기 전까지 마음이 무거워질 수 있어요.','#배움','#성찰','#삶의이유','resources/analysis_type/VALUES_04'),
                                                                                                                    ('VALUES_05','VALUES','균형 조율자','극단보다 균형을 선호해서 리스크를 잘 관리해요. 감정과 현실, 관계와 목표 사이를 조율해 안정적인 결정을 만들어요. 다만 결정을 미루는 습관으로 보일 수 있어요.','#합리적','#자유','#중심잡기','resources/analysis_type/VALUES_05'),
                                                                                                                    ('VALUES_06','VALUES','용기 있는 개척자','변화를 두려워하기보다 실행으로 답을 찾는 편이에요. 해보면서 배우는 속도가 빠르고, 낯선 길에서도 스스로 기준을 세워요. 다만 추진이 강해 휴식 신호를 놓칠 수 있어요.','#실행력','#변화','#새로움','resources/analysis_type/VALUES_06');