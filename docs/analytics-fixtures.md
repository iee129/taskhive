# Analytics Fixtures

## 시드 데이터셋

테스트 프로젝트에 3개의 태스크를 생성:

| 태스크 | 생성일 | 상태 변경 이력 |
|--------|--------|---------------|
| T1 | D+0 | TODO → IN_PROGRESS (D+1) → DONE (D+2) |
| T2 | D+0 | TODO → IN_PROGRESS (D+1) |
| T3 | D+1 | TODO |

여기서 D = 기준일(from).

## 기대값 손계산

### Burndown (from=D, to=D+2)

| 날짜 | 잔여 태스크 | 계산 근거 |
|------|------------|-----------|
| D | 2 | T1(TODO), T2(TODO), T3 미생성 → 2 |
| D+1 | 3 | T1(IN_PROGRESS), T2(IN_PROGRESS), T3(TODO) → 3 |
| D+2 | 2 | T1(DONE), T2(IN_PROGRESS), T3(TODO) → 2 |

### CFD (from=D, to=D+2)

| 날짜 | TODO | IN_PROGRESS | DONE |
|------|------|------------|------|
| D | 2 | 0 | 0 |
| D+1 | 1 | 2 | 0 |
| D+2 | 1 | 1 | 1 |

### Cycle Time

| 태스크 | 시작(IN_PROGRESS) | 완료(DONE) | 소요일 |
|--------|-----------------|------------|--------|
| T1 | D+1 | D+2 | 1 |

T2, T3는 미완료이므로 cycle-time 결과에 미포함.

## 구현 참고

- `burndown` / `cfd` 엔드포인트: `task_status_history` 기반으로 각 날짜별 태스크 상태 역산
- `cycle-time` 엔드포인트: DONE 태스크의 IN_PROGRESS 진입~DONE 전환 사이 일수
- 날짜 형식: `yyyy-MM-dd` (ISO-8601)
