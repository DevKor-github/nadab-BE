# Nadab Backend

사용자의 일상 답변을 바탕으로 AI 일간·주간·월간·유형 리포트를 생성하고, 친구·소셜 상호작용과 운영 기능을 제공하는 Spring Boot 백엔드 서버입니다.

---

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.7, Spring Security, Spring Data JPA |
| Database | PostgreSQL, Flyway |
| AI | Spring AI, OpenAI, Google Gemini |
| Infrastructure | AWS EC2, S3, KMS, Firebase Cloud Messaging |
| API / View | Swagger/OpenAPI, Thymeleaf |
| Test | JUnit 5, Mockito, AssertJ, Testcontainers |
| Build / CI/CD | Gradle, GitHub Actions |

---

## 주요 기능

- OAuth 및 이메일 기반 인증, JWT 토큰 관리
- 오늘의 질문과 답변 기록, 이미지 처리
- AI 기반 일간·주간·월간·유형 리포트 생성
- 친구, 피드 공유, 댓글, 좋아요, 신고·차단
- 푸시 알림, 이메일 인증, 알림 설정
- 앱 버전, 사용자 통계 등 관리자 기능
- 사용자 지갑 및 크리스탈 이력 관리

---

## 로컬 개발 환경 설정

### 1. 저장소 클론

```bash
git clone https://github.com/DevKor-github/morae-BE.git
cd morae-BE
```

커밋 도구를 사용하려면 Node.js를 설치한 뒤 패키지를 설치합니다.

```bash
npm install
```

### 2. JDK 설정

- JDK 21을 설치합니다.
- IntelliJ에서 프로젝트 루트 또는 `build.gradle`을 프로젝트로 엽니다.
- Project SDK와 Gradle JVM을 JDK 21로 설정합니다.

### 3. PostgreSQL 준비

로컬 PostgreSQL에 사용할 데이터베이스를 생성합니다. 애플리케이션 시작 시 Flyway가 `src/main/resources/db/migration`의 마이그레이션을 자동 적용하며, Hibernate는 스키마를 `validate` 모드로 검증합니다.

---

## 테스트

단위 테스트는 JUnit 5, Mockito, AssertJ를 사용합니다. 데이터베이스 통합 테스트는 PostgreSQL Testcontainers를 사용하므로 Docker Desktop이 실행 중이어야 합니다.

```powershell
# 전체 테스트
.\gradlew.bat test

# 단일 테스트 클래스
.\gradlew.bat test --tests "com.devkor.ifive.nadab.domain.auth.application.AuthServiceV2Test"
```

`develop` 브랜치 대상 Pull Request에는 GitHub Actions가 전체 테스트를 자동 실행합니다.

---

## 배포 환경

- `develop` 브랜치 push: 개발 EC2 자동 배포
- `main` 브랜치 push: 운영 EC2 자동 배포
- GitHub Actions에서 JDK 21로 실행 JAR을 빌드합니다.
- 빌드 산출물을 SCP로 EC2에 전송하고 프로필별 환경 변수를 주입해 애플리케이션을 재시작합니다.
- PostgreSQL, S3, KMS, Firebase 및 외부 AI API는 환경별 자격 증명으로 연동합니다.

배포 관련 secret은 GitHub Environment의 `dev`, `prod` 설정에서 관리하며 저장소에 직접 기록하지 않습니다.

---

## 브랜치 전략

- `main`: 운영 배포 브랜치
- `develop`: 개발 통합 및 개발 서버 배포 브랜치
- 기능 개발 브랜치: `feat/*`, `fix/*`, `refactor/*`, `chore/*` 등 작업 목적에 맞게 생성

기능 브랜치는 `develop`을 기준으로 작업하고 Pull Request를 통해 병합합니다. 운영 반영은 `develop`에서 검증된 변경을 `main`으로 병합해 진행합니다.

---

## 프로젝트 구조

```text
src
├─ main
│  ├─ java/com/devkor/ifive/nadab
│  │  ├─ domain        # 비즈니스 도메인
│  │  └─ global        # 공통 설정, 보안, 예외, 인프라, 유틸리티
│  └─ resources
│     ├─ db/migration  # Flyway 마이그레이션
│     ├─ templates     # 관리자·통계 Thymeleaf 화면
│     └─ application*.yml
└─ test
   └─ java/com/devkor/ifive/nadab
      └─ infra         # Testcontainers 및 테스트 빌더
```

각 도메인은 기능에 따라 다음 계층을 조합합니다.

- `api`: Controller와 요청·응답 DTO
- `application`: 유스케이스, 트랜잭션, 이벤트, 스케줄러
- `core`: Entity, Repository, Domain Service, 내부 모델
- `infra`: 외부 AI API, 스토리지 등 외부 연동 구현

공통 설정과 여러 도메인이 함께 사용하는 기능은 `global`에 둡니다. 데이터베이스 스키마 변경은 JPA 설정에 의존하지 않고 Flyway 마이그레이션으로 관리합니다.

---

## 커밋 컨벤션

Conventional Commits 기반의 commitlint, Husky, Commitizen을 사용합니다.

```text
<type>(<scope>): <subject>

<body>
```

### Type

| Type | 설명 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 변경 |
| `chore` | 빌드·설정 등 기타 작업 |
| `style` | 동작에 영향을 주지 않는 스타일 변경 |
| `refactor` | 기능 변경 없는 구조 개선 |
| `test` | 테스트 추가 또는 보완 |
| `perf` | 성능 개선 |
| `ci` | CI/CD 설정 변경 |
| `revert` | 이전 커밋 되돌리기 |

### Scope

허용 scope는 다음과 같습니다.

```text
admin, auth, user, ai, report, moderation, search, stats,
friend, social, notify, infra, db, global
```

- subject는 변경 내용을 한국어로 간결하게 작성합니다.
- subject 끝에 마침표를 붙이지 않습니다.
- 맥락이 필요한 변경은 body에 무엇을 바꿨고 왜 바꿨는지 작성합니다.
- scope는 생략할 수 있지만, 사용할 경우 허용 목록 중 하나를 선택해야 합니다.

```bash
# 직접 작성
git commit -m "feat(report): 월간 리포트 조회 기능 추가"

# Commitizen 프롬프트 사용
npm run commit
```

---

## 참고

- 마이그레이션: `src/main/resources/db/migration`
- CI/CD: `.github/workflows`

