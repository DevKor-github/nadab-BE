-- 오타 및 맥락상 어색한 문장 수정

UPDATE daily_questions
SET empathy_guide = '와 저렇게 살고 싶다! 라고 생각되는 라이프스타일, 곰곰이 생각해보면 하나쯤은 떠오를 거예요.'
WHERE id = 34
  AND empathy_guide = '와 저렿게 살고 싶다! 라고 생각되는 라이프스타일, 곰곰이 생각해보면 하나쯤은 떠오를 거에요.';

UPDATE daily_questions
SET hint_guide = '공통점을 떠올리면 나만의 미적 기준, 취향에 대해 생각해보기 쉬울 거예요.'
WHERE id = 42
  AND hint_guide = '공통점을 떠올리면 나만의 미적 기준, 취향에 대해 생각해보기 쉬울거에요.';

UPDATE daily_questions
SET empathy_guide = '마음을 관리하는 방법이 있다는 건 든든해요.'
WHERE id = 83
  AND empathy_guide = '마음의 관리하는 방법이 있다는 건 든든해요.';

UPDATE daily_questions
SET leading_question_guide = '어려움을 이겨낼 나만의 전략이 있나요?'
WHERE id = 117
  AND leading_question_guide = '이려움을 이겨낼 나만의 전략이 있나요?';

UPDATE daily_questions
SET hint_guide = '''일단 나가자'' 같은 짧은 생각이나 보상 약속인가요?'
WHERE id = 127
  AND hint_guide = '일단 나가자'' 같은 짧은 생각이나 보상 약속인가요?';

UPDATE daily_questions
SET hint_guide = '''오늘만 하자''는 마음인가요, 아니면 미래를 위한 인내인가요?'
WHERE id = 146
  AND hint_guide = '오늘만 하자''는 마음인가요 아니면 미래를 위한 인내인가요?';

UPDATE daily_questions
SET hint_guide = '다정함, 책임감 혹은 유머 감각인가요?'
WHERE id = 225
  AND hint_guide = '다정함,책임감 혹은 유머 감각인가요?';

UPDATE daily_questions
SET hint_guide = '''다시 하면 돼'' 혹은 ''배운 게 있어'' 같은 생각인가요?'
WHERE id = 264
  AND hint_guide = '다시 하면 돼'' 혹은 ''배운 게 있어'' 같은 생각인가요?';

UPDATE daily_questions
SET empathy_guide = '미래에 지금의 나를 되돌아본다면 해주고 싶은 말이 분명 있을 거예요.'
WHERE id = 299
  AND empathy_guide = '미래에 지금의 나를 되돌아본다면 해주고 싶은 말이 분명 있을거에요.';

UPDATE daily_questions
SET question_text = '최근에 꼭 필요하지 않지만 산 물건은 무엇인가요?'
WHERE id = 301
  AND question_text = '최근에 꼭 필요하지 않지만 산 물건은 무멋인가요?';

UPDATE daily_questions
SET hint_guide = '등록했지만 3일도 가지 못한 헬스장인가요? 아니면 야심 차게 구독했지만 공부하지 않은 영어 회화인가요?'
WHERE id = 303
  AND hint_guide = '등록했지만 3일도 가지 못한 헬스장인가요? 아니면 야심차게 구독했지만 공부하지 않은 영어 회화인가요?';

UPDATE daily_questions
SET question_text = '하나의 감정을 더 이상 느끼지 않을 수 있다면 무엇을 고르고 싶나요?'
WHERE id = 380
  AND question_text = '하나의 감정을 더이상 느끼지 않을 수 있다면 무엇을 고르고 싶나요?';

UPDATE daily_questions
SET leading_question_guide = '작업 중 먹는 것이 나에게 보상이 되나요?'
WHERE id = 456
  AND leading_question_guide = '작업 중 먹는 것이 나에게 보상이에요?';

UPDATE daily_questions
SET empathy_guide = '모두 거절을 해야 할 때가 찾아와요.'
WHERE id = 575
  AND empathy_guide = '모두 거절을 해야할 때가 찾아와요.';

UPDATE daily_questions
SET empathy_guide = '최선을 다하더라도 포기해야 할 때가 와요.'
WHERE id = 576
  AND empathy_guide = '최선을 다하더라도 포기해야할 때가 와요.';

UPDATE daily_questions
SET leading_question_guide = '편안함이 회피였던 경험을 생각해봐요.'
WHERE id = 583
  AND leading_question_guide = '편안함이 회피였는던 경험을 생각해봐요.';

UPDATE daily_questions
SET empathy_guide = '수년 전 일이어도 계속 생각나서 고통스러운 상처, 있잖아요.'
WHERE id = 590
  AND empathy_guide = '수 년 전 일이어도 계속 생각나서 고통스러운 상처, 있잖아요.';

UPDATE daily_questions
SET hint_guide = '직업을 선택하는 기준은 무엇이 되어야 할까요?'
WHERE id = 592
  AND hint_guide = '직업을 선택하는 기준은 무엇이 되어야할까요?';

UPDATE daily_questions
SET leading_question_guide = '반대로 과정이 좋지 않다면 결과는 중요하지 않은 걸까요?'
WHERE id = 598
  AND leading_question_guide = '반대로 과정이 좋지 않다면 결과는 중요하지 않은걸까요?';

UPDATE daily_questions
SET leading_question_guide = '그 뉴스를 본 뒤 뭔가 하고 싶어지거나, 반대로 하기 싫어진 게 있었나요?'
WHERE id = 604
  AND leading_question_guide = '그 뉴스를 보고 어떤 감정이 들었나요?';

UPDATE daily_questions
SET empathy_guide = '주변 사람이나 과거의 나와 같이 되고 싶지 않은 모습이 있을 거예요.'
WHERE id = 614
  AND empathy_guide = '주변 사람이나 과거의 나와 같이 되고 싶지 않은 모습이 있을거예요.';

UPDATE daily_questions
SET empathy_guide = '조금만 자존심을 굽혔다면 달랐을 것 같은 게 있나요?'
WHERE id = 616
  AND empathy_guide = '조금만 자존심을 굽혔다면 달랐을 것 같은게 있나요?';

UPDATE daily_questions
SET empathy_guide = '일상 속에서 마주치는 노인 분들을 보며 나도 저렇게 나이 들고 싶다는 생각을 해본 적 있나요?'
WHERE id = 621
  AND empathy_guide = '일상 속에서 마주치는 노인 분들을 보며 나도 저렇게 나이들고 싶다는 생각을 해본 적 있나요?';

UPDATE daily_questions
SET leading_question_guide = '더 큰 성장을 위해 내가 의식적으로 노력해야 할 부분은 무엇인가요?'
WHERE id = 637
  AND leading_question_guide = '더 큰 성장을 위해 내가 의식적으로 노력해야 할 부분은?';

UPDATE daily_questions
SET empathy_guide = '없으면 안 되는 물건은 무엇일까요?'
WHERE id = 647
  AND empathy_guide = '없으면 안되는 물건은 무엇일까요?';

UPDATE daily_questions
SET hint_guide = '어려움도 있었겠지만 감사함을 느끼는 소중한 순간도 있었을 거예요.'
WHERE id = 652
  AND hint_guide = '어려움도 있었겠지만 감사함을 느끼는 소중한 순간도 있었을거에요.';