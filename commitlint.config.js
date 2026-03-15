module.exports = {
    extends: ['@commitlint/config-conventional'],
    rules: {
        "type-enum": [2, "always", [
            "feat",
            "fix",
            "docs",
            "chore",
            "style",
            "refactor",
            "test",
            "perf",
            "ci",
            "revert"
        ]],
        "scope-enum": [2, "always", [
            "auth",
            "user",
            "ai",
            "friend",
            "notify",
            "infra",
            "db",
            "global"
        ]],
        "footer-empty": [2, "always"],
        "subject-case": [0]
    },
    prompt: {
        settings: {},
        messages: {
            skip: ':건너뛰기 가능',
            max: '최대 %d자까지 입력할 수 있어요',
            min: '최소 %d자 이상 입력해주세요',
            emptyWarning: '내용을 비워둘 수는 없어요',
            upperLimitWarning: '입력한 내용이 너무 길어요',
            lowerLimitWarning: '입력한 내용이 너무 짧아요',
        },
        questions: {
            type: {
                description: '[Type] 이번 커밋의 변경 유형을 선택해주세요:',
                enum: {
                    feat: {
                        description: '✨ 새로운 기능 추가',
                        title: '기능',
                        emoji: '✨',
                    },
                    fix: {
                        description: '🐛 버그 수정',
                        title: '버그 수정',
                        emoji: '🐛',
                    },
                    docs: {
                        description: '📚 문서 관련 변경사항',
                        title: '문서',
                        emoji: '📚',
                    },
                    style: {
                        description: '💎 코드 의미에는 영향을 주지 않는 스타일 변경 (공백, 포맷, 세미콜론 등)',
                        title: '스타일',
                        emoji: '💎',
                    },
                    refactor: {
                        description: '📦 버그 수정이나 기능 추가가 아닌 코드 리팩토링',
                        title: '리팩토링',
                        emoji: '📦',
                    },
                    perf: {
                        description: '🚀 성능 개선을 위한 코드 변경',
                        title: '성능',
                        emoji: '🚀',
                    },
                    test: {
                        description: '🚨 테스트 추가 또는 기존 테스트 보완',
                        title: '테스트',
                        emoji: '🚨',
                    },
                    ci: {
                        description: '⚙️ CI 설정 또는 관련 스크립트 변경 (예: GitHub Actions 워크플로 수정)',
                        title: 'CI 설정',
                        emoji: '⚙️',
                    },
                    chore: {
                        description: '♻️ 소스 코드나 테스트 외의 기타 작업 (예: 디렉토리 구조 변경, 패키지 설치, .gitignore 수정)',
                        title: '기타 작업',
                        emoji: '♻️',
                    },
                    revert: {
                        description: '🗑 이전 커밋 되돌리기',
                        title: '되돌리기',
                        emoji: '🗑',
                    },
                },
            },
            scope: {
                description: '[Scope] 이번 변경이 적용된 범위를 선택해주세요 (범위 생략하려면 empty 선택)',
                enum: {
                    auth: {
                        description: '🔐 인증/인가 도메인 (예: OAuth2, JWT, 세션)'
                    },
                    user: {
                        description: '🙋‍♂️ 사용자 도메인 (예: 프로필, 내 정보)'
                    },
                    ai: {
                        description: '🤖 AI 처리 (예: 벡터 검색, OpenAI API, 모델 응답 로직)'
                    },
                    friend: {
                        description: '👥 친구 도메인 (예: 친구 신청, 수락, 목록 관리)'
                    },
                    notify: {
                        description: '📧 알림/이메일 전송 (예: 질문 알림, 인증 이메일 전송)'
                    },
                    infra: {
                        description: '🛠️ 설정/인프라 (예: 스크립트, 환경 설정, 배포 설정)'
                    },
                    db: {
                        description: '🗄️ DB/마이그레이션 (예: Flyway, 스키마 변경)'
                    },
                    global: {
                        description: '🌎 global 폴더 설정 (config, shared, README 등 변경)'
                    }
                }
            },
            subject: {
                description: '[Subject] 핵심 변경 내용을 간결하게 적어주세요 (예: 사용자 도메인 추가, 프로필 이미지 업로드 기능 추가)',
            },
            body: {
                description: '[Body] 필요하다면, 무엇을 변경했고 왜 변경했는지 써주세요 (예: 조회 성능 최적화 – 쿼리 실행 시간 단축 목적)',
            },
        },
    }
};
