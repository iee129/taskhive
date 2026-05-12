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

응답 헤더에 Refresh Token 쿠키도 함께 설정됩니다:
```
Set-Cookie: refreshToken=<uuid>; Path=/api/auth; HttpOnly; SameSite=Lax; Max-Age=604800
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

### 응답 (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "email": "user@example.com",
  "name": "홍길동"
}
```

Refresh Token 쿠키도 함께 설정됩니다.

### 에러 응답

| 상황 | 코드 | 응답 형식 |
|------|------|----------|
| 이메일 또는 비밀번호 불일치 | 401 | `{"error": "이메일 또는 비밀번호가 올바르지 않습니다"}` |

---

## POST /api/auth/refresh

Refresh Token으로 새 Access Token을 발급합니다.

### 요청

```http
POST /api/auth/refresh
Cookie: refreshToken=<uuid>
```

### 응답 (200 OK)

```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
  "expiresIn": 900
}
```

새 Refresh Token 쿠키도 함께 갱신됩니다 (Rotation).

### 에러 응답

| 상황 | 코드 | 응답 형식 |
|------|------|----------|
| 쿠키 없음 또는 유효하지 않은 토큰 | 401 | `{"error": "유효하지 않은 Refresh Token입니다"}` |
| 만료된 토큰 | 401 | `{"error": "만료된 Refresh Token입니다"}` |

---

## POST /api/auth/logout

Refresh Token을 무효화합니다.

### 요청

```http
POST /api/auth/logout
Cookie: refreshToken=<uuid>
```

### 응답 (204 No Content)

Refresh Token 쿠키가 삭제됩니다.

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

### 에러 응답

| 상황 | 코드 | 응답 형식 |
|------|------|----------|
| Authorization 헤더 없음 또는 토큰 변조/만료 | 401 | `{"error": "인증이 필요합니다"}` |

---

## PUT /api/auth/password

비밀번호를 변경합니다. JWT 필수.

### 요청

```http
PUT /api/auth/password
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
Content-Type: application/json
```

```json
{
  "currentPassword": "password123",
  "newPassword": "newpassword456"
}
```

### 응답 (200 OK)

응답 본문 없음.

### 에러 응답

| 상황 | 코드 | 응답 형식 |
|------|------|----------|
| 현재 비밀번호 불일치 | 401 | `{"error": "이메일 또는 비밀번호가 올바르지 않습니다"}` |
| 미인증 | 401 | `{"error": "인증이 필요합니다"}` |

---

## 토큰 사용 방법

Access Token (유효기간 **15분**):
```http
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

만료 시 프론트엔드 Axios 인터셉터가 자동으로 `/api/auth/refresh`를 호출하여 재발급합니다.
