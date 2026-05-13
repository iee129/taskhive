# TaskHive API 레퍼런스

베이스 URL: `http://localhost:8080` (로컬) / `https://<backend>` (셀프호스팅 또는 운영)

## 인증 방식

| 방식 | 설명 |
|------|------|
| Bearer Token | `Authorization: Bearer <accessToken>` 헤더 |
| Refresh Token | HttpOnly Cookie `refreshToken` (자동 전송) |

액세스 토큰 유효 기간: **15분**  
리프레시 토큰 유효 기간: **7일**

---

## Auth API

### 회원가입
```
POST /api/auth/register
```
**Body**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```
**응답** `200 OK` — 이메일 인증 안내 메시지 반환 (토큰 미발급)

> 회원가입 후 이메일로 인증 링크가 발송됩니다. 인증 완료 전 로그인 불가.

---

### 이메일 인증
```
GET /api/auth/verify-email?token={token}
```
| 상태 | 설명 |
|------|------|
| `200 OK` | 인증 성공 |
| `400 Bad Request` | 토큰 만료 또는 유효하지 않은 토큰 |

---

### 로그인
```
POST /api/auth/login
```
> Rate Limit: 분당 10회 (초과 시 HTTP 429)

**Body**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
**응답** `200 OK`
```json
{
  "accessToken": "eyJhbGci...",
  "name": "홍길동",
  "email": "user@example.com"
}
```
리프레시 토큰은 `Set-Cookie: refreshToken=...` (HttpOnly) 헤더로 발급됩니다.

| 에러 코드 | HTTP | 설명 |
|-----------|------|------|
| `EMAIL_NOT_VERIFIED` | 403 | 이메일 미인증 계정 |
| `INVALID_TOKEN` | 401 | 자격증명 불일치 |

---

### 토큰 갱신
```
POST /api/auth/refresh
```
Cookie의 `refreshToken`을 사용하여 새 액세스 토큰을 발급합니다.

**응답** `200 OK`
```json
{ "accessToken": "eyJhbGci..." }
```

---

### 로그아웃
```
POST /api/auth/logout
```
서버측 리프레시 토큰을 무효화하고 Cookie를 삭제합니다. `200 OK`

---

### 비밀번호 재설정 요청
```
POST /api/auth/forgot-password
```
**Body**
```json
{ "email": "user@example.com" }
```
미등록 이메일이어도 `200 OK` 반환 (이메일 존재 여부 노출 방지)

---

### 비밀번호 재설정
```
POST /api/auth/reset-password
```
**Body**
```json
{
  "token": "reset-token-from-email",
  "newPassword": "newPassword123"
}
```
| 에러 코드 | HTTP | 설명 |
|-----------|------|------|
| `TOKEN_EXPIRED` | 400 | 토큰 만료 (1시간) |
| `TOKEN_ALREADY_USED` | 400 | 이미 사용된 토큰 |

---

## 사용자 API

### 이메일로 사용자 검색
```
GET /api/users/search?email={query}&projectId={id}
```
> 인증 필요. 본인 및 이미 해당 프로젝트 멤버인 사용자는 제외됩니다.

**응답** `200 OK`
```json
[
  { "userId": 2, "name": "김철수", "email": "chulsoo@example.com" }
]
```

---

## 프로젝트 API

> 모든 엔드포인트 인증 필요.

### 내 프로젝트 목록
```
GET /api/projects
```
소유 및 멤버로 참여 중인 프로젝트를 모두 반환합니다.

**응답** `200 OK`
```json
[
  {
    "id": 1,
    "name": "TaskHive",
    "description": "팀 작업 관리 플랫폼",
    "ownerId": 1,
    "createdAt": "2025-01-01T00:00:00",
    "members": [
      { "userId": 1, "name": "홍길동", "email": "hong@example.com", "role": "OWNER", "createdAt": "..." },
      { "userId": 2, "name": "김철수", "email": "chulsoo@example.com", "role": "MEMBER", "createdAt": "..." }
    ]
  }
]
```

---

### 프로젝트 상세
```
GET /api/projects/{id}
```
| 에러 코드 | HTTP | 설명 |
|-----------|------|------|
| `NOT_PROJECT_MEMBER` | 403 | 비멤버 접근 |
| `PROJECT_NOT_FOUND` | 404 | 프로젝트 없음 |

---

### 프로젝트 생성
```
POST /api/projects
```
**Body**
```json
{
  "name": "새 프로젝트",
  "description": "설명 (선택)"
}
```
프로젝트 생성자는 자동으로 `OWNER` 역할 멤버로 등록됩니다.

---

### 프로젝트 수정
```
PUT /api/projects/{id}
```
> 멤버 이상 접근 가능.

---

### 프로젝트 삭제
```
DELETE /api/projects/{id}
```
> `OWNER` 역할만 가능. `Member` 시도 시 `403 FORBIDDEN`.

---

## 프로젝트 멤버 API

### 멤버 목록 조회
```
GET /api/projects/{projectId}/members
```
> 프로젝트 멤버만 조회 가능.

---

### 멤버 초대
```
POST /api/projects/{projectId}/members
```
**Body**
```json
{ "email": "invite@example.com" }
```
| 에러 코드 | HTTP | 설명 |
|-----------|------|------|
| `MEMBER_ALREADY_EXISTS` | 409 | 이미 멤버 |
| `USER_NOT_FOUND` | 404 | 미가입 사용자 |

초대된 사용자의 역할은 `MEMBER`로 설정됩니다.

---

### 멤버 제거
```
DELETE /api/projects/{projectId}/members/{userId}
```
| 에러 코드 | HTTP | 설명 |
|-----------|------|------|
| `LAST_OWNER` | 400 | 유일한 Owner 제거 불가 |
| `MEMBER_NOT_FOUND` | 404 | 멤버 없음 |

---

## 태스크 API

### 태스크 목록
```
GET /api/tasks?status={status}&priority={priority}&search={keyword}
```
| 파라미터 | 값 | 설명 |
|----------|-----|------|
| `status` | `TODO` \| `IN_PROGRESS` \| `DONE` | 상태 필터 |
| `priority` | `LOW` \| `MEDIUM` \| `HIGH` | 우선순위 필터 |
| `search` | 문자열 | 제목 부분 검색 |

---

### 태스크 상세
```
GET /api/tasks/{id}
```
> 태스크가 프로젝트에 속한 경우 해당 프로젝트 멤버만 접근 가능. 비멤버 → `403`.

---

### 태스크 생성
```
POST /api/tasks
```
**Body**
```json
{
  "title": "태스크 제목",
  "description": "설명",
  "status": "TODO",
  "priority": "MEDIUM",
  "dueDate": "2025-12-31T00:00:00",
  "projectId": 1,
  "assigneeId": 2
}
```
`projectId`가 있는 경우 해당 프로젝트 멤버만 생성 가능.

---

### 태스크 수정
```
PUT /api/tasks/{id}
```

---

### 태스크 삭제
```
DELETE /api/tasks/{id}
```
Soft Delete (deleted_at 설정).

---

### 태스크 상태 일괄 업데이트
```
PATCH /api/tasks/{id}/status
```
**Body**
```json
{ "status": "DONE" }
```

---

## 댓글 API

### 댓글 목록
```
GET /api/tasks/{taskId}/comments
```
> 태스크가 프로젝트에 속한 경우 해당 프로젝트 멤버만 조회 가능.

---

### 댓글 추가
```
POST /api/tasks/{taskId}/comments
```
**Body**
```json
{ "content": "댓글 내용" }
```

---

### 댓글 삭제
```
DELETE /api/tasks/{taskId}/comments/{commentId}
```
> 작성자 본인만 삭제 가능.

---

## 통계 API

### 통계 요약
```
GET /api/stats
```
**응답** `200 OK`
```json
{
  "totalTasks": 42,
  "completedTasks": 20,
  "completionRate": 47.6,
  "todoCount": 10,
  "inProgressCount": 12,
  "doneCount": 20,
  "highPriorityCount": 5,
  "mediumPriorityCount": 20,
  "lowPriorityCount": 17
}
```

---

### 전체 활동 피드 (최신 50개)
```
GET /api/stats/activities
```

---

### 태스크별 활동 이력
```
GET /api/stats/activities/task/{taskId}
```

---

## Dev API

> `dev` 프로파일에서만 활성화됩니다. 프로덕션 환경에서는 이 엔드포인트가 존재하지 않습니다.

### 테스트 데이터 시드
```
POST /api/dev/seed
```
테스트 계정·프로젝트·태스크를 생성합니다. 이미 시드된 경우에도 안전하게 재호출 가능합니다 (idempotent).

**응답** `200 OK`
```json
{ "result": "seeded" }
```
또는 이미 시드된 경우:
```json
{ "result": "already_seeded" }
```

생성 데이터:
- `test@example.com` / `Test1234!` (Demo Project OWNER)
- `member@example.com` / `Test1234!` (Demo Project MEMBER)
- `Demo Project` 프로젝트 1개
- 태스크 5개 (TODO×2, IN_PROGRESS×2, DONE×1)

---

## AI API

### AI 설정 상태 조회
```
GET /api/ai/capabilities
```
인증 불필요. 현재 활성 AI 프로바이더와 클라우드 여부를 반환합니다.

**응답** `200 OK`
```json
{
  "enabled": true,
  "provider": "ollama",
  "cloudProvider": false
}
```

| 필드 | 설명 |
|------|------|
| `enabled` | AI 기능 활성 여부 (`AI_PROVIDER=none`이면 `false`) |
| `provider` | 활성 프로바이더: `"ollama"` \| `"groq"` \| `"none"` |
| `cloudProvider` | `true`이면 UI에 경고 배너(AiProviderBanner) 표시 |

---

### 태스크 제안 (Ollama 필요)
```
POST /api/ai/suggest-task
```
**Body**
```json
{ "prompt": "사용자 로그인 기능을 구현해야 해" }
```
**응답** `200 OK` — 제목·설명·우선순위가 채워진 `TaskRequest` 반환

---

### AI 태스크 즉시 생성
```
POST /api/ai/create-task
```
AI가 제안한 내용으로 태스크를 바로 저장합니다.

---

## 에러 응답 형식

```json
{
  "code": "ERROR_CODE_NAME",
  "message": "에러 설명",
  "status": 400
}
```

## 에러 코드 목록

| 코드 | HTTP | 설명 |
|------|------|------|
| `TASK_NOT_FOUND` | 404 | 태스크 없음 |
| `PROJECT_NOT_FOUND` | 404 | 프로젝트 없음 |
| `USER_NOT_FOUND` | 404 | 사용자 없음 |
| `MEMBER_NOT_FOUND` | 404 | 프로젝트 멤버 없음 |
| `USER_ALREADY_EXISTS` | 400 | 이미 사용 중인 이메일 |
| `MEMBER_ALREADY_EXISTS` | 409 | 이미 프로젝트 멤버 |
| `FORBIDDEN` | 403 | 접근 권한 없음 |
| `NOT_PROJECT_MEMBER` | 403 | 프로젝트 비멤버 |
| `INVALID_TOKEN` | 401 | 유효하지 않은 토큰 |
| `INVALID_INPUT` | 400 | 입력값 오류 |
| `RATE_LIMIT_EXCEEDED` | 429 | 요청 한도 초과 |
| `EMAIL_NOT_VERIFIED` | 403 | 이메일 미인증 |
| `TOKEN_EXPIRED` | 400 | 토큰 만료 |
| `TOKEN_ALREADY_USED` | 400 | 이미 사용된 토큰 |
| `LAST_OWNER` | 400 | 유일한 Owner 제거 불가 |
