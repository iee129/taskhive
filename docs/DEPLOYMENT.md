# TaskHive 공개 데모 배포 가이드

이 문서는 TaskHive를 **Neon Postgres + Render(백엔드) + Vercel(프론트엔드)** 무료 티어 조합으로 약 30분 안에 공개 URL에 배포하는 단계별 가이드입니다.

배포 후에는 `docs/MANUAL_TESTING_GUIDE.md`의 13개 기능별 검증 체크리스트를 따라 정상 동작 여부를 확인할 수 있습니다.

---

## 사전 준비

| 계정 | 가입 링크 | 용도 | 무료 한도 |
|------|-----------|------|-----------|
| Neon | https://neon.tech | Postgres 16 데이터베이스 | 0.5 GB 스토리지, idle 후 자동 슬립 |
| Render | https://render.com | Spring Boot 백엔드 | 750시간/월, 15분 idle 후 슬립 |
| Vercel | https://vercel.com | React 프론트엔드 (Vite) | 무제한 정적 호스팅 |
| GitHub | (이미 보유) | 소스 저장소, Secrets | — |

모두 신용카드 없이 GitHub 로그인만으로 가입 가능합니다.

---

## Phase 1: Neon Postgres 생성 (≈5분)

1. https://neon.tech → **Sign in with GitHub**
2. **Create a project** 클릭
   - Project name: `taskhive`
   - Postgres version: `16`
   - Region: 사용자와 가장 가까운 곳 (예: `Asia Pacific (Singapore)`)
3. 프로젝트 생성 직후 표시되는 **Connection string** 패널에서 다음 두 가지를 모두 기록:
   - `Pooled connection` (포트 5432, SSL 강제) — 백엔드용
   - 개별 필드: `Hostname`, `Port`, `Database`, `Role`, `Password`

연결 문자열은 아래 형식입니다:
```
postgres://<user>:<password>@<host>.neon.tech/neondb?sslmode=require
```

> ⚠️ Spring Boot는 `jdbc:` 프리픽스를 요구하므로 Phase 2에서 다음과 같이 변환해 입력합니다:
> ```
> jdbc:postgresql://<host>.neon.tech:5432/neondb?sslmode=require
> ```

---

## Phase 2: Render 백엔드 배포 (≈10분)

### 2-1. Web Service 생성

1. https://render.com → **Sign in with GitHub**
2. 우상단 **New +** → **Web Service** 클릭
3. **Connect a repository** → `iee129/taskhive` 선택 → **Connect**
4. Render가 `auth/render.yaml`을 자동 감지하면 **Apply Blueprint** 버튼이 표시됨 → 클릭
   - 감지되지 않으면 수동 입력:
     - **Name**: `taskhive-backend`
     - **Runtime**: Docker
     - **Dockerfile Path**: `./auth/Dockerfile`
     - **Docker Context**: `./auth`
     - **Health Check Path**: `/actuator/health`
     - **Region**: Singapore (Neon과 동일 권장)
     - **Instance Type**: Free

### 2-2. 환경변수 입력

Render 콘솔의 **Environment** 탭에서 다음 4개를 입력:

| Key | Value | 비고 |
|-----|-------|------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<neon-host>:5432/neondb?sslmode=require` | Phase 1의 호스트 |
| `SPRING_DATASOURCE_USERNAME` | (Neon Role 값) | |
| `SPRING_DATASOURCE_PASSWORD` | (Neon Password 값) | |
| `CORS_ORIGINS` | `http://localhost` | Phase 3에서 Vercel URL 추가 예정 |

`SPRING_PROFILES_ACTIVE=demo`, `AI_PROVIDER=none`, `JWT_SECRET`(자동 생성), `SERVER_PORT=8080`는 `render.yaml`에 이미 정의되어 있어 자동 적용됩니다.

### 2-3. 첫 배포 + 검증

1. **Manual Deploy** → **Deploy latest commit** 클릭
2. 빌드 시작 (5–10분 소요). 로그에서 다음 마커를 확인:
   ```
   Migrating schema "public" to version "1 - baseline"
   Migrating schema "public" to version "2 - task status history"
   ...
   Migrating schema "public" to version "6 - labels"
   Successfully applied 6 migrations
   ```
3. Deploy 완료 후 표시되는 URL(`https://taskhive-backend-xxxx.onrender.com`)을 기록
4. 동일 URL의 `/actuator/health` 접속 → `{"status":"UP"}` 응답이면 성공
5. 동일 URL의 `/swagger-ui.html` 접속 → Swagger UI 로드 확인

> 💡 demo 프로파일이면 `DemoSeederRunner`가 자동 실행되어 `test@example.com / Test1234!` 계정과 예시 프로젝트·태스크·라벨이 생성됩니다 (`auth/src/main/java/com/taskhive/config/DemoSeederRunner.java`).

---

## Phase 3: Vercel 프론트엔드 배포 (≈5분)

### 3-1. 프로젝트 import

1. https://vercel.com → **Sign in with GitHub**
2. **Add New...** → **Project** → `iee129/taskhive` → **Import**
3. **Configure Project** 화면에서:
   - **Framework Preset**: Vite (자동 감지)
   - **Root Directory**: `frontend`  ← ⚠️ 반드시 수정
   - **Build Command**: `npm run build` (기본값 유지)
   - **Output Directory**: `dist` (기본값 유지)

### 3-2. 환경변수 입력

**Environment Variables** 섹션 펼치고:

| Key | Value |
|-----|-------|
| `VITE_API_URL` | Phase 2에서 받은 Render URL (예: `https://taskhive-backend-xxxx.onrender.com`) |

### 3-3. 배포

**Deploy** 클릭 → 2–3분 후 `https://taskhive-<random>.vercel.app` 형태 URL 발급. 이 URL을 기록합니다.

---

## Phase 4: CORS 및 회귀 검증 (≈3분)

### 4-1. CORS_ORIGINS 업데이트

1. Render 콘솔 → `taskhive-backend` → **Environment** → `CORS_ORIGINS` 편집
2. 값을 다음으로 변경 (쉼표 구분, 공백 없음):
   ```
   http://localhost,https://<vercel-url>.vercel.app
   ```
3. 저장 → Render가 자동으로 재배포 (약 30초)

### 4-2. 동작 확인

- Vercel URL 접속 → 로그인 페이지가 정상 표시되면 CORS OK
- DevTools Network 탭에서 `/api/auth/login` 요청 → `Access-Control-Allow-Origin` 헤더에 Vercel URL이 반환되는지 확인

---

## Phase 5: GitHub Actions 자동 배포 (선택, ≈3분)

master 브랜치에 push할 때 Render가 자동 재배포되도록 deploy hook을 연결합니다.

1. Render 콘솔 → `taskhive-backend` → **Settings** → **Deploy Hook** → URL 복사
2. GitHub 저장소 → **Settings** → **Secrets and variables** → **Actions**
3. **New repository secret** 클릭:
   - **Name**: `RENDER_DEPLOY_HOOK_URL`
   - **Secret**: 위에서 복사한 URL
4. 이후 master 브랜치 push 시 `.github/workflows/deploy.yml`이 자동으로 Render 재배포 트리거

Vercel은 GitHub 통합으로 master push마다 production 재배포, PR push마다 preview 환경을 자동 생성합니다 — 별도 설정 불필요.

---

## 환경변수 매트릭스

| Key | 위치 | 필수 | 예시 |
|-----|------|------|------|
| `SPRING_DATASOURCE_URL` | Render | ✅ | `jdbc:postgresql://...neon.tech:5432/neondb?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Render | ✅ | Neon role |
| `SPRING_DATASOURCE_PASSWORD` | Render | ✅ | Neon password |
| `SPRING_PROFILES_ACTIVE` | Render | ✅ | `demo` (시드 데이터 + 이메일 인증 우회) |
| `JWT_SECRET` | Render | ✅ | `render.yaml`이 자동 생성 |
| `CORS_ORIGINS` | Render | ✅ | `http://localhost,https://*.vercel.app` |
| `AI_PROVIDER` | Render | ✅ | `none` / `ollama` / `groq` |
| `GROQ_API_KEY` | Render | AI=groq 시 | `gsk_xxx` |
| `MAIL_HOST` 등 | Render | 선택 | SMTP 설정 시 |
| `SENTRY_DSN` | Render | 선택 | 에러 모니터링 시 |
| `VITE_API_URL` | Vercel | ✅ | Render URL |
| `RENDER_DEPLOY_HOOK_URL` | GitHub Secrets | 선택 | Phase 5 자동 배포용 |

---

## 트러블슈팅

### 백엔드가 부팅 중 죽음 (`Connection refused`)

원인: Neon idle 슬립 또는 SSL 미설정.
해결: `SPRING_DATASOURCE_URL`에 `?sslmode=require`가 붙어 있는지 확인.

### Flyway 마이그레이션 실패

원인: 기존 DB에 충돌하는 객체 존재.
해결: Neon 콘솔의 **SQL Editor**에서 `DROP SCHEMA public CASCADE; CREATE SCHEMA public;` 실행 후 Render 재배포.

### Vercel 페이지가 로딩되지만 로그인 시 CORS 오류

원인: Phase 4를 건너뛰었거나, `CORS_ORIGINS`에 Vercel URL이 누락됨.
해결: Render `CORS_ORIGINS`에 정확한 Vercel URL(프로토콜 포함) 추가 + 재배포.

### Render 무료 티어에서 첫 응답이 30–60초 지연

이건 정상입니다. 15분 idle 후 슬립이 발동되어 콜드스타트가 발생하며, 프론트엔드의 `WakingUp` 컴포넌트가 로딩 화면과 경과 시간을 표시합니다. README:18에 명시.

### 데모 계정 로그인 실패 (`이메일 인증이 필요합니다`)

원인: `SPRING_PROFILES_ACTIVE`가 `demo`가 아님.
해결: Render Environment에서 `SPRING_PROFILES_ACTIVE=demo` 확인 후 재배포. `prod` 프로파일에서는 시더가 동작하지 않습니다.

---

## 배포 후 검증

배포가 성공했다면 다음 두 URL이 모두 200을 반환해야 합니다:

```bash
curl -s -o /dev/null -w "Backend: %{http_code}\n" https://<render-url>/actuator/health
curl -s -o /dev/null -w "Frontend: %{http_code}\n" https://<vercel-url>/
```

기능 단위 수동 검증은 [`docs/MANUAL_TESTING_GUIDE.md`](MANUAL_TESTING_GUIDE.md)의 시나리오를 따라 수행하세요.

---

## 비용 관리

세 서비스 모두 무료 티어 한도 안에서는 결제 정보 없이 사용 가능합니다. 다음 임계값을 넘으면 서비스가 자동 정지(다음 달 리셋)됩니다:

- Neon: 0.5 GB 스토리지, 191시간 컴퓨트/월
- Render: 750시간/월 (단일 무료 서비스는 항상 가능)
- Vercel: 100 GB 대역폭/월 (개인 프로젝트는 거의 도달 불가)

신용카드 등록 없이 Hard limit이 동작하므로 예상치 못한 청구 위험이 없습니다.
