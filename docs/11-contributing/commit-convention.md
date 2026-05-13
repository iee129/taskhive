# 커밋 컨벤션

## 형식

```
{type}({scope}): {subject}

{body}        ← 선택사항
```

## Type

| Type | 용도 |
|------|------|
| `feat` | 새 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 변경 |
| `refactor` | 기능 변경 없는 코드 개선 |
| `test` | 테스트 추가·수정 |
| `chore` | 빌드·설정·의존성 변경 |
| `style` | 포맷팅, 세미콜론 등 (로직 변경 없음) |
| `perf` | 성능 개선 |
| `ci` | CI/CD 설정 변경 |

## Scope (선택)

| Scope | 대상 |
|-------|------|
| `auth` | 인증·인가 |
| `task` | 태스크 기능 |
| `project` | 프로젝트 기능 |
| `db` | 데이터베이스 |
| `k8s` | Kubernetes |
| `docker` | Docker |
| `fe` | 프론트엔드 |

## 예시

```bash
# 기능 추가
git commit -m "feat(auth): JWT 필터 및 SecurityConfig 구현"

# 버그 수정
git commit -m "fix(auth): UsernameNotFoundException import 누락 수정"

# 문서
git commit -m "docs: 아키텍처 다이어그램 추가"

# 리팩터링
git commit -m "refactor(task): TaskService 메서드 분리"

# 설정
git commit -m "chore: spring-boot-devtools 의존성 추가"

# 여러 줄 커밋 (body 포함)
git commit -m "feat(task): 태스크 CRUD API 구현

- GET /api/tasks: 내 태스크 목록
- POST /api/tasks: 태스크 생성
- PUT /api/tasks/{id}: 태스크 수정
- DELETE /api/tasks/{id}: 태스크 삭제"
```

## 규칙

- Subject는 **50자 이하**, 마침표 없음
- 한국어·영어 모두 허용 (프로젝트 내 일관성 유지)
- 현재형 명령문: "추가", "수정", "삭제" (과거형 금지: "추가했음", "추가됨")
- Body는 "무엇을" 이 아닌 "왜"에 집중
