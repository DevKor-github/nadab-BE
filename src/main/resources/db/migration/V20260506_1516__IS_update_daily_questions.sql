-- 04/28 질문 리스트 -> 05/06 질문 리스트 변경사항 반영
-- 기존 값이 일치하는 경우에만 업데이트되도록 WHERE 조건에 이전 값을 포함

UPDATE daily_questions
SET question_level = 2
WHERE id = 1
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '지금 입은 옷이나 주변 소품을 봐요.'
WHERE id = 1
  AND hint_guide = '지금 입은 옷이나 주변 소품을 보세요.';

UPDATE daily_questions
SET hint_guide = '플레이리스트 맨 위에 있는 곡을 봐요.'
WHERE id = 2
  AND hint_guide = '플레이리스트 맨 위에 있는 곡을 보세요.';

UPDATE daily_questions
SET question_text = '비 올 때 듣고 싶은 노래가 있다면, 어떤 노래인가요?'
WHERE id = 3
  AND question_text = '비 올 때 듣고 싶은 노래가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 3
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '차분한 발라드나 연주곡을 떠올려 봐요.'
WHERE id = 3
  AND hint_guide = '차분한 발라드나 연주곡을 떠올려 보세요.';

UPDATE daily_questions
SET leading_question_guide = '비 오는 날에 유독 그 노래가 듣고 싶어지는 이유가 있을까요?'
WHERE id = 3
  AND leading_question_guide = '이 노래를 들으면 어떤 기분이 드나요?';

UPDATE daily_questions
SET hint_guide = '커피나 티 혹은 에이드 종류를 생각해봐요.'
WHERE id = 4
  AND hint_guide = '커피나 티 혹은 에이드 종류를 생각해보세요.';

UPDATE daily_questions
SET leading_question_guide = '그 맛을 볼 때 왜 마음이 편해지나요?'
WHERE id = 4
  AND leading_question_guide = '그 맛을 볼 때 왜 기분이 놓이나요?';

UPDATE daily_questions
SET question_text = '계절이 바뀌 때 기억나는 냄새가 있다면, 어떤 냄새인가요?'
WHERE id = 5
  AND question_text = '계절이 바뀔 때 기억나는 냄새가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 5
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '풀냄새나 찬 바람 혹은 낙엽 향을 생각해봐요.'
WHERE id = 5
  AND hint_guide = '풀냄새나 찬 바람 혹은 낙엽 향을 생각해보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 6
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '사과, 포도, 딸기 등 떠오르는 걸 말해봐요.'
WHERE id = 6
  AND hint_guide = '사과, 포도, 딸기 등 떠오르는 걸 말해보세요.';

UPDATE daily_questions
SET empathy_guide = '과일의 단맛은 기분을 좋게 만들어줘요.'
WHERE id = 6
  AND empathy_guide = '과일의 단맛은 행복하게 만들죠,';

UPDATE daily_questions
SET leading_question_guide = '그 과일과 관련된 추억이 있나요?'
WHERE id = 6
  AND leading_question_guide = '그 과일을 먹을 때 어떤 즐거움이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 7
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '자주 쓰는 머그컵이나 유리잔을 봐요.'
WHERE id = 7
  AND hint_guide = '자주 쓰는 머그컵이나 유리잔을 보세요.';

UPDATE daily_questions
SET empathy_guide = '매일 쓰는 물건에도 은근히 정이 가잖아요.'
WHERE id = 7
  AND empathy_guide = '매일 쓰는 물건에도 정이 가기 마련이에요.';

UPDATE daily_questions
SET hint_guide = '브이로그, 운동, 공부 등 즐겨찾기를 봐요.'
WHERE id = 8
  AND hint_guide = '브이로그, 운동, 공부 등 즐겨찾기를 보세요.';

UPDATE daily_questions
SET empathy_guide = '요즘 빠져있는 영상이 있을 거예요.'
WHERE id = 8
  AND empathy_guide = '관심사가 어디에 있는지 보여주는 신호예요.';

UPDATE daily_questions
SET question_text = '가장 좋아하는 날씨는 어떤 건가요?'
WHERE id = 9
  AND question_text = '가장 좋아하는 날씨는 어떤 상태인가요?';

UPDATE daily_questions
SET leading_question_guide = '그런 날씨에 밖에 나가면 어떤 기분이 드나요?'
WHERE id = 9
  AND leading_question_guide = '그런 날씨에 밖을 나가면 어떤 기분인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 10
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '달콤한 크림빵이나 담백한 식빵을 떠올려 봐요.'
WHERE id = 10
  AND hint_guide = '달콤한 크림빵이나 담백한 식빵을 떠올려 보세요.';

UPDATE daily_questions
SET empathy_guide = '빵집 문 열면 손이 먼저 가는 곳이 있잖아요.'
WHERE id = 10
  AND empathy_guide = '취향이 가장 솔직하게 드러나는 순간이에요.';

UPDATE daily_questions
SET hint_guide = '최근에 가본 장소나 운동을 찾아봐요.'
WHERE id = 11
  AND hint_guide = '최근에 가본 장소나 운동을 찾아보세요.';

UPDATE daily_questions
SET empathy_guide = '새로운 걸 해보면 의외로 나에 대해 알게 되기도 해요.'
WHERE id = 11
  AND empathy_guide = '몰랐던 모습을 발견하는 건 신선한 경험이에요.';

UPDATE daily_questions
SET leading_question_guide = '그걸 하면서 나에 대해 새로 알게 된 게 있나요?'
WHERE id = 11
  AND leading_question_guide = '이 일이 일상을 어떻게 생기 있게 만드나요?';

UPDATE daily_questions
SET hint_guide = '자주 가는 카페나 공원 벤치를 떠올려 봐요.'
WHERE id = 12
  AND hint_guide = '자주 가는 카페나 공원 벤치를 떠올려 보세요.';

UPDATE daily_questions
SET hint_guide = '거울이나 메모장 혹은 이어폰을 확인해봐요.'
WHERE id = 13
  AND hint_guide = '거울이나 메모장 혹은 이어폰을 확인해보세요.';

UPDATE daily_questions
SET empathy_guide = '가방 속에 꼭 넣고 다니는 것들이 있잖아요.'
WHERE id = 13
  AND empathy_guide = '물건 하나에도 평소 습관이 담겨 있어요.';

UPDATE daily_questions
SET leading_question_guide = '이 물건이 나에 대해 말해주는 건 뭘까요?'
WHERE id = 13
  AND leading_question_guide = '이 물건에 담긴 모습은 어떤 걸까요?';

UPDATE daily_questions
SET hint_guide = '침대 위나 소파 한쪽을 생각해봐요.'
WHERE id = 14
  AND hint_guide = '침대 위나 소파 한쪽을 생각해보세요.';

UPDATE daily_questions
SET question_text = '주말 아침에 가장 하고 싶은 일은 무엇인가요?'
WHERE id = 15
  AND question_text = '주말 아침에 가장 하고 싶은 활동은 무엇인가요?';

UPDATE daily_questions
SET hint_guide = '늦잠? 브런치? 산책? 뭐가 먼저 떠오르나요?'
WHERE id = 15
  AND hint_guide = '느지막한 식사나 가벼운 산책을 그려보세요.';

UPDATE daily_questions
SET leading_question_guide = '그 시간이 왜 그렇게 소중하게 느껴질까요?'
WHERE id = 15
  AND leading_question_guide = '이 활동이 주말을 어떻게 더 즐겁게 할까요?';

UPDATE daily_questions
SET leading_question_guide = '그 신발을 신고 어디로 가고 싶나요?'
WHERE id = 16
  AND leading_question_guide = '그 신발을 신고 어디로 가고 싶은가요?';

UPDATE daily_questions
SET hint_guide = '현지 맛집, 예쁜 카페, 기념품 가게 혹은 미술관인가요?'
WHERE id = 17
  AND hint_guide = '조용한 사찰, 현지인 추천 맛집, 기념품 가게 혹은 미술관인가요?';

UPDATE daily_questions
SET empathy_guide = '여행을 가기도 전부터 꼭 가보고 싶은 곳이 있잖아요.'
WHERE id = 17
  AND empathy_guide = '여행을 가기도 전부터 꼭 가고 싶은 공간이 있기 마련이죠.';

UPDATE daily_questions
SET leading_question_guide = '그 기준이 나에 대해 뭘 말해주는 것 같나요?'
WHERE id = 18
  AND leading_question_guide = '어떤 분위기에 끌려 책을 집어 드나요?';

UPDATE daily_questions
SET hint_guide = '편안한 옷이나 화려한 옷을 떠올려 봐요.'
WHERE id = 19
  AND hint_guide = '편안한 옷이나 화려한 옷을 떠올려 보세요.';

UPDATE daily_questions
SET question_text = '좋아하는 향이 있다면, 어떤 향인가요?'
WHERE id = 20
  AND question_text = '좋아하는 향수나 섬유유연제 향이 있나요?';

UPDATE daily_questions
SET question_text = '남들은 모르지만 혼자 발견한 풍경이 있다면, 어떤 풍경인가요?'
WHERE id = 21
  AND question_text = '남들은 모르지만 혼자 발견한 풍경이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 21
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '길가에 핀 꽃이나 특이한 구름을 떠올려 봐요.'
WHERE id = 21
  AND hint_guide = '길가에 핀 꽃이나 특이한 구름을 떠올려 보세요.';

UPDATE daily_questions
SET empathy_guide = '나만 발견한 풍경이 있으면 왜지 뿌듯하잖아요.'
WHERE id = 21
  AND empathy_guide = '자신만의 시선으로 세상을 보는 건 멋진 일이에요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 22
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '소설이나 예술 혹은 자기계발 분야를 골라봐요.'
WHERE id = 22
  AND hint_guide = '소설이나 예술 혹은 자기계발 분야를 골라보세요.';

UPDATE daily_questions
SET empathy_guide = '어떤 주제에 꽂히면 시간 가는 줄 모르잖아요.'
WHERE id = 22
  AND empathy_guide = '호기심이 어디로 향하는지 살펴보는 시간이에요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 23
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '생존 아이템 말고 마음이 편해지는 걸로 골라봐요.'
WHERE id = 23
  AND hint_guide = '즐거움을 주는 물건이나 책을 골라보세요.';

UPDATE daily_questions
SET leading_question_guide = '그걸 고른 이유가 나에 대해 뭘 말해주는 것 같나요?'
WHERE id = 23
  AND leading_question_guide = '이 선택이 삶에서 왜 그렇게 중요할까요?';

UPDATE daily_questions
SET question_text = '입었을 때 가장 나다운 옷차림은 무엇인가요?'
WHERE id = 24
  AND question_text = '입었을 때 가장 본인다운 옷차림은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 24
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '그 옷을 입은 나는 어떤 기분인가요?'
WHERE id = 24
  AND leading_question_guide = '그 옷을 입었을 때 왜 자신감이 생기나요?';

UPDATE daily_questions
SET question_text = '나만 알고 있는 맛집이 있다면, 어떤 곳인가요?'
WHERE id = 25
  AND question_text = '나만 알고 있는 맛집이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 25
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '숨겨진 식당이나 작은 카페를 떠올려 봐요.'
WHERE id = 25
  AND hint_guide = '숨겨진 식당이나 작은 카페를 떠올려 보세요.';

UPDATE daily_questions
SET question_text = '스트레스를 받을 때 찾게 되는 노래는 어떤 분위기인가요?'
WHERE id = 26
  AND question_text = '스트레스를 받을 때 듣는 플레이리스트가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 26
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 27
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '여행 계획이나 미래의 내 모습을 떠올려 봐요.'
WHERE id = 27
  AND hint_guide = '여행 계획이나 미래의 내 모습을 떠올려 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 28
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '장미, 튤립 혹은 들꽃을 생각해봐요.'
WHERE id = 28
  AND hint_guide = '장미, 튤립 혹은 들꽃을 생각해보세요.';

UPDATE daily_questions
SET question_text = '사진 찍을 때 주로 담는 장면은 무엇인가요?'
WHERE id = 29
  AND question_text = '사진 찍을 때 주로 담는 피사체는 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 29
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '폰 갤러리를 열면 어떤 사진이 가장 많은지 봐봐요.'
WHERE id = 29
  AND empathy_guide = '기억하고 싶은 순간이 사진에 남아요.';

UPDATE daily_questions
SET question_level = 1
WHERE id = 30
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '넷플릭스 켜면 제일 먼저 뭘 찾나요?'
WHERE id = 30
  AND hint_guide = '로맨스, 스릴러 혹은 다큐멘터리처럼 각양각색의 영화가 있죠.';

UPDATE daily_questions
SET empathy_guide = '영화 속에서 다양한 세상을 만날 수 있잖아요.'
WHERE id = 30
  AND empathy_guide = '영화를 통해서는 다양한 세상을 접할 수 있어요.';

UPDATE daily_questions
SET leading_question_guide = '그 장르에 끌리는 이유가 뭘까요?'
WHERE id = 30
  AND leading_question_guide = '그 중에서도 가장 좋아하는 장르가 주는 즐거움은 무엇일까요?';

UPDATE daily_questions
SET question_text = '좋아하는 목소리나 말투가 있다면, 어떤 느낌인가요?'
WHERE id = 31
  AND question_text = '매력을 느끼는 목소리나 말투가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 31
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '차분한 말투나 밝은 목소리를 떠올려 봐요.'
WHERE id = 31
  AND hint_guide = '차분한 말투나 밝은 목소리를 떠올려 보세요.';

UPDATE daily_questions
SET question_text = '오래된 물건 중 버리지 못하는 게 있다면, 어떤 물건인가요?'
WHERE id = 32
  AND question_text = '오래된 물건 중 버리지 못하는 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 32
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '누군가에게 선물할 때 가장 신경 쓰는 건 무엇인가요?'
WHERE id = 33
  AND question_text = '누군가에게 선물할 때 가장 고려하는 것은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 33
  AND question_level = 4;

UPDATE daily_questions
SET empathy_guide = '선물 고르는 데도 나만의 스타일이 있잖아요.'
WHERE id = 33
  AND empathy_guide = '나의 마음을 전달하는 나만의 기준이 있어요.';

UPDATE daily_questions
SET question_text = '어떤 라이프스타일로 살고 싶나요?'
WHERE id = 34
  AND question_text = '추구하는 라이프스타일이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 34
  AND question_level = 4;

UPDATE daily_questions
SET empathy_guide = '"와, 저렇게 살고 싶다!" 하고 느낀 적 있을 거예요.'
WHERE id = 34
  AND empathy_guide = '와 저렇게 살고 싶다! 라고 생각되는 라이프스타일, 곰곰이 생각해보면 하나쯤은 떠오를 거예요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 35
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '글귀나 그림 혹은 음악 스타일을 떠올려 봐요.'
WHERE id = 35
  AND hint_guide = '글귀나 그림 혹은 음악 스타일을 떠올려 보세요.';

UPDATE daily_questions
SET empathy_guide = '유독 마음이 가는 작가나 아티스트가 있잖아요.'
WHERE id = 35
  AND empathy_guide = '취향의 정점이 누구인지 확인해보는 시간이에요.';

UPDATE daily_questions
SET question_text = '나만의 특별한 요리 레시피가 있다면, 어떤 건가요?'
WHERE id = 36
  AND question_text = '나만의 특별한 요리 레시피가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 36
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '간단한 라면이나 정성 들인 찌개를 생각해봐요.'
WHERE id = 36
  AND hint_guide = '간단한 라면이나 정성 들인 찌개를 생각해보세요.';

UPDATE daily_questions
SET leading_question_guide = '특별 레시피를 만들게 된 이야기도 들려줘요.'
WHERE id = 36
  AND leading_question_guide = '특별 레시피를 만들게 된 이야기도 들려주세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 37
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '전시회에서 작품을 보는 나만의 방식이 있다면, 어떤 건가요?'
WHERE id = 38
  AND question_text = '전시회에 가면 작품을 보는 나만의 방식이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 38
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '천천히 하나씩 보는지 맘에 드는 것만 보는지 생각해봐요.'
WHERE id = 38
  AND hint_guide = '천천히 하나씩 보는지 맘에 드는 것만 보는지 생각해보세요.';

UPDATE daily_questions
SET empathy_guide = '전시회에서 나만의 감상법이 있다면 그것도 취향이죠.'
WHERE id = 38
  AND empathy_guide = '예술을 즐기는 태도도 중요한 취향이에요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 39
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '벚꽃 핀 거리나 단풍 든 산을 그려봐요.'
WHERE id = 39
  AND hint_guide = '벚꽃 핀 거리나 단풍 든 산을 그려보세요.';

UPDATE daily_questions
SET leading_question_guide = '그 풍경을 보면 어떤 기분이 드나요?'
WHERE id = 39
  AND leading_question_guide = '그 풍경을 볼 때 마음의 변화에 대해서도 떠올려봐요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 40
  AND question_level = 4;

UPDATE daily_questions
SET leading_question_guide = '모으기 시작한 계기가 있나요?'
WHERE id = 40
  AND leading_question_guide = '왜 모으게 됐는지에 대해서도 떠올려봐요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 41
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '어떤 분위기의 사람이 되고 싶은지 상상해봐요.'
WHERE id = 41
  AND hint_guide = '내가 바라는 단단한 모습을 그려보세요.';

UPDATE daily_questions
SET empathy_guide = '10년 뒤의 나, 어떤 분위기를 풍기고 있을까요?'
WHERE id = 41
  AND empathy_guide = '미래의 모습을 상상하면 오늘을 살 힘이 생겨요.';

UPDATE daily_questions
SET question_text = '내가 모은 물건들의 공통점은 무엇인가요?'
WHERE id = 42
  AND question_text = '나의 미적 기준은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 42
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '공통점을 떠올리면 나만의 미적 기준이 보일 거예요.'
WHERE id = 42
  AND hint_guide = '공통점을 떠올리면 나만의 미적 기준, 취향에 대해 생각해보기 쉬울 거예요.';

UPDATE daily_questions
SET leading_question_guide = '그 기준이 나에 대해 뭘 말해주는 것 같나요?'
WHERE id = 42
  AND leading_question_guide = '나만의 미적기준이 생기게 된 이유도 생각해봐요.';

UPDATE daily_questions
SET question_text = '딱 한 권만 남길 수 있다면 어떤 책인가요?'
WHERE id = 43
  AND question_text = '인생에서 딱 한 권의 책만 남긴다면 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 43
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '가장 감명 깊게 읽은 책을 생각해봐요.'
WHERE id = 43
  AND hint_guide = '가장 감명 깊게 읽은 책을 생각해보세요.';

UPDATE daily_questions
SET empathy_guide = '나에게 가장 큰 울림을 준 책이 있을 거예요.'
WHERE id = 43
  AND empathy_guide = '내 인생의 철학이 담긴 책을 고르는 일이에요.';

UPDATE daily_questions
SET leading_question_guide = '이 책이 나에게 어떤 걸 남겨줬나요?'
WHERE id = 43
  AND leading_question_guide = '이 책이 나에게 어떤 가르침을 주었나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 44
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '따뜻함, 성실함, 자유 같은 단어를 생각해봐요.'
WHERE id = 44
  AND hint_guide = '따뜻함, 성실함, 자유 같은 단어를 생각해보세요.';

UPDATE daily_questions
SET empathy_guide = '나를 딱 한 단어로 표현한다면 뭐가 떠오를까요?'
WHERE id = 44
  AND empathy_guide = '나라는 사람을 한 줄로 정의해보는 시간이에요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 45
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '내 취향이 남들과 가장 다른 점은 뭘까요?'
WHERE id = 46
  AND question_text = '나의 취향이 다른 사람과 가장 다른 점은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 46
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '남들은 잘 모르는 나만의 취향을 떠올려봐요.'
WHERE id = 46
  AND hint_guide = '독특한 식성이나 음악 취향을 생각해보세요.';

UPDATE daily_questions
SET empathy_guide = '남들과 좀 다른 내 취향, 은근 자랑스럽잖아요.'
WHERE id = 46
  AND empathy_guide = '나만의 개성을 발견하는 중요한 질문이에요.';

UPDATE daily_questions
SET question_text = '상상 속 완벽한 하루는 어떤 모습인가요?'
WHERE id = 47
  AND question_text = '꿈꾸는 완벽한 하루는 어떤 모습인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 47
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '아침에 눈 뜨는 것부터 잠들기까지 상상해봐요.'
WHERE id = 47
  AND hint_guide = '아침부터 밤까지의 일정을 쭉 그려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 48
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '성격이나 대화 스타일을 떠올려 봐요.'
WHERE id = 48
  AND hint_guide = '성격이나 대화 스타일을 떠올려 보세요.';

UPDATE daily_questions
SET leading_question_guide = '그 공통점이 나에 대해 뭘 알려주나요?'
WHERE id = 48
  AND leading_question_guide = '왜 그런 사람들에게 자꾸 마음이 갈까요?';

UPDATE daily_questions
SET question_text = '누군가 나에 대한 책을 쓴다면 제목은 뭘까요?'
WHERE id = 49
  AND question_text = '누군가 나에 대해 쓴다면 어떤 제목의 책일까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 49
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '재밌는 제목이나 진지한 제목을 생각해봐요.'
WHERE id = 49
  AND hint_guide = '재밌는 제목이나 진지한 제목을 생각해보세요.';

UPDATE daily_questions
SET question_text = '예전엔 좋아했는데 지금은 아닌 취향이 있다면, 어떤 건가요?'
WHERE id = 50
  AND question_text = '취향이 변했다면 그 계기는 무엇이었나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 50
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '예전엔 싫었지만 지금은 좋은 것을 찾아봐요.'
WHERE id = 50
  AND hint_guide = '예전엔 싫었지만 지금은 좋은 것을 찾아보세요.';

UPDATE daily_questions
SET empathy_guide = '취향이 바뀌면 나도 바뀐 거잖아요.'
WHERE id = 50
  AND empathy_guide = '변화는 성장의 증거이기도 해요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 53
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '커피 한 잔이나 퇴근길 풍경이었나요?'
WHERE id = 53
  AND hint_guide = '커피 한 잔이나 퇴근길 풍경이었나요.';

UPDATE daily_questions
SET empathy_guide = '잠시 숨을 고르던 때를 기억해 봐요.'
WHERE id = 53
  AND empathy_guide = '잠시 숨을 고르던 때를 기억해 보세요.';

UPDATE daily_questions
SET question_text = '최근에 눈물이 났던 적이 있다면, 어떤 순간이었나요?'
WHERE id = 54
  AND question_text = '최근에 눈물이 났던 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 54
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '슬픈 영화나 속상했던 일을 떠올려 봐요.'
WHERE id = 54
  AND hint_guide = '슬픈 영화나 속상했던 일을 떠올려 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 55
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '친구와 수다 떨던 일이나 예능 프로그램을 생각해봐요.'
WHERE id = 57
  AND hint_guide = '친구와 수다 떨던 일이나 예능 프로그램을 생각해보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 59
  AND question_level = 1;

UPDATE daily_questions
SET question_level = 2
WHERE id = 60
  AND question_level = 1;

UPDATE daily_questions
SET question_text = '불안한 마음이 들 때 나를 잡아주는 문장이 있다면, 어떤 문장인가요?'
WHERE id = 62
  AND question_text = '불안한 마음이 들 때 나를 잡아주는 문장이 있나요?';

UPDATE daily_questions
SET question_text = '내일 일어날 일 중 설레는 게 있다면, 어떤 건가요?'
WHERE id = 63
  AND question_text = '내일 일어날 일 중 설레는 게 있나요?';

UPDATE daily_questions
SET empathy_guide = '쑥스러우면서도 기분 좋은 순간을 느껴봐요.'
WHERE id = 64
  AND empathy_guide = '쑥스러우면서도 기분 좋은 순간을 느껴보세요.';

UPDATE daily_questions
SET leading_question_guide = '혼자만의 시간이 나에게 어떤 의미인가요?'
WHERE id = 65
  AND leading_question_guide = '그 감정을 해소하거나 더 느끼기 위해서 어떤 활동을 하고 있나요?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 66
  AND question_level = 2;

UPDATE daily_questions
SET question_text = '최근에 질투를 느꼈던 순간이 있다면 어떤 상황이었나요?'
WHERE id = 67
  AND question_text = '질투라는 감정을 느낀 적이 있나요?';

UPDATE daily_questions
SET hint_guide = '그 때의 대상이나 상황을 떠올려 봐요.'
WHERE id = 67
  AND hint_guide = '부러운 대상이나 상황을 떠올려 보세요.';

UPDATE daily_questions
SET leading_question_guide = '내가 왜 그 순간 마음이 흔들렸을까요?'
WHERE id = 67
  AND leading_question_guide = '내가 왜 그 지점에서 마음이 흔들렸을까요?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 68
  AND question_level = 2;

UPDATE daily_questions
SET question_text = '최근에 용기를 냈던 경험이 있다면, 어떤 순간이었나요?'
WHERE id = 69
  AND question_text = '최근에 용기를 냈던 경험이 있나요?';

UPDATE daily_questions
SET question_text = '기분이 울적할 때 찾게 되는 음식은 무엇인가요?'
WHERE id = 70
  AND question_text = '기분이 울적할 때 찾는 음식이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 71
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '찬물 마시기나 짧은 산책을 생각해봐요.'
WHERE id = 71
  AND hint_guide = '찬물 마시기나 짧은 산책을 생각해보세요.';

UPDATE daily_questions
SET question_text = '슬픈 감정을 실컷 쏟아내고 나면 어떤가요?'
WHERE id = 72
  AND question_text = '슬픈 기분을 충분히 표현하고 나면 어떤가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 72
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '남들 시선을 많이 신경 쓰는 편인가요? 어떤 상황에서 특히 그런가요?'
WHERE id = 73
  AND question_text = '남들의 시선을 얼마나 신경 쓰는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 73
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '최근에 느낀 감정 중 이름 붙이기 힘든 게 있었다면, 어떤 느낌이었나요?'
WHERE id = 74
  AND question_text = '최근에 느낀 감정 중 이름 붙이기 힘든 게 있었나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 74
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '내가 생각하는 나만의 장점은 무엇인가요?'
WHERE id = 75
  AND question_text = '나를 가장 자부심 느끼게 하는 특징은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 75
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '그 점이 일상에서 어떤 힘이 되나요?'
WHERE id = 75
  AND leading_question_guide = '이 특징이 삶 속에서 어떻게 작용하고 있을까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 76
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '내가 가장 예민하게 반응하는 포인트는 무엇인가요?'
WHERE id = 77
  AND question_text = '내가 가장 예민하게 반응하는 지점은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 77
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '과거의 나에게 해주고 싶은 위로가 있다면, 어떤 말인가요?'
WHERE id = 78
  AND question_text = '과거의 나에게 해주고 싶은 위로가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 78
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 79
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '소소한 일상이 행복인지, 큰 성취가 행복인지 떠올려봐요.'
WHERE id = 79
  AND hint_guide = '돈, 건강, 여유 혹은 사랑인가요?';

UPDATE daily_questions
SET question_text = '최근에 누군가를 용서했던 경험이 있다면, 어떤 일이었나요?'
WHERE id = 80
  AND question_text = '최근에 누군가를 용서한 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 80
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '감정을 솔직하게 표현해서 후련했던 순간이 있다면, 어떤 때였나요?'
WHERE id = 81
  AND question_text = '감정을 솔직하게 표현해서 후련했던 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 81
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '두려움을 극복하고 무언가 해낸 적이 있다면, 어떤 일이었나요?'
WHERE id = 82
  AND question_text = '두려움을 극복하고 무언가 해낸 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 82
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '발표나 시험, 혹은 낯선 곳으로 떠난 여행인가요?'
WHERE id = 82
  AND hint_guide = '발표나 시험 혹은 낯선 곳으로의 여행인가요?';

UPDATE daily_questions
SET question_text = '내 감정을 다스리는 나만의 루틴이 있다면, 어떤 건가요?'
WHERE id = 83
  AND question_text = '내 감정을 다스리는 나만의 루틴이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 83
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '남에게 말하지 못한 비밀스러운 걱정이 있다면, 어떤 건가요?'
WHERE id = 84
  AND question_text = '남에게 말하지 못한 비밀스러운 걱정이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 84
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 85
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '한참 슬펐을 때 나를 건져준 건 무엇인가요?'
WHERE id = 86
  AND question_text = '슬픔에 깊이 빠졌을 때 나를 건져 올린 것은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 86
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '다른 사람을 보며 멋지다고 느끼는 포인트는 무엇인가요?'
WHERE id = 87
  AND question_text = '내가 가장 아름답다고 느끼는 사람의 모습은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 87
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '최근에 나의 한계를 느꼈던 적이 있다면, 어떤 순간이었나요?'
WHERE id = 88
  AND question_text = '최근에 나의 한계를 느꼈던 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 88
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '더 이상은 무리다 싶었던 때를 떠올려봐요.'
WHERE id = 88
  AND hint_guide = '체력의 한계나 능력의 부족함이었나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 89
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '지금 내가 가장 필요한 감정은 뭘까요?'
WHERE id = 90
  AND question_text = '지금 이 순간 내가 가장 필요로 하는 감정은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 90
  AND question_level = 4;

UPDATE daily_questions
SET leading_question_guide = '그 감정을 채우기 위해 오늘 뭘 해볼 수 있을까요?'
WHERE id = 90
  AND leading_question_guide = '그 감정을 채우기 위해 오늘 무엇을 시도해 볼 계획인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 91
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '나에 대해 가장 좋아하는 점을 한번 떠올려봐요.'
WHERE id = 91
  AND empathy_guide = '보석 중 가장 빛나는 부분을 골라보는 기분이에요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 92
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '오늘 하루도 수고했어요. 나에게 한마디 건네봐요.'
WHERE id = 92
  AND empathy_guide = '누가 뭐래도 언제나 내 편이 되어줄 수 있어요.';

UPDATE daily_questions
SET question_text = '내가 세상에 태어나길 잘했다고 느낀 순간이 있다면, 어떤 때였나요?'
WHERE id = 93
  AND question_text = '내가 세상에 태어나길 잘했다고 느낀 순간이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 93
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '살아있어서 좋다고 느낀 순간, 있잖아요.'
WHERE id = 93
  AND empathy_guide = '삶의 의미를 발견하는 가장 벅찬 질문이에요.';

UPDATE daily_questions
SET question_text = '가장 힘들었던 순간에서 배운게 있다면 무엇인가요?'
WHERE id = 94
  AND question_text = '고통스러운 순간이 나에게 준 가르침이 있다면 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 94
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '힘든 시간을 지나면서 배운 게 있잖아요.'
WHERE id = 94
  AND empathy_guide = '아픔은 때로 나를 더 깊은 사람으로 만들어요.';

UPDATE daily_questions
SET question_text = '내가 생각하는 진짜 용기란 무엇인가요?'
WHERE id = 95
  AND question_text = '내가 생각하는 진정한 용기란 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 95
  AND question_level = 5;

UPDATE daily_questions
SET leading_question_guide = '지금 내가 용기 내고 싶은 건 뭐나요?'
WHERE id = 95
  AND leading_question_guide = '나는 지금 그 용기를 실천하며 살고 있나요?';

UPDATE daily_questions
SET question_text = '내 인생에 가장 큰 영향을 준 감정은 뭘까요?'
WHERE id = 96
  AND question_text = '인생에서 가장 큰 영향을 준 감정은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 96
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '돌이켜보면 나를 이끌어온 감정이 하나 있을 거예요.'
WHERE id = 96
  AND empathy_guide = '내 삶의 방향을 결정한 감정의 줄기가 있을 거예요.';

UPDATE daily_questions
SET question_text = '마지막 순간에 느끼고 싶은 감정이 있다면, 어떤 감정인가요?'
WHERE id = 97
  AND question_text = '내가 죽기 전 마지막으로 느끼고 싶은 감정은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 97
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '인생의 끝에서 느끼고 싶은 감정, 한번 상상해봐요.'
WHERE id = 97
  AND empathy_guide = '인생 전체를 돌아보게 하는 마지막 질문이에요.';

UPDATE daily_questions
SET question_text = '나만 아는 내 마음의 모양은 어떤 걸까요?'
WHERE id = 98
  AND question_text = '세상에 오직 나만 알고 있는 내 마음은 어떤 모양인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 98
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '나에게 사랑이란 어떤 감정인가요?'
WHERE id = 99
  AND question_text = '사랑이라는 감정을 어떻게 정의하고 싶나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 99
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '내 인생을 감정 한 줄로 요약하면 뭘까요?'
WHERE id = 100
  AND question_text = '지금까지의 내 인생을 한 줄의 감정으로 요약한다면 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 100
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '내 인생을 한 줄로 적는다면 어떤 감정일까요?'
WHERE id = 100
  AND empathy_guide = '내 삶을 관통하는 하나의 느낌을 찾아보세요.';

UPDATE daily_questions
SET question_text = '매일 밤 자기 전 꼭 지키는 습관이 있다면, 어떤 건가요?'
WHERE id = 102
  AND question_text = '매일 밤 자기 전 꼭 지키는 습관이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 102
  AND question_level = 1;

UPDATE daily_questions
SET question_level = 2
WHERE id = 103
  AND question_level = 1;

UPDATE daily_questions
SET empathy_guide = '내 몸의 리듬이 언제 가장 활발한지 봐요.'
WHERE id = 103
  AND empathy_guide = '내 몸의 리듬이 언제 가장 활발한지 보세요.';

UPDATE daily_questions
SET empathy_guide = '쉴 때만큼은 몸이 원하는 대로 맡겨봐요.'
WHERE id = 104
  AND empathy_guide = '쉴 때만큼은 몸이 원하는 대로 맡겨보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 105
  AND question_level = 1;

UPDATE daily_questions
SET leading_question_guide = '이 물건을 챙기고 다닌 뒤로 일상이 어떻게 달라졌나요?'
WHERE id = 105
  AND leading_question_guide = '이 물건을 챙기고 다닌 이후로 삶은 어떻게 변화했나요?';

UPDATE daily_questions
SET question_text = '식사 후에 습관적으로 하는 행동이 있다면, 어떤 건가요?'
WHERE id = 106
  AND question_text = '식사 후에 습관적으로 하는 행동이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 106
  AND question_level = 1;

UPDATE daily_questions
SET empathy_guide = '밥을 먹고 난 뒤의 작은 여유를 봐요.'
WHERE id = 106
  AND empathy_guide = '밥을 먹고 난 뒤의 작은 여유를 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 107
  AND question_level = 1;

UPDATE daily_questions
SET leading_question_guide = '그 행동을 할 때 마음이 얼마나 편해지나요?'
WHERE id = 108
  AND leading_question_guide = '그 행동을 할 때 마음이 얼마나 놓이나요?';

UPDATE daily_questions
SET question_text = '하루를 시작할 때 듣는 음악 장르가 있다면, 어떤 장르인가요?'
WHERE id = 110
  AND question_text = '하루를 시작할 때 듣는 음악 장르가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 110
  AND question_level = 1;

UPDATE daily_questions
SET empathy_guide = '몰입의 즐거움을 느꼈던 곳을 떠올려 봐요.'
WHERE id = 112
  AND empathy_guide = '몰입의 즐거움을 느꼈던 곳을 떠올려 보세요.';

UPDATE daily_questions
SET question_text = '스트레스가 쌓였을 때 나도 모르게 하는 행동이 있다면, 어떤 건가요?'
WHERE id = 113
  AND question_text = '스트레스가 쌓였을 때 나도 모르게 하는 행동이 있나요?';

UPDATE daily_questions
SET question_text = '미루고 싶을 때 나를 움직이게 하는 나만의 방법이 있다면, 어떤 건가요?'
WHERE id = 115
  AND question_text = '할 일을 미루고 싶을 때 나를 움직이게 하는 방법이 있나요?';

UPDATE daily_questions
SET question_text = '반복되는 일상에서 가장 지루하게 느끼는 순간은 언제인가요?'
WHERE id = 116
  AND question_text = '매일 반복되는 일상에서 가장 지루한 순간은 언제인가요?';

UPDATE daily_questions
SET empathy_guide = '반복의 권태로움을 느끼는 지점을 찾아봐요.'
WHERE id = 116
  AND empathy_guide = '반복의 권태로움을 느끼는 지점을 찾아보세요.';

UPDATE daily_questions
SET empathy_guide = '습관을 바꾸는 건 정말 쉽지 않은 일이에요.'
WHERE id = 117
  AND empathy_guide = '습관을 바꾸는 건 결코 쉬운 일이 아니에요.';

UPDATE daily_questions
SET question_text = '하루 일정을 정리하는 나만의 도구가 있다면, 어떤 건가요?'
WHERE id = 118
  AND question_text = '하루 일정을 정리하는 나만의 도구가 있나요?';

UPDATE daily_questions
SET question_text = '내가 지키는 습관 중 남들에게 추천하고 싶은 게 있다면, 어떤 건가요?'
WHERE id = 119
  AND question_text = '내가 지키는 습관 중 남들에게 추천하고 싶은 게 있나요?';

UPDATE daily_questions
SET empathy_guide = '휴식과 업무의 균형을 어떻게 맞추는지 봐요.'
WHERE id = 120
  AND empathy_guide = '휴식과 업무의 균형을 어떻게 맞추는지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 121
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '늦잠 자기나 대청소 혹은 취미 활동을 생각해봐요.'
WHERE id = 121
  AND hint_guide = '늦잠 자기나 대청소 혹은 취미 활동을 생각해보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 122
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '쉬운 일부터 하는지 아니면 급한 일부터 하는지 봐요.'
WHERE id = 122
  AND hint_guide = '쉬운 일부터 하는지 아니면 급한 일부터 하는지 보세요.';

UPDATE daily_questions
SET question_text = '건강을 위해 매일 챙겨 먹는 게 있다면, 어떤 건가요?'
WHERE id = 123
  AND question_text = '건강을 위해 매일 챙겨 먹는 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 123
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '비타민이나 물 마시기 혹은 건강식을 떠올려 봐요.'
WHERE id = 123
  AND hint_guide = '비타민이나 물 마시기 혹은 건강식을 떠올려 보세요.';

UPDATE daily_questions
SET question_text = '약속 시간에 늦지 않기 위해 하는 준비가 있다면, 어떤 건가요?'
WHERE id = 124
  AND question_text = '약속 시간에 늦지 않기 위해 하는 준비가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 124
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '시간을 대하는 태도에 성격이 묻어나잖아요.'
WHERE id = 124
  AND empathy_guide = '시간을 대하는 태도에 성격이 묻어있기 마련이에요.';

UPDATE daily_questions
SET question_text = '가계부를 쓰거나 돈을 관리하는 방법이 있다면, 어떤 건가요?'
WHERE id = 125
  AND question_text = '가계부를 쓰거나 돈을 관리하는 방법이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 125
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '업무나 공부를 시작하기 전 마음을 가다듬는 행동이 있다면, 어떤 건가요?'
WHERE id = 126
  AND question_text = '업무나 공부를 시작하기 전 마음을 가다듬는 행동이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 126
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '운동하기 싫을 때 나를 움직이게 하는 주문이 있다면, 어떤 건가요?'
WHERE id = 127
  AND question_text = '운동하기 싫을 때 나를 움직이게 하는 주문이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 127
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '일단 나가자'' 같은 짧은 생각이나 보상 약속인가요?'
WHERE id = 127
  AND hint_guide = '''일단 나가자'' 같은 짧은 생각이나 보상 약속인가요?';

UPDATE daily_questions
SET question_text = '여행 가방을 싸는 나만의 규칙이 있다면, 어떤 건가요?'
WHERE id = 128
  AND question_text = '여행 가방을 싸는 나만의 규칙이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 128
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '기념일에 반복적으로 하는 일이 있다면, 어떤 건가요?'
WHERE id = 129
  AND question_text = '기념일에 반복적으로 하는 일이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 129
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 130
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 131
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '중요한 일을 앞두고 지키는 징크스 같은 행동이 있다면, 어떤 건가요?'
WHERE id = 132
  AND question_text = '중요한 일을 앞두고 지키는 징크스 같은 행동이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 132
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '계절이 바뀔 때 집 안에서 바꾸는 게 있다면, 어떤 건가요?'
WHERE id = 133
  AND question_text = '계절이 바뀔 때 집 안에서 바꾸는 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 133
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 134
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '산책 시간이나 밥 먹는 시간 혹은 아예 없는지 봐요.'
WHERE id = 134
  AND hint_guide = '산책 시간이나 밥 먹는 시간 혹은 아예 없는지 보세요.';

UPDATE daily_questions
SET question_text = '화난 마음을 가라앉히는 나만의 루틴이 있다면, 어떤 건가요?'
WHERE id = 135
  AND question_text = '화난 마음을 가라앉히는 나만의 루틴이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 135
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '매일 일기를 쓰거나 기록을 남기는 습관이 있다면, 어떤 방식인가요?'
WHERE id = 136
  AND question_text = '매일 일기를 쓰거나 기록을 남기는 습관이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 136
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 137
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '새로운 취미를 시작할 때 거치는 과정이 있다면, 어떤 건가요?'
WHERE id = 138
  AND question_text = '새로운 취미를 시작할 때 거치는 과정이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 138
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 139
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '중요한 약속 전날 꼭 하는 준비가 있다면, 어떤 건가요?'
WHERE id = 140
  AND question_text = '중요한 약속 전날 꼭 하는 준비가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 140
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '지금의 습관이 10년 뒤 나를 어디로 데려갈까요?'
WHERE id = 141
  AND question_text = '지금 지키는 습관이 10년 뒤 나를 어디로 데려갈까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 141
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '건강 습관이나 공부 습관을 떠올려 봐요.'
WHERE id = 141
  AND hint_guide = '건강 습관이나 공부 습관을 떠올려 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 142
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '일찍 일어나기나 정직하게 말하기 같은 건가요?'
WHERE id = 142
  AND hint_guide = '일찍 일어나기나 정직하게 말하기 같은 것인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 143
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '습관을 보면 나라는 사람이 보이기도 해요.'
WHERE id = 143
  AND empathy_guide = '사소한 행동 하나에 나의 본질이 담겨 있어요.';

UPDATE daily_questions
SET question_text = '남들이 보는 나는 어떤 사람일까요?'
WHERE id = 144
  AND question_text = '누군가 내 일상을 관찰한다면 나를 어떤 사람이라 할까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 144
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '남들이 보는 나는 어떤 사람일까, 가끔 궁금하죠.'
WHERE id = 144
  AND empathy_guide = '객관적으로 나를 바라보는 건 낯설지만 필요한 일이에요.';

UPDATE daily_questions
SET question_text = '나에게 ''규칙적인 하루''란 어떤 건가요?'
WHERE id = 145
  AND question_text = '내가 생각하는 ''규칙적인 삶''의 정의는 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 145
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '어떤 날이 규칙적인 하루로 기억되는지 떠올려봐요.'
WHERE id = 145
  AND hint_guide = '정해진 시간에 일어나는 것인가요 아니면 할 일을 다 하는 것인가요?';

UPDATE daily_questions
SET empathy_guide = '규칙적인 하루도 내가 정의하기 나름인 법이에요.'
WHERE id = 145
  AND empathy_guide = '삶의 질서는 스스로 정하는 거예요.';

UPDATE daily_questions
SET leading_question_guide = '그리고 규칙적인 하루는 어떤 기분을 가져왔는지도 생각해볼까요?'
WHERE id = 145
  AND leading_question_guide = '그 질서가 나에게 구속인가요 아니면 자유인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 146
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '오늘만 하자는 마음인가요, 아니면 미래를 위한 인내인가요?'
WHERE id = 146
  AND hint_guide = '''오늘만 하자''는 마음인가요, 아니면 미래를 위한 인내인가요?';

UPDATE daily_questions
SET question_text = '반복되는 일상 속, 나를 가장 뿌듯하게 만드는 루틴은 무엇인가요?'
WHERE id = 147
  AND question_text = '나의 루틴 중 가장 자랑스러운 행동은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 147
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '그 행동이 유독 나를 뿌듯하게 만드는 진짜 이유는 무엇일까요?'
WHERE id = 147
  AND hint_guide = '매일 운동하기나 아침 일찍 읽기인가요?';

UPDATE daily_questions
SET empathy_guide = '매일 지켜내는 루틴들이 모두 소중하지만, 그중에서도 스스로를 가장 뿌듯하게 만드는 순간이 있지 않나요?'
WHERE id = 147
  AND empathy_guide = '누가 시키지 않아도 스스로 해내는 멋진 일이에요.';

UPDATE daily_questions
SET leading_question_guide = '가끔은 귀찮거나 건너뛰고 싶은 날도 있을 텐데, 그럼에도 불구하고 그 루틴을 계속 유지하게 만드는 힘은 어디서 나오는지도 떠올려봐요.'
WHERE id = 147
  AND leading_question_guide = '그 행동이 나라는 사람의 자존감을 얼마나 높여주나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 148
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '과거의 나와 지금의 나를 한 번 떠올려봐요.'
WHERE id = 148
  AND empathy_guide = '조금씩 더 나은 방향으로 나아가고 있는 증거예요.';

UPDATE daily_questions
SET question_text = '일상을 꾸려간다는 게 나에게 어떤 의미인가요?'
WHERE id = 149
  AND question_text = '일상을 유지하는 것이 나에게 어떤 의미인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 149
  AND question_level = 5;

UPDATE daily_questions
SET question_level = 2
WHERE id = 150
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '평범한 제목이나 시적인 제목을 생각해봐요.'
WHERE id = 150
  AND hint_guide = '평범한 제목이나 시적인 제목을 생각해보세요.';

UPDATE daily_questions
SET hint_guide = '부모님이나 형제 혹은 배우자를 떠올려 봐요.'
WHERE id = 151
  AND hint_guide = '부모님이나 형제 혹은 배우자를 떠올려 보세요.';

UPDATE daily_questions
SET empathy_guide = '가장 가까운 사이일수록 마음이 더 잘 보여요.'
WHERE id = 151
  AND empathy_guide = '가장 가까운 사이일수록 마음의 지도가 보여요.';

UPDATE daily_questions
SET question_text = '만났을 때 기운을 얻게 되는 사람이 있다면, 누구인가요?'
WHERE id = 153
  AND question_text = '만났을 때 기운을 얻게 되는 사람이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 153
  AND question_level = 1;

UPDATE daily_questions
SET question_level = 2
WHERE id = 156
  AND question_level = 1;

UPDATE daily_questions
SET question_text = '나를 웃게 만드는 재주가 있는 친구가 있다면, 어떤 사람인가요?'
WHERE id = 158
  AND question_text = '나를 웃게 만드는 재주가 있는 친구가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 158
  AND question_level = 1;

UPDATE daily_questions
SET question_text = '요즘 새로 알게 된 사람 중 호감이 가는 사람이 있다면, 어떤 점이 끌리나요?'
WHERE id = 159
  AND question_text = '요즘 새로 알게 된 사람 중 호감 가는 사람이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 159
  AND question_level = 1;

UPDATE daily_questions
SET question_level = 2
WHERE id = 160
  AND question_level = 1;

UPDATE daily_questions
SET question_text = '사람들이 말하는 나의 첫인상은 어떤가요?'
WHERE id = 161
  AND question_text = '사람들이 말하는 나의 첫인상은 어떠한가요?';

UPDATE daily_questions
SET hint_guide = '차갑다 혹은 친근하다는 말을 들어봤나요?'
WHERE id = 161
  AND hint_guide = '차갑다 혹은 "친근하다"는 말을 들어봤나요?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 162
  AND question_level = 2;

UPDATE daily_questions
SET question_text = '친구를 사귀 때 가장 중요하게 보는 점은 무엇인가요?'
WHERE id = 163
  AND question_text = '친구를 사귈 때 중요하게 생각하는 포인트는 무엇인가요?';

UPDATE daily_questions
SET question_text = '반대되는 성격의 사람과도 잘 지내는 편인가요?'
WHERE id = 164
  AND question_text = '나와 반대되는 성격의 사람과 잘 지내는 편인가요?';

UPDATE daily_questions
SET question_text = '주변 사람들로부터 자주 듣는 부탁이 있다면, 어떤 건가요?'
WHERE id = 165
  AND question_text = '주변 사람들로부터 자주 듣는 부탁이 있나요?';

UPDATE daily_questions
SET question_text = '서운한 점이 있을 때 솔직하게 말하는 편인가요? '
WHERE id = 166
  AND question_text = '서운한 점이 있을 때 솔직하게 말하는 편인가요?';

UPDATE daily_questions
SET hint_guide = '바로 말하는지 아니면 참았다가 나중에 말하는지 봐요.'
WHERE id = 166
  AND hint_guide = '바로 말하는지 아니면 참았다가 나중에 말하는지 보세요.';

UPDATE daily_questions
SET question_level = 1
WHERE id = 167
  AND question_level = 2;

UPDATE daily_questions
SET question_text = '다른 사람에게 내 이야기를 어디까지 털어놓나요?'
WHERE id = 168
  AND question_text = '타인에게 내 이야기를 어디까지 털어놓나요?';

UPDATE daily_questions
SET hint_guide = '가벼운 일상만 말하는지 깊은 고민도 말하는지 봐요.'
WHERE id = 168
  AND hint_guide = '가벼운 일상만 말하는지 깊은 고민도 말하는지 보세요.';

UPDATE daily_questions
SET question_text = '다른 사람의 고민을 잘 들어주는 편인가요? '
WHERE id = 169
  AND question_text = '다른 사람의 고민을 잘 들어주는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 171
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 172
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '미안함, 후련함, 부담감 등 다양한 감정을 느낄 수 있어요.'
WHERE id = 172
  AND hint_guide = '미안함이나 부담감이 먼저 드는 편인가요?';

UPDATE daily_questions
SET empathy_guide = '거절은 나를 지키는 방법이기도 해요.'
WHERE id = 172
  AND empathy_guide = '거절은 나를 지키는 정당한 권리이기도 해요.';

UPDATE daily_questions
SET leading_question_guide = '거절할 때 주로 하는 생각도 떠올려봐요.'
WHERE id = 172
  AND leading_question_guide = '거절을 어렵게 만드는 생각의 원인은 무엇인가요?';

UPDATE daily_questions
SET question_text = '진정한 친구는 몇 명이면 충분하다고 생각하나요?'
WHERE id = 173
  AND question_text = '진정한 친구는 몇 명이면 충분하다고 생각하시나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 173
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '그 기준을 떠올렸을 때, 지금 내 곁에 자연스럽게 그려지는 얼굴은 몇 명인가요?'
WHERE id = 173
  AND hint_guide = '1~2명이면 족한지 아니면 많을수록 좋은지 보세요.';

UPDATE daily_questions
SET empathy_guide = '진정한 친구란 무엇일까요?'
WHERE id = 173
  AND empathy_guide = '관계의 양보다 질이 더 중요할 때가 있죠.';

UPDATE daily_questions
SET leading_question_guide = '지금 곁에 있는 친구의 수로 충분한가요? 아니면 내가 마음속으로 바라는 이상적인 숫자가 따로 있나요?'
WHERE id = 173
  AND leading_question_guide = '그 숫자가 나에게 주는 심리적 안정감은 어느 정도인가요?';

UPDATE daily_questions
SET question_text = '관계에서 오는 스트레스를 어떻게 해결하나요?'
WHERE id = 174
  AND question_text = '관계에서 오는 스트레스를 어떻게 해결하시나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 174
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '나를 힘들게 하는 사람과 거리를 두는 방법이 있다면, 어떤 건가요?'
WHERE id = 175
  AND question_text = '나를 힘들게 하는 사람과 거리를 두는 방법이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 175
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 176
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '누군가에게 실망했을 때 어떤 태도를 취하나요?'
WHERE id = 177
  AND question_text = '누군가에게 실망했을 때 어떤 태도를 취하시나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 177
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '조용히 마음을 정리하는지 아니면 대화로 푸는지 봐요.'
WHERE id = 177
  AND hint_guide = '조용히 마음을 정리하는지 아니면 대화로 푸는지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 178
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 179
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '다른 사람 칭찬을 잘해주는 편인가요? '
WHERE id = 180
  AND question_text = '타인의 칭찬에 인색하지 않은 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 180
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '진심으로 칭찬을 자주 하는지 아니면 쑥스러워하는지 봐요.'
WHERE id = 180
  AND hint_guide = '진심으로 칭찬을 자주 하는지 아니면 쑥스러워하는지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 181
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '가족이나 정말 친한 친구를 떠올려 봐요.'
WHERE id = 181
  AND hint_guide = '가족이나 정말 친한 친구를 떠올려 보세요.';

UPDATE daily_questions
SET leading_question_guide = '그 사람 앞에서는 왜 유독 마음이 편해지나요?'
WHERE id = 181
  AND leading_question_guide = '그 사람 앞에서는 왜 유독 마음이 놓이나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 182
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '먼저 대화를 거는지 혹은 시간을 두는지 봐요.'
WHERE id = 182
  AND hint_guide = '먼저 대화를 거는지 혹은 시간을 두는지 보세요.';

UPDATE daily_questions
SET question_text = '다투면 먼저 사과하는 편인가요? 사과할 때 나만의 방식이 있나요?'
WHERE id = 183
  AND question_text = '갈등이 생기면 먼저 사과하는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 183
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '잘잘못을 따지기보다 미안함을 먼저 말하는지 봐요.'
WHERE id = 183
  AND hint_guide = '잘잘못을 따지기보다 미안함을 먼저 말하는지 보세요.';

UPDATE daily_questions
SET question_text = '사람들에게 내 이야기를 잘 하는 편인가요?'
WHERE id = 184
  AND question_text = '사람들에게 내 이야기를 잘 안 하는 이유가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 184
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '상처받기 싫어서인지 혹은 말주변이 없어서인지 봐요.'
WHERE id = 184
  AND hint_guide = '상처받기 싫어서인지 혹은 말주변이 없어서인지 보세요.';

UPDATE daily_questions
SET question_text = '사람들 사이에서 소외감을 느꼈던 적이 있다면, 어떤 상황이었나요?'
WHERE id = 185
  AND question_text = '사람들 사이에서 소외감을 느낀 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 185
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 186
  AND question_level = 4;

UPDATE daily_questions
SET leading_question_guide = '어떤 경험에서 믿어준다는 느낌을 받았는지도 떠올려봐요.'
WHERE id = 186
  AND leading_question_guide = '왜 그들이 나를 믿어준다고 생각하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 187
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '누군가에게 의도치 않게 상처를 준 적이 있다면, 어떤 일이었나요?'
WHERE id = 188
  AND question_text = '누군가에게 의도치 않게 상처 준 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 188
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 189
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '참고 맞추는지 혹은 단호하게 거절하는지 봐요.'
WHERE id = 189
  AND hint_guide = '참고 맞추는지 혹은 단호하게 거절하는지 보세요.';

UPDATE daily_questions
SET question_text = '관계를 정리할 때 나만의 기준이 있다면, 어떤 건가요?'
WHERE id = 190
  AND question_text = '관계를 정리할 때 나만의 기준이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 190
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '거짓말이나 무례함 같은 기준이 있는지 봐요.'
WHERE id = 190
  AND hint_guide = '거짓말이나 무례함 같은 기준이 있는지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 191
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 192
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '누군가에게 배운 삶의 자세가 있다면 무엇인가요?'
WHERE id = 193
  AND question_text = '누군가에게 배운 삶의 태도가 있다면 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 193
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '부지런함이나 다정한 말투를 생각해봐요.'
WHERE id = 193
  AND hint_guide = '부지런함이나 다정한 말투를 생각해보세요.';

UPDATE daily_questions
SET question_text = '나의 약한 모습을 보여주는 게 두려운가요?'
WHERE id = 194
  AND question_text = '나의 약점을 보여주는 게 두려운가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 194
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 195
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 196
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '새로운 이야기가 즐거운지 아니면 긴장되는지 봐요.'
WHERE id = 196
  AND hint_guide = '새로운 이야기가 즐거운지 아니면 긴장되는지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 197
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '고민 들어주기나 실질적인 도움을 생각해봐요.'
WHERE id = 197
  AND hint_guide = '고민 들어주기나 실질적인 도움을 생각해보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 198
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 199
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '내가 꿈꾸는 이상적인 관계는 어떤 모습인가요?'
WHERE id = 200
  AND question_text = '내가 꿈꾸는 성숙한 관계는 어떤 모습인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 200
  AND question_level = 4;

UPDATE daily_questions
SET empathy_guide = '서로에게 긍정적인 영향을 주는 사이를 그려봐요.'
WHERE id = 200
  AND empathy_guide = '서로에게 긍정적인 영향을 주는 사이를 그려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 201
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '밝은 척하는지 아니면 묵묵히 자리를 지키는지 생각해봐요.'
WHERE id = 201
  AND hint_guide = '밝은 척하는지 아니면 묵묵히 자리를 지키는지 보세요.';

UPDATE daily_questions
SET question_text = '다른 사람의 성공을 축하해줄 때, 진심으로 기뻐할 수 있나요?'
WHERE id = 202
  AND question_text = '타인의 성공을 진심으로 축하해주고 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 202
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '질투가 먼저 나는지 아니면 기쁨이 먼저 나는지 봐요.'
WHERE id = 202
  AND hint_guide = '질투가 먼저 나는지 아니면 기쁨이 먼저 나는지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 203
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '사람들과 헤어지고 집에 가는 길에 드는 생각은 무엇인가요?'
WHERE id = 204
  AND question_text = '사람들과의 만남이 끝나고 돌아오는 길에 드는 생각은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 204
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '즐거움인지 혹은 공허함이나 피곤함인지 봐요.'
WHERE id = 204
  AND hint_guide = '즐거움인지 혹은 공허함이나 피곤함인지 보세요.';

UPDATE daily_questions
SET question_text = '내가 생각하는 믿음의 무게는 어느 정도인가요?'
WHERE id = 205
  AND question_text = '내가 생각하는 신뢰의 무게는 어느 정도인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 205
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '한 번 깨지면 끝인지 아니면 회복 가능한지 봐요.'
WHERE id = 205
  AND hint_guide = '한 번 깨지면 끝인지 아니면 회복 가능한지 보세요.';

UPDATE daily_questions
SET question_text = '사람들에게 어떤 부분에서 인정받고 싶나요?'
WHERE id = 206
  AND question_text = '사람들에게 받고 싶은 인정은 어떤 종류인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 206
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '누구에게나 인정받고 싶은 마음이 있잖아요.'
WHERE id = 206
  AND empathy_guide = '누구에게나 인정받고 싶은 욕구가 있기 마련이에요.';

UPDATE daily_questions
SET question_text = '나를 기억할 때 사람들이 떠올렸으면 하는 이미지는 무엇인가요?'
WHERE id = 207
  AND question_text = '나를 기억할 때 사람들이 떠올렸으면 하는 이미지는?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 207
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '관계에서 가장 힘든 순간은 어떤 때인가요?'
WHERE id = 208
  AND question_text = '관계에서 가장 힘들게 느껴지는 지점은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 208
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '사람 사이의 진심은 언제 확인된다고 생각하나요?'
WHERE id = 209
  AND question_text = '사람 사이의 진심은 언제 확인된다고 생각하시나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 209
  AND question_level = 5;

UPDATE daily_questions
SET question_level = 2
WHERE id = 210
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '1점부터 10점 사이에서 골라봐요.'
WHERE id = 210
  AND hint_guide = '1점부터 10점 사이에서 골라보세요.';

UPDATE daily_questions
SET question_text = '사랑이 시작될 때 나만 아는 신호가 있다면, 어떤 건가요?'
WHERE id = 211
  AND question_text = '사랑이 시작될 때 나만 아는 신호가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 211
  AND question_level = 1;

UPDATE daily_questions
SET question_text = '좋아하는 사람에게 제일 먼저 보여주고 싶은 내 모습은 뭘까요?'
WHERE id = 212
  AND question_text = '좋아하는 사람에게 가장 먼저 보여주고 싶은 장점은?';

UPDATE daily_questions
SET question_text = '첫눈에 반한 적이 있다면, 어떤 느낌이었나요?'
WHERE id = 214
  AND question_text = '첫눈에 반한 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 214
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '첫인상이 중요한지 아니면 오래 봐야 하는지 봐요.'
WHERE id = 214
  AND hint_guide = '첫인상이 중요한지 아니면 오래 봐야 하는지 보세요.';

UPDATE daily_questions
SET empathy_guide = '첫눈에 반하는 순간, 정말 있을까요?'
WHERE id = 214
  AND empathy_guide = '운명적인 만남에 대한 나의 생각을 묻는 질문이에요.';

UPDATE daily_questions
SET leading_question_guide = '사랑에 빠지는 속도에 대해서도 적어봐요.'
WHERE id = 214
  AND leading_question_guide = '사랑에 빠지는 속도에 대해서도 적어보세요.';

UPDATE daily_questions
SET question_text = '좋아하는 사람의 외모에서 끌리는 포인트는 무엇인가요?'
WHERE id = 216
  AND question_text = '좋아하는 사람의 외적인 매력 중 끌리는 점은?';

UPDATE daily_questions
SET question_text = '설렘의 유통기한은 언제까지라고 생각하나요?'
WHERE id = 217
  AND question_text = '내가 생각하는 설렘의 유통기한은 언제까지인가요?';

UPDATE daily_questions
SET hint_guide = '수시로 톡을 하는지 아니면 용건만 간단히 하는지 봐요.'
WHERE id = 218
  AND hint_guide = '수시로 톡을 하는지 아니면 용건만 간단히 하는지 보세요.';

UPDATE daily_questions
SET empathy_guide = '연락 자주 하는 게 애정의 척도라고 생각하나요?'
WHERE id = 218
  AND empathy_guide = '연락의 빈도가 애정의 척도라고 생각하시나요?';

UPDATE daily_questions
SET question_text = '사랑하는 사람의 습관 중 귀여운 점이 있다면, 어떤 건가요?'
WHERE id = 219
  AND question_text = '사랑하는 사람의 습관 중 귀여운 점이 있나요?';

UPDATE daily_questions
SET hint_guide = '특이한 웃음소리나 말버릇을 생각해봐요.'
WHERE id = 219
  AND hint_guide = '특이한 웃음소리나 말버릇을 생각해보세요.';

UPDATE daily_questions
SET question_level = 1
WHERE id = 220
  AND question_level = 2;

UPDATE daily_questions
SET question_text = '좋아하는 사람에게 주고 싶은 선물은 무엇인가요?'
WHERE id = 221
  AND question_text = '좋아하는 사람에게 주고 싶은 선물은?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 221
  AND question_level = 2;

UPDATE daily_questions
SET hint_guide = '예전의 이상형 조건과 지금 옆에 있는 사람의 특징을 비교해봐요.'
WHERE id = 222
  AND hint_guide = '예전의 이상형 조건과 지금 옆에 있는 사람의 특징을 비교해보세요.';

UPDATE daily_questions
SET question_text = '사랑과 우정의 경계선은 어디라고 생각하나요?'
WHERE id = 223
  AND question_text = '사랑과 우정의 경계선은 어디라고 생각하시나요?';

UPDATE daily_questions
SET question_text = '질투심이 들 때 나는 어떻게 반응하나요?'
WHERE id = 224
  AND question_text = '질투심을 느낄 때 내 반응은 어떠한가요?';

UPDATE daily_questions
SET hint_guide = '솔직하게 표현하는지 아니면 속으로 삭이는지 봐요.'
WHERE id = 224
  AND hint_guide = '솔직하게 표현하는지 아니면 속으로 삭이는지 보세요.';

UPDATE daily_questions
SET question_level = 1
WHERE id = 225
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 226
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '함께 걸어갈 사람에게 바라는 모습을 그려봐요.'
WHERE id = 226
  AND empathy_guide = '함께 걸어갈 사람에게 바라는 모습을 그려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 227
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '대화로 바로 푸는지 혹은 감정을 식히고 말하는지 봐요.'
WHERE id = 227
  AND hint_guide = '대화로 바로 푸는지 혹은 감정을 식히고 말하는지 보세요.';

UPDATE daily_questions
SET question_text = '사랑하는 사람과 나누고 싶은 깊은 대화 주제가 있다면, 어떤 건가요?'
WHERE id = 228
  AND question_text = '사랑하는 사람과 나누고 싶은 깊은 대화의 주제는?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 228
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '사랑이 나를 더 나은 사람으로 만든 적이 있다면, 어떻게 달라졌나요?'
WHERE id = 229
  AND question_text = '사랑이 나를 더 나은 사람으로 만든 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 229
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 230
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '가만히 들어주는지 혹은 해결책을 제시하는지 봐요.'
WHERE id = 230
  AND hint_guide = '가만히 들어주는지 혹은 해결책을 제시하는지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 231
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 232
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '필수라고 생각하나요, 아니면 선택이라고 생각하나요?'
WHERE id = 232
  AND hint_guide = '필수라고 생각하는지 아니면 선택이라고 생각하시나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 233
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '시간이 흐르며 변하는 감정의 색깔을 골라봐요.'
WHERE id = 233
  AND empathy_guide = '시간이 흐르며 변하는 감정의 색깔을 골라보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 234
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '말없이 표정으로 티를 내는지 혹은 조근조근 말하는지 봐요.'
WHERE id = 234
  AND hint_guide = '말없이 표정으로 티를 내는지 혹은 조근조근 말하는지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 235
  AND question_level = 4;

UPDATE daily_questions
SET empathy_guide = '이별 후에 알게 된 것들을 생각해볼까요?'
WHERE id = 235
  AND empathy_guide = '헤어짐의 아픔도 나를 자라게 하는 밑거름이 돼요.';

UPDATE daily_questions
SET leading_question_guide = '이후에 달라졌던 나의 태도나 행동 등을 떠올려봐요.'
WHERE id = 235
  AND leading_question_guide = '그 깨달음이 다음 사랑에 어떤 영향을 주나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 236
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 237
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '사랑에서 ''진심을 다한다''는 건 어떤 건가요?'
WHERE id = 238
  AND question_text = '내가 생각하는 사랑에서의 ''헌신''이란 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 238
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 239
  AND question_level = 4;

UPDATE daily_questions
SET empathy_guide = '서로 맞추어가는 과정에서 내가 놓을 수 있는 걸 찾아봐요.'
WHERE id = 239
  AND empathy_guide = '서로 맞추어가는 과정에서 내가 놓을 수 있는 걸 찾아보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 240
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 241
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '마음이 식는 것인지 사람이 바뀌는 것인지 봐요.'
WHERE id = 241
  AND hint_guide = '마음이 식는 것인지 사람이 바뀌는 것인지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 242
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '인생의 긴 여정을 누구와 어떻게 보낼지 그려봐요.'
WHERE id = 242
  AND empathy_guide = '인생의 긴 여정을 누구와 어떻게 보낼지 그려보세요.';

UPDATE daily_questions
SET question_text = '사랑에 빠졌을 때 가장 두려운 건 뭘까요?'
WHERE id = 243
  AND question_text = '내가 사랑에 빠졌을 때 가장 두려워하는 것은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 243
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '나를 잃어버리는 것인지 혹은 상처받는 것인지 봐요.'
WHERE id = 243
  AND hint_guide = '나를 잃어버리는 것인지 혹은 상처받는 것인지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 244
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '노력해서 일구는 것인지 우연히 찾아오는 것인지 봐요.'
WHERE id = 244
  AND hint_guide = '노력해서 일구는 것인지 우연히 찾아오는 것인지 보세요.';

UPDATE daily_questions
SET empathy_guide = '사랑이 찾아오는 건지, 내가 만들어가는 건지 궁금해요.'
WHERE id = 244
  AND empathy_guide = '사랑을 대하는 나의 가장 깊은 태도를 묻는 질문이에요.';

UPDATE daily_questions
SET question_text = '사랑하는 사람 때문에 내가 바뀐 점이 있다면, 어떤 건가요?'
WHERE id = 245
  AND question_text = '사랑하는 사람을 위해 내가 바꾼 가장 큰 부분은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 245
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '생활 패턴이나 성격 혹은 삶의 목표를 생각해봐요.'
WHERE id = 245
  AND hint_guide = '생활 패턴이나 성격 혹은 삶의 목표를 생각해보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 246
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '사랑하는 사람에게 끝내 하지 못한 말이 있다면, 어떤 말인가요?'
WHERE id = 247
  AND question_text = '사랑하는 사람에게 끝내 하지 못한 말이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 247
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '내 인생에서 사랑이 차지하는 비중은 몇 퍼센트쯤일까요?'
WHERE id = 248
  AND question_text = '사랑이 인생에서 차지하는 비중은 몇 퍼센트인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 248
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '내 삶에서 사랑은 얼마만큼의 자리를 차지하고 있을까요?'
WHERE id = 248
  AND empathy_guide = '내 삶의 우선순위를 숫자로 확인해보는 질문이에요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 249
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '깊은 대화나 말 없는 편안함을 생각해봐요.'
WHERE id = 249
  AND hint_guide = '깊은 대화나 말 없는 편안함을 생각해보세요.';

UPDATE daily_questions
SET question_text = '내가 생각하는 ''좋은 파트너''란 어떤 사람인가요?'
WHERE id = 250
  AND question_text = '내가 정의하는 ''진정한 파트너십''이란 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 250
  AND question_level = 5;

UPDATE daily_questions
SET empathy_guide = '함께 걷는 사람과 나누고 싶은 핵심 가치를 찾아봐요.'
WHERE id = 250
  AND empathy_guide = '함께 걷는 사람과 나누고 싶은 핵심 가치를 찾아보세요.';

UPDATE daily_questions
SET hint_guide = '건강, 자유, 가족 같은 단어들을 나열해 봐요.'
WHERE id = 251
  AND hint_guide = '건강, 자유, 가족 같은 단어들을 나열해 보세요.';

UPDATE daily_questions
SET question_text = '지칠 때 힘이 되는 나만의 문장이 있다면, 어떤 문장인가요?'
WHERE id = 252
  AND question_text = '지칠 때 힘이 되는 나만의 문장이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 252
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '평소 아끼는 글귀나 명언을 떠올려 봐요.'
WHERE id = 252
  AND hint_guide = '평소 아끼는 글귀나 명언을 떠올려 보세요.';

UPDATE daily_questions
SET hint_guide = '일을 끝냈을 때나 도움을 줬을 때를 봐요.'
WHERE id = 253
  AND hint_guide = '일을 끝냈을 때나 도움을 줬을 때를 보세요.';

UPDATE daily_questions
SET question_text = '5년 뒤 ''잘 살고 있다''고 느끼려면 뭐가 필요할까요?'
WHERE id = 254
  AND question_text = '내가 생각하는 ''성공''은 어떤 모습인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 254
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '역사 속 인물이나 주변 어른을 생각해봐요.'
WHERE id = 255
  AND hint_guide = '역사 속 인물이나 주변 어른을 생각해보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 256
  AND question_level = 1;

UPDATE daily_questions
SET question_level = 2
WHERE id = 257
  AND question_level = 1;

UPDATE daily_questions
SET question_text = '무엇을 가장 배우고 싶나요?'
WHERE id = 258
  AND question_text = '가장 배우고 싶은 기술이나 지식은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 258
  AND question_level = 1;

UPDATE daily_questions
SET leading_question_guide = '왜 그 단어가 설렘을 가져올까요?'
WHERE id = 259
  AND leading_question_guide = '왜 그 단어가 설렘을 가져욜까요?';

UPDATE daily_questions
SET question_text = '나의 좌우명은 무엇인가요?'
WHERE id = 260
  AND question_text = '나의 좌우명이나 생활 신조가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 260
  AND question_level = 1;

UPDATE daily_questions
SET hint_guide = '꼭 무겁거나 진지한 한 줄일 필욘 없어요.'
WHERE id = 260
  AND hint_guide = '정직하게 살자 혹은 즐겁게 살자 같은 것인가요?';

UPDATE daily_questions
SET empathy_guide = '마음속에 늘 새겨두고 있는 한 줄이 있나요?'
WHERE id = 260
  AND empathy_guide = '삶의 기준이 되는 한 줄을 확인해보는 질문이에요.';

UPDATE daily_questions
SET leading_question_guide = '이 문장이 내 삶에 어떻게 영향을 미쳤나요?'
WHERE id = 260
  AND leading_question_guide = '이 신조가 내 선택에 어떤 영향을 주나요?';

UPDATE daily_questions
SET hint_guide = '나를 위한 투자나 선물을 샀던 때를 봐요.'
WHERE id = 261
  AND hint_guide = '나를 위한 투자나 선물을 샀던 때를 보세요.';

UPDATE daily_questions
SET hint_guide = '그중에서도 가만히 보고 있으면 ''나도 저렇게 나이 들고 싶다''는 마음이 드는 어른들이 있죠.'
WHERE id = 262
  AND hint_guide = '여유 있고 다정한 주변 어른을 생각해보세요.';

UPDATE daily_questions
SET empathy_guide = '여유 있고 다정한 모습 또는 끝없이 도전하는 태도 등 멋진 어른의 모습은 참 다양해요.'
WHERE id = 262
  AND empathy_guide = '가끔 "나도 저렇게 살고 싶다"는 생각이 들어요.';

UPDATE daily_questions
SET leading_question_guide = '수많은 어른들 중에서도, 유독 그 모습이 멋지게 다가온 이유는 무엇인가요?'
WHERE id = 262
  AND leading_question_guide = '왜 그 모습이 그렇게 멋져 보였을까요?';

UPDATE daily_questions
SET question_text = '매일 지키고 싶은 나만의 마음가짐이 있다면, 어떤 건가요?'
WHERE id = 263
  AND question_text = '가장 중요하게 생각하는 삶의 태도는 무엇인가요?';

UPDATE daily_questions
SET hint_guide = '다시 하면 돼'' 혹은 ''배운 게 있어'' 같은 생각인가요?'
WHERE id = 264
  AND hint_guide = '''다시 하면 돼'' 혹은 ''배운 게 있어'' 같은 생각인가요?';

UPDATE daily_questions
SET question_text = '내가 생각하는 ''성실하다''는 건 어떤 건가요?'
WHERE id = 266
  AND question_text = '내가 생각하는 ''성실함''의 정의는 무엇인가요?';

UPDATE daily_questions
SET hint_guide = '매일 같은 일을 하는 것인지 포기하지 않는 것인지 봐요.'
WHERE id = 266
  AND hint_guide = '매일 같은 일을 하는 것인지 포기하지 않는 것인지 보세요.';

UPDATE daily_questions
SET empathy_guide = '성실하다는 게 뭘까, 가끔 생각하게 되죠.'
WHERE id = 266
  AND empathy_guide = '꾸준함에 대한 나만의 기준을 묻는 질문이에요.';

UPDATE daily_questions
SET question_text = '사람들에게 보여주고 싶은 나의 모습은 무엇인가요?'
WHERE id = 267
  AND question_text = '타인에게 보여주고 싶은 나의 가치관은 무엇인가요?';

UPDATE daily_questions
SET hint_guide = '전문성이나 따뜻한 성품을 생각해봐요.'
WHERE id = 267
  AND hint_guide = '전문성이나 따뜻한 성품을 생각해보세요.';

UPDATE daily_questions
SET hint_guide = '금처럼 아까운 것인지 흐르는 물처럼 자연스러운 것인지 봐요.'
WHERE id = 268
  AND hint_guide = '금처럼 아까운 것인지 흐르는 물처럼 자연스러운 것인지 보세요.';

UPDATE daily_questions
SET hint_guide = '퇴사나 고백, 혹은 낯선 곳에서의 도전인가요?'
WHERE id = 269
  AND hint_guide = '퇴사나 고백 혹은 낯선 곳으로의 도전인가요?';

UPDATE daily_questions
SET hint_guide = '무슨 일이 있어도 지키는지 혹은 유연하게 대처하는지 봐요.'
WHERE id = 270
  AND hint_guide = '무슨 일이 있어도 지키는지 혹은 유연하게 대처하는지 보세요.';

UPDATE daily_questions
SET leading_question_guide = '약속에 대한 평소 생각을 들려줘요.'
WHERE id = 270
  AND leading_question_guide = '약속에 대한 평소 생각을 들려주세요.';

UPDATE daily_questions
SET question_text = '내가 생각하는 ''공정하다''는 건 어떤 건가요?'
WHERE id = 271
  AND question_text = '내가 생각하는 ''공정함''이란 무엇인가요?';

UPDATE daily_questions
SET hint_guide = '똑같이 나누는 것인지 노력만큼 받는 것인지 봐요.'
WHERE id = 271
  AND hint_guide = '똑같이 나누는 것인지 노력만큼 받는 것인지 보세요.';

UPDATE daily_questions
SET empathy_guide = '세상을 바라보는 나만의 정의로운 기준을 찾아봐요.'
WHERE id = 271
  AND empathy_guide = '세상을 바라보는 나만의 정의로운 기준을 찾아보세요.';

UPDATE daily_questions
SET hint_guide = '내 성공이 실력인지 혹은 운이 좋았던 것인지 봐요.'
WHERE id = 272
  AND hint_guide = '내 성공이 실력인지 혹은 운이 좋았던 것인지 보세요.';

UPDATE daily_questions
SET question_text = '나는 ''직감''을 믿는 편인가요?'
WHERE id = 273
  AND question_text = '나에게 ''직관''은 믿을 만한 가이드인가요?';

UPDATE daily_questions
SET hint_guide = '첫인상을 믿는 편인지 혹은 근거를 찾는 편인지 봐요.'
WHERE id = 273
  AND hint_guide = '첫인상을 믿는 편인지 혹은 근거를 찾는 편인지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 274
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '포기하고 싶지 않은 신념을 하나 찾아봐요.'
WHERE id = 274
  AND hint_guide = '포기하고 싶지 않은 신념을 하나 찾아보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 275
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '소소한 일상 혹은 큰 성취인지 생각해봐요.'
WHERE id = 275
  AND hint_guide = '소소한 일상 혹은 큰 성취인지 생각해보세요.';

UPDATE daily_questions
SET question_text = '죽음 너머에는 무엇이 기다리고 있다고 생각하나요?'
WHERE id = 276
  AND question_text = '사후 세계나 영혼의 존재를 믿으시나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 276
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '무언가 새로운 세상이 있을까요, 아니면 모든 것이 멈추고 사라지는 완전한 끝일까요?'
WHERE id = 276
  AND hint_guide = '믿음의 여부가 지금 내 삶에 어떤 영향을 주나요?';

UPDATE daily_questions
SET empathy_guide = '평소에 죽음에 대해 해본 생각들을 떠올려봐요.'
WHERE id = 276
  AND empathy_guide = '삶 너머의 것에 대한 나의 가치관을 묻는 질문이에요.';

UPDATE daily_questions
SET leading_question_guide = '그 끝을 떠올려보면 기분이 어떤가요?'
WHERE id = 276
  AND leading_question_guide = '이 생각이 죽음을 대하는 내 태도를 어떻게 바꾸나요?';

UPDATE daily_questions
SET question_text = '내가 가장 자유롭다고 느끼는 순간은 언제인가요?'
WHERE id = 277
  AND question_text = '내가 생각하는 ''자유''의 범위는 어디까지인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 277
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '가장 최근에 얽매이는 것 없이 진짜 홀가분하다고 느꼈던 순간은 언제였나요?'
WHERE id = 277
  AND hint_guide = '내 마음대로 하는 것인지 스스로를 다스리는 것인지 보세요.';

UPDATE daily_questions
SET empathy_guide = '자유로운 느낌은 사람마다 다른 순간, 다른 이유로 찾아와요.'
WHERE id = 277
  AND empathy_guide = '구속 없는 상태가 아닌 책임 있는 선택에 대해 생각해보세요.';

UPDATE daily_questions
SET leading_question_guide = '그렇게 느낀 이유는 무엇인가요?'
WHERE id = 277
  AND leading_question_guide = '나는 지금 충분히 자유로운 삶을 살고 있나요?';

UPDATE daily_questions
SET question_text = '내가 주변 사람들에게 줄 수 있는 좋은 영향은 뭘까요?'
WHERE id = 278
  AND question_text = '세상을 바꾸는 힘은 어디에서 나온다고 생각하시나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 278
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 279
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '그 돈으로 사고 싶은 것보다 하고 싶은 일을 생각해봐요.'
WHERE id = 279
  AND hint_guide = '그 돈으로 사고 싶은 것보다 하고 싶은 일을 생각해보세요.';

UPDATE daily_questions
SET question_text = '요즘 나의 일상 균형은 잘 맞고 있나요? 어느 쪽으로 기울어져 있나요?'
WHERE id = 280
  AND question_text = '내가 생각하는 ''삶의 균형''이란 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 280
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '일과 휴식,나와 타인 사이의 적당한 지점을 찾아봐요.'
WHERE id = 280
  AND empathy_guide = '일과 휴식,나와 타인 사이의 적당한 지점을 찾아보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 281
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '그 안에 다양한 이유가 얽혀있을거에요.'
WHERE id = 281
  AND hint_guide = '안식처인가요 아니면 책임져야 할 짐인가요?';

UPDATE daily_questions
SET empathy_guide = '가족을 생각할 때 드는 마음을 떠올려봐요.'
WHERE id = 281
  AND empathy_guide = '가장 가깝고도 복잡한 관계에 대해 생각해보세요.';

UPDATE daily_questions
SET leading_question_guide = '그 이유들을 톺아보면서 가족과의 관계가 내 삶에 어떤 영향을 미쳤는지도 떠올려봐요.'
WHERE id = 281
  AND leading_question_guide = '가족에 대한 내 생각이 내 삶을 어떻게 규정하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 282
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 283
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '끝까지 고집하는지 혹은 유연하게 맞추는지 봐요.'
WHERE id = 283
  AND hint_guide = '끝까지 고집하는지 혹은 유연하게 맞추는지 보세요.';

UPDATE daily_questions
SET question_text = '삶의 의미를 어디에서 찾나요?'
WHERE id = 284
  AND question_text = '삶의 의미를 어디에서 찾으시나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 284
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '왜 살아야 하는지에 대한 나만의 답을 찾아봐요.'
WHERE id = 284
  AND empathy_guide = '왜 살아야 하는지에 대한 나만의 답을 찾아보세요.';

UPDATE daily_questions
SET leading_question_guide = '삶의 원동력이 되는 것들을 찬찬히 떠올려봐요.'
WHERE id = 284
  AND leading_question_guide = '삶의 원동력이 되는 것들을 찬찬히 떠올려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 285
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '함께 쌓은 추억이나 건강 혹은 깊은 신뢰를 떠올려 봐요.'
WHERE id = 285
  AND hint_guide = '함께 쌓은 추억이나 건강 혹은 깊은 신뢰를 떠올려 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 286
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '지금과 완전히 다른 직업이나 성격을 그려봐요.'
WHERE id = 286
  AND hint_guide = '지금과 완전히 다른 직업이나 성격을 그려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 287
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '생계 수단인지 혹은 나를 표현하는 방법인지 봐요.'
WHERE id = 287
  AND hint_guide = '생계 수단인지 혹은 나를 표현하는 방법인지 보세요.';

UPDATE daily_questions
SET question_text = '더 나은 세상을 위해 소소하게라도 하고 있는 행동이 있나요?'
WHERE id = 288
  AND question_text = '세상에 나누고 싶은 나의 좋은 영향은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 288
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '작은 행동도 누군가에게 큰 힘이 될 때가 있죠.'
WHERE id = 288
  AND hint_guide = '누군가에게 힘이나 위로를 줬던 일을 보세요.';

UPDATE daily_questions
SET empathy_guide = '누군가를 위해 문을 잡아주거나 작은 쓰레기를 줍는 것처럼, 사소했던 행동들까지 떠올려봐요.'
WHERE id = 288
  AND empathy_guide = '작은 친절 하나가 세상을 따뜻하게 만들어요.';

UPDATE daily_questions
SET leading_question_guide = '그 행동 속에 담겼던 내 마음도 적어볼까요?'
WHERE id = 288
  AND leading_question_guide = '이 마음이 내 삶의 목표와 어떻게 연결될까요?';

UPDATE daily_questions
SET question_text = '내가 가장 중요하게 생각하는 옮고 그름의 기준은 무엇인가요?'
WHERE id = 289
  AND question_text = '내가 가장 중요하게 생각하는 도덕적 가치는 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 289
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '정직, 책임, 배려 등이 될 수도 있어요.'
WHERE id = 289
  AND hint_guide = '정직, 책임, 배려 등이 있어요.';

UPDATE daily_questions
SET empathy_guide = '가장 마음이 쓰이는 지점을 보면 내 기준이 느껴져요.'
WHERE id = 289
  AND empathy_guide = '양심의 가책을 느끼는 지점을 보면 내 도덕성이 보여요.';

UPDATE daily_questions
SET leading_question_guide = '이런 기준을 지키기 위해 어떤 행동을 해왔을까요?'
WHERE id = 289
  AND leading_question_guide = '이를 위해 어떤 행동을 해왔을까요?';

UPDATE daily_questions
SET question_text = '사람은 원래 착하다고 생각하나요, 아니면 노력해야 착해질 수 있다고 생각하나요?'
WHERE id = 290
  AND question_text = '인간의 본성은 선하다고 믿나요 악하다고 믿나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 290
  AND question_level = 4;

UPDATE daily_questions
SET empathy_guide = '사람에 대한 나의 근본적인 시각을 확인해봐요.'
WHERE id = 290
  AND empathy_guide = '사람에 대한 나의 근본적인 시각을 확인해보세요.';

UPDATE daily_questions
SET question_text = '과거로 돌아갈 수 있다면 바꾸고 싶은 선택이 있다면, 어떤 건가요?'
WHERE id = 291
  AND question_text = '과거로 돌아갈 수 있다면 바꾸고 싶은 선택이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 291
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 292
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 293
  AND question_level = 4;

UPDATE daily_questions
SET empathy_guide = '내 인생을 한 단어로 적는다면 뭐가 떠오를까요?'
WHERE id = 293
  AND empathy_guide = '삶 전체를 관통하는 핵심 키워드를 찾아보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 294
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '끝까지 따지는 편인지 혹은 시간이 해결해주길 기다리는지 봐요.'
WHERE id = 294
  AND hint_guide = '끝까지 따지는 편인지 혹은 시간이 해결해주길 기다리는지 보세요.';

UPDATE daily_questions
SET question_text = '힘든 시간을 보내고 있는 사람에게, 어떤 말을 건네고 싶나요?'
WHERE id = 295
  AND question_text = '고난을 겪고 있는 사람에게 해주고 싶은 말은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 295
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '내가 어려웠던 순간에 나에게 전해주고 싶었던 말일 수도 있어요.'
WHERE id = 295
  AND hint_guide = '진심 어린 위로인가요 아니면 현실적인 조언인가요?';

UPDATE daily_questions
SET empathy_guide = '힘들어하는 누군가를 마주했을 때, 입 밖으로 꺼내지는 못했어도 마음속으로 꼭 전하고 싶었던 진심 어린 문장을 가만히 떠올려봐요.'
WHERE id = 295
  AND empathy_guide = '내가 겪은 아픔에서 얻은 지혜를 나누는 일이에요.';

UPDATE daily_questions
SET leading_question_guide = '그 따뜻한 말들이 결국 나 스스로에게도 해주고 싶었던 말은 아니었을지 생각하며, 소중한 사람에게 건네고 싶은  말들을 적어볼까요?'
WHERE id = 295
  AND leading_question_guide = '그 말이 나 자신에게도 위로가 될 수 있을까요?';

UPDATE daily_questions
SET question_text = '나를 기억하는 사람들에게 어떤 인상을 남기고 싶나요?'
WHERE id = 296
  AND question_text = '내가 세상에 남기고 싶은 유산은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 296
  AND question_level = 4;

UPDATE daily_questions
SET empathy_guide = '죽음 이후에도 남겨질 나의 흔적에 대해 생각해봐요.'
WHERE id = 296
  AND empathy_guide = '죽음 이후에도 남겨질 나의 흔적에 대해 생각해보세요.';

UPDATE daily_questions
SET question_text = '도전을 해야할 때 나의 마음은 어떤가요?'
WHERE id = 297
  AND question_text = '나에게 ''도전''은 설레는 일인가요 아니면 두려운 일인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 297
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '실패에 대한 걱정이나 성공에 대한 기대 중 무엇이 더 큰지 봐요.'
WHERE id = 297
  AND hint_guide = '실패에 대한 걱정이나 성공에 대한 기대 중 무엇이 더 큰지 보세요.';

UPDATE daily_questions
SET question_text = '남과 나를 비교하게 될 때 어떤 생각이 드나요?'
WHERE id = 298
  AND question_text = '타인과 나를 비교하게 될 때 어떤 생각이 드나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 298
  AND question_level = 4;

UPDATE daily_questions
SET hint_guide = '부러움인지 혹은 자책인지 솔직한 기분을 생각해봐요.'
WHERE id = 298
  AND hint_guide = '부러움인지 혹은 자책인지 솔직한 기분을 생각해보세요.';

UPDATE daily_questions
SET empathy_guide = '남들과 비교하며 마음이 흔들리는 순간이 있잖아요.'
WHERE id = 298
  AND empathy_guide = '남들과 비교하며 마음이 흔들리는 순간이 있기 마련이에요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 299
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '지나온 시간에서 가장 가치 있는 것을 골라봐요.'
WHERE id = 299
  AND hint_guide = '지나온 시간에서 가장 가치 있는 것을 골라보세요.';

UPDATE daily_questions
SET empathy_guide = '먼 훇날 지금을 돌아본다면 어떤 마음이 들까요?'
WHERE id = 299
  AND empathy_guide = '미래에 지금의 나를 되돌아본다면 해주고 싶은 말이 분명 있을 거예요.';

UPDATE daily_questions
SET question_text = '내 인생에서 꼭 한 번은 경험하고 싶은 순간이 있다면, 어떤 건가요?'
WHERE id = 300
  AND question_text = '나에게 ''죽음''은 어떤 의미로 다가오나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 300
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '두려운 끝인지 혹은 자연스러운 과정의 일부인지 봐요.'
WHERE id = 300
  AND hint_guide = '두려운 끝인지 혹은 자연스러운 과정의 일부인지 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 301
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '충동구매든, 귀여워서든, 떠오르는 걸 적어봐요.'
WHERE id = 301
  AND hint_guide = '내 마음에 드는 것만으로도 존재가치가 충분한 물건들을 생각해보세요.';

UPDATE daily_questions
SET empathy_guide = '꼭 필요하진 않았는데 그냥 사버린 것들이 있잖아요.'
WHERE id = 301
  AND empathy_guide = '필요하진 않지만 꼭 사고 싶은 물건들이 있어요.';

UPDATE daily_questions
SET leading_question_guide = '그걸 사면서 어떤 기분이었나요?'
WHERE id = 301
  AND leading_question_guide = '왜 꼭 사야하진 않지만 구매까지 하게 된 이유도 생각해봐요.';

UPDATE daily_questions
SET question_text = '평생 한 가지 음식만 먹을 수 있다면 뭘 고르겠어요?'
WHERE id = 302
  AND question_text = '평생 한 가지 음식만 먹어야 할 때 선택하고 싶은 음식은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 302
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '든든한 밥이나 면 요리 혹은 평소 가장 좋아하는 음식을 떠올려 봐요.'
WHERE id = 302
  AND hint_guide = '든든한 밥이나 면 요리 혹은 평소 가장 좋아하는 음식을 떠올려 보세요.';

UPDATE daily_questions
SET empathy_guide = '매일 먹어도 질리지 않는 음식을 떠올려봐요.'
WHERE id = 302
  AND empathy_guide = '매일 먹어도 질리지 않는 음식이 하나쯤 있어요.';

UPDATE daily_questions
SET leading_question_guide = '왜 그 음식을 유독 좋아하는지도 생각해봐요.'
WHERE id = 302
  AND leading_question_guide = '그 음식을 먹을 때 왜 유독 마음이 편안해지나요?';

UPDATE daily_questions
SET question_text = '야심 차게 시작했다가 금방 포기한 취미가 있다면, 어떤 건가요?'
WHERE id = 303
  AND question_text = '야심 차게 시작했지만 딱 일주일 만에 포기한 취미는 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 303
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '3일 만에 접은 헬스장? 아니면 야심 차게 구독한 영어 회화인가요?'
WHERE id = 303
  AND hint_guide = '등록했지만 3일도 가지 못한 헬스장인가요? 아니면 야심 차게 구독했지만 공부하지 않은 영어 회화인가요?';

UPDATE daily_questions
SET question_text = '사진을 많이 찍나요, 눈으로만 담는 것을 즐기나요?'
WHERE id = 304
  AND question_text = '사진을 많이 찍나요 눈으로만 담는걸 즐기나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 304
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '최근 여행이나 나들이 때를 떠올려봐요.'
WHERE id = 304
  AND hint_guide = '기록과 현장감 중에서 무엇을 더 중요하게 생각하나요?';

UPDATE daily_questions
SET question_text = '위시리스트에서 가장 사고 싶은 것은 무엇인가요?'
WHERE id = 305
  AND question_text = '위시리스트에서 가장 사고 싶은 것은 뭔가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 305
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '최근 산 것 중 가장 만족스러운 건 무엇인가요?'
WHERE id = 306
  AND question_text = '최근 산 것 중 가장 만족스러운 건 뭔가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 306
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '옷, 음식, 소품 등 최근 산 것을 떠올려봐요.'
WHERE id = 306
  AND hint_guide = '왜 그 소비가 유독 뿌듯했나요?';

UPDATE daily_questions
SET leading_question_guide = '그 물건이 일상에 어떤 변화를 줬나요?'
WHERE id = 306
  AND leading_question_guide = '소비 이후로 뭐가 달라졌는지 떠올려봐요.';

UPDATE daily_questions
SET question_level = 1
WHERE id = 307
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 308
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '인스타, 유튜브, 당근 등 뭐가 떠오르나요?'
WHERE id = 308
  AND hint_guide = '단순 시간 떼우기인가요 아니면 꽂혀서 계속 들어가게 되는 건가요?';

UPDATE daily_questions
SET question_text = '언젠가 살아보고 싶은 곳이 있다면, 어디인가요?'
WHERE id = 309
  AND question_text = '나중에 살고 싶은 곳은 어디인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 309
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 310
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '취미에 일주일에 얼마나 시간을 쓰나요?'
WHERE id = 311
  AND question_text = '취미활동으로 일주일에 어느정도의 시간을 들이나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 311
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 312
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '이동하는 시간에 나는 주로 무슨 생각을 하나요?'
WHERE id = 313
  AND question_text = '버스와 지하철 중 어떤 걸 더 좋아하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 313
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '음악 듣기, 멍 때리기, SNS 보기 등을 떠올려봐요.'
WHERE id = 313
  AND hint_guide = '지하철을 타며 예측할 수 있는 이동하는 것을 선호하기도 해요.';

UPDATE daily_questions
SET empathy_guide = '이동 시간은 의외로 나만의 소중한 사색 시간이 되기도 해요.'
WHERE id = 313
  AND empathy_guide = '버스를 타면서 풍경을 즐기기도 하고';

UPDATE daily_questions
SET leading_question_guide = '그 시간이 나에게 어떤 의미인가요?'
WHERE id = 313
  AND leading_question_guide = '어떤 걸 더 선호하고 그 이유는 무엇인지 들려주세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 314
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '나만의 공부 스타일이 있다면, 어떤 건가요?'
WHERE id = 315
  AND question_text = '공부 스타일은 어떻게 되나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 315
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '카페파인가요, 도서관파인가요, 아니면 집에서 혼자 하는 게 편한가요?'
WHERE id = 315
  AND hint_guide = '가장 집중이 잘되는 짧은 시간 동안 집중하는 방식으로 공부하는 방법도 있어요.';

UPDATE daily_questions
SET empathy_guide = '오래 집중하는 스타일도 있고, 짧게 몰아서 하는 스타일도 있어요.'
WHERE id = 315
  AND empathy_guide = '최대한 긴 시간을 공부에 쏟아붓는 공부법도 있고';

UPDATE daily_questions
SET question_level = 2
WHERE id = 316
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 1
WHERE id = 317
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '18번 곡이 있다면 뭘지 떠올려봐요.'
WHERE id = 317
  AND hint_guide = '그 노래의 어떤 부분이 가장 좋은지 궁금해요.';

UPDATE daily_questions
SET leading_question_guide = '그 노래를 부를 때 어떤 기분이 드나요?'
WHERE id = 317
  AND leading_question_guide = '그리고 노래방을 어떻게 즐기는 걸 좋아하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 318
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 319
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '힘들 때마다 찾게 되는 곳이 있다면, 어디인가요?'
WHERE id = 320
  AND question_text = '힘들 때마다 찾게 되는 곳이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 320
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '한 번쯤은 시도해보고 싶은 패션 스타일이 있다면, 어떤 건가요?'
WHERE id = 321
  AND question_text = '한 번쯤은 시도해보고 싶은 패션 스타일이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 321
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 322
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '그때 주로 어디에서 누구와 무엇을 하고 싶은지 상상해 봐요.'
WHERE id = 322
  AND empathy_guide = '그때 주로 어디에서 누구와 무엇을 하고 싶은지 상상해 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 323
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '그 브랜드의 어떤 점이 좋은지 떠올려봐요.'
WHERE id = 323
  AND hint_guide = '그 브랜드가 가진 분위기나 철학이 나의 어떤 점과 닮아 있다고 느끼나요?';

UPDATE daily_questions
SET empathy_guide = '유독 끌리는 브랜드가 있잖아요.'
WHERE id = 323
  AND empathy_guide = '그 브랜드의 물건을 처음 샀을 때나 매장을 방문했을 때의 설렘이 기억나나요?';

UPDATE daily_questions
SET leading_question_guide = '그 브랜드의 어떤 물건들을 갖고 있나요?'
WHERE id = 323
  AND leading_question_guide = '이 브랜드의 물건들이 내 일상에 어떤 작은 행복이나 자부심을 더해주나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 324
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '왜 하필 지금 그곳의 풍경이 간절하게 떠오르는지 생각해 봐요.'
WHERE id = 324
  AND empathy_guide = '왜 하필 지금 그곳의 풍경이 간절하게 떠오르는지 생각해 보세요.';

UPDATE daily_questions
SET question_text = '좋아하는 시의 제목이나 글귀가 있다면 어떤 건가요?'
WHERE id = 325
  AND question_text = '좋아하는 시의 제목이나 글귀가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 325
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '어디서 처음 봤는지도 떠올려봐요.'
WHERE id = 325
  AND hint_guide = '힘들 때마다 그 문장을 부적처럼 꺼내 보며 위로를 받나요?';

UPDATE daily_questions
SET question_text = '평소 좋아하는 메이크업 분위기는 무엇인가요?'
WHERE id = 326
  AND question_text = '평소 선호하는 화장 분위기는 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 326
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '메이크업을 마치고 거울을 볼 때 어떤 기분이 드나요?'
WHERE id = 326
  AND empathy_guide = '화장을 마친 거울 속의 나를 볼 때 어떤 기분이 드나요?';

UPDATE daily_questions
SET leading_question_guide = '메이크업은 나를 숨기는 건가요, 아니면 더 드러내는 건가요?'
WHERE id = 326
  AND leading_question_guide = '화장은 나를 숨기는 도구인가요, 아니면 드러내는 수단인가요?';

UPDATE daily_questions
SET question_text = '유독 마음이 끌리는 외국어가 있다면 무엇인가요?'
WHERE id = 327
  AND question_text = '좋아하는 외국어는 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 327
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '낯설지만 멋지게 느껴지는 이유를 적어보고'
WHERE id = 327
  AND hint_guide = '그 언어를 사용하는 나라에 살고 있는 나를 상상해본 적 있나요?';

UPDATE daily_questions
SET empathy_guide = '어떤 단어의 발음이 예뻐서, 혹은 좋아하는 영화나 노래의 분위기가 좋아서 자꾸만 귀를 기울이게 되는 언어가 있는지 떠올려 보세요.'
WHERE id = 327
  AND empathy_guide = '그 언어의 어떤 발음이나 뉘앙스가 특히 매력적인가요?';

UPDATE daily_questions
SET leading_question_guide = '내가 그 언어를 쓰게 된다면 가장 먼저 무엇을 하고 싶은지도 떠올려봐요.'
WHERE id = 327
  AND leading_question_guide = '새로운 언어를 배울 때 내가 확장되는 기분을 느끼나요?';

UPDATE daily_questions
SET question_text = '꾸준히 수집하는 물건이 있다면 무엇인가요?'
WHERE id = 328
  AND question_text = '꾸준히 수집하는 물건이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 328
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 329
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 330
  AND question_level = 3;

UPDATE daily_questions
SET question_text = 'SNS에 일상을 올리는 편인가요? 올린다면 어떤 순간을 주로 올리나요?'
WHERE id = 331
  AND question_text = 'SNS에 일상을 올리는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 331
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 먹은 것 중 기분까지 완벽하게 충전해준 메뉴는 무엇인가요?'
WHERE id = 332
  AND question_text = '오늘 먹은 것 중 기분까지 완벽하게 충전해준 메뉴는?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 332
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '맛과 함께 그 순간 같이 있었던 사람이나 장소의 분위기도 생각해봐요.'
WHERE id = 332
  AND hint_guide = '맛뿐만 아니라 함께한 사람이나 장소의 분위기는 어땠나요?';

UPDATE daily_questions
SET empathy_guide = '한 입 먹는 순간 행복해지는 음식이 있었나요?'
WHERE id = 332
  AND empathy_guide = '한 입 먹는 순간 "행복해!"라고 외치게 된 음식이 있나요?';

UPDATE daily_questions
SET leading_question_guide = '이 메뉴가 좋았던 이유는 또 무엇일까요?'
WHERE id = 332
  AND leading_question_guide = '이 메뉴를 생각하면 떠오르는 기분 좋은 에너지를 적어봐요.';

UPDATE daily_questions
SET question_text = '오늘 기억에 남는 소리나 향기가 있다면, 어떤 건가요?'
WHERE id = 333
  AND question_text = '오늘 나의 오감을 자극한 인상 깊은 소리나 향기는 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 333
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '어떤 기억이 떠올라서, 또는 너무 새로워서 등 다양한 이유로 기억에 남을 수 있죠.'
WHERE id = 333
  AND hint_guide = '그 감각이 나의 무의식 속에 잠들어 있던 어떤 기억을 깨웠나요?';

UPDATE daily_questions
SET empathy_guide = '길 가다 들은 노래, 혹은 코끝을 스친 계절의 냄새를 떠올려볼까요?'
WHERE id = 333
  AND empathy_guide = '길 가다 들은 노래, 혹은 코끝을 스친 계절의 냄새는?';

UPDATE daily_questions
SET leading_question_guide = '그 감각을 통해 하루를 더 풍성하게 느꼈던 순간을 기록해봐요.'
WHERE id = 333
  AND leading_question_guide = '감각을 통해 하루를 더 풍성하게 느꼈던 순간을 기록하세요.';

UPDATE daily_questions
SET question_text = '이번 주 가장 꽂혔던 키워드는 뭐였나요?'
WHERE id = 334
  AND question_text = '이번 주 나의 관심을 독차지한 뜨거운 키워드는 뭐였나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 334
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '왜 그 키워드가 맘에 들었는지 적어봐요.'
WHERE id = 334
  AND hint_guide = '그 관심사가 나를 설레게 하는 이유는 무엇인가요?';

UPDATE daily_questions
SET empathy_guide = '요즘 들어 유독 내 검색창에 자주 등장했거나, 친구들과 대화할 때 반복해서 나왔던 단어를 생각해봐요.'
WHERE id = 334
  AND empathy_guide = '요즘 유독 눈길이 가고 더 알고 싶어지는 분야가 있나요?';

UPDATE daily_questions
SET leading_question_guide = '그리고 그 키워드를 중심으로 있었던 일들도 생각해봐요.'
WHERE id = 334
  AND leading_question_guide = '그 열정을 더 깊게 파고들기 위해 내가 할 수 있는 즐거운 일은 무엇일까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 335
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '그 선택이 행복했던 가장 핵심적인 이유가 궁금해요.'
WHERE id = 335
  AND hint_guide = '만족감의 근원이 실용성이었나요, 아니면 나를 위한 선물이었나요?';

UPDATE daily_questions
SET empathy_guide = '오늘 소비 중 너무 뿌듯해서 나에게 칭찬해주고 싶었던게 있나요?'
WHERE id = 335
  AND empathy_guide = '소비나 시간 활용 중 마음을 가장 풍요롭게 한 건 무엇인가요?';

UPDATE daily_questions
SET leading_question_guide = '이전의 소비들도 떠올려보며 나를 행복하게 만드는 것들을 정리해봐요.'
WHERE id = 335
  AND leading_question_guide = '이런 만족을 더 자주 누리기 위해 나에게 필요한 투자는 뭘까?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 336
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '거창한 휴식이 아니더라도 나를 복잡하게 만들던 생각들로부터 잠시 숨을 돌리게 해준게 뭐였는지 궁금해요.'
WHERE id = 336
  AND hint_guide = '그 활동이 왜 나에게 그토록 큰 평온함을 주었을까요?';

UPDATE daily_questions
SET empathy_guide = '생각을 멈추고 온전히 몰입하거나 쉴 수 있었던 활동이나 물건이 있었나요?'
WHERE id = 336
  AND empathy_guide = '생각을 멈추고 온전히 몰입하거나 쉴 수 있었던 활동은?';

UPDATE daily_questions
SET leading_question_guide = '만약 조금도 쉬지 못해서 힘들었었다면 나를 위한 조그만 휴식 방법을 떠올려봐요.'
WHERE id = 336
  AND leading_question_guide = '내일도 그 평온한 시간을 나에게 선물할 수 있을까요?';

UPDATE daily_questions
SET question_text = '오늘 온전한 휴식시간을 보냈나요?'
WHERE id = 337
  AND question_text = '오늘 나답게 쉬는 법을 아주 멋지게 실천했나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 337
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '오늘의 휴식이 나에게 어떤 에너지를 주었는지, 혹은 쉼이 부족했던 오늘 하루 내 마음의 상태는 어땠는지 기록해봐요.'
WHERE id = 337
  AND hint_guide = '그 쉼이 나의 컨디션을 회복시키는 데 얼마나 효과적이었나요?';

UPDATE daily_questions
SET empathy_guide = '오늘은 어떤 방식으로 휴식을 가졌나요?'
WHERE id = 337
  AND empathy_guide = '남들이 하는 휴식 말고, 나만이 아는 진정한 쉼의 방식은?';

UPDATE daily_questions
SET leading_question_guide = '그리고 다음번 휴식을 위한 작고 확실한 계획을 세워봐요.'
WHERE id = 337
  AND leading_question_guide = '"나답게 쉰다는 것"을 정의하는 나만의 문장을 만들어봐요.';

UPDATE daily_questions
SET question_text = '오늘 새롭게 알게 된 것 중 더 궁금해진 건 무엇이었나요?'
WHERE id = 338
  AND question_text = '오늘 새롭게 접한 정보 중 나의 호기심을 깨운 건 무엇이었나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 338
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '어떤 점이 더 알고 싶어졌는지 궁금해요.'
WHERE id = 338
  AND hint_guide = '그 정보가 나의 생각이나 행동에 어떤 신선한 바람을 일으켰나요?';

UPDATE daily_questions
SET empathy_guide = '오늘 새롭게 알게 된 것 중 더 알고 싶어진게 있었나요?'
WHERE id = 338
  AND empathy_guide = '처음 알게 된 지식이나 나를 놀라게 한 새로운 관점은?';

UPDATE daily_questions
SET leading_question_guide = '원래도 그 분야에 관심이 있었나요?'
WHERE id = 338
  AND leading_question_guide = '잊지 않도록 나만의 한 줄 지식 노트를 남겨보세요.';

UPDATE daily_questions
SET question_level = 1
WHERE id = 339
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '조회수와 상관없이 내가 가장 즐겁게 이야기할 수 있는 주제가 있나요?'
WHERE id = 339
  AND hint_guide = '그 콘텐츠를 통해 사람들에게 어떤 정보나 감동을 전달하고 싶나요?';

UPDATE daily_questions
SET empathy_guide = '내가 관심 있는 내용을 같은 관심사를 갖은 사람들과 공유하는 건 재밌는 일이에요.'
WHERE id = 339
  AND empathy_guide = '조회수와 상관없이 내가 가장 즐겁게 이야기할 수 있는 주제는 무엇인가요?';

UPDATE daily_questions
SET leading_question_guide = '그 주제에 관심을 갖게 된 이유가 궁금해요.'
WHERE id = 339
  AND leading_question_guide = '영상 속의 내 모습이 현실의 나와 얼마나 닮아 있기를 바라나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 340
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '그 공간에서 어떤 시간을 보내고 싶나요?'
WHERE id = 340
  AND leading_question_guide = '현실의 방에서 구현하지 못한 나의 어떤 꿈이 그 새로운 방에 투영되어 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 341
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '그 상상이 나에게 어떤 위로를 주나요?'
WHERE id = 341
  AND leading_question_guide = '그 상상을 마치고 현실로 돌아올 때 나에게 어떤 감정적 변화가 생기나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 342
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '화가 나거나 슬펐던 최근의 순간을 떠올려 봐요.'
WHERE id = 342
  AND empathy_guide = '화가 나거나 슬펐던 최근의 순간을 떠올려 보세요.';

UPDATE daily_questions
SET question_text = '내가 느끼는 감정과 남들이 보는 내 감정, 얼마나 다른가요?'
WHERE id = 343
  AND question_text = '내면의 감정과 타인이 인식하는 감정의 강도는 얼마나 다른가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 343
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '감정을 일부러 숨기는 편인가요, 아니면 오히려 크게 표현하는 편인가요?'
WHERE id = 343
  AND hint_guide = '감정을 의도적으로 숨겨서 무덤덤해 보이나요, 아니면 더 잘 전달하기 위해 실제보다 강하게 표현하나요?';

UPDATE daily_questions
SET empathy_guide = '나는 화가 났지만 상대는 무덤덤했던 적, 혹은 별거 아닌 감정이 과하게 전달된 적 있나요?'
WHERE id = 343
  AND empathy_guide = '나는 격렬했지만 타인은 무덤덤했던 순간, 혹은 내 작은 감정이 타인에게 과하게 전달되었던 기억을 떠올려 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 344
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 345
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 346
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 속으로 나를 가만히 안아준 적이 있다면, 어떤 순간이었나요?'
WHERE id = 347
  AND question_text = '오늘 속으로 나를 가만히 안아준 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 347
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 348
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 349
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 350
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '내 안에서 낯선 감정이 고개를 들었던 적이 있다면, 어떤 느낌이었나요?'
WHERE id = 351
  AND question_text = '내 안에서 낯선 감정이 고개를 들었던 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 351
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '그 감정이 평소의 나와 어떻게 달랐는지 살펴봐요.'
WHERE id = 351
  AND hint_guide = '그 감정이 평소의 나와 어떻게 달랐는지 살펴보세요.';

UPDATE daily_questions
SET question_text = '나를 가장 잘 설명해주는 노래 한 소절이 있다면, 어떤 노래인가요?'
WHERE id = 352
  AND question_text = '나를 가장 잘 설명해주는 노래 한 소절이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 352
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 353
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 354
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 355
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 아주 잠깐이라도 행복이 스친 순간이 있다면, 어떤 순간이었나요?'
WHERE id = 356
  AND question_text = '오늘 아주 잠깐이라도 행복이 스친 순간이 있나요?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 356
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '축 처져 있다가 기분이 다시 살아난 계기가 있다면, 어떤 건가요?'
WHERE id = 357
  AND question_text = '축 처져 있다가 기분이 다시 살아난 계기가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 357
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 내 마음이 회색빛처럼 흐릿했던 때가 있다면, 어떤 순간이었나요?'
WHERE id = 358
  AND question_text = '오늘 내 마음이 회색빛처럼 흐릿했던 때가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 358
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 359
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 하루 중 가장 단골손님처럼 찾아온 감정은 무엇인가요?'
WHERE id = 360
  AND question_text = '오늘 하루 중 가장 단골손님처럼 찾아온 감정은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 360
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 361
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 감정의 온도가 급격히 변한 시점이 있다면, 어떤 때였나요?'
WHERE id = 362
  AND question_text = '오늘 감정의 온도가 급격히 변한 시점이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 362
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 363
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 364
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '그 감정이 폭풍처럼 몰아쳤던 당시를 떠올려봐요.'
WHERE id = 364
  AND hint_guide = '그 감정이 폭풍처럼 몰아쳤던 당시를 떠올려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 365
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 366
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '이번 주에 아직 꺼내지 못한 감정이 있다면, 무엇인가요?'
WHERE id = 367
  AND question_text = '이번 주, 아직 차마 꺼내지 못한 감정이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 367
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 368
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '머릿속을 스치는 가장 직관적인 단어 하나를 골라봐요.'
WHERE id = 368
  AND empathy_guide = '머릿속을 스치는 가장 직관적인 단어 하나를 골라보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 369
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '왜 그것을 피하고 싶었는지 솔직한 마음을 적어봐요.'
WHERE id = 369
  AND hint_guide = '왜 그것을 피하고 싶었는지 솔직한 마음을 적어보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 370
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 371
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 372
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘이 지나도 꼭 간직하고 싶은 감정이 있다면, 어떤 건가요?'
WHERE id = 373
  AND question_text = '오늘이 지나도 꼭 간직하고 싶은 감정이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 373
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '그 기분이 나를 찾아온 특별한 순간을 기록해봐요.'
WHERE id = 373
  AND hint_guide = '그 기분이 나를 찾아온 특별한 순간을 기록해두세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 374
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '이번 주 통틀어 가장 환하게 웃었던 기쁜 순간은 언제인가요?'
WHERE id = 375
  AND question_text = '이번 주 통틀어 가장 환하게 웃었던 기쁜 순간은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 375
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 감정을 말로 설명하기 참 어려웠던 순간이 있다면, 어떤 때였나요?'
WHERE id = 376
  AND question_text = '오늘 감정을 말로 설명하기 참 어려웠던 순간은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 376
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 완전히 방전됐다가 다시 충전된 순간이 있다면, 어떤 때였나요?'
WHERE id = 377
  AND question_text = '오늘 완전히 방전됐다가 다시 충전된 순간이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 377
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 378
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 379
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 380
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 381
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '가장 힘들었던 경험은 무엇이었나요?'
WHERE id = 382
  AND question_text = '가장 끔찍한 경험은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 382
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '내 모습을 숨김없이 다 보여준다면 어떤 일이 벌어질 것 같나요?'
WHERE id = 383
  AND question_text = '내 모습을 숨김없이 다 보여준다면 어떤 일이 벌어질까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 383
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '나도 이해 안 되는 내 모습이 있다면, 어떤 모습인가요?'
WHERE id = 384
  AND question_text = '나조차 이해가 안 되는 내 모습은 어떤 것인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 384
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '나의 어떤 면을 아직 받아들이지 못하고 있나요?'
WHERE id = 385
  AND question_text = '나의 어떤 면을 아직 ''괜찮아''라고 해주지 못하고 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 385
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '지금 겪고 있는 힘든 일이 있다면 무엇인가요?'
WHERE id = 386
  AND question_text = '지금 힘든 시간이 내게 가르쳐주는 게 있다면 뭘까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 386
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '싫어하는 것이 나에게 주는 의외의 이점이 있을까요?'
WHERE id = 387
  AND question_text = '정말 싫은 것이 나에게 주는 이득이 혹시 있을까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 387
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '내 삶에서 가장 들키기 싫은 건 무엇인가요?'
WHERE id = 388
  AND question_text = '내 삶에서 ''들키면 안 돼''라고 느끼는 부분이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 388
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '감정을 숨기기 어려워하는 편인가요?'
WHERE id = 389
  AND question_text = '감정이 표정에 잘 드러나는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 389
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '감정에 쉽게 휘둘리는 편인가요? '
WHERE id = 390
  AND question_text = '감정에 쉽게 휘둘리는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 390
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 391
  AND question_level = 5;

UPDATE daily_questions
SET question_level = 2
WHERE id = 392
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '감정을 정리한 뒤에 대화하는 편인가요, 대화하면서 정리하는 편인가요?'
WHERE id = 393
  AND question_text = '감정을 추스리고 대화하는 걸 선호하나요, 아니면 대화를 통해 감정을 추스리는 걸 좋아하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 393
  AND question_level = 5;

UPDATE daily_questions
SET leading_question_guide = '나의 방식이 관계에 미치는 영향도 떠올려봐요.'
WHERE id = 393
  AND leading_question_guide = '나의 방식이 관계에 어떤 영향을 주나요?';

UPDATE daily_questions
SET question_text = '다른 사람의 감정에 얼마나 예민하게 반응하는 편인가요?'
WHERE id = 394
  AND question_text = '타인에게 감정이 많이 영향을 받는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 394
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '주변 사람의 기분에 따라 내 기분이 어떻게 달라지는지 떠올려봐요.'
WHERE id = 394
  AND hint_guide = '하나의 감정만 느낄 수 있다면 무엇을 남기고 싶나요?';

UPDATE daily_questions
SET empathy_guide = '가까운 사람의 기분에 따라 내 기분도 같이 흔들리는 경험, 있잖아요.'
WHERE id = 394
  AND empathy_guide = '주변 사람의 기분에 따라 내 기분이 어떻게 바뀌는지 떠올려봐요.';

UPDATE daily_questions
SET leading_question_guide = '타인의 감정에 영향받는 것이 나에게 어떤 의미인가요?'
WHERE id = 394
  AND leading_question_guide = '하나의 감정만 느낄 수 있다면 무엇을 남기고 싶나요?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 395
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '참다 참다 폭발한 적이 있다면, 어떤 상황이었나요?'
WHERE id = 396
  AND question_text = '참다 참다 폭발한 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 396
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '어떤 일들이 쌓였었는지 써볼까요?'
WHERE id = 396
  AND hint_guide = '그때 어떤 일이 있었나요?';

UPDATE daily_questions
SET empathy_guide = '쌓인 감정이 터진 경험을 떠올려봐요.'
WHERE id = 396
  AND empathy_guide = '쌓인 감정이 터진 경험이요.';

UPDATE daily_questions
SET leading_question_guide = '왜 더이상 참기 어려웠는지도 적어봐요.'
WHERE id = 396
  AND leading_question_guide = '폭발 후에 어떤 감정이 들었나요?';

UPDATE daily_questions
SET question_text = '나만의 짜증이 나는 포인트는 무엇인가요?'
WHERE id = 397
  AND question_text = '짜증이 나는 포인트가 정해져 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 397
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '반복적으로 짜증난다고 느껴왔던 부분이 있나요?'
WHERE id = 397
  AND empathy_guide = '반복적으로 짜증나는 상황이요.';

UPDATE daily_questions
SET leading_question_guide = '짜증났던 경험 속 짜증의 이유를 찾아봐요.'
WHERE id = 397
  AND leading_question_guide = '짜증 포인트가 나의 가치관과 연결되나요?';

UPDATE daily_questions
SET question_text = '화난 감정이 얼마나 오래가나요?'
WHERE id = 398
  AND question_text = '화난 감정을 오래 가져가는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 398
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '화가 금방 풀리는 편인지, 느리게 풀리는 편인지 궁금해요.'
WHERE id = 398
  AND hint_guide = '화가 안 풀릴 때 어떻게 하나요?';

UPDATE daily_questions
SET empathy_guide = '최근에 화가 났던 경험을 떠올려볼까요?'
WHERE id = 398
  AND empathy_guide = '금방 풀리는지, 오래가는지요.';

UPDATE daily_questions
SET leading_question_guide = '화가 풀리는 과정도 적어봐요.'
WHERE id = 398
  AND leading_question_guide = '용서와 잊음의 차이는 뭔가요?';

UPDATE daily_questions
SET question_text = '울고 싶어도 꾹 참아야만 했던 순간, 주저하게 만든 건 무엇이었나요?'
WHERE id = 399
  AND question_text = '울고 싶은데 못 운 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 399
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '눈물이 차올랐지만 애써 삼켜야 했던 구체적인 장면이나 그때의 분위기를 떠올려 봐요.'
WHERE id = 399
  AND empathy_guide = '눈물이 안 나왔던 경험이요.';

UPDATE daily_questions
SET leading_question_guide = '그때 미처 쏟아내지 못한 감정들이 지금의 나에게는 어떤 기억으로 남아있는지 적어볼까요?'
WHERE id = 399
  AND leading_question_guide = '참은 감정은 어디로 갔을까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 400
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '슬픈 영화나 노래를 일부러 찾아보며 감정을 쏟아내고 싶었던 적이 있나요?'
WHERE id = 401
  AND question_text = '슬픈 노래나 영화를 일부러 찾아보는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 401
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '다른 슬픈 이야기에 내 마음을 비춰볼 때 어떤 느낌이 드는지 떠올려 봐요.'
WHERE id = 401
  AND hint_guide = '그럴 때 어떤 기분이 드나요?';

UPDATE daily_questions
SET empathy_guide = '기분이 가라앉는 날에 굳이 슬픈 것을 고르는 나만의 이유가 있나요?'
WHERE id = 401
  AND empathy_guide = '슬픈 콘텐츠에 끌리는지요.';

UPDATE daily_questions
SET leading_question_guide = '그렇게 한바탕 감정을 쏟아내고 나면 내 기분은 어떻게 달라지나요?'
WHERE id = 401
  AND leading_question_guide = '슬픔을 일부러 느끼는 것이 카타르시스인가요?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 402
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '밤에 갑자기 불안해진 적이 있다면, 어떤 생각이 찾아왔나요?'
WHERE id = 403
  AND question_text = '밤에 갑자기 불안해진 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 403
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '실패가 두려운 편인가요? 어떤 실패가 가장 무서운가요?'
WHERE id = 404
  AND question_text = '실패가 두려운 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 404
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '걱정이 많은 편인가요? 요즘 가장 큰 걱정은 뭔가요?'
WHERE id = 405
  AND question_text = '걱정이 많은 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 405
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '혼자 있을 때 불안함을 어느정도 느끼나요?'
WHERE id = 406
  AND question_text = '혼자 있으면 불안한가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 406
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '미래가 불안할 때, 어떤 부분이 가장 걱정되나요?'
WHERE id = 407
  AND question_text = '미래가 불안한 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 407
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '만약 감정이 사라진다면 어떨까요?'
WHERE id = 408
  AND question_text = '감정이 없다면 어떤 삶일까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 408
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 409
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '감정을 글로 쓰면 정리가 되나요? 쓰고 나면 뭐가 달라지나요?'
WHERE id = 410
  AND question_text = '감정을 글로 쓰면 정리가 되나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 410
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '감정의 회복 속도가 빠른 편인가요 혹은 느린편인가요?'
WHERE id = 411
  AND question_text = '감정의 회복 속도가 빠른 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 411
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '고마운 마음을 잘 표현하는 편인가요? '
WHERE id = 412
  AND question_text = '고마운 마음을 잘 표현하는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 412
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '미안하다는 표현을 잘하는 편인가요? '
WHERE id = 413
  AND question_text = '미안하다는 표현을 잘하는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 413
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 1
WHERE id = 414
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 415
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '갑자기 아파서 가지 못하게 된 여행을 생각해봐요.'
WHERE id = 415
  AND hint_guide = '갑자기 아파서 가지 못하게 된 여행을 생각해보세요.';

UPDATE daily_questions
SET question_text = '워라밸이 얼마나 중요한가요?'
WHERE id = 416
  AND question_text = '워라벨이 중요한가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 416
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '최근에 시작한 일 중 루틴에 넣고 싶은 일이 있다면, 어떤 건가요?'
WHERE id = 417
  AND question_text = '최근에 시작한 일 중 루틴에 넣고 싶은 일이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 417
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오래 쓰는 물건에 정이 드는 편인가요? '
WHERE id = 418
  AND question_text = '휴대폰을 바꾸는 주기가 어떻게 되나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 418
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '지금 가장 오래 쓰고 있는 물건을 떠올려봐요.'
WHERE id = 418
  AND hint_guide = '하나의 휴대폰을 길게 사용하다 고장날 때 즈음 바꾸는 사람도 있어요.';

UPDATE daily_questions
SET empathy_guide = '오래 쓴 물건에는 추억이 깃들기도 하잖아요.'
WHERE id = 418
  AND empathy_guide = '새로운 휴대폰으로 바꾸며 신선한 기능을 경험하는 사람도 있고';

UPDATE daily_questions
SET leading_question_guide = '그 물건과의 시간이 나에게 어떤 의미인가요?'
WHERE id = 418
  AND leading_question_guide = '어떤 유형인지, 왜 그렇게 하는지 써봐요.';

UPDATE daily_questions
SET question_text = '잠 들기 전 꼭 하는 일이 있다면, 어떤 건가요?'
WHERE id = 419
  AND question_text = '잠 들기 전 꼭 하는 일이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 419
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '약속 없는 휴일엔 무엇을 하나요?'
WHERE id = 420
  AND question_text = '약속 없는 휴일엔 뭘 하나요?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 420
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 1
WHERE id = 421
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '음악 듣기, 창밖 보기 혹은 스마트폰 보기를 생각해봐요.'
WHERE id = 421
  AND hint_guide = '음악 듣기, 창밖 보기 혹은 스마트폰 보기를 생각해보세요.';

UPDATE daily_questions
SET question_text = '나도 모르게 반복하는, 조금 신경 쓰이는 버릇은 무엇인가요?'
WHERE id = 422
  AND question_text = '나도 모르게 반복하는, 조금 신경 쓰이는 버릇은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 422
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 나를 정신없이 달리게 만든 주범은 무엇인가요?'
WHERE id = 423
  AND question_text = '오늘 나를 정신없이 달리게 만든 주범은 누구?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 423
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '가장 분주하고 숨 가빴던 시간대를 떠올려 봐요.'
WHERE id = 423
  AND empathy_guide = '가장 분주하고 숨 가빴던 시간대를 떠올려 보세요.';

UPDATE daily_questions
SET question_text = '딱 5분만 투자해서 매일 하고 싶은 작은 습관은 무엇인가요?'
WHERE id = 424
  AND question_text = '딱 5분만 투자해서 매일 하고 싶은 작은 습관은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 424
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 아무 생각 없이 기계적으로 반복한 행동은 무엇인가요?'
WHERE id = 425
  AND question_text = '오늘 아무 생각 없이 기계적으로 반복한 행동은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 425
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 나의 에너지를 가장 많이 잡아먹은 일은 무엇인가요?'
WHERE id = 426
  AND question_text = '오늘 나의 에너지를 가장 많이 잡아먹은 일은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 426
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 몸이 가장 바쁘게 움직였던 시간은 언제인가요?'
WHERE id = 427
  AND question_text = '오늘 몸이 가장 바쁘게 움직였던 골든타임은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 427
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '나를 움직이게 한 원동력은 무엇이었는지 짚어봐요.'
WHERE id = 427
  AND hint_guide = '나를 움직이게 한 원동력은 무엇이었는지 짚어보세요.';

UPDATE daily_questions
SET question_text = '계속 미루고 있는 일 하나를 지금 꺼낸다면 무엇인가요?'
WHERE id = 428
  AND question_text = '계속 미루고 있는 일 하나를 지금 꺼낸다면?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 428
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 429
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 430
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 431
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 432
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '나에게 가장 잘 어울리는 ''하루의 모양''을 그려봐요.'
WHERE id = 432
  AND leading_question_guide = '나에게 가장 잘 어울리는 ''하루의 모양''을 그려보세요.';

UPDATE daily_questions
SET question_text = '오늘 루틴 중에서 나를 칭찬해주고 싶은 순간은 무엇인가요?'
WHERE id = 433
  AND question_text = '오늘 나의 루틴 속에서 얻은 가장 칭찬해주고 싶은 성공은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 433
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 434
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 435
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 436
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '손이 안 가서 끝까지 붙잡고 있었던 일이 있다면, 어떤 건가요?'
WHERE id = 437
  AND question_text = '손이 안 가서 끝까지 붙잡고 있었던 일이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 437
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '운동을 시작하게 된 계기가 있다면, 어떤 건가요?'
WHERE id = 438
  AND question_text = '운동을 시작하게 된 계기가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 438
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '좋아하는 운동은 어떤 운동인가요?'
WHERE id = 439
  AND question_text = '주로 어떤 운동을 하나요?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 439
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '그 운동을 좋아하는 이유를 떠올려봐요.'
WHERE id = 439
  AND hint_guide = '그 운동을 선택한 이유가 있나요?';

UPDATE daily_questions
SET empathy_guide = '헬스, 러닝, 수영 등 다양한 운동이 있죠.'
WHERE id = 439
  AND empathy_guide = '헬스, 러닝, 수영 등 떠오르는 대로 적어봐요.';

UPDATE daily_questions
SET leading_question_guide = '이 운동을 하며 갖게 된 목표도 있다면 적어봐요.'
WHERE id = 439
  AND leading_question_guide = '그 운동이 나의 성격과 닮은 점이 있나요?';

UPDATE daily_questions
SET question_text = '운동이 나에게 주는 가장 큰 선물은 무엇인가요?'
WHERE id = 440
  AND question_text = '일주일에 몇 번 운동하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 440
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '체력, 자신감, 스트레스 해소 등 떠오르는 것을 적어봐요.'
WHERE id = 440
  AND hint_guide = '그 횟수에 만족하나요?';

UPDATE daily_questions
SET empathy_guide = '몸을 움직이고 나면 기분이 달라지는 걸 느낀 적 있을 거예요.'
WHERE id = 440
  AND empathy_guide = '정확하지 않아도 괜찮아요. 대략적으로요.';

UPDATE daily_questions
SET leading_question_guide = '운동이 나의 일상에 어떤 변화를 가져다줬나요?'
WHERE id = 440
  AND leading_question_guide = '이상적인 횟수와 현실의 차이는 왜 생기나요?';

UPDATE daily_questions
SET question_text = '몸을 움직이고 난 뒤의 기분은 어떤가요?'
WHERE id = 441
  AND question_text = '운동은 주로 언제 하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 441
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '상쾌함, 뿌듯함, 피곤함 등 솔직한 느낌을 적어봐요.'
WHERE id = 441
  AND hint_guide = '그 시간대를 선택한 이유가 있나요?';

UPDATE daily_questions
SET empathy_guide = '땀 흘리고 나면 개운한 기분, 있잖아요.'
WHERE id = 441
  AND empathy_guide = '아침, 점심, 저녁 중 언제가 많은지 떠올려봐요.';

UPDATE daily_questions
SET leading_question_guide = '그 기분이 나를 다시 움직이게 만드나요?'
WHERE id = 441
  AND leading_question_guide = '시간대가 운동 효과나 기분에 영향을 주나요?';

UPDATE daily_questions
SET question_text = '공부나 작업을 시작하기 전에 꼭 하는 게 있다면, 어떤 건가요?'
WHERE id = 442
  AND question_text = '공부나 작업을 시작하기 전에 꼭 하는 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 442
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '집중이 잘 되는 시간대가 있다면, 언제인가요?'
WHERE id = 443
  AND question_text = '집중이 잘 되는 시간대가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 443
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 1
WHERE id = 444
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '공부할 때 음악을 듣는 편인가요? 어떤 분위기의 음악인가요?'
WHERE id = 445
  AND question_text = '공부할 때 음악을 듣는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 445
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '무언가에 한 번 몰입하면 집중력은 보통 얼마나 지속되나요?'
WHERE id = 446
  AND question_text = '한 번 집중하면 오래 가는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 446
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '여러 일을 동시에 챙기는 편인가요, 아니면 하나에 깊이 몰입하는 편인가요?'
WHERE id = 447
  AND question_text = '멀티태스킹을 잘하는 편이에요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 447
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '어느 쪽일 때 내가 더 편안하게 능력을 발휘한다고 느끼는지 궁금해요.'
WHERE id = 447
  AND hint_guide = '어떤 방식이 더 효율적이라고 느끼나요?';

UPDATE daily_questions
SET empathy_guide = '한꺼번에 많은 일을 할 때 내 마음은 보통 어떤 상태인지 가만히 살펴보세요.'
WHERE id = 447
  AND empathy_guide = '여러 가지를 동시에 하는 편인지 하나에 몰두하는 편인지 떠올려봐요.';

UPDATE daily_questions
SET leading_question_guide = '나만의 작업 스타일에서 비롯되는 결과물의 특징이 있을까요?'
WHERE id = 447
  AND leading_question_guide = '나의 작업 방식이 나의 성격과 닮은 부분이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 448
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 449
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '집중이 안 될 때 나를 다시 잡아주는 나만의 방법이 있다면, 어떤 건가요?'
WHERE id = 450
  AND question_text = '공부할 때 핸드폰을 어떻게 하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 450
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '찬물 마시기, 스트레칭, 짧은 산책 등을 떠올려봐요.'
WHERE id = 450
  AND hint_guide = '핸드폰 때문에 집중이 깨진 적이 얼마나 자주 있나요?';

UPDATE daily_questions
SET empathy_guide = '집중이 풀릴 때 나만의 리셋 버튼이 있으면 좋잖아요.'
WHERE id = 450
  AND empathy_guide = '치워두는 편인지, 옆에 두는 편인지 떠올려봐요.';

UPDATE daily_questions
SET leading_question_guide = '그 방법이 나를 다시 집중하게 만드는 비결은 뭘까요?'
WHERE id = 450
  AND leading_question_guide = '핸드폰과의 거리가 집중력과 어떤 관계가 있는 것 같나요?';

UPDATE daily_questions
SET question_text = '번아웃이 온 적이 있다면, 어떤 상황에서였나요?'
WHERE id = 451
  AND question_text = '번아웃이 온 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 451
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '공부하다 잠깐 쉴 때 주로 뭘 하나요?'
WHERE id = 452
  AND question_text = '공부하다 쉴 때 뭘 하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 452
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '쉬고 있는데도 불안했던 적이 있다면, 그 불안은 어디에서 오는 걸까요?'
WHERE id = 453
  AND question_text = '쉬는 게 불안한 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 453
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '잘하고 싶은 마음 vs 해야 하는 마음, 뭐가 더 크나요?'
WHERE id = 454
  AND question_text = '잘하고 싶은 마음과 해야 하는 마음 중 뭐가 더 크나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 454
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 455
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '공부나 작업할 때 옆에 꼭 있어야 편한 것이 있다면, 어떤 건가요?'
WHERE id = 456
  AND question_text = '공부할 때 간식이나 음료가 옆에 있어야 하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 456
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '텀블러, 인형, 음악 등 나만의 안정 아이템을 떠올려봐요.'
WHERE id = 456
  AND hint_guide = '간식을 먹는 게 집중에 도움이 되나요?';

UPDATE daily_questions
SET empathy_guide = '옆에 있어야 마음이 놓이는 것들이 있잖아요.'
WHERE id = 456
  AND empathy_guide = '작업할 때 옆에 두는 것들이 있다면 떠올려봐요.';

UPDATE daily_questions
SET leading_question_guide = '그것이 내 곁에 있으면 어떤 점이 달라지나요?'
WHERE id = 456
  AND leading_question_guide = '작업 중 먹는 것이 나에게 보상이 되나요?';

UPDATE daily_questions
SET question_text = '물건을 잘 버리는 편인가요? 버릴 때 기준이 있다면 어떤 건가요?'
WHERE id = 457
  AND question_text = '물건을 잘 버리는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 457
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '물건의 자리가 정해져 있는 편인가요? 나만의 정리 규칙이 있다면 어떤 건가요?'
WHERE id = 458
  AND question_text = '물건의 자리가 정해져 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 458
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 459
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '주변이 어수선할 때 그대로 두는 편인가요, 아니면 정리를 해야만 마음이 편해지나요?'
WHERE id = 460
  AND question_text = '어지러운 방에서도 편한 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 460
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '정리 안 된 공간에서 어떤 행동을 취하는지 궁금해요.'
WHERE id = 460
  AND leading_question_guide = '정돈에 대한 기준이 나에게 어디서 온 건지 생각해봐요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 461
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '지금 당장 버리고 싶은 것이 있다면, 어떤 건가요?'
WHERE id = 462
  AND question_text = '지금 당장 버리고 싶은 것이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 462
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '공간이 깨끗해지면 마음도 달라지나요?'
WHERE id = 463
  AND question_text = '청소를 얼마나 자주 하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 463
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '방 정리 후 느꼈던 기분을 떠올려봐요.'
WHERE id = 463
  AND hint_guide = '그 주기에 만족하나요?';

UPDATE daily_questions
SET empathy_guide = '정리된 공간은 마음까지 시원하게 만들어줄 때가 있어요.'
WHERE id = 463
  AND empathy_guide = '매일, 주 1회, 한 달에 한 번 등 대략적으로 떠올려봐요.';

UPDATE daily_questions
SET leading_question_guide = '공간과 마음의 연결고리는 무엇일까요?'
WHERE id = 463
  AND leading_question_guide = '청소 주기가 나의 생활 패턴을 보여주는 것 같나요?';

UPDATE daily_questions
SET question_text = '운동할 때 꼭 듣는 음악이 있다면, 어떤 건가요?'
WHERE id = 464
  AND question_text = '운동할 때 꼭 듣는 음악이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 464
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '운동 전에 꼭 하는 루틴이 있다면, 어떤 건가요?'
WHERE id = 465
  AND question_text = '운동 전 루틴이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 465
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '운동 후에 꼭 하는 것이 있다면, 어떤 건가요?'
WHERE id = 466
  AND question_text = '운동 후 꼭 하는 것이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 466
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 467
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '운동하면서 나 자신에 대해 알게 된 점이 있나요?'
WHERE id = 468
  AND question_text = '운동할 때 거울을 자주 보나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 468
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '인내심, 한계, 좋아하는 것 등을 떠올려봐요.'
WHERE id = 468
  AND hint_guide = '거울 속 나를 볼 때 어떤 기분인가요?';

UPDATE daily_questions
SET empathy_guide = '몸을 움직이다 보면 나에 대해 새롭게 알게 되는 것들이 있어요.'
WHERE id = 468
  AND empathy_guide = '자세 확인이든, 몸 변화 확인을 위해서 거울을 활용할 수 있어요.';

UPDATE daily_questions
SET leading_question_guide = '운동이 나를 이해하는 데 어떤 도움을 주나요?'
WHERE id = 468
  AND leading_question_guide = '그 외에도 운동할 때의 습관이 있는지 떠올려봐요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 469
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '운동 목표가 있다면, 어떤 목표인가요?'
WHERE id = 470
  AND question_text = '운동 목표가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 470
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '나만의 작은 성취를 기록하는 방식이 있다면, 어떤 건가요?'
WHERE id = 471
  AND question_text = '운동 기록을 남기는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 471
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '사진, 메모, 다이어리 등 나만의 방식을 떠올려봐요.'
WHERE id = 471
  AND hint_guide = '기록이 동기부여에 도움이 되나요?';

UPDATE daily_questions
SET empathy_guide = '작은 성취도 기록하면 뿌듯함이 오래가요.'
WHERE id = 471
  AND empathy_guide = '앱, 수첩, 사진 등 어떤 방식이든요.';

UPDATE daily_questions
SET leading_question_guide = '기록하는 습관이 나에게 어떤 동기부여를 주나요?'
WHERE id = 471
  AND leading_question_guide = '기록하는 습관이 다른 영역에도 영향을 주나요?';

UPDATE daily_questions
SET question_text = '운동으로 성취감을 느낀 적이 있다면, 어떤 순간이었나요?'
WHERE id = 472
  AND question_text = '운동으로 성취감을 느낀 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 472
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '물건을 살 때 가격을 꼼꼼히 따져보는 편인가요, 아니면 가격보다 더 중요하게 여기는 나만의 기준이 있나요?'
WHERE id = 473
  AND question_text = '가격 비교를 꼼꼼히 하는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 473
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '소비할 때 가장 고민하는 부분에서 답을 찾을 수 있을지도 몰라요.'
WHERE id = 473
  AND leading_question_guide = '절약과 시간 중 뭐가 더 중요한가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 474
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '누군가에게 돈을 쓸 때 기분이 어떤가요?'
WHERE id = 475
  AND question_text = '누군가에게 돈 쓰는 게 좋은 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 475
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '소비 습관 중 바꾸고 싶은 게 있다면, 어떤 건가요?'
WHERE id = 476
  AND question_text = '소비 습관 중 바꾸고 싶은 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 476
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '나에게 ''돈''이란 한마디로 무엇인가요?'
WHERE id = 477
  AND question_text = '나에게 ''돈''이란 한마디로 뭔가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 477
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '돈 때문에 고민했던 경험이 있다면, 어떤 상황이었나요?'
WHERE id = 478
  AND question_text = '돈을 빌려본 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 478
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '나에게 투자할 가치가 있는 분야는 무엇인가요?'
WHERE id = 479
  AND question_text = '나에게 투자할 가치가 있는 분야는 뭔가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 479
  AND question_level = 5;

UPDATE daily_questions
SET question_level = 2
WHERE id = 480
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '지금 구독하고 있는 서비스는 무엇이 있나요?'
WHERE id = 481
  AND question_text = '구독 서비스에 얼마나 쓰고 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 481
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '구독 서비스들이 주는 즐거움을 떠올려봐요.'
WHERE id = 481
  AND hint_guide = '다 쓰고 있나요?';

UPDATE daily_questions
SET empathy_guide = '수많은 선택지 중에서도 유독 이 서비스들을 선택한 이유가 있나요?'
WHERE id = 481
  AND empathy_guide = '넷플릭스, 음악, 앱 등이요.';

UPDATE daily_questions
SET leading_question_guide = '가끔은 넘쳐나는 구독 서비스 사이에서 나만의 기준을 가지고 가볍게 정리해 본 경험이 있는지도 적어봐요.'
WHERE id = 481
  AND leading_question_guide = '안 쓰는 구독을 유지하는 이유는 뭔가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 482
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '꿈을 기록해본 적이 있다면, 어떤 경험이었나요?'
WHERE id = 483
  AND question_text = '꿈을 기록해본 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 483
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '자기 전에 하루를 돌아보면 어떤 장면이 주로 떠오르나요?'
WHERE id = 484
  AND question_text = '자기 전에 하루를 돌아보는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 484
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '밤에 갑자기 마음이 무거워진 적이 있다면, 어떤 생각이 찾아왔나요?'
WHERE id = 485
  AND question_text = '밤에 갑자기 마음이 무거워진 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 485
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 486
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 1
WHERE id = 487
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '이불 속에서 눈을 감으면 가장 먼저 떠오르는 생각은 뭔가요?'
WHERE id = 488
  AND question_text = '눕고 나서 실제로 잠들기까지 얼마나 걸리나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 488
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '오늘 있었던 일이나 내일 할 일을 떠올려봐요.'
WHERE id = 488
  AND hint_guide = '잠이 오지 않아 뒤척이는 시간 동안 스마트폰을 보거나 다른 행동을 하나요?';

UPDATE daily_questions
SET empathy_guide = '잠들기 전 고요한 순간에 떠오르는 생각이 있잖아요.'
WHERE id = 488
  AND empathy_guide = '불을 끄고 누운 직후, 머릿속을 채우는 가장 주된 생각이나 감정은 무엇인가요?';

UPDATE daily_questions
SET leading_question_guide = '그 생각이 나를 편안하게 하나요, 아니면 잠을 방해하나요?'
WHERE id = 488
  AND leading_question_guide = '눈을 감고 잠을 청하는 그 고요한 시간이 나에게는 휴식인가요, 아니면 고통인가요?';

UPDATE daily_questions
SET question_text = '생각할 틈도 없이 자동으로 나오는 나만의 반응이 있다면, 어떤 건가요?'
WHERE id = 489
  AND question_text = '생각할 틈 없이 ''자동''으로 나오는 내 반응이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 489
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '겉으론 ''응''이라고 하지만 속으론 ''싫어'' 하는 게 있다면, 어떤 건가요?'
WHERE id = 490
  AND question_text = '겉으론 ''응''이라고 하지만 속으론 ''싫어'' 하는 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 490
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 491
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 일과 삶의 균형을 위해 노력한 게 있다면, 어떤 건가요?'
WHERE id = 492
  AND question_text = '오늘 일과 삶 사이의 균형을 잡기 위해 노력한 점은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 492
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 493
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '친한 친구가 큰 잘못을 하고 숨겨달라고 하면 어떻게 할 것 같나요?'
WHERE id = 494
  AND question_text = '친한 친구가 큰 잘못을 저지르고 숨겨달라 하면 도와줄 건가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 494
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 495
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '최근 나의 시간과 에너지가 주로 누구에게 향했는지 돌아봐요.'
WHERE id = 495
  AND empathy_guide = '최근 나의 시간과 에너지가 주로 누구에게 향했는지 돌아보세요.';

UPDATE daily_questions
SET question_text = '상대의 약한 모습을 알게 되면 더 가까워진 느낌인가요, 부담스러운가요?'
WHERE id = 496
  AND question_text = '상대방의 약점을 알게 되면 가까워진 느낌인가요, 아니면 부담스러운가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 496
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '누군가의 약점을 처음 알게 되었던 순간의 기분을 떠올려 봐요.'
WHERE id = 496
  AND empathy_guide = '누군가의 약점을 처음 알게 되었던 순간의 기분을 떠올려 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 497
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '내가 일방적으로 더 많이 줬거나, 반대로 과한 배려를 받았던 순간을 떠올려봐요.'
WHERE id = 497
  AND empathy_guide = '내가 일방적으로 더 많이 줬거나, 반대로 과한 배려를 받았던 구체적인 순간의 기분을 떠올려 보세요.';

UPDATE daily_questions
SET question_text = '어떤 연락 스타일을 갖고 있나요?'
WHERE id = 498
  AND question_text = '연락을 자주 하는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 498
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '넓고 얕은 관계와 좁고 깊은 관계 중 뭐가 더 좋나요?'
WHERE id = 499
  AND question_text = '넓고 얕은 인간 관계와 좁고 깊은 인간 관계 중 무엇을 더 선호하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 499
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 500
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '친구 혹은 가족과 의견이 달랐던 상황을 떠올려봐요.'
WHERE id = 500
  AND hint_guide = '친구 혹은 가족과 의견이 달랐던 상황을 떠올려보세요.';

UPDATE daily_questions
SET question_text = '인간관계에 쏟는 에너지는 어느 정도인가요?'
WHERE id = 501
  AND question_text = '인간관계에 쏟는 에너지는 어느정도인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 501
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 502
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 503
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 504
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 1
WHERE id = 505
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '친구들과 함께하다 보면 서로의 다양한 면을 알게 돼요.'
WHERE id = 505
  AND empathy_guide = '친구들과 함께하다보면 서로의 다양한 면을 알게돼요..';

UPDATE daily_questions
SET question_text = '나만 노력하는 것 같은 관계가 있다면, 어떤 관계인가요?'
WHERE id = 506
  AND question_text = '나만 노력하는 것 같은 관계가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 506
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '가족이라서 참고 있는 게 있다면, 어떤 건가요?'
WHERE id = 507
  AND question_text = '가족이라서 참는게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 507
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '오래 알았는데 멀어진 사람이 있다면, 어떤 사이였나요?'
WHERE id = 508
  AND question_text = '오래 알았는데 멀어진 사람 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 508
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '부모님한테 인정받고 싶은 마음이 있다면, 어떤 부분에서 인정받고 싶나요?'
WHERE id = 509
  AND question_text = '부모님한테 인정받고 싶은 마음이 아직 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 509
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '실망해도 기회를 또 주는 편인가요? '
WHERE id = 510
  AND question_text = '실망해도 기회를 또 주는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 510
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '믿고 싶은 건가요, 끊기 싫어서인가요?'
WHERE id = 510
  AND hint_guide = '믿고 싶인가요, 끊기 싫어서인가요?';

UPDATE daily_questions
SET question_text = '다수 의견이랑 다를 때 말하는 편인가요? '
WHERE id = 511
  AND question_text = '다수 의견이랑 다를 때 말하는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 511
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '상황이 사람을 바꾼다고 느꼈던 적이 있다면, 어떤 경험이었나요?'
WHERE id = 512
  AND question_text = '상황이 사람을 바꾼다고 느낀 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 512
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '외로움이 무서워서 관계를 유지한 적이 있다면, 어떤 관계였나요?'
WHERE id = 513
  AND question_text = '외로움이 무서워서 관계를 유지한 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 513
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '요즘 가장 신경쓰이는 사람이 있다면, 어떤 이유인가요?'
WHERE id = 514
  AND question_text = '요즘 가장 신경쓰이는 사람이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 514
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 1
WHERE id = 515
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '지금 가장 보고 싶은 존재는 무엇인가요?'
WHERE id = 516
  AND question_text = '지금 가장 보고 싶은 존재는 뭔가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 516
  AND question_level = 4;

UPDATE daily_questions
SET question_text = '나 자신과 했던 약속 중 아직 유효한 게 있다면, 어떤 건가요?'
WHERE id = 517
  AND question_text = '나 자신과 했던 약속 중 아직 유효한 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 517
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 518
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 519
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '내가 가진 선한 영향력은 어떤 모습인지 생각해봐요.'
WHERE id = 519
  AND leading_question_guide = '내가 가진 선한 영향력은 어떤 모습인지 생각해보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 520
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 521
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 522
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 523
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '말을 건네고 싶었지만 꾹 참은 사람이 있다면, 어떤 말을 하고 싶었나요?'
WHERE id = 524
  AND question_text = '말을 건네고 싶었지만 꾹 참은 사람이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 524
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 525
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 관계 때문에 마음이 조금 무거웠다면, 어떤 일이었나요?'
WHERE id = 526
  AND question_text = '오늘 관계 때문에 마음이 조금 무거웠던 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 526
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '지금 내 곁에서 가장 소중하게 느껴지는 사람은 누구인가요?'
WHERE id = 527
  AND question_text = '지금 내 곁에서 가장 소중하게 느껴지는 사람은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 527
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 528
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '그 사람에게 아직 다 하지 못한 말이 있다면 적어봐요.'
WHERE id = 528
  AND leading_question_guide = '그 사람에게 아직 다 하지 못한 말이 있다면 적어보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 529
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '오늘 내가 먼저 용기 내어 다가간 적이 있다면, 어떤 순간이었나요?'
WHERE id = 530
  AND question_text = '오늘 내가 먼저 용기 내어 다가간 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 530
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '다가간 후에 느꼈던 안도감이나 기쁨을 기억해봐요.'
WHERE id = 530
  AND leading_question_guide = '다가간 후에 느꼈던 안도감이나 기쁨을 기억해보세요.';

UPDATE daily_questions
SET question_level = 1
WHERE id = 531
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '누군가의 웃음소리가 나에게 주는 에너지를 느껴봐요.'
WHERE id = 531
  AND leading_question_guide = '누군가의 웃음소리가 나에게 주는 에너지를 느껴보세요.';

UPDATE daily_questions
SET question_text = '가장 가까운 사람에게 꼭 전하고 싶은 말이 있다면, 어떤 말인가요?'
WHERE id = 532
  AND question_text = '가장 가까운 사람에게 꼭 전하고 싶은 말이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 532
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 533
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '힘든 시간을 같이 견뎌준 고마운 인연을 떠올려봐요.'
WHERE id = 533
  AND empathy_guide = '힘든 시간을 같이 견뎌준 고마운 인연을 떠올려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 534
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 535
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 536
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '곁에 있어줘서 가장 고마운 사람은 누구인가요?'
WHERE id = 537
  AND question_text = '주변인 중 누구의 죽음이 가장 괴로울 것 같은가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 537
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '지금 떠오르는 이름 하나를 적어봐요.'
WHERE id = 537
  AND hint_guide = '그 사람이 떠오른 이유는 뭔가요?';

UPDATE daily_questions
SET empathy_guide = '당연하게 느끼지만, 없으면 정말 허전할 사람이 있잖아요.'
WHERE id = 537
  AND empathy_guide = '무겁지만 솔직하게 떠올려봐요. 없는 상황을 떠올려도 힘든 사람이 있나요?';

UPDATE daily_questions
SET leading_question_guide = '그 사람이 내 곁에 있어줘서 달라진 건 무엇인가요?'
WHERE id = 537
  AND leading_question_guide = '그 존재가 나에게 어떤 의미인지 알려주는 것 같나요?';

UPDATE daily_questions
SET question_text = '다시 잡고 싶은 인연이 있다면, 어떤 사이였나요?'
WHERE id = 538
  AND question_text = '다시 잡고 싶은 인연이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 538
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 539
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '나에게 상처를 줬지만 덕분에 강해진 경험이 있나요?'
WHERE id = 540
  AND question_text = '절대 잊지 못할 나쁜 사람이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 540
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '그때 하지 못한 말이 있다면, 지금은 할 수 있을까요?'
WHERE id = 541
  AND question_text = '다시 돌아가서 싸우고 싶은 순간이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 541
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 542
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '주변 사람을 덕질해 본 적이 있다면, 누구였나요?'
WHERE id = 543
  AND question_text = '주변 사람을 덕질해 본 적이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 543
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '나에게 우정이란 어떤 의미인가요?'
WHERE id = 544
  AND question_text = '우정이란 어떤 의미인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 544
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '소중한 사람에게 아직 전하지 못한 고마운 말이 있나요?'
WHERE id = 545
  AND question_text = '지금 당장 죽는다면 누구에게 어떤 걸 말하지 않은 것을 후회할 것 같나요? ';

UPDATE daily_questions
SET question_level = 2
WHERE id = 545
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '가족, 친구, 혹은 나 자신에게 하고 싶은 말을 떠올려봐요.'
WHERE id = 545
  AND hint_guide = '왜 아직 그 말을 하지 못했나요?';

UPDATE daily_questions
SET empathy_guide = '마음속에 담아만 두고 있는 고마운 마음이 있을 거예요.'
WHERE id = 545
  AND empathy_guide = '편하게 떠오르는 사람과 말을 생각해봐요.';

UPDATE daily_questions
SET leading_question_guide = '그 말을 전한다면 어떤 기분이 들까요?'
WHERE id = 545
  AND leading_question_guide = '그 말을 전한다면 관계가 어떻게 달라질까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 546
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 547
  AND question_level = 5;

UPDATE daily_questions
SET question_level = 2
WHERE id = 548
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '내가 자란 환경에서 바꾸 수 있다면, 뭘 바꾸고 싶나요?'
WHERE id = 549
  AND question_text = '자란 방식에 대해 바꿀 수 있다면, 무엇을 바꾸고 싶나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 549
  AND question_level = 5;

UPDATE daily_questions
SET question_level = 2
WHERE id = 550
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 551
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '어린 시절이 행복했다고 느끼나요?'
WHERE id = 552
  AND question_text = '어린 시절은 대부분의 다른 사람들보다 행복했다고 느끼나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 552
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '연애할 때 혼자만의 시간은 어떤 의미인가요?'
WHERE id = 553
  AND question_text = '연애할 때 혼자만의 시간도 중요한가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 553
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '혼자만의 시간에 에너지가 생기는지 생각해봐요.'
WHERE id = 553
  AND hint_guide = '혼자만의 시간에 에너지가 생기는지 생각해보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 554
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '좋아하는 감정에서 사랑이라는 감정으로 바뀐 순간을 떠올려봐요.'
WHERE id = 554
  AND hint_guide = '좋아하는 감정에서 사랑이라는 감정으로 바뀐 순간을 떠올려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 555
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '최근의 다툼에서 즉각적인 대화를 원했는지, 혹은 혼자만의 시간이 필요했는지 떠올려 봐요.'
WHERE id = 555
  AND empathy_guide = '최근의 다툼에서 즉각적인 대화를 원했는지, 혹은 혼자만의 시간이 필요했는지 떠올려 보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 556
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '설렘과 책임감을 비교해봐요.'
WHERE id = 556
  AND hint_guide = '설렘과 책임감을 비교해보세요.';

UPDATE daily_questions
SET question_level = 1
WHERE id = 557
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '좋아하는 사람 앞에서 나도 모르게 하는 행동을 떠올려봐요.'
WHERE id = 557
  AND hint_guide = '좋아하는 사람 앞에서 나도 모르게 하는 행동을 떠올려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 558
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '문자 보내기 전 고민했던 시간을 떠올려봐요.'
WHERE id = 558
  AND hint_guide = '문자 보내기 전 고민했던 시간을 떠올려보세요.';

UPDATE daily_questions
SET question_level = 1
WHERE id = 559
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '가장 사랑받는다고 느낀 순간을 떠올려봐요.'
WHERE id = 559
  AND hint_guide = '가장 사랑받는다고 느낀 순간을 떠올려보세요.';

UPDATE daily_questions
SET question_text = '이별 후 얼마나 지나야 새 연애를 시작할 수 있을 것 같나요?'
WHERE id = 560
  AND question_text = '이별 후 얼마나 있어야 새 연애가 가능한가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 560
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '전 애인과 친구가 될 수 있다고 생각하나요? '
WHERE id = 561
  AND question_text = '전 애인과 친구가 될 수 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 561
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '전 애인과의 현재 관계를 떠올려봐요.'
WHERE id = 561
  AND hint_guide = '전 애인과의 현재 관계를 떠올려보세요.';

UPDATE daily_questions
SET empathy_guide = '이별 후 어떤 관계를 유지하느냐가 감정 정리 방식을 보여줘요.'
WHERE id = 561
  AND empathy_guide = '이별 후 관계가 감정 정리 방식을 보여줘요.';

UPDATE daily_questions
SET question_text = '연인에게 절대 져줄 수 없는 것이 무엇인가요?'
WHERE id = 562
  AND question_text = '연인에게 절대 져줄 수 없는 것이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 562
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '연애에서 꺾이지 않았던 것을 떠올려봐요.'
WHERE id = 562
  AND hint_guide = '연애에서 꺾이지 않았던 것을 떠올려보세요.';

UPDATE daily_questions
SET question_text = '끌리는 사람과 좋은 사람 중 누가 더 좋은가요?'
WHERE id = 563
  AND question_text = '끌리는 사람과 좋은 사람 중 누구를 선택하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 563
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '두 유형을 각각 떠올려봐요.'
WHERE id = 563
  AND hint_guide = '두 유형을 각각 떠올려보세요.';

UPDATE daily_questions
SET leading_question_guide = '최근의 연애를 떠올려봐요.'
WHERE id = 563
  AND leading_question_guide = '최근의 연애를 떠올려보세요.';

UPDATE daily_questions
SET question_text = '이상형과 실제 사귄 사람이 같은 편인가요? 다르다면 어떤 점이 다른가요?'
WHERE id = 564
  AND question_text = '이상형과 실제 사귄 사람이 같나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 564
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '이상형과 전 연인들을 떠올려봐요.'
WHERE id = 564
  AND hint_guide = '이상형과 전 연인들을 떠올려보세요.';

UPDATE daily_questions
SET question_text = '이상형의 조건 중 양보할 수 있는 것과 없는 것은 무엇인가요?'
WHERE id = 565
  AND question_text = '이상형의 조건 중 협상 가능한 것과 불가능한 것은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 565
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '좋아해도 깨던 점과 좋아하면 안 보이는 조건을 떠올려봐요.'
WHERE id = 565
  AND hint_guide = '좋아해도 깨던 점과 좋아하면 안 보이는 조건을 떠올려보세요.';

UPDATE daily_questions
SET question_text = '처음엔 별로였는데 나중에 좋아진 적이 있다면, 어떤 경험이었나요?'
WHERE id = 566
  AND question_text = '처음엔 별로였는데 나중에 좋아진 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 566
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '뭘 알게 되고 나서 달라졌나요?'
WHERE id = 566
  AND hint_guide = '처음엔 친구였는데 어느 순간 달리 보인 사람을 떠올려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 567
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 568
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '내 인생에서 사랑은 어떤 의미인가요?'
WHERE id = 569
  AND question_text = '인생에서 사랑과 애정은 어떤 의미인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 569
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '이상형 중 특이한 조건이 있다면, 어떤 건가요?'
WHERE id = 570
  AND question_text = '본인의 이상형 중 특이한 조건이 있나요?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 570
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 571
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 1
WHERE id = 572
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 573
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 1
WHERE id = 574
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '"남들이 뭐라 할까" 생각했던 순간을 떠올려봐요.'
WHERE id = 574
  AND hint_guide = '"남들이 뭐라 할까" 생각했던 순간을 떠올려보세요.';

UPDATE daily_questions
SET question_text = '거절을 해야 할 때 나는 어떻게 하나요?'
WHERE id = 575
  AND question_text = '거절을 잘하는 편인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 575
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 576
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '최근에 접은 것과 안 접은 것을 떠올려봐요.'
WHERE id = 576
  AND hint_guide = '최근에 접은 것과 안 접은 것을 떠올려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 577
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '크게 잃고 나서 배운 것을 떠올려봐요.'
WHERE id = 577
  AND hint_guide = '크게 잃고 나서 배운 것을 떠올려보세요.';

UPDATE daily_questions
SET question_text = '나에게 좀 더 다정해지고 싶은 순간이 있나요?'
WHERE id = 578
  AND question_text = '나를 싫어하는 순간이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 578
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '최근 나에게 좀 더 부드럽게 대했으면 좋았을 순간을 떠올려봐요.'
WHERE id = 578
  AND hint_guide = '스스로가 한심하게 느껴졌던 순간을 떠올려보세요.';

UPDATE daily_questions
SET empathy_guide = '나에게 엄격해지는 순간, 사실은 다정함이 더 필요한 때이기도 해요.'
WHERE id = 578
  AND empathy_guide = '그 순간 싫어하는 건 ''나''인가요, ''그 행동''인가요?';

UPDATE daily_questions
SET leading_question_guide = '나에게 다정해진다는 건 어떤 모습일까요?'
WHERE id = 578
  AND leading_question_guide = '그 순간마다 어떻게 행동하나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 579
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '바꾸고 싶은 나의 모습이 있다면, 어떤 건가요?'
WHERE id = 580
  AND question_text = '바꾸고 싶은 나의 모습이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 580
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '"이것만 달랐으면" 하고 생각하는 것을 떠올려봐요.'
WHERE id = 580
  AND hint_guide = '"이것만 달랐으면" 하고 생각하는 것을 떠올려보세요.';

UPDATE daily_questions
SET question_text = '내가 틀렸다는 걸 인정하기 어려운 주제가 있다면 무엇인가요?'
WHERE id = 581
  AND question_text = '내가 틀렸다는 걸 인정하기 어려운 주제가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 581
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '유난히 마음을 많이 쓰는 논쟁들을 떠올려봐요.'
WHERE id = 581
  AND hint_guide = '유난히 마음을 많이 쓰는 논쟁들을 떠올려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 582
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '한계를 인정했던 순간을 떠올려봐요.'
WHERE id = 582
  AND empathy_guide = '한계를 인정했던 순간을 떠올려보세요.';

UPDATE daily_questions
SET question_text = '편안함이 성장을 막고 있다고 느낀 적이 있다면, 어떤 순간이었나요?'
WHERE id = 583
  AND question_text = '편안함이 성장을 막고 있다고 느낀 적 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 583
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '삶에서 놓치고 있는 게 있다면 무엇인가요?'
WHERE id = 584
  AND question_text = '삶에서 놓치고 있는 게 있다면 뭔가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 584
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '나에게 엄격한 편인가요, 관대한 편인가요?'
WHERE id = 585
  AND question_text = '스스로에게 엄격한가요, 관대한가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 585
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 586
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 587
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '과거의 나에게 ''괜찮아, 그럴 수 있어''라고 말해주고 싶은 순간이 있나요?'
WHERE id = 588
  AND question_text = '예전에 한 행동 중 아직도 부끄러운 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 588
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '어린 시절이나 예전의 서툴러웠던 나를 떠올려봐요.'
WHERE id = 588
  AND hint_guide = '왜 부끄럽나요?';

UPDATE daily_questions
SET empathy_guide = '지나고 보면 그때의 나도 최선을 다하고 있었을 거예요.'
WHERE id = 588
  AND empathy_guide = '갑자기 떠올려서 이불킥하게 되는 기억, 있잖아요.';

UPDATE daily_questions
SET leading_question_guide = '그때의 나에게 지금 건네고 싶은 한마디는 무엇인가요?'
WHERE id = 588
  AND leading_question_guide = '그때 나는 왜 그랬을까요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 589
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '왜 그 순간으로 돌아가고 싶은지 떠올려볼까요?'
WHERE id = 589
  AND hint_guide = '왜 그 순간으로 돌아가고 싶은 볼까요?';

UPDATE daily_questions
SET question_text = '과거에 받은 상처 중 아직 아물지 않은 게 있다면, 어떤 건가요?'
WHERE id = 590
  AND question_text = '과거에 받은 상처 중 아직 안 나은 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 590
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '잊고 싶은 기억이 있다면, 어떤 종류의 기억인가요?'
WHERE id = 591
  AND question_text = '잊고 싶은 기억이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 591
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 592
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 1
WHERE id = 593
  AND question_level = 2;

UPDATE daily_questions
SET hint_guide = '둘 중 하나를 선택했던 순간을 떠올려봐요.'
WHERE id = 593
  AND hint_guide = '둘 중 하나를 선택했던 순간을 떠올려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 594
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '성공했다고 생각했던 순간을 떠올려봐요.'
WHERE id = 594
  AND hint_guide = '성공했다고 생각했던 순간을 떠올려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 595
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 596
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 597
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '좋은 의도의 나쁜 결과와 나쁜 의도의 좋은 결과, 뭐가 더 나쁜가요?'
WHERE id = 598
  AND question_text = '좋은 마음의 나쁜 결과와 나쁜 마음의 좋은 결과 중 어느 쪽이 더 나쁜가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 598
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 599
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 600
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '꼭 이겨야겠다고 느낀 순간을 떠올려봐요.'
WHERE id = 600
  AND hint_guide = '꼭 이겨야겠다고 느낀 순간을 떠올려보세요.';

UPDATE daily_questions
SET question_text = '겉과 속이 다를 때, 나는 어떤 마음인가요?'
WHERE id = 601
  AND question_text = '내게 가장 위선적인 부분이 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 601
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '웃고 있지만 속으론 힘들었던 때를 떠올려봐요.'
WHERE id = 601
  AND hint_guide = '그 위선을 누가 알면 어떤 기분일 것 같나요?';

UPDATE daily_questions
SET empathy_guide = '겉과 속이 다른 순간, 그 안에서 마음이 복잡해질 때가 있죠.'
WHERE id = 601
  AND empathy_guide = '''이러면 안 되는데'' 하면서도 하는 것을 떠올려보세요.';

UPDATE daily_questions
SET leading_question_guide = '진짜 내 모습을 보여줄 수 있는 사람이 있나요?'
WHERE id = 601
  AND leading_question_guide = '왜 그걸 고치지 못하고 있나요?';

UPDATE daily_questions
SET question_text = '죽기 전에 꼭 해보고 싶은 건 무엇인가요?'
WHERE id = 602
  AND question_text = '죽기 전에 꼭 해보고 싶은 건 뭔가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 602
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 1
WHERE id = 603
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '지금까지 해 온 일, 앞으로 하고 싶은 일을 떠올려봐요.'
WHERE id = 603
  AND empathy_guide = '지금까지 해 온 일, 앞으로 하고 싶은 일을 떠올려보세요.';

UPDATE daily_questions
SET leading_question_guide = '뉴스든 예능이든 다큐든 떠오르는 장면을 상상해봐요.'
WHERE id = 603
  AND leading_question_guide = '뉴스든 예능이든 다큐든 떠오르는 장면을 상상해보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 604
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = 'SNS에서 본 이야기나 친구와 나눈 대화도 괜찮아요.'
WHERE id = 604
  AND hint_guide = '그 뉴스를 보고 어떤 감정이 들었나요?';

UPDATE daily_questions
SET empathy_guide = '세상 돌아가는 이야기 중에 유독 마음에 남는 게 있잖아요.'
WHERE id = 604
  AND empathy_guide = '최근에 본 사회 이슈, 정치, 경제, 연예, 범죄 등 떠올려보세요.';

UPDATE daily_questions
SET leading_question_guide = '그 이야기가 나의 어떤 관심사를 보여주는 걸까요?'
WHERE id = 604
  AND leading_question_guide = '그 뉴스를 본 뒤 뭔가 하고 싶어지거나, 반대로 하기 싫어진 게 있었나요?';

UPDATE daily_questions
SET question_text = '괜찮지 않은데 ''괜찮아''라고 넘긴 적이 있다면, 어떤 순간이었나요?'
WHERE id = 605
  AND question_text = '가장 자주 합리화하는 행동은 뭔가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 605
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '최근에 나를 다독인 순간을 떠올려봐요.'
WHERE id = 605
  AND hint_guide = '왜 유독 그 행동에 대해서 합리화하게 되나요?';

UPDATE daily_questions
SET empathy_guide = '나에게 괜찮다고 말하면서 사실은 괜찮지 않았던 적, 있잖아요.'
WHERE id = 605
  AND empathy_guide = '반복적으로 잘못해도 어쩔 수 없었어, 이번만 등의 말로 합리화해 온 일이 있을 거예요.';

UPDATE daily_questions
SET leading_question_guide = '진짜 괜찮아지려면 나에게 뭐가 필요할까요?'
WHERE id = 605
  AND leading_question_guide = '어떻게 합리화해왔는지도 떠올려봐요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 606
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '어떤 순간에 나를 더 안아주고 싶어지나요?'
WHERE id = 607
  AND question_text = '어떤 상황에서 가장 초라해지나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 607
  AND question_level = 5;

UPDATE daily_questions
SET hint_guide = '지치거나 서운할 때, 나를 토닥여준 순간을 떠올려봐요.'
WHERE id = 607
  AND hint_guide = '비교될 때, 무시당할 때, 준비 안 됐을 때...';

UPDATE daily_questions
SET empathy_guide = '가끔은 나 자신을 꼭 안아주고 싶은 날이 있잖아요.'
WHERE id = 607
  AND empathy_guide = '괜히 주눅 드는 순간이 있잖아요.';

UPDATE daily_questions
SET leading_question_guide = '나를 안아줄 때 가장 필요한 말 한마디는 무엇일까요?'
WHERE id = 607
  AND leading_question_guide = '그 순간 제일 신경 쓰이는 게 뭔가요?';

UPDATE daily_questions
SET question_text = '채워지지 않는 결핍이 있다면 무엇인가요?'
WHERE id = 608
  AND question_text = '채워지지 않는 결핍이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 608
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '회피하고 있는게 있다면 무엇인가요?'
WHERE id = 609
  AND question_text = '회피하고 있는 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 609
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '가장 최근에 생각이 바뀐 주제가 있다면, 어떤 건가요?'
WHERE id = 610
  AND question_text = '가장 최근에 생각이 바뀐 주제가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 610
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 611
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '사람이 바뀐 걸까요 아니면 사람은 그대로지만 바라보는 방식 또는 표현하는 방식이 바뀐 걸까요?'
WHERE id = 611
  AND hint_guide = '사람이 바뀐 걸까요 아니면 사람은 그대로지만 바라보는 방식 또는 표현하는 방식이 바뀐걸까요?';

UPDATE daily_questions
SET question_text = '절대 바뀌지 않을 것 같은 생각이 있다면, 어떤 건가요?'
WHERE id = 612
  AND question_text = '절대 바뀌지 않을 것 같은 생각이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 612
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '예전엔 중요했는데 지금은 중요하지 않은 것이 있다면 무엇인가요?'
WHERE id = 613
  AND question_text = '예전엔 중요했는데 지금은 아닌 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 613
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '절대 되고 싶지 않은 사람의 모습은 무엇인가요?'
WHERE id = 614
  AND question_text = '절대 되고 싶지 않은 사람의 모습이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 614
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 615
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '자존심 때문에 놓친 게 있다면, 어떤 건가요?'
WHERE id = 616
  AND question_text = '자존심 때문에 놓친 게 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 616
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '자존심을 지켰더라도 계속 아쉬움이 남는 일을 떠올려봐요.'
WHERE id = 616
  AND hint_guide = '자존심을 지켰더라도 계속 아쉬움이 남는 일을 떠올려보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 617
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '예전보다 조심스러워진 부분이 있다면, 어떤 건가요?'
WHERE id = 618
  AND question_text = '나이 들면서 보수적으로 변했다고 느끼나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 618
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '확신이 무너졌던 경험이 있다면, 어떤 일이었나요?'
WHERE id = 619
  AND question_text = '확신이 무너진 경험이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 619
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 1
WHERE id = 620
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '5년 뒤 나는 어떤 사람이 되어 있을까요?'
WHERE id = 621
  AND question_text = '어떤 할머니/할아버지가 되고 싶나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 621
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 622
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 623
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '이번 주가 나에게 조용히 건네준 교훈은 무엇인가요?'
WHERE id = 624
  AND question_text = '이번 주가 나에게 조용히 건네준 교훈이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 624
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '많은 일들 속에서 와닿은 교훈이 있을거에요.'
WHERE id = 624
  AND hint_guide = '예전의 나라면 그냥 지나쳤을 일들에서 무엇을 배웠나요?';

UPDATE daily_questions
SET empathy_guide = '이번주의 일들을 되돌아봐요.'
WHERE id = 624
  AND empathy_guide = '일련의 사건들을 통해 새롭게 깨달은 삶의 진리는?';

UPDATE daily_questions
SET leading_question_guide = '앞으로의 자세와 그 교훈을 적어봐요.'
WHERE id = 624
  AND leading_question_guide = '다음 주를 더 멋지게 살아가게 할 나만의 지침은?';

UPDATE daily_questions
SET question_text = '예전이라면 크게 흔들렸을 상황에서, 의외로 덤덤해진 나를 발견한 순간이 있었나요?'
WHERE id = 625
  AND question_text = '요즘 예전보다 덜 흔들리는 나를 발견했나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 625
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '성장한 나 자신에게 기분 좋은 눈인사를 건네봐요.'
WHERE id = 625
  AND leading_question_guide = '성장한 나 자신에게 기분 좋은 눈인사를 건네보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 626
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 627
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 628
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '스스로 결정했을 때의 그 짜릿한 자유로움을 기억해봐요.'
WHERE id = 628
  AND hint_guide = '스스로 결정했을 때의 그 짜릿한 자유로움을 기억해보세요.';

UPDATE daily_questions
SET question_text = '예전에는 당연했는데 지금은 다르게 보이는 것이 있다면, 어떤 건가요?'
WHERE id = 629
  AND question_text = '예전에는 당연했는데 지금은 다르게 보이는 것이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 629
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 630
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '그 성장을 일궈낸 나의 끈기와 노력을 충분히 인정해줘요.'
WHERE id = 630
  AND hint_guide = '그 성장을 일궈낸 나의 끈기와 노력을 충분히 인정해주세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 631
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '문득 나 자신이 어른스러워졌다고 느낀 최근의 순간은 언제인가요?'
WHERE id = 632
  AND question_text = '최근 나 자신이 조금 어른이 되었다고 느낀 순간은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 632
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '변화의 원인을 찾아보고 나만의 성장 일기를 써봐요.'
WHERE id = 632
  AND leading_question_guide = '변화의 원인을 찾아보고 나만의 성장 일기를 써보세요.';

UPDATE daily_questions
SET question_text = '오늘 나의 원칙을 잘 지켜냈다면, 어떤 방식으로 지켰나요?'
WHERE id = 633
  AND question_text = '오늘 나만의 소신이나 원칙을 멋지게 지켜냈나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 633
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '나를 지켜주는 이 든든한 기준들이 왜 중요한지 생각해봐요.'
WHERE id = 633
  AND leading_question_guide = '나를 지켜주는 이 든든한 기준들이 왜 중요한지 생각해보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 634
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '내가 추구하는 관계의 온도는 몇 도인지 생각해봐요.'
WHERE id = 634
  AND leading_question_guide = '내가 추구하는 관계의 온도는 몇 도인지 생각해보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 635
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 636
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '나중에 꼭 다시 꺼내보고 싶은 한 줄의 지혜를 남겨봐요.'
WHERE id = 636
  AND leading_question_guide = '나중에 꼭 다시 꺼내보고 싶은 한 줄의 지혜를 남기세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 637
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '실패조차 배움으로 바꾸는 나만의 특별한 능력을 믿어봐요.'
WHERE id = 637
  AND hint_guide = '실패조차 배움으로 바꾸는 나만의 특별한 능력을 믿어보세요.';

UPDATE daily_questions
SET question_text = '가보지 않은 길에 대한 미련을 지우는 나만의 방식은 무엇인가요?'
WHERE id = 638
  AND question_text = '선택하지 않은 길에 대한 미련을 터는 나만의 방법은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 638
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '지금의 내 선택을 더 빛나게 만들 한마디를 적어봐요.'
WHERE id = 638
  AND leading_question_guide = '지금의 내 선택을 더 빛나게 만들 한마디를 적어보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 639
  AND question_level = 3;

UPDATE daily_questions
SET empathy_guide = '상상만 해도 가슴 뛰는 나의 미래 모습을 그려봐요.'
WHERE id = 639
  AND empathy_guide = '상상만 해도 가슴 뛰는 나의 미래 모습을 그려보세요.';

UPDATE daily_questions
SET question_text = '나의 생각과 삶의 태도에 가장 큰 변화를 준 사람은 누구인가요?'
WHERE id = 640
  AND question_text = '나의 인격이나 가치관에 가장 큰 영향을 준 인물은?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 640
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '앞으로 어떤 사람이 되고 싶은지도 같이 생각해봐요.'
WHERE id = 640
  AND leading_question_guide = '앞으로 어떤 긍정적인 영향을 세상에 전하고 싶은가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 641
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '어떤 미래를 꿈꾸면서 선택을 했나요?'
WHERE id = 641
  AND hint_guide = '그 선택의 이유를 미래의 나에게 설명해준다면요?';

UPDATE daily_questions
SET empathy_guide = '오늘 내린 결정들의 미래를 떠올려봐요.'
WHERE id = 641
  AND empathy_guide = '오늘 내린 결정들이 1년 뒤의 나에게 어떤 선물이 될까요?';

UPDATE daily_questions
SET leading_question_guide = '미래의 내가 오늘의 나에게 보낼 선택에 대한 한마디도 떠올려봐요.'
WHERE id = 641
  AND leading_question_guide = '미래의 내가 오늘의 나에게 보낼 고마움의 메시지를 상상해봐요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 642
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '실패 속에서 다양한 기회를 얻기도 하고 발전을 하기도 하죠.'
WHERE id = 642
  AND hint_guide = '이 배움이 나를 얼마나 더 단단하게 만들어줄까요?';

UPDATE daily_questions
SET empathy_guide = '실패가 꼭 나쁜 일만은 아니에요.'
WHERE id = 642
  AND empathy_guide = '실패라고 생각했던 순간 속에 숨겨진 진주는 무엇이었나요?';

UPDATE daily_questions
SET leading_question_guide = '최근의 실패 이후의 일들을 훑어봐요.'
WHERE id = 642
  AND leading_question_guide = '아픔조차 성장의 밑거름으로 쓰는 나를 응원해주세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 643
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '어떤 이유로 놀랐었는지 생각해봐요.'
WHERE id = 643
  AND hint_guide = '그 발견이 나를 더 입체적이고 매력적으로 느끼게 하나요?';

UPDATE daily_questions
SET empathy_guide = '내 자신임에도 시간이 흐르면서 새롭게 발견하는 면들도 있어요.'
WHERE id = 643
  AND empathy_guide = '"나에게 이런 모습이 있었어?" 싶었던 순간이 있나요?';

UPDATE daily_questions
SET leading_question_guide = '발견한 계기는 무엇이었나요?'
WHERE id = 643
  AND leading_question_guide = '새로 알게 된 나를 친구에게 소개하듯 짧게 적어보세요.';

UPDATE daily_questions
SET question_level = 2
WHERE id = 644
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '포기를 선택한 이유도 떠올려봐요.'
WHERE id = 644
  AND hint_guide = '그게 지쳐서였나요, 아니면 더 소중한 걸 지키기 위해서였나요?';

UPDATE daily_questions
SET leading_question_guide = '포기하기로 결정한 이유도 생각해봐요.'
WHERE id = 644
  AND leading_question_guide = '포기 뒤에 찾아온 뜻밖의 평온함을 솔직하게 기록해보세요.';

UPDATE daily_questions
SET question_text = '성장했다고 느낀 순간이 있다면, 어떤 때였나요?'
WHERE id = 645
  AND question_text = '"나 정말 많이 컸다"라고 스스로 감격한 순간은 언제인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 645
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '성장을 가능하게 했던 나의 숨은 노력들을 칭찬해줘요.'
WHERE id = 645
  AND hint_guide = '성장을 가능하게 했던 나의 숨은 노력들을 칭찬해주세요.';

UPDATE daily_questions
SET question_text = '지금보다 나은 나를 위해 요즘 시도하고 있는 것이 있다면, 어떤 건가요?'
WHERE id = 646
  AND question_text = '나를 한 단계 더 발전하게 만들 계획은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 646
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 647
  AND question_level = 4;

UPDATE daily_questions
SET leading_question_guide = '그 물건과의 소중한 기억도 떠올려봐요.'
WHERE id = 647
  AND leading_question_guide = '그 물건과의 추억에 대해서도 떠올려봐요.';

UPDATE daily_questions
SET question_text = '절대 장난스럽게 얘기할 수 없는 진지한 주제가 있다면, 무엇인가요?'
WHERE id = 648
  AND question_text = '절대 장난스럽게 얘기할 수 없는 진지하거나 심각한 주제가 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 648
  AND question_level = 5;

UPDATE daily_questions
SET question_level = 2
WHERE id = 649
  AND question_level = 5;

UPDATE daily_questions
SET question_text = '어떤 방식으로 유명해지고 싶나요?'
WHERE id = 650
  AND question_text = '유명해지고 싶나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 650
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '세상 누구든 한 명과 저녁을 먹을 수 있다면 누구를 고르겠어요?'
WHERE id = 651
  AND question_text = '상대의 생사와 관계 없이 아무나 한 명과 저녁을 함께할 수 있다면 누구와 저녁을 먹고 싶나요?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 651
  AND question_level = 3;

UPDATE daily_questions
SET question_level = 2
WHERE id = 652
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '능력과 상관없이 하고 싶은 직업이 있다면, 어떤 건가요?'
WHERE id = 653
  AND question_text = '능력과 상관없이 하고 싶은 직업이 있나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 653
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '가장 좋아하는 문장은 무엇인가요?'
WHERE id = 654
  AND question_text = '인상 깊은 문장은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 654
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '그 문장이 내 삶에 어떻게 영향을 미쳤는지도 떠올려봐요.'
WHERE id = 654
  AND hint_guide = '그 문장이 내 삶의 중요한 가이드라인이 되어준 적이 있나요?';

UPDATE daily_questions
SET empathy_guide = '그 문장을 처음 접했던 장소와 시간의 분위기를 기억하나요?'
WHERE id = 654
  AND empathy_guide = '그 문장을 처음 만났던 장소와 시간의 분위기를 기억하나요?';

UPDATE daily_questions
SET leading_question_guide = '유독 마음에 와닿은 이유도 적어봐요.'
WHERE id = 654
  AND leading_question_guide = '다른 사람에게도 그 문장을 꼭 추천해주고 싶은 이유가 무엇인가요?';

UPDATE daily_questions
SET question_level = 1
WHERE id = 655
  AND question_level = 3;

UPDATE daily_questions
SET question_text = '세상을 더 따뜻하게 만들기 위해서 하고 싶은 일은 무엇인가요?'
WHERE id = 656
  AND question_text = '추천하는 사람이 대통령이 된다면 누구를 뽑을 건가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 656
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '주변에 작은 도움이나 따뜻한 말 한마디를 떠올려봐요.'
WHERE id = 656
  AND hint_guide = '그분을 선택한 마음 밑바닥에는 어떤 희망이 담겨 있나요?';

UPDATE daily_questions
SET empathy_guide = '작은 친절 하나가 세상을 바꾸기도 해요.'
WHERE id = 656
  AND empathy_guide = '더 나은 세상을 위해 어떤 리더가 필요하다고 생각하나요?';

UPDATE daily_questions
SET leading_question_guide = '그 작은 실천이 나에게는 어떤 기분을 줄까요?'
WHERE id = 656
  AND leading_question_guide = '세상이 따뜻하게 변한다면 나의 하루는 어떻게 달라지나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 657
  AND question_level = 3;

UPDATE daily_questions
SET leading_question_guide = '나도 누군가에게는 이미 충분히 멋진 사람이라는 거, 알고 있나요?'
WHERE id = 657
  AND leading_question_guide = '나도 누군가에게는 이미 충분히 멋진 사람이라는 걸 아시나요?';

UPDATE daily_questions
SET question_text = '믿는 미신이 있다면 무엇인가요?'
WHERE id = 658
  AND question_text = '미신을 믿으시나요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 658
  AND question_level = 3;

UPDATE daily_questions
SET hint_guide = '논리적으로 이해는 안되지만 걱정되는 마음 또는 습관으로 지키고 있는 미신이 있나요?'
WHERE id = 658
  AND hint_guide = '불안을 다독이려 나만의 작은 규칙에 기대고 있지는 않나요?';

UPDATE daily_questions
SET empathy_guide = '연인에게 신발 선물하지 않기, 밤에 휘파람 불지 않기처럼 소소한 미신부터 떠올려봐요.'
WHERE id = 658
  AND empathy_guide = '논리로는 설명 안 되지만 마음을 위해 지키는 습관이 있나요?';

UPDATE daily_questions
SET leading_question_guide = '언제부터 그 미신을 신경쓰기 시작했는지, 왜 지키기게 된 것 같은지도 떠올려봐요.'
WHERE id = 658
  AND leading_question_guide = '그 약속을 지켰을 때 내 마음이 한결 가벼워진다면 충분해요.';

UPDATE daily_questions
SET question_text = '어떤 상황에서도 절대 하지 않을 것 같은 한 가지 일은 무엇인가요?'
WHERE id = 659
  AND question_text = '당신이 절대 할 수 없는 한 가지 일은 무엇인가요?';

UPDATE daily_questions
SET question_level = 2
WHERE id = 659
  AND question_level = 4;

UPDATE daily_questions
SET question_level = 2
WHERE id = 660
  AND question_level = 3;
