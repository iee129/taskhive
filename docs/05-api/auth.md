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
| `name` | string | 필수 | 100자 이하 |

### 응답 (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0...",
  "email": "user@example.com",
  "name": "홍길동"
}
```

### 에러 응답

| 상황 | 코드 | 메시지 |
|------|------|--------|
| 이메일 형식 오류 | 400 | `email: 올바른 이메일 형식이 아닙니다` |
| 비밀번호 8자 미만 | 400 | `password: 비밀번호는 8자 이상이어야 합니다` |
| 이메일 중복 | 409 | `이미 사용 중인 이메일입니다` |

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
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0...",
  "email": "user@example.com",
  "name": "홍길동"
}
```

### 에러 응답

| 상황 | 코드 | 메시지 |
|------|------|--------|
| 이메일 또는 비밀번호 불일치 | 401 | `이메일 또는 비밀번호가 올바르지 않습니다` |

---

## 토큰 사용 방법

발급된 `token`을 이후 모든 보호된 요청의 `Authorization` 헤더에 포함:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

토큰 유효기간: **24시간**. 만료 후 재로그인 필요.
