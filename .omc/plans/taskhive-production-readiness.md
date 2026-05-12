# TaskHive 프로덕션 준비 계획

**목표**: 불특정 다수가 쓸 수 있는 공개 서비스 수준으로 누락 항목 보완  
**기준**: 요구사항 분석 결과 (2026-05-12)  
**제외**: Railway sleep 정책, Ollama Railway 동작 검증 (열린 질문)

---

## 우선순위 분류

| 단계 | 항목 | 이유 |
|------|------|------|
| P1 — 즉시 | Rate Limiting, 이메일 인증, 비밀번호 재설정, 계정 탈퇴, Sentry | 보안·신뢰성 필수 |
| P2 — 중요 | 소셜 로그인, 인앱 알림, 개인정보처리방침·이용약관 | UX·법적 요건 |
| P3 — 선택 | 파일 첨부, 다국어 지원 | 기능 완성도 |

---

## Phase 13 — 보안·신뢰성 (P1)

### US-001: Rate Limiting

**목적**: 브루트포스·DDoS 방어

**구현 대상**: `auth/src/main/java/com/taskhive/`

**구현 내용**:
1. `pom.xml`에 Bucket4j + Spring Boot Starter 의존성 추가
   ```xml
   <dependency>
     <groupId>com.github.bucket4j</groupId>
     <artifactId>bucket4j-spring-boot-starter</artifactId>
     <version>8.10.1</version>
   </dependency>
   ```
2. `config/RateLimitConfig.java` 생성 — 엔드포인트별 제한 정의
   - `/auth/login`: 10회/분
   - `/auth/register`: 5회/분
   - `/auth/reset-password`: 3회/분
   - 그 외 API: 100회/분
3. `filter/RateLimitFilter.java` — IP 기반 필터 (OncePerRequestFilter)
4. 429 Too Many Requests 응답 시 `ErrorCode.RATE_LIMIT_EXCEEDED` 반환

**수용 기준**:
- `/auth/login`에 11번째 요청 시 HTTP 429 반환
- `X-RateLimit-Remaining` 헤더 포함
- 단위 테스트: `RateLimitFilterTest.java`

---

### US-002: 이메일 발송 인프라

**목적**: 이메일 인증·비밀번호 재설정의 공통 기반

**구현 내용**:
1. `pom.xml`에 `spring-boot-starter-mail` 추가
2. `auth/.env.example`에 SMTP 변수 추가
   ```
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=<gmail>
   MAIL_PASSWORD=<app-password>
   MAIL_FROM=noreply@taskhive.app
   ```
3. `service/EmailService.java` — 템플릿 기반 이메일 발송
4. `resources/templates/email/` — Thymeleaf HTML 템플릿
   - `verification.html` (이메일 인증)
   - `reset-password.html` (비밀번호 재설정)

**수용 기준**:
- `EmailService.sendVerificationEmail()` 호출 시 템플릿 렌더링 후 발송
- `EmailServiceTest.java` — Mockito로 JavaMailSender 목 처리

---

### US-003: 이메일 인증 (회원가입)

**목적**: 스팸 계정 방어, 유효한 이메일 주소 확인

**DB 변경** (`auth/src/main/resources/db/migration/`):
```sql
-- V5__add_email_verification.sql
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN verification_token VARCHAR(64);
ALTER TABLE users ADD COLUMN verification_token_expires_at TIMESTAMP;
```

**구현 내용**:
1. `entity/User.java` — `emailVerified`, `verificationToken`, `verificationTokenExpiresAt` 필드 추가
2. `service/AuthService.java` — `register()` 수정: 가입 후 인증 이메일 발송
3. `controller/AuthController.java` — `GET /auth/verify-email?token={token}` 엔드포인트 추가
4. 미인증 계정 로그인 시 HTTP 403 + `EMAIL_NOT_VERIFIED` 에러 코드 반환

**수용 기준**:
- 회원가입 후 DB `email_verified = false`, 인증 토큰 저장됨
- 올바른 토큰으로 `GET /auth/verify-email` 호출 시 `email_verified = true`
- 만료 토큰(24시간) 사용 시 HTTP 400
- 미인증 계정 로그인 시 HTTP 403

---

### US-004: 비밀번호 재설정 (이메일 기반)

**목적**: 비밀번호를 잊은 사용자 계정 복구

**DB 변경**:
```sql
-- V6__add_password_reset.sql
CREATE TABLE password_reset_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  token VARCHAR(64) NOT NULL UNIQUE,
  expires_at TIMESTAMP NOT NULL,
  used_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

**구현 내용**:
1. `entity/PasswordResetToken.java`
2. `repository/PasswordResetTokenRepository.java`
3. `controller/AuthController.java` 엔드포인트 추가:
   - `POST /auth/forgot-password` — 이메일 입력 → 재설정 링크 발송
   - `POST /auth/reset-password` — 토큰 + 새 비밀번호 → 변경 처리
4. `service/PasswordResetService.java`
5. 프론트엔드: `frontend/src/pages/ForgotPasswordPage.tsx`, `ResetPasswordPage.tsx`
6. React Router에 `/forgot-password`, `/reset-password` 경로 추가

**수용 기준**:
- `POST /auth/forgot-password` 후 DB에 토큰 저장, 이메일 발송
- 유효 토큰으로 `POST /auth/reset-password` 시 비밀번호 변경 + 토큰 무효화
- 만료 토큰(1시간) 사용 시 HTTP 400
- 이미 사용된 토큰 재사용 시 HTTP 400
- `PasswordResetServiceTest.java` 단위 테스트

---

### US-005: 계정 탈퇴

**목적**: GDPR 요건, 사용자 데이터 삭제 권리

**구현 내용**:
1. `controller/UserController.java` — `DELETE /users/me` 엔드포인트
2. `service/UserService.java` — `withdrawAccount()`:
   - 사용자 소유 프로젝트·태스크·댓글 Soft Delete
   - `users` 테이블 `deleted_at` 업데이트
   - 모든 Refresh Token 무효화
3. 프론트엔드: 설정 페이지에 "계정 탈퇴" 버튼 + 확인 모달

**수용 기준**:
- `DELETE /users/me` 후 `users.deleted_at` 값이 설정됨
- 탈퇴한 이메일로 재로그인 시 HTTP 401
- 탈퇴 후 해당 사용자의 Refresh Token 모두 무효화

---

### US-006: Sentry 에러 모니터링

**목적**: 프로덕션 에러 실시간 추적

**백엔드**:
1. `pom.xml`에 `sentry-spring-boot-starter` 추가
2. `auth/.env.example`에 `SENTRY_DSN=<dsn>` 추가
3. `application-prod.yml`에 Sentry 설정
4. `GlobalExceptionHandler.java` — 5xx 에러 Sentry 자동 캡처 (이미 `@RestControllerAdvice` 있음)

**프론트엔드**:
1. `npm install @sentry/react`
2. `frontend/.env.example`에 `VITE_SENTRY_DSN=<dsn>` 추가
3. `frontend/src/main.tsx` — Sentry.init() 추가

**수용 기준**:
- 백엔드에서 RuntimeException 발생 시 Sentry 대시보드에 이슈 생성
- 프론트엔드에서 ErrorBoundary 오류 시 Sentry 캡처

---

## Phase 14 — UX·법적 요건 (P2)

### US-007: Google OAuth 소셜 로그인

**목적**: 가입 마찰 감소, 공개 서비스 기본 기대치

**구현 내용**:
1. `pom.xml`에 `spring-boot-starter-oauth2-client` 추가
2. `auth/.env.example`에 Google OAuth 변수 추가
   ```
   GOOGLE_CLIENT_ID=<client-id>
   GOOGLE_CLIENT_SECRET=<client-secret>
   ```
3. `config/SecurityConfig.java` — OAuth2 로그인 핸들러 추가
4. `service/OAuth2UserService.java` — Google 계정 → 로컬 User 매핑
5. `controller/AuthController.java` — `GET /auth/oauth2/callback/google`
6. 프론트엔드: 로그인 페이지에 "Google로 계속하기" 버튼

**수용 기준**:
- Google 계정으로 로그인 후 JWT 발급
- 기존 이메일 계정과 동일 이메일인 경우 계정 연동

---

### US-008: 개인정보처리방침·이용약관 페이지

**목적**: 법적 요건, 공개 서비스 필수

**구현 내용**:
1. `frontend/src/pages/PrivacyPage.tsx` — 개인정보처리방침
2. `frontend/src/pages/TermsPage.tsx` — 이용약관
3. React Router에 `/privacy`, `/terms` 경로 추가
4. 회원가입 폼에 "이용약관 및 개인정보처리방침에 동의합니다" 체크박스 추가
5. 푸터 컴포넌트에 링크 추가

**수용 기준**:
- `/privacy`, `/terms` URL 접근 시 페이지 렌더링
- 회원가입 시 약관 동의 체크박스 미체크 → 제출 불가
- 푸터에 두 링크 표시

---

### US-009: 인앱 알림

**목적**: 마감일, 태스크 배정 알림 사용자 경험

**DB 변경**:
```sql
-- V7__add_notifications.sql
CREATE TABLE notifications (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  type VARCHAR(50) NOT NULL,
  message TEXT NOT NULL,
  read_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

**구현 내용**:
1. `entity/Notification.java`, `repository/NotificationRepository.java`
2. `service/NotificationService.java` — 태스크 배정·마감일 도래 시 알림 생성
3. `controller/NotificationController.java` — `GET /notifications`, `PATCH /notifications/{id}/read`
4. WebSocket으로 실시간 알림 푸시 (기존 STOMP 인프라 재활용)
5. 프론트엔드: 네비게이션 바에 알림 아이콘 + 드롭다운

**수용 기준**:
- 태스크 배정 시 담당자에게 알림 DB 저장 + WebSocket 실시간 전송
- `PATCH /notifications/{id}/read` 호출 시 `read_at` 설정
- 프론트엔드 알림 아이콘에 미읽음 뱃지 표시

---

## Phase 15 — 기능 완성도 (P3, 선택)

### US-010: 파일 첨부

**목적**: 태스크에 파일 첨부 (이미지, 문서)

**인프라**: AWS S3 또는 Cloudflare R2 (무료 10GB)

**구현 내용**:
1. `service/FileStorageService.java` — S3/R2 업로드·다운로드
2. `controller/FileController.java` — `POST /files/upload`, `GET /files/{id}`
3. `entity/Attachment.java` — task_id 외래키
4. 프론트엔드: 태스크 상세에 파일 업로드 드롭존

**수용 기준**:
- 10MB 이하 파일 업로드 후 URL 반환
- 허용 타입 외 파일 업로드 시 HTTP 400

---

### US-011: 다국어 지원 (i18n)

**목적**: 영어권 사용자 접근성

**구현 내용**:
1. `npm install react-i18next`
2. `frontend/src/locales/ko.json`, `en.json`
3. 언어 전환 토글 (헤더)

**수용 기준**:
- 영어 선택 시 UI 텍스트가 영어로 전환
- 선택한 언어가 localStorage에 유지됨

---

## 구현 순서 권장

```
Phase 13 (P1) → Phase 14 (P2) → Phase 15 (P3, 선택)

Phase 13 내 순서:
US-002 (이메일 인프라) → US-003 (이메일 인증) → US-004 (비밀번호 재설정)
US-001 (Rate Limiting) — 독립적, 병렬 가능
US-005 (계정 탈퇴) — 독립적, 병렬 가능
US-006 (Sentry) — 독립적, 병렬 가능
```

---

## 리스크·완화

| 리스크 | 완화 |
|--------|------|
| Gmail SMTP 발송 한도 (500건/일) | 초기 단계면 충분, 이후 SendGrid/Mailgun으로 교체 |
| Google OAuth 심사 기간 | 개발 중 테스트 계정 10개로 운용 가능 |
| Sentry 무료 플랜 한도 (5K 이벤트/월) | 초기 트래픽엔 충분 |
| S3 비용 | Cloudflare R2 무료 티어(10GB) 사용 |

---

## 검증 단계

| Phase | 검증 방법 |
|-------|----------|
| US-001 | `curl -X POST /auth/login` 11회 반복 → 429 확인 |
| US-002~004 | Mailtrap(테스트 SMTP)으로 이메일 수신 확인 |
| US-005 | 탈퇴 후 재로그인 시도 → 401 확인 |
| US-006 | 의도적 예외 발생 후 Sentry 대시보드 확인 |
| US-007 | Google 계정으로 실제 로그인 플로우 테스트 |
| US-008 | `/privacy`, `/terms` 접속, 가입 폼 약관 미동의 제출 시도 |
| US-009 | 태스크 배정 후 WebSocket 실시간 알림 수신 확인 |
