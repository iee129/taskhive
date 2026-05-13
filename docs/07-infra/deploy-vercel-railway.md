# Vercel + Railway 배포 가이드

로컬에 Docker/Kubernetes 없이 무료 플랜으로 실제 URL을 얻을 수 있는 배포 방식이다.  
프론트엔드는 **Vercel**, 백엔드(Spring Boot) + PostgreSQL + Redis는 **Railway**에 올린다.

---

## 아키텍처

```
브라우저
  └─ https://taskhive.vercel.app  (Vercel — React SPA)
       └─ HTTPS/WSS → https://taskhive-backend.up.railway.app  (Railway — Spring Boot)
                          ├─ PostgreSQL 16  (Railway Add-on)
                          └─ Redis 7        (Railway Add-on)
```

---

## 1단계 — Railway 백엔드 배포

### 1-1. 프로젝트 생성

1. [railway.app](https://railway.app) 로그인 후 **New Project** 클릭
2. **Deploy from GitHub repo** 선택 → `taskhive` 저장소 연결
3. **Root Directory** 를 `auth` 로 설정 (Spring Boot 모듈)
4. Railway가 `auth/railway.toml` 을 자동으로 감지해 Dockerfile 빌더를 사용함

### 1-2. PostgreSQL 추가

1. 프로젝트 대시보드 → **Add Service** → **Database** → **PostgreSQL**
2. 추가 완료 후 **Variables** 탭에서 자동 주입 변수 확인:
   - `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`
   - Railway는 `DATABASE_URL` 도 함께 제공함

### 1-3. Redis 추가

1. **Add Service** → **Database** → **Redis**
2. `REDIS_URL` 변수가 자동 주입됨

### 1-4. 환경변수 설정

백엔드 서비스 → **Variables** 탭에서 아래 값을 입력한다  
(`auth/.env.example` 참고):

| 변수 | 값 | 비고 |
|------|----|------|
| `SPRING_PROFILES_ACTIVE` | `prod` | |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://${{PGHOST}}:${{PGPORT}}/${{PGDATABASE}}` | Railway 변수 참조 |
| `SPRING_DATASOURCE_USERNAME` | `${{PGUSER}}` | |
| `SPRING_DATASOURCE_PASSWORD` | `${{PGPASSWORD}}` | |
| `REDIS_HOST` | `${{REDISHOST}}` | Redis 서비스 변수 |
| `REDIS_PORT` | `${{REDISPORT}}` | |
| `JWT_SECRET` | 256비트 이상 무작위 문자열 | `openssl rand -hex 32` |
| `CORS_ORIGINS` | `https://taskhive.vercel.app` | Vercel 배포 URL |

> Railway 변수 참조 문법: `${{SERVICE_NAME.VARIABLE}}`  
> PostgreSQL 서비스가 `Postgres`라면 `${{Postgres.PGHOST}}` 형식 사용

### 1-5. 배포 확인

```bash
curl https://<backend>.up.railway.app/actuator/health
# {"status":"UP"} 응답 확인
```

---

## 2단계 — Vercel 프론트엔드 배포

### 2-1. 프로젝트 생성

1. [vercel.com](https://vercel.com) 로그인 후 **Add New Project** 클릭
2. `taskhive` 저장소 Import
3. **Root Directory** 를 `frontend` 로 설정
4. **Framework Preset** → `Vite` 자동 감지됨

### 2-2. 환경변수 설정

**Environment Variables** 섹션에서 추가:

| 변수 | 값 |
|------|----|
| `VITE_API_URL` | `https://<your-app>.up.railway.app` |

### 2-3. 빌드 설정 확인

Vercel이 자동으로 아래를 사용함:
- **Build Command**: `npm run build`
- **Output Directory**: `dist`

`frontend/vercel.json` 의 rewrites 규칙이 React Router SPA 라우팅을 처리함:

```json
{
  "rewrites": [
    { "source": "/((?!api/).*)", "destination": "/index.html" }
  ]
}
```

### 2-4. 배포 후 Railway CORS 업데이트

Vercel 배포 URL이 확정되면 Railway 환경변수 `CORS_ORIGINS` 를 실제 URL로 갱신:

```
CORS_ORIGINS=https://taskhive-<hash>.vercel.app
```

---

## 3단계 — 검증 체크리스트

### 헬스체크

```bash
# 백엔드 상태
curl https://<backend>.up.railway.app/actuator/health

# 프론트엔드 접근
curl -I https://<frontend>.vercel.app
```

### 로그인 동작

1. `https://<frontend>.vercel.app/login` 접속
2. 회원가입 또는 기존 계정으로 로그인
3. 브라우저 DevTools → Network 탭에서 `POST /auth/login` 요청이 Railway URL로 전송되는지 확인

### WebSocket 실시간 동기화

1. 두 브라우저 탭에서 동일 프로젝트 칸반 보드 열기
2. 한 탭에서 카드 상태 변경
3. 다른 탭에 즉시 반영되는지 확인 (STOMP over WebSocket)

> Railway는 HTTP Upgrade를 지원하므로 WebSocket이 별도 설정 없이 동작함

### CORS 검증

```bash
curl -I -H "Origin: https://<frontend>.vercel.app" \
  https://<backend>.up.railway.app/actuator/health
# Access-Control-Allow-Origin 헤더 확인
```

---

## 트러블슈팅

| 증상 | 원인 | 해결 |
|------|------|------|
| `502 Bad Gateway` | Spring Boot 시작 시간 초과 | railway.toml `healthcheckTimeout` 값 증가 |
| `CORS error` | CORS_ORIGINS 불일치 | Railway 변수에서 Vercel URL 정확히 입력 |
| 새로고침 시 404 | SPA 라우팅 미처리 | `vercel.json` rewrites 규칙 확인 |
| WebSocket 연결 실패 | `wss://` 프로토콜 | 프론트엔드 코드에서 `wss://` 사용 여부 확인 |
| DB 연결 실패 | 환경변수 참조 오류 | Railway 변수 탭에서 `${{...}}` 참조 문법 확인 |

---

## 관련 파일

| 파일 | 용도 |
|------|------|
| `auth/railway.toml` | Railway 빌드·헬스체크 설정 |
| `auth/.env.example` | Railway 환경변수 목록 |
| `frontend/vercel.json` | Vercel SPA 라우팅 규칙 |
| `frontend/.env.example` | Vercel 환경변수 예시 |
| `frontend/src/api/client.ts` | VITE_API_URL 적용 |
