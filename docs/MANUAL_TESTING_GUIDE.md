# TaskHive 수동 테스트 가이드

배포된 TaskHive에서 **모든 마일스톤 기능이 정상 동작하는지** 직접 클릭하며 검증할 수 있는 시나리오 모음입니다.

각 시나리오는 다음 형식입니다:

- **목적**: 어떤 마일스톤·기능을 검증하는가
- **사전 조건**: 시작 전 필요한 상태
- **단계**: 클릭/입력/관찰 순서
- **기대 결과**: 화면·응답에서 확인해야 할 것
- **실패 시 점검**: 실패할 때 의심해야 할 부분

> 💡 모든 시나리오는 demo 계정 `test@example.com` / `Test1234!`로 진행합니다.
> 새로 회원가입한 계정으로 시작하고 싶다면 §1을 참고하세요.

---

## 0. 환경 준비

| 위치 | URL |
|------|-----|
| 프론트엔드 | `https://<vercel-url>.vercel.app` |
| 백엔드 헬스체크 | `https://<render-url>.onrender.com/actuator/health` |
| Swagger UI | `https://<render-url>.onrender.com/swagger-ui.html` |

### 0-1. 사전 헬스체크 (1분)

```bash
curl -s https://<render-url>/actuator/health | grep -q '"UP"' && echo "✅ 백엔드 정상"
curl -s -o /dev/null -w "%{http_code}\n" https://<vercel-url>/ | grep -q "200" && echo "✅ 프론트엔드 정상"
```

> ⚠️ Render 무료 티어는 15분 idle 후 슬립합니다. 첫 응답이 30–60초 지연될 수 있고 화면 상단에 "서버를 깨우는 중..." 안내가 표시됩니다. 그동안 기다리면 됩니다.

---

## 시나리오 1. 데모 계정 로그인 (M1 인증)

**목적**: JWT 발급 + HttpOnly Refresh Token 쿠키 + 자동 시드 데이터 확인

**단계**
1. Vercel URL 접속 → 로그인 페이지 표시 확인
2. Email: `test@example.com`, Password: `Test1234!` 입력 → **로그인** 클릭
3. DevTools (F12) → **Application** → **Cookies** 확인

**기대 결과**
- `/projects` 또는 `/tasks` 경로로 리다이렉트
- 사이드바에 7개 메뉴 (프로젝트·태스크·칸반 보드·통계·프로필·설정·로그아웃) 표시
- 쿠키에 `refreshToken` (HttpOnly, Secure, SameSite=Lax) 존재
- localStorage에 `token` (access token) 저장
- DevTools Network → 첫 요청에 `Authorization: Bearer ...` 헤더 포함

**실패 시**
- "이메일 인증이 필요합니다" → Render Environment의 `SPRING_PROFILES_ACTIVE=demo` 확인
- CORS 오류 → Render `CORS_ORIGINS`에 Vercel URL 포함 여부 확인
- 401 응답 → 백엔드 로그에서 JWT 시크릿 길이 확인 (256-bit 이상)

---

## 시나리오 2. 프로젝트 생성·멤버 초대 (M1 프로젝트)

**목적**: OWNER/MEMBER 역할, LAST_OWNER 방어

**단계**
1. 사이드바 **프로젝트** → 시드 프로젝트 1–2개 표시 확인
2. **+ 새 프로젝트** → 이름 `테스트 프로젝트` → 생성
3. 프로젝트 상세 → **멤버 초대** → 이메일 자동완성 동작 확인 (다른 시드 계정이 있으면 표시)
4. 자기 자신을 OWNER에서 제거 시도

**기대 결과**
- 새 프로젝트 즉시 표시, 본인이 OWNER로 자동 등록
- 멤버 초대 시 이메일 입력 중 자동완성 드롭다운
- 자기 자신을 마지막 OWNER 상태에서 제거 시 → `400 LAST_OWNER` 에러 + 안내

---

## 시나리오 3. 태스크 CRUD + 라벨 (M1 태스크 + M8 라벨)

**목적**: 태스크 CRUD, 라벨 생성·부착·필터링

**단계**
1. **태스크** 메뉴 → 시드 태스크 목록 표시 확인
2. **+ 태스크 추가** → 제목 `라벨 테스트`, 설명 `**굵게** *기울임* \`코드\``, 프로젝트 선택, 우선순위=HIGH, 마감일=내일 → 저장
3. 생성한 태스크 클릭 → 상세 드로어 → **설명**이 마크다운 렌더링 확인 (굵게/기울임/코드)
4. 같은 드로어 → **라벨 추가** → 새 라벨 `버그`, 색상 `#ef4444` 생성
5. 다시 라벨 선택 다중 박스에서 `버그` 선택 → 태스크 카드/행에 빨간 칩 표시 확인
6. 태스크 목록 상단 **라벨 필터** → `버그` 선택 → 해당 라벨 부착 태스크만 표시

**기대 결과**
- 마크다운: `**굵게**` → **굵게**, `*기울임*` → *기울임*, 인라인 코드 회색 배경
- 라벨 부착 즉시 카드에 색상 칩 렌더링
- `GET /api/tasks?labelId=X` 응답이 필터링된 결과 반환 (Network 탭 확인)

**실패 시**
- 마크다운이 raw 텍스트로 표시 → frontend 빌드 시 `react-markdown` 누락
- 라벨 필터가 동작 안 함 → 백엔드 `TaskRepository.findFiltered` JPQL 확인

---

## 시나리오 4. XSS 방어 (M8 마크다운)

**목적**: DOMPurify XSS 차단 검증

**단계**
1. 태스크 생성 시 설명에 다음 페이로드 입력:
   ```
   <script>alert('XSS')</script>
   <img src=x onerror="alert('XSS')">
   <a href="javascript:alert('XSS')">클릭</a>
   안전한 [링크](https://example.com)
   ```
2. 저장 후 태스크 상세 드로어 열기

**기대 결과**
- `alert()` 다이얼로그가 절대 뜨지 않음
- `<script>` 태그 제거됨
- `<img onerror>` 속성 제거됨
- `javascript:` href는 빈 링크로 변환됨
- "안전한 링크"만 정상 렌더링

---

## 시나리오 5. 칸반 드래그 (M1 칸반)

**목적**: 태스크 상태 드래그&드롭 변경 → 활동 이력 기록

**단계**
1. **칸반 보드** 메뉴 → TODO/IN_PROGRESS/DONE 3열 표시 확인
2. TODO의 카드 하나를 IN_PROGRESS 열로 드래그
3. 우상단 알림 또는 사이드바 활동 피드 확인

**기대 결과**
- 카드가 새 열로 즉시 이동, 새로고침 후에도 유지
- `task_activities` 테이블에 `STATUS_CHANGED` 액션 기록 (Swagger `/api/stats/activity` 또는 통계 페이지에서 확인)
- WebSocket을 통해 실시간 알림이 다른 탭에 전파됨 (브라우저 두 탭으로 동시 접속해서 검증)

---

## 시나리오 6. 분석 대시보드 차트 (M6)

**목적**: 번다운·CFD·사이클타임 차트 렌더링

**단계**
1. 시드 데이터로 충분한 태스크가 있는 프로젝트를 선택했는지 확인
2. **통계** 메뉴 → 페이지 하단으로 스크롤
3. 다음 3개 차트가 렌더링되는지 확인:
   - **번다운 차트** (LineChart, X=날짜, Y=잔여 태스크 수)
   - **CFD 차트** (AreaChart, 색상 누적 영역 — TODO/IN_PROGRESS/DONE)
   - **사이클 타임** (BarChart, X=태스크 ID, Y=소요일)
4. Network 탭에서 3개 호출 확인:
   - `GET /api/projects/{id}/analytics/burndown?from=...&to=...`
   - `GET /api/projects/{id}/analytics/cfd?from=...&to=...`
   - `GET /api/projects/{id}/analytics/cycle-time`

**기대 결과**
- 모든 차트가 데이터를 받아 시각화
- 빈 프로젝트라면 "데이터 없음" 메시지

---

## 시나리오 7. Cmd-K 커맨드 팔레트 + 단축키 (M5)

**목적**: 키보드 단축키, 퍼지 검색

**단계**
1. 임의 페이지에서 `Cmd+K` (macOS) 또는 `Ctrl+K` (Windows) → 팔레트 열림
2. 검색창에 `버그` 입력 → 시드 태스크 중 매칭 결과 표시
3. 결과 클릭 → 해당 태스크 상세 또는 페이지로 이동
4. `Esc` → 팔레트 닫힘
5. 페이지 본문 포커스 상태에서 `/` 키 → 검색 포커스
6. `?` 키 → 단축키 안내 시트 표시

**기대 결과**
- 검색 결과 실시간 표시 (`GET /api/search?q=...` 호출)
- ILIKE 매칭으로 대소문자 무관 매칭

---

## 시나리오 8. 다크모드 토글 (M8)

**목적**: Ant Design 다크 테마 + localStorage 영속

**단계**
1. 사이드바 하단 ☀️/🌙 아이콘 옆 **스위치** 클릭
2. 화면 전체가 다크 테마로 전환되는지 확인 (배경/카드/입력 필드)
3. **브라우저 새로고침** (F5)
4. localStorage 확인: `taskhive_dark_mode` = `true`

**기대 결과**
- 토글 즉시 모든 페이지·컴포넌트가 다크 적용
- 새로고침 후에도 다크 유지
- 다시 토글 → 라이트 복귀 + localStorage `false`로 저장

---

## 시나리오 9. PAT 생성·사용·폐기 (M3-1)

**목적**: 개인 API 토큰 SHA-256 해시 인증

**단계**
1. **설정** 메뉴 → **개인 API 토큰** 섹션
2. **새 토큰 생성** → 이름 `CLI test` → 생성 클릭
3. 표시되는 **평문 토큰 1회 응답** 복사 (이후 다시 못 봄)
4. 터미널에서 PAT로 API 호출:
   ```bash
   curl -H "Authorization: Bearer <복사한 PAT>" \
        https://<render-url>/api/tasks | head -5
   ```
5. 응답이 200 + JSON 배열이면 성공
6. 다시 **설정** → 해당 토큰의 **폐기** 클릭
7. 동일 curl 재실행 → `401` 응답

**기대 결과**
- 평문 토큰은 응답 직후 1회만 표시
- DB에는 SHA-256 해시만 저장 (Neon SQL Editor로 `SELECT token_hash FROM personal_access_tokens` 확인 가능)
- 폐기 후 즉시 인증 차단

---

## 시나리오 10. 웹훅 SSRF 가드 + 서명 검증 (M3-3)

**목적**: SSRF private IP 차단, HMAC-SHA256 서명, 5회 실패 자동 비활성

**단계**
1. 프로젝트 설정 → **웹훅** 섹션
2. URL = `http://localhost:1234` 입력 → 등록 시도
3. URL = `http://127.0.0.1/abc` 입력 → 등록 시도
4. URL = `https://webhook.site/<your-uuid>` (https://webhook.site 에서 무료 발급) → 등록
5. 시크릿 = `mysecret` 입력, 이벤트 = `task.created` 체크
6. 새 태스크 생성 후 webhook.site 페이지 확인

**기대 결과**
- localhost/127.0.0.1 URL → `400 SSRF_BLOCKED` 에러
- webhook.site 페이지에 POST 수신 표시
  - Body: `{"event":"task.created","timestamp":"...","payload":{...}}`
  - Header: `X-TaskHive-Signature: sha256=...`
- 시크릿이 `mysecret`이면, 수신측에서 `HmacSHA256(mysecret, body)`가 헤더 값과 일치

**5회 실패 자동 비활성 검증 (선택)**
- 잘못된 URL(`https://webhook.site/invalid-uuid`)로 등록 후 5번 태스크 생성 → 웹훅 자동 비활성화 (`enabled=false`), 백엔드 로그에서 "5회 연속 실패 → 비활성화" 메시지 확인

---

## 시나리오 11. AI 코파일럿 (M2, 선택)

**목적**: AI 기능 활성/비활성 분기 확인

기본 데모 환경은 `AI_PROVIDER=none`이므로 AI 기능이 숨겨져 있습니다. 동작을 보고 싶다면:

1. Render Environment → `AI_PROVIDER=groq` + `GROQ_API_KEY=gsk_xxx` 추가 → 재배포
2. Vercel URL 재접속 → 상단에 **클라우드 LLM 사용 경고 배너** 표시
3. 태스크 상세 → **AI 요약 생성** 버튼 클릭 → 활동 이력 기반 코멘트 자동 추가
4. 태스크 목록 상단 자연어 필터 → `이번 주 마감인 높은 우선순위` 입력 → 필터 자동 적용
5. **브레인덤프** 모달 → 큰 텍스트 입력 → 분해 결과 체크박스 → 선택 항목 일괄 생성

**기대 결과**
- `AI_PROVIDER=none`이면 AI 버튼·필터·배너 모두 숨김
- `groq` 활성 시 7개 AI 기능 (요약·필터·브레인덤프·스탠드업·우선순위·블로커·공수추정) 동작

---

## 시나리오 12. Swagger UI로 API 직접 테스트

**목적**: 모든 엔드포인트를 브라우저에서 직접 호출

**단계**
1. `https://<render-url>/swagger-ui.html` 접속
2. `POST /api/auth/login` → **Try it out** → Body에 demo 자격증명 입력 → Execute
3. 응답의 `accessToken` 복사
4. 우상단 **Authorize** → `bearerAuth` 필드에 토큰 붙여넣기 → Authorize
5. 이후 임의 엔드포인트(예: `GET /api/projects`)에 **Try it out** → Execute → 200 응답

**기대 결과**
- Swagger 그룹 약 12개 (Auth/Projects/Tasks/Comments/Stats/Analytics/Labels/Search/PAT/Webhooks/AI/Users)
- 인증 후 모든 엔드포인트에 `Authorization` 헤더 자동 첨부

---

## 시나리오 13. 관찰 가능성 (M9 폴리시)

**목적**: X-Request-Id 전파, JaCoCo 커버리지, N+1 차단

### 13-1. X-Request-Id 응답 헤더

1. DevTools Network → 임의 API 호출 클릭
2. **Response Headers** 섹션에 `X-Request-Id: <UUID 8자리>` 존재 확인
3. 동일 ID가 백엔드 로그에 표시되어 분산 디버깅 가능

### 13-2. N+1 차단 확인

1. `GET /api/tasks` 호출 시 Render 로그에서 Hibernate 쿼리 수가 태스크 수에 비례하지 않는지 확인
2. `@EntityGraph(attributePaths = {"project", "assignee", "labels"})` 효과로 단일 쿼리에 LEFT JOIN 포함

### 13-3. JaCoCo 리포트 (로컬에서만)

배포 환경에는 노출 안 되지만, 로컬에서 다음으로 생성 가능:
```bash
cd apps/server && ./gradlew check
open build/reports/jacoco/test/html/index.html
```

---

## 전체 체크리스트 (요약)

| # | 검증 항목 | 마일스톤 | 상태 |
|---|-----------|----------|------|
| 1 | `/actuator/health` 200 응답 | M1 | □ |
| 2 | demo 계정 로그인 성공 | M1 | □ |
| 3 | 시드 프로젝트·태스크 표시 | M1 | □ |
| 4 | 칸반 드래그 상태 변경 + 활동 기록 | M1 | □ |
| 5 | 라벨 생성·부착·필터링 | M8-labels | □ |
| 6 | 마크다운 렌더링 + XSS 차단 | M8-markdown | □ |
| 7 | 다크모드 토글 + 새로고침 영속 | M8-darkmode | □ |
| 8 | 분석 대시보드 3차트 렌더링 | M6 | □ |
| 9 | Cmd+K 팔레트 + 퍼지 검색 | M5 | □ |
| 10 | PAT 생성·인증·폐기 | M3-1 | □ |
| 11 | 웹훅 등록 + SSRF 차단 + HMAC 서명 | M3-3 | □ |
| 12 | AI 기능 (AI_PROVIDER=groq 시) | M2 | □ |
| 13 | X-Request-Id 헤더 응답 | M9 | □ |
| 14 | Swagger UI 인증 + 호출 | 공통 | □ |
| 15 | 새 회원가입 + 이메일 인증 흐름 | M1 | □ |

모두 통과하면 TaskHive의 모든 마일스톤(M1~M10) 동작이 검증된 것입니다.

---

## 회귀 테스트 자동화

위 시나리오는 수동 검증용이며, CI에서는 다음이 자동 실행됩니다:

- **백엔드 단위/통합 테스트**: `./gradlew check` (63개, `.github/workflows/ci.yml`)
- **프론트엔드 타입 검사**: `npx tsc --noEmit`
- **E2E 시나리오**: Playwright 4개 spec (`.github/workflows/e2e.yml`)

자세한 사항은 [`TESTING.md`](TESTING.md)를 참조하세요.
