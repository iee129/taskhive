# API 개요

## Base URL

| 환경 | URL |
|------|-----|
| 로컬 개발 | `http://localhost:8080` |
| Docker Compose | `http://localhost:8080` |
| 프로덕션 (K8s) | `https://taskhive.example.com` |

## 인증

모든 보호된 엔드포인트는 `Authorization` 헤더에 Bearer 토큰 필요:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4...
```

토큰은 로그인(`POST /api/auth/login`) 또는 회원가입(`POST /api/auth/register`) 응답에서 발급됨.

## 공통 응답 형식

### 성공
```json
{
  "id": 1,
  "field": "value",
  "createdAt": "2026-05-12T10:00:00Z"
}
```

### 에러
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "제목은 필수입니다",
  "timestamp": "2026-05-12T10:00:00Z"
}
```

### 검증 에러 (다중)
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": [
    "email: 올바른 이메일 형식이 아닙니다",
    "password: 비밀번호는 8자 이상이어야 합니다"
  ],
  "timestamp": "2026-05-12T10:00:00Z"
}
```

## HTTP 상태 코드 요약

| 코드 | 의미 |
|------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 204 | 성공 (응답 본문 없음) |
| 400 | 입력 오류 |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 충돌 (이메일 중복 등) |
| 500 | 서버 오류 |

## Content-Type

모든 요청과 응답: `application/json`

## 날짜 형식

ISO 8601 UTC: `2026-05-12T10:00:00Z`  
날짜만: `2026-05-12`

## 엔드포인트 목록

| 그룹 | 메서드 | 경로 | 인증 |
|------|--------|------|------|
| Auth | POST | `/api/auth/register` | 불필요 |
| Auth | POST | `/api/auth/login` | 불필요 |
| Tasks | GET | `/api/tasks` | JWT |
| Tasks | POST | `/api/tasks` | JWT |
| Tasks | PUT | `/api/tasks/{id}` | JWT |
| Tasks | DELETE | `/api/tasks/{id}` | JWT |
| Health | GET | `/actuator/health` | 불필요 |
