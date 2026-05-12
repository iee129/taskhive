# auth 브랜치 검증 계획

## 검증 범위

auth 브랜치(`auth/` 폴더)의 Spring Boot 인증 구현이 의도한 동작을 올바르게 수행하는지 검증한다.
master는 건드리지 않으며, 보완이 필요한 코드는 auth 브랜치에만 반영한다.

---

## 검증 기준 (5개 카테고리)

### C1. 보안 설정
| 기준 | 검증 방법 | 파일 |
|------|----------|------|
| `/api/auth/register`, `/api/auth/login`만 인증 없이 접근 가능 | 통합 테스트 + SecurityConfig 코드 리뷰 | `SecurityConfig.java:43-45` |
| JWT 없는 보호 경로 → 401 (403 아님) | `me_JWT없음_401` 테스트 통과 | `AuthControllerTest.java:112` |
| 인증 실패 시 JSON 응답 `{"error": "인증이 필요합니다"}` | MockMvc 응답 body 검증 | `SecurityConfig.java:38-42` |
| CSRF 비활성화 + STATELESS 세션 | SecurityConfig 코드 리뷰 | `SecurityConfig.java:33-36` |

### C2. JWT 구현
| 기준 | 검증 방법 | 파일 |
|------|----------|------|
| `generateToken`이 subject(email)과 expiration을 포함 | 단위 테스트 | `JwtUtil.java:25-32` |
| `extractEmail`이 토큰에서 이메일 정확히 추출 | 단위 테스트 | `JwtUtil.java:34-36` |
| `isValid`가 변조된 토큰에 false 반환 | 단위 테스트 | `JwtUtil.java:38-45` |
| 만료된 토큰에 `isValid` false 반환 | 단위 테스트 | `JwtUtil.java:38-45` |

### C3. API 엔드포인트 동작
| 기준 | HTTP | 기대 응답 | 테스트 |
|------|------|----------|--------|
| POST /api/auth/register 정상 | 200 | `{token, email, name}` | `register_정상_200_token반환` |
| POST /api/auth/register 중복 이메일 | 400 | `{error}` | `register_중복이메일_400` |
| POST /api/auth/register 빈 이름 | 400 | `{errors: [...]}` | `register_빈이름_400` |
| POST /api/auth/login 정상 | 200 | `{token, email, name}` | `login_정상_200_token반환` |
| POST /api/auth/login 잘못된 비밀번호 | 401 | `{error}` | `login_잘못된비밀번호_401` |
| GET /api/auth/me JWT 없음 | 401 | `{error}` | `me_JWT없음_401` |
| GET /api/auth/me 유효한 JWT | 200 | `{email, name}` | `me_유효한JWT_200_사용자정보반환` |

### C4. 예외 처리
| 예외 타입 | HTTP | 응답 형식 | 핸들러 |
|----------|------|----------|--------|
| `IllegalArgumentException` | 400 | `{"error": "..."}` | `GlobalExceptionHandler.java:13` |
| `BadCredentialsException` | 401 | `{"error": "이메일 또는 비밀번호가 올바르지 않습니다"}` | `GlobalExceptionHandler.java:18` |
| `MethodArgumentNotValidException` | 400 | `{"errors": ["field: msg"]}` | `GlobalExceptionHandler.java:24` |

### C5. 테스트 커버리지
| 항목 | 현재 상태 | 기준 |
|------|----------|------|
| AuthServiceTest — register/login 단위 테스트 4개 | ✅ 존재 | 통과 필수 |
| AuthControllerTest — 7개 통합 시나리오 | ✅ 존재 | 통과 필수 |
| `getMe` 서비스 단위 테스트 | ❌ 누락 | 추가 필요 |
| 변조/만료 JWT로 `/me` 호출 시 401 통합 테스트 | ❌ 누락 | 추가 필요 |
| password @Size(min=8) validation 실패 테스트 | ❌ 누락 | 추가 권장 |

---

## 현재 코드 분석 결과

### 이슈 목록 (우선순위별)

#### P1 — 테스트 커버리지 누락 (보완 필수)

**이슈 1**: `AuthServiceTest`에 `getMe` 단위 테스트 없음
- `AuthService.getMe`는 `userRepository.findByEmail`을 호출하고 없으면 `IllegalArgumentException`을 던지는 로직이 있으나 검증 안 됨
- 추가할 케이스: `getMe_정상_사용자정보반환`, `getMe_없는이메일_예외발생`

**이슈 2**: 변조/만료 JWT로 `/api/auth/me` 호출 시 401 통합 테스트 없음
- 현재: JWT 없는 케이스만 검증
- 추가할 케이스: `me_변조JWT_401`

#### P2 — 응답 설계 개선 (권장)

**이슈 3**: `/api/auth/me` 응답에 `token: null` 포함
- `AuthResponse(null, email, name)` → JSON: `{"token": null, "email": "...", "name": "..."}`
- null 필드를 직렬화에서 제외하거나(`@JsonInclude(NON_NULL)`), 별도 `MeResponse` DTO 사용 권장
- 현재 테스트(`me_유효한JWT_200_사용자정보반환`)가 `token` 필드를 검증하지 않아 통과하지만, 클라이언트에서 혼동 가능

#### P3 — 커버리지 보강 (선택)

**이슈 4**: password 7자 이하 등 RegisterRequest validation 실패 케이스 테스트 없음
- `@Size(min=8)`이 선언되어 있으나 통합 테스트에서 검증 안 됨

---

## 검증 실행 계획

### Step 1: 기존 테스트 전체 실행 (현황 파악)
```bash
cd auth && mvn test
```
- `AuthServiceTest` 4개 + `AuthControllerTest` 7개 = 총 11개 통과 확인
- surefire-reports에서 실패 여부 확인

### Step 2: 누락 테스트 추가 (P1 보완)
파일: `auth/src/test/java/com/taskhive/service/AuthServiceTest.java`
- `getMe_정상_사용자정보반환`: `findByEmail` mock → email/name 검증
- `getMe_없는이메일_예외발생`: `findByEmail` empty → `IllegalArgumentException` 검증

파일: `auth/src/test/java/com/taskhive/controller/AuthControllerTest.java`
- `me_변조JWT_401`: `"Bearer invalid.token.here"` 헤더로 GET /me → 401 검증

### Step 3: /me 응답 null 처리 (P2 보완)
두 가지 옵션:
- **옵션 A** (권장): `AuthResponse`에 `@JsonInclude(JsonInclude.Include.NON_NULL)` 추가 → token=null이면 JSON에서 생략
- **옵션 B**: `MeResponse(email, name)` 별도 DTO 생성 → `AuthController.me`가 반환

### Step 4: 재실행 및 전체 통과 확인
```bash
cd auth && mvn test
```
총 13개(또는 14개) 테스트 GREEN 확인

### Step 5: 검증 완료 후 커밋 및 푸시
```
auth 브랜치 검증 테스트 보완
```
```bash
git push origin auth
```

---

## 수용 기준 (Acceptance Criteria)

1. `mvn test` 실행 시 모든 테스트 GREEN (BUILD SUCCESS)
2. `getMe` 서비스 단위 테스트 2개 추가됨
3. 변조 JWT로 `/me` 호출 시 401 통합 테스트 추가됨
4. `/me` 응답에서 `token` null 필드 미노출 (`@JsonInclude(NON_NULL)` 적용)
5. 코드 변경은 auth 브랜치에만 반영 (master 미변경)
