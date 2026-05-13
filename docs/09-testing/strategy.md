# 테스트 전략

## 테스트 피라미드

```
          /\
         /E2E\          Playwright — 비인증 리다이렉트, 폼 유효성
        /------\
       /통합 테스트\      Testcontainers(PostgreSQL) + MockMvc(H2)
      /------------\
     / 단위 테스트   \   Mockito 74개 + @DataJpaTest 12개
    /________________\
```

## 계층별 전략

| 계층 | 도구 | 범위 | 격리 |
|------|------|------|------|
| 단위 (백엔드) | JUnit 5 + Mockito | Service, Util, JwtUtil | Mock 의존성 |
| 슬라이스 | `@DataJpaTest` + H2 | Repository 쿼리 검증 | 인메모리 DB |
| 통합 (MockMvc) | `@SpringBootTest` + H2 | Controller 전체 플로우 | 인메모리 DB |
| 통합 (컨테이너) | Testcontainers + PostgreSQL | 실제 DB 전체 플로우 | Docker 컨테이너 |
| 컴포넌트 | Vitest + RTL + MSW | 렌더링, 사용자 이벤트 | API 목업 |
| E2E | Playwright | 브라우저 실제 플로우 | 실제 앱 서버 |

## 현재 커버리지 (Phase 7 기준)

### 백엔드

```
mvn verify 결과 (2026-05-12):
  총 테스트:  118개
  통과:       105개
  Skip:        13개 (Testcontainers — Docker 미실행)
  실패:         0개

JaCoCo:
  라인 커버리지:   ≥ 80% ✅
  브랜치 커버리지: ≥ 70% ✅
```

커버리지 제외 대상 (프레임워크 주도 코드):
- `com/taskhive/dto/**`, `com/taskhive/model/**`
- `com/taskhive/aspect/**`, `com/taskhive/filter/**`
- `config/SecurityConfig`, `config/JwtFilter`

### 프론트엔드

```
Vitest 결과:
  Test Files: 2 passed
  Tests:     11 passed (FilterBar 6 + LoginPage 5)
```

## 커버리지 목표

| 계층 | 목표 | 현재 상태 |
|------|------|-----------|
| Service (백엔드) | 90%+ | ✅ 단위 테스트 74개로 달성 |
| Repository | 주요 쿼리 검증 | ✅ TaskRepositoryTest 12개 |
| Controller | 인증 경로 통합 | ✅ MockMvc + Testcontainers |
| JaCoCo 라인 | ≥ 80% | ✅ 달성 |
| JaCoCo 브랜치 | ≥ 70% | ✅ 달성 |
| 프론트엔드 컴포넌트 | 주요 UI 검증 | ✅ FilterBar, LoginPage |

## 테스트 프로파일

| 프로파일 | 활성화 | DB | 용도 |
|----------|--------|-----|------|
| `test` | `@ActiveProfiles("test")` | H2 인메모리 | 단위·슬라이스·MockMvc |
| `tc` | `@ActiveProfiles("tc")` | PostgreSQL (Testcontainers) | 통합 테스트 |

## 테스트 데이터 원칙

- 테스트는 독립적 — 실행 순서에 의존 금지
- `@BeforeEach`에서 상태 초기화 (`@DirtiesContext` 대신 고유값 사용)
- 프로덕션 DB 사용 금지 — H2 또는 Testcontainers 컨테이너 사용
- 임의값은 `@Test` 내부에서 명시적 설정 (난수 금지)
- Testcontainers는 Docker 없이도 `disabledWithoutDocker=true`로 skip 처리

## CI 통합 (예정 — Phase 11)

```yaml
# .github/workflows/ci-backend.yml
- name: Test with JaCoCo
  run: mvn verify -f auth/pom.xml

# .github/workflows/ci-frontend.yml
- name: Component Tests
  run: cd frontend && npm run test

- name: E2E Tests
  run: cd frontend && npx playwright install && npm run test:e2e
```

## 실행 명령 요약

```bash
# 백엔드 — 단위 + 슬라이스 + MockMvc (빠름)
mvn test -f auth/pom.xml

# 백엔드 — 커버리지 게이트 포함 전체 (Docker 있으면 Testcontainers 포함)
mvn verify -f auth/pom.xml

# 프론트엔드 — 컴포넌트 테스트
cd frontend && npm run test

# 프론트엔드 — E2E
cd frontend && npm run test:e2e
```
