# 경쟁 프로젝트 비교 분석

> 분석 일자: 2026-05-12  
> 목적: GitHub 포트폴리오 프로젝트와 비교하여 TaskHive의 차별점 확인 및 강화 방향 도출

---

## 분석 대상 프로젝트

| 프로젝트 | Stars | 스택 | 특징 |
|---------|-------|------|------|
| [ivangfr/springboot-react-jwt-token](https://github.com/ivangfr/springboot-react-jwt-token) | 184 | Spring Boot 4 + Java 25 + React JS | 테스트 가장 체계적 |
| [ali-bouali/spring-boot-3-jwt-security](https://github.com/ali-bouali/spring-boot-3-jwt-security) | 2,044 | Spring Boot 3.1 + Java 17 | 가장 많이 참고됨, 백엔드 전용 |
| [mehedi008h/project-mangement](https://github.com/mehedi008h/project-mangement) | 8 | Spring Boot 3 + Java 17 + React JS | Task 도메인 가장 유사 |
| [aidanwhiteley/books](https://github.com/aidanwhiteley/books) | 108 | Spring Boot 4 + Java 21 + Angular | 인증 구현 완성도 최고 (다른 도메인) |

---

## 기술 수준 비교표

| 차별점 | TaskHive | ivangfr (184★) | ali-bouali (2044★) | mehedi (8★) |
|--------|----------|----------------|---------------------|-------------|
| Refresh Token Rotation | ✅ | ❌ | ❌ (재사용) | ❌ |
| HttpOnly Cookie | ✅ | ❌ (헤더 전달) | ❌ (Body 전달) | ❌ |
| SameSite=Lax CSRF 방어 | ✅ | ❌ | ❌ | ❌ |
| TypeScript 프론트엔드 | ✅ | ❌ (JS) | 없음 | ❌ (JS) |
| PESSIMISTIC_WRITE 비관적 락 | ✅ | ❌ | ❌ | ❌ |
| 실질적 Task 관리 도메인 | ✅ | ❌ (주문) | ❌ (인증만) | ✅ (미완) |
| 페이지네이션 | 🚧 예정 | ❌ | ❌ | ❌ |
| 칸반 보드 | 🚧 예정 | ❌ | ❌ | ❌ |
| CI/CD | 🚧 예정 | ❌ | ❌ | ❌ |
| Docker Compose | 🚧 예정 | ✅ | ✅ | ❌ |
| 테스트 체계성 | 33개 통과 | **가장 체계적** | 미흡 | 없음 |
| AI 기능 | 🚧 예정 | ❌ | ❌ | ❌ |
| OpenAPI 문서 | 🚧 예정 | ✅ | ❌ | ❌ |

---

## 각 프로젝트 상세 분석

### 1. ivangfr/springboot-react-jwt-token (184★)

**배울 점 — 테스트 아키텍처:**

컨트롤러 슬라이스 테스트가 가장 체계적. `@WebMvcTest` + `@Import(SecurityConfig.class)` 패턴으로 실제 필터 체인까지 포함:

```java
// 각 엔드포인트마다 성공/실패/경계값 케이스 분리
void signUp_returns409WhenSaveUserThrowsDataIntegrityViolation()
void authenticate_returns400WhenPasswordBlank()
```

**이식 가치:** Phase 7 테스트 고도화 시 이 패턴 적용 → JaCoCo 80% 달성 경로

**TaskHive가 앞서는 점:**
- Access Token 단독 발급 (Refresh Token 없음)
- JS 프론트엔드 (TypeScript 미사용)
- HttpOnly Cookie 미사용 → XSS 취약

**기타 참고사항:**
- `Spotless` + `google-java-format` 코드 포맷 자동화 → CI 파이프라인에 통합 검토
- Spring Boot 4.0.6 + Java 25 추적 (버전 업데이트 주기 참고)

---

### 2. ali-bouali/spring-boot-3-jwt-security (2,044★)

**분석 요약:**

가장 많이 참고되는 프로젝트지만 치명적 결함 존재:

```java
// /refresh 응답에 기존 Refresh Token 그대로 반환 — Rotation 없음
authResponse.refreshToken(refreshToken)  // 재사용!
```

토큰을 Response Body로 전달 (HttpOnly Cookie 미사용), HS256 서명 (TaskHive 대비 약한 알고리즘).

**배울 점 — DB 토큰 블랙리스트:**

`Token` 엔티티로 발급된 Access Token을 DB에 보관하고 로그아웃 시 `revoked=true`:

```java
public class Token {
    public String token;
    public boolean revoked;   // 폐기 여부
    public boolean expired;   // 만료 여부
    @ManyToOne User user;
}
```

**활용 가능성:** 현재 TaskHive는 Refresh Token만 DB 관리. 향후 Access Token 즉시 무효화가 필요하면 이 패턴 도입 검토.

**배울 점 — 세분화된 RBAC:**

Role 외에 Permission 열거형 추가:
```java
enum Permission {
    ADMIN_READ, ADMIN_CREATE, MANAGER_READ, MANAGER_CREATE ...
}
```

TaskHive의 USER/ADMIN 2단계 Role에서 확장 시 참고.

---

### 3. mehedi008h/project-mangement (8★)

**가장 유사한 도메인 (Project + Task + 팀원 배정)**

**배울 점 — 도메인 UX 기능:**

- **이메일 알림**: 태스크 배정 시 `JavaMailSender`로 자동 이메일 전송 → 향후 계획 항목과 연결
- **Cloudinary CDN**: 프로필 이미지·첨부파일 외부 스토리지 패턴

**TaskHive가 압도적으로 앞서는 점:**
- TypeScript vs JavaScript
- Refresh Token Rotation vs 없음
- 테스트 33개 vs 사실상 없음
- 2023년 이후 미업데이트 vs 활발히 진행 중

---

## 포트폴리오 생태계 공통 패턴

검색된 30+ 프로젝트를 관통하는 공통점:

> "Spring Boot + React JWT Task 관리" 카테고리에서  
> **Refresh Token Rotation + HttpOnly Cookie + TypeScript + 비관적 락**을  
> 동시에 갖춘 프로젝트는 사실상 존재하지 않음.

모든 프로젝트에서 발견된 공통 취약점:
- Access Token 단독 발급 (Refresh Token 없거나 있어도 rotation 미구현)
- TypeScript 미사용 또는 부분 적용
- HttpOnly Cookie 미사용 (XSS 노출)
- 페이지네이션 없음
- 동시성 제어 없음

---

## TaskHive 실질적 포지셔닝

Phase 5~11 완료 시 TaskHive가 갖는 항목:

```
✅ Refresh Token Rotation + PESSIMISTIC_WRITE (보안·동시성)
✅ HttpOnly Cookie + SameSite=Lax (CSRF 방어)
✅ TypeScript 풀스택
✅ JaCoCo 80% + Testcontainers + Playwright E2E (테스트)
✅ TanStack Query + Redis 캐싱 (성능)
✅ AI 자연어 태스크 생성 + 일간 다이제스트 (Ollama 기반)
✅ Docker Compose (postgres + redis + ollama 자급자족)
✅ GitHub Actions CI/CD + GHCR 이미지 푸시
```

이 조합은 포트폴리오 Java 풀스택 카테고리에서 **실질적 상위 1% 수준**.

---

## 우선순위별 개선 방향

### 단기 (Phase 5~6)

| 항목 | 출처 | 적용 방법 |
|------|------|---------|
| `@WebMvcTest + @Import(SecurityConfig)` 패턴 | ivangfr | Phase 7 테스트 작성 시 적용 |
| OpenAPI(SpringDoc) 통합 | ivangfr | Phase 5에 이미 계획됨 |
| 페이지네이션 | — | Phase 6에 이미 계획됨 |

### 중기 (Phase 7~9)

| 항목 | 출처 | 적용 방법 |
|------|------|---------|
| Spotless + google-java-format | ivangfr | CI 파이프라인에 lint 단계 추가 |
| Permission 기반 세분화 RBAC | ali-bouali | Phase 5 아키텍처 고도화 시 검토 |

### 장기 (향후 계획)

| 항목 | 출처 | 검토 수준 |
|------|------|---------|
| 이메일 알림 (태스크 배정) | mehedi | 향후 계획 항목 (`JavaMailSender`) |
| DB 기반 Access Token 블랙리스트 | ali-bouali | 필요 시 도입 (현재 오버엔지니어링) |

---

## 결론

**TaskHive의 현재 코드(Phase 1~4)는 이미 분석된 모든 프로젝트보다 보안·타입 안전성 측면에서 앞서 있음.**

남은 과제:
1. **테스트 커버리지** — ivangfr 패턴 이식 (Phase 7)
2. **기능 완성도** — 페이지네이션·칸반·댓글·AI (Phase 6)
3. **인프라** — Docker + CI/CD (Phase 10~11)

이 세 가지를 채우면 포트폴리오 차별화 목표 달성.
