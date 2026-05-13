# curl 예제

로컬 개발 환경(`http://localhost:8080`) 기준.

## 1. 회원가입

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "name": "홍길동"
  }'
```

**응답 예시:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "user@example.com",
  "name": "홍길동"
}
```

## 2. 로그인

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

토큰을 환경 변수에 저장하면 이후 명령어가 간편:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

echo $TOKEN
```

## 3. 태스크 목록 조회

```bash
curl -X GET http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN"
```

## 4. 태스크 생성

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "API 명세 작성",
    "description": "REST API Markdown 문서 작성",
    "dueDate": "2026-06-30"
  }'
```

## 5. 태스크 상태 변경

```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "API 명세 작성",
    "status": "IN_PROGRESS"
  }'
```

## 6. 태스크 삭제

```bash
curl -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN"
# 성공 시 HTTP 204 (응답 본문 없음)
```

## 7. 헬스체크

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

## 공통 옵션

| 플래그 | 설명 |
|--------|------|
| `-s` | 진행률 숨김 (스크립트에 유용) |
| `-v` | 헤더 포함 상세 출력 |
| `-w "\n%{http_code}"` | 응답 후 HTTP 코드 출력 |
| `-o /dev/null` | 응답 본문 버림 (코드만 확인 시) |
