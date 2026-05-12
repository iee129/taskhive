# Deep Interview Spec: TaskHive AI 코파일러 차별화

## Metadata
- Interview ID: di-taskhive-next-2026
- Rounds: 7
- Final Ambiguity Score: 17%
- Type: brownfield
- Generated: 2026-05-12
- Threshold: 0.20
- Initial Context Summarized: no
- Status: PASSED

## Clarity Breakdown
| 차원 | 점수 | 가중치 | 가중합 |
|------|------|--------|--------|
| Goal Clarity | 0.90 | 35% | 0.315 |
| Constraint Clarity | 0.80 | 25% | 0.200 |
| Success Criteria | 0.82 | 25% | 0.205 |
| Context Clarity | 0.75 | 15% | 0.113 |
| **Total Clarity** | | | **0.833** |
| **Ambiguity** | | | **17%** |

---

## Goal

TaskHive를 **"Ollama 기반 AI 코파일러가 내장된 팀 태스크 관리 도구"** 로 포지셔닝하여 Jira·Notion·Linear와 차별화한다.

핵심 포지셔닝: *로컬 LLM으로 프라이버시를 보호하면서 자연어로 태스크를 생성·요약·검색하는 스마트 태스크 관리*

구현은 3개 Phase로 나뉜다:
- **Phase 1 (1순위):** 자연어 태스크 생성 강화
- **Phase 2:** AI 태스크 요약 코멘트
- **Phase 3:** 지능형 자연어 필터링

---

## Constraints

- **AI 백엔드:** Ollama (로컬 LLM) 전용 — 외부 API(OpenAI, Claude) 사용 안 함
- **기술 스택:** 기존 Spring Boot 3 + React 18 + Ant Design 유지
- **배포:** Railway(백엔드) + Vercel(프론트엔드) 유지
- **솔로 개발:** 기능 범위를 Phase별로 분리해 과부하 방지
- **기존 코드 재활용:** `AiController`, `AiService`, `/api/ai/suggest-task` 엔드포인트 확장

---

## Non-Goals

- OpenAI / Claude / Gemini 등 외부 API 연동
- 실시간 협업(커서 위치, 라이브 업데이트) — Phase 1~3 범위 외
- 데이터 시각화(번다운 차트, 스프린트 비로드) — 추후 고려
- 모바일 앱 / PWA

---

## Acceptance Criteria

### Phase 1: 자연어 태스크 생성 강화

- [ ] 태스크 생성 모달에 "AI로 작성" 토글/탭이 존재한다
- [ ] 사용자가 자연어 설명(예: "로그인 API 구현, 이번 주 금요일까지, 중요")을 입력하면 AI가 제목·설명·우선순위·마감일을 추출해 폼에 자동 채운다
- [ ] AI 제안 결과를 사용자가 수동으로 편집한 후 저장할 수 있다
- [ ] AI 생성 버튼 클릭 시 로딩 스피너가 표시되고, 오류 시 fallback 메시지가 표시된다
- [ ] 인터넷 연결 없이(Ollama 로컬 실행 환경에서) 정상 동작한다
- [ ] 기존 `/api/ai/suggest-task` + `/api/ai/create-task` 엔드포인트를 확장하거나 대체한다
- [ ] TypeScript 빌드(`npx tsc --noEmit`) 오류 없음
- [ ] 백엔드 Maven 빌드(`./mvnw package -DskipTests`) 성공

### Phase 2: AI 태스크 요약 코멘트

- [ ] 태스크 상세 페이지에 "AI 요약 생성" 버튼이 존재한다
- [ ] 클릭 시 태스크 제목·설명·기존 코멘트 내용을 Ollama에 전달해 요약을 생성한다
- [ ] 생성된 요약이 새 코멘트로 자동 삽입된다 (코멘트 작성자: AI Bot 또는 현재 사용자)
- [ ] `POST /api/tasks/{taskId}/ai-summary` 엔드포인트가 추가된다

### Phase 3: 지능형 자연어 필터링

- [ ] 태스크 목록 상단에 자연어 검색바가 존재한다 (예: "다음 주까지 HIGH 우선순위 태스크")
- [ ] AI가 자연어를 파싱해 status·priority·dueDate·assignee 필터로 변환한다
- [ ] 변환된 필터가 기존 `GET /api/tasks?status=&priority=` 쿼리로 적용된다
- [ ] 자연어 입력 없이 기존 개별 필터도 정상 동작한다 (하위 호환)

---

## Assumptions Exposed & Resolved

| 가정 | 도전 | 결정 |
|------|------|------|
| 3가지 방향 모두 필요 | 하나만 고른다면? | AI 코파일러 단일 집중 |
| 고품질 AI = 외부 API 필요 | Ollama로도 충분한가? | Ollama 유지 — 프라이버시 + 비용 0 |
| 3개 AI 기능 동시 구현 | 가장 임팩트 있는 1개는? | 자연어 태스크 생성 → Phase 1 우선 |

---

## Technical Context

**기존 구현 재활용 경로:**

```
auth/src/main/java/com/taskhive/
├── controller/AiController.java     # POST /api/ai/suggest-task, /api/ai/create-task
├── service/AiService.java           # Ollama REST 호출 로직
└── dto/AiTaskRequest.java           # { prompt: String }

frontend/src/
├── api/tasks.ts                     # createTask 등 기존 API 클라이언트
└── pages/TasksPage.tsx              # 태스크 생성 모달 위치
```

**Phase 1 구현 방향:**
- `AiTaskRequest`에 `dueDate` 추출 필드 추가 (또는 별도 `AiEnhancedResponse` DTO)
- `AiService.generateTask()`의 Ollama 프롬프트 개선 (마감일·담당자 파싱 포함)
- 프론트엔드: 태스크 생성 모달에 "AI 자동 작성" 탭 추가

---

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| TaskHive | core domain | name, phases, stack | contains all others |
| User | core domain | email, name, role | creates Tasks, owns Projects |
| AICopilot | core feature | backend=Ollama, phases | belongs to TaskHive |
| Ollama | external system | url, model | used by AICopilot |
| NaturalLanguageTaskCreation | feature (Phase 1) | prompt, title, description, priority, dueDate | extends AiController |
| TaskSummaryComment | feature (Phase 2) | taskId, summary, commentId | extends CommentService |
| IntelligentFiltering | feature (Phase 3) | query, filters | extends TaskController |
| Goals | concept | portfolio, real service, learning | drives priority |
| Competitor | external concept | Jira, Notion, Linear | motivates differentiation |

## Ontology Convergence

| 라운드 | 엔티티 수 | 신규 | 변경 | 안정 | Stability |
|--------|----------|------|------|------|-----------|
| 1 | 4 | 4 | - | - | N/A |
| 2 | 5 | 1 | 1 | 3 | 80% |
| 3 | 8 | 3 | 1 | 4 | 63% |
| 4 | 5 | 0 | 1 | 4 | **100%** |
| 5 | 6 | 1 | 0 | 5 | 83% |
| 6 | 9 | 3 | 1 | 5 | 78% |
| 7 | 9 | 0 | 0 | 9 | **100%** |

---

## Interview Transcript

<details>
<summary>Full Q&A (7 rounds)</summary>

### Round 1
**Q:** TaskHive는 어떤 목적으로 개발되고 있나요?
**A:** 모두 포함하고 싶어 (포트폴리오 + 실제 서비스 + 학습 + 오픈소스)
**Ambiguity:** 87% (Goal: 0.20, Constraints: 0.00, Criteria: 0.00, Context: 0.40)

### Round 2
**Q:** 지금 가장 원하는 것은? (포트폴리오 완성도 vs 기능 차별화 vs 실사용자 확보 vs 배포 완성도)
**A:** 기능 차별화
**Ambiguity:** 70% (Goal: 0.45, Constraints: 0.30, Criteria: 0.00, Context: 0.40)

### Round 3
**Q:** Jira·Notion과 다르다는 반응을 이끌어낼 기능 방향은?
**A:** 데이터 시각화 차별화, 실시간 협업 UX, AI 통합 강화 (3가지 선택)
**Ambiguity:** 51% (Goal: 0.65, Constraints: 0.35, Criteria: 0.45, Context: 0.50)

### Round 4 [CONTRARIAN]
**Q:** 하나만 고를 수 있다면 TaskHive의 영혼은?
**A:** AI 코파일러
**Ambiguity:** 39% (Goal: 0.80, Constraints: 0.45, Criteria: 0.55, Context: 0.55)

### Round 5
**Q:** AI 백엔드를 어떻게 할 예정인가요?
**A:** Ollama 유지 (로컬 LLM)
**Ambiguity:** 32% (Goal: 0.82, Constraints: 0.65, Criteria: 0.55, Context: 0.65)

### Round 6 [SIMPLIFIER]
**Q:** 가장 적은 노력으로 'AI쟐네' 반응을 이끌 기능은?
**A:** 태스크 요약 코멘트, 자연어 태스크 생성, 지능형 필터링 (3가지 선택)
**Ambiguity:** 22% (Goal: 0.88, Constraints: 0.72, Criteria: 0.75, Context: 0.72)

### Round 7
**Q:** 3가지 중 가장 먼저 구현할 1순위는?
**A:** 자연어 태스크 생성 강화
**Ambiguity:** 17% ✅ (Goal: 0.90, Constraints: 0.80, Criteria: 0.82, Context: 0.75)

</details>
