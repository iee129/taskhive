# Deep Interview Spec: TaskHive 프로젝트 멤버 초대 & 공유

## Metadata
- Interview ID: di-taskhive-001
- Rounds: 6
- Final Ambiguity Score: 16%
- Type: brownfield
- Generated: 2026-05-12
- Threshold: 20%
- Status: PASSED

## Clarity Breakdown
| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.93 | 35% | 0.326 |
| Constraint Clarity | 0.82 | 25% | 0.205 |
| Success Criteria | 0.80 | 25% | 0.200 |
| Context Clarity | 0.75 | 15% | 0.113 |
| **Total Clarity** | | | **0.843** |
| **Ambiguity** | | | **16%** |

## Goal
프로젝트 오너가 이미 가입된 사용자를 이메일로 검색해 즉시 프로젝트 멤버로 추가하고, Owner / Member 두 가지 역할로 협업할 수 있게 한다.

## Constraints
- 초대는 이미 가입된(이메일 인증 완료) 사용자만 가능 — 미가입자 초대 이메일 불필요
- 역할은 **Owner** / **Member** 두 가지만 존재
- **Member**는 태스크 생성·수정·삭제·댓글 작성 가능 (태스크 삭제도 허용)
- **Owner만** 프로젝트를 삭제할 수 있음
- 멤버 초대·제거는 Owner와 Member 모두 가능
- 공유 단위는 **프로젝트 레벨** (태스크 단위 공유 없음)

## Non-Goals
- 미가입자 초대 이메일 발송
- 읽기 전용(ReadOnly) 역할 — 향후 확장 가능, MVP에서 제외
- 태스크 단위 권한 분리
- 프로젝트당 멤버 수 상한 (현재 미정)

## Acceptance Criteria
- [ ] `ProjectMember` 엔티티와 리포지토리가 존재함 (project, user, role: OWNER/MEMBER)
- [ ] `GET /api/projects/{id}/members` — 멤버 목록 반환 (인증된 멤버만 조회 가능)
- [ ] `POST /api/projects/{id}/members` — 이메일로 사용자 검색 후 즉시 추가, 이미 멤버면 409 반환
- [ ] `DELETE /api/projects/{id}/members/{userId}` — 멤버 제거 (Owner 또는 Member 가능, 단 유일한 Owner는 제거 불가)
- [ ] `GET /api/users/search?email=` — 등록된 사용자를 이메일로 검색 (본인 및 이미 멤버 제외)
- [ ] 프로젝트 삭제(`DELETE /api/projects/{id}`)는 Owner만 가능 — Member 시도 시 HTTP 403
- [ ] 비멤버가 프로젝트의 태스크·댓글에 접근 시 HTTP 403 반환
- [ ] 프론트엔드: 프로젝트 카드에 멤버 아바타 슬롯 표시 (최대 N개 + 초과 시 +n 표기)
- [ ] 프론트엔드: 아바타 클릭 → 멤버 관리 모달 (초대·제거·현재 멤버 목록)
- [ ] 초대된 사용자는 자신의 프로젝트 목록에 공유된 프로젝트가 표시됨

## Assumptions Exposed & Resolved
| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| 3가지 역할 필요 | ReadOnly 실제 사용 시나리오가 적다면? | Owner / Member 2가지로 단순화 |
| 미가입자도 초대 가능해야 한다 | 이메일 링크 복잡도 vs 즉시 추가 편의성 | 가입자만 즉시 추가 |
| Member는 태스크 삭제 불가 | Owner만 삭제 가능하면? | 프로젝트 삭제만 Owner 전용, 태스크 삭제는 Member도 가능 |

## Technical Context (Brownfield)

### 현재 구조
- `Project` 모델: `owner` 필드(User) — 단일 소유자 모델
- `Task`, `Comment`는 모두 Project에 귀속
- `ProjectController`: `GET/POST /api/projects`, `DELETE /api/projects/{id}` 존재
- `SecurityConfig`: JWT 인증 후 `@AuthenticationPrincipal`로 현재 사용자 식별
- `UserRepository.findByEmail()`: 이미 존재 (deletedAt IS NULL 조건 포함)

### 필요한 변경
1. **새 엔티티**: `ProjectMember(id, project, user, role: OWNER/MEMBER, createdAt)`
2. **새 리포지토리**: `ProjectMemberRepository`
3. **새 서비스**: `ProjectMemberService` (초대, 제거, 권한 체크)
4. **새 컨트롤러**: `ProjectMemberController` (`/api/projects/{id}/members`)
5. **User 검색 API**: `UserController` 또는 기존 컨트롤러에 `GET /api/users/search?email=` 추가
6. **권한 체크 업데이트**: `ProjectService`, `TaskService`, `CommentService`에서 멤버십 확인 로직 추가
7. **프론트엔드**: 프로젝트 카드 컴포넌트에 멤버 아바타 + 관리 모달 추가
8. **프론트엔드**: 프로젝트 목록 API에 멤버 정보 포함

## Ontology (Key Entities)
| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| Project | core domain | id, name, owner, createdAt | has many Tasks, has many ProjectMembers |
| User | core domain | id, email, name, emailVerified | owns many Projects, member of many Projects |
| ProjectMember | supporting | id, role(OWNER/MEMBER), createdAt | belongs to Project, belongs to User |
| Task | supporting | id, title, status, assignee | belongs to Project |

## Ontology Convergence
| Round | Entities | New | Changed | Stable | Stability |
|-------|----------|-----|---------|--------|-----------|
| 2 | Project, User, Role(3가지) | 3 | — | — | N/A |
| 3 | Project, User, ProjectMember, Role(3가지) | 1 | — | 3 | 75% |
| 4 | Project, User, ProjectMember(OWNER/MEMBER) | 0 | 1(Role→enum) | 3 | 100% |
| 5 | Project, User, ProjectMember | 0 | 0 | 3 | 100% |

## Interview Transcript
<details>
<summary>전체 Q&A (6라운드)</summary>

### Round 1
**Q:** 어떤 기능을 추가하고 싶으신가요?
**A:** 팀원 초대 / 공유
**Ambiguity:** 69%

### Round 2
**Q:** 초대된 사람은 무엇을 할 수 있게 하고 싶으신가요?
**A:** 오너가 권한을 수정할 수 있으면 좋겠어. 권한에는 읽기 전용, 수정 가능, 오너 세 가지.
**Ambiguity:** 49%

### Round 3
**Q:** 초대 절차는 어떤 흐름이 자연스럽나요?
**A:** 이메일로 이미 가입된 사용자 검색 → 즉시 추가
**Ambiguity:** 31%

### Round 4 (Contrarian)
**Q:** ReadOnly가 실제로 필요한 시나리오가 많지 않다면 2가지 역할로 단순화하는 건 어떤가요?
**A:** 2가지로 단순화 (Owner / Member)
**Ambiguity:** 25%

### Round 5
**Q:** Member는 Owner에 비해 무엇을 할 수 없어야 하나요?
**A:** 프로젝트 삭제 불가
**Ambiguity:** 20%

### Round 6
**Q:** 멤버 관리 UI는 어디에 두는 게 자연스럽나요?
**A:** 프로젝트 카드에 멤버 슬롯 + 모달
**Ambiguity:** 16%
</details>
