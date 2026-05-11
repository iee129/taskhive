# 인증 API

## POST /api/auth/register

회원가입 — 새 사용자를 생성하고 JWT를 반환합니다.

### 요청

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `email` | string | 필수 | 이메일 형식, 중복 불가 |
| `password` | string | 필수 | 8자 이상 |
| `name` | string | 필수 | 2~50자 |

### 응답 (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "email": "user@example.com",
  "name": "홍길동"
}
```

### 에러 응답

| 상황 | 코드 | 응답 형식 |
|------|------|----------|
| 이메일 형식 오류 / 이름 길이 위반 / 비밀번호 8자 미만 | 400 | `{"errors": ["field: 메시지"]}` |
| 이메일 중복 | 400 | `{"error": "Email already in use: ..."}` |

---

## POST /api/auth/login

로그인 — 자격 증명을 검증하고 JWT를 반환합니다.

### 요청

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

| 필드 | 타입 | 필수 |
|------|------|------|
| `email` | string | 필수 |
| `password` | string | 필수 |

### 응답 (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "email": "user@example.com",
  "name": "홍길동"
}
```

### 에러 응답

| 상황 | 코드 | 응답 형식 |
|------|------|----------|
| 이메일 또는 비밀번호 불일치 | 401 | `{"error": "이메일 또는 비밀번호가 올바르지 않습니다"}` |

---

## GET /api/auth/me

현재 로그인된 사용자 정보를 반환합니다. JWT 필수.

### 요청

```http
GET /api/auth/me
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

### 응답 (200 OK)

```json
{
  "email": "user@example.com",
  "name": "홍길동"
}
```

> `token` 필드는 포함되지 않습니다.

### 에러 응답

| 상황 | 코드 | 응답 형식 |
|------|------|----------|
| Authorization 헤더 없음 또는 토큰 변조/만료 | 401 | `{"error": "인증이 필요합니다"}` |

---

## 토큰 사용 방법

발급된 `token`을 이후 모든 보호된 요청의 `Authorization` 헤더에 포함:

```http
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

토큰 유효기간: **1시간**. 만료 후 재로그인 필요.
