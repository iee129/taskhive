# TaskHive 기여 가이드

먼저 기여를 고려해 주셔서 감사합니다! 모든 기여를 환영합니다.

## 빠른 시작 (로컬 개발)

```bash
git clone https://github.com/iee129/taskhive.git
cd taskhive

# 전체 스택 실행 (Docker)
docker compose up

# 또는 개별 실행
cd apps/server && ./gradlew bootRun   # 백엔드 :8080
cd apps/web && npm install && npm run dev  # 프론트엔드 :5173
```

### 필수 요구사항

| 도구 | 버전 |
|------|------|
| Java | 21 |
| Gradle | 8.7 (wrapper 포함) |
| Node | 20+ |
| Docker Desktop | 최신 |

## PR 절차

1. 이슈를 먼저 열거나 기존 이슈를 할당받으세요.
2. `master`에서 기능 브랜치를 체크아웃하세요 (소문자 한 단어, 예: `webhook`, `darkmode`).
3. 변경사항을 작성하고 테스트를 통과시키세요.
4. PR을 열기 전에 로컬에서 CI 게이트를 확인하세요:
   ```bash
   cd apps/server && ./gradlew check -q
   cd apps/web && npx tsc --noEmit && npm run lint
   ```
5. PR 템플릿을 작성하고 제출하세요.

## 코드 스타일

- **백엔드**: Spring Boot 컨벤션 준수, Lombok 활용, 빈 레이어 분리
- **프론트엔드**: TypeScript strict, 함수형 컴포넌트, Ant Design 컴포넌트 우선
- **테스트**: 새 비즈니스 로직에는 단위 테스트 또는 통합 테스트 필수
- **커밋**: 한글 명사형 어미 (예: `AI 프로바이더 추상화 추가`)

## 행동 강령

이 프로젝트는 [기여자 행동 강령](CODE_OF_CONDUCT.md)을 따릅니다.
