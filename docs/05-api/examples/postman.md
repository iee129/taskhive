# Postman 사용 가이드

## 환경 설정

### Environment 생성

1. Postman 우측 상단 **Environments** → **+** 클릭
2. 이름: `TaskHive Local`
3. 변수 추가:

| Variable | Initial Value | Current Value |
|----------|--------------|---------------|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |
| `token` | _(비워둠)_ | _(로그인 후 자동 입력)_ |

### 자동 토큰 저장 (Pre-request / Tests 스크립트)

로그인 요청의 **Tests** 탭에 아래 스크립트 추가:

```javascript
const res = pm.response.json();
if (res.token) {
    pm.environment.set("token", res.token);
    console.log("Token saved:", res.token.substring(0, 20) + "...");
}
```

이후 모든 요청의 **Authorization** 탭:
- Type: `Bearer Token`
- Token: `{{token}}`

---

## 요청 컬렉션

### Auth

#### Register
- **Method**: POST
- **URL**: `{{baseUrl}}/api/auth/register`
- **Body** (raw JSON):
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

#### Login
- **Method**: POST
- **URL**: `{{baseUrl}}/api/auth/login`
- **Body** (raw JSON):
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- **Tests** 탭: 위의 자동 토큰 저장 스크립트 추가

---

### Tasks

#### Get Task List
- **Method**: GET
- **URL**: `{{baseUrl}}/api/tasks`
- **Auth**: Bearer Token `{{token}}`

#### Create Task
- **Method**: POST
- **URL**: `{{baseUrl}}/api/tasks`
- **Auth**: Bearer Token `{{token}}`
- **Body** (raw JSON):
```json
{
  "title": "새 태스크",
  "description": "설명",
  "dueDate": "2026-06-30"
}
```

#### Update Task
- **Method**: PUT
- **URL**: `{{baseUrl}}/api/tasks/{{taskId}}`
- **Auth**: Bearer Token `{{token}}`
- **Body** (raw JSON):
```json
{
  "title": "수정된 제목",
  "status": "IN_PROGRESS"
}
```

#### Delete Task
- **Method**: DELETE
- **URL**: `{{baseUrl}}/api/tasks/{{taskId}}`
- **Auth**: Bearer Token `{{token}}`

---

## 컬렉션 Export / Import

Postman Collection v2.1 형식으로 내보낸 후 `docs/05-api/examples/TaskHive.postman_collection.json`으로 저장 예정 (Phase 3 이후).

팀원 공유 방법:
1. 컬렉션 우클릭 → **Export** → Collection v2.1
2. 저장된 JSON 파일을 git에 커밋
3. 팀원은 Postman에서 **Import** → 파일 선택
