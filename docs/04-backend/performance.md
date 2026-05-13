# 백엔드 성능 최적화

## 개요 (Phase 8)

| 항목 | 방법 | 효과 |
|------|------|------|
| N+1 제거 | JOIN FETCH 쿼리 | 태스크 목록 쿼리 N+1 → 1회 |
| DB 인덱스 | `@Index` 어노테이션 | 필터·삭제 쿼리 Index Scan |
| Redis 캐싱 | `@Cacheable` / `@CacheEvict` | 프로젝트 목록 재조회 0 |

---

## N+1 제거

### 문제 상황

`Task`는 `assignee(User)`, `project(Project)`를 LAZY 로드.  
기존 `findAllByDeletedAtIsNull()` 실행 시 태스크 N개 → assignee N번 추가 조회.

```
SELECT * FROM tasks WHERE deleted_at IS NULL     -- 1회
SELECT * FROM users WHERE id = ?                 -- N회 (assignee)
SELECT * FROM projects WHERE id = ?              -- N회 (project)
```

### 해결 — JOIN FETCH

```java
// TaskRepository.java
@Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.project " +
       "WHERE t.deletedAt IS NULL")
List<Task> findAllWithAssociations();
```

```
SELECT t.*, u.*, p.* FROM tasks t
  LEFT JOIN users u ON u.id = t.assignee_id
  LEFT JOIN projects p ON p.id = t.project_id
WHERE t.deleted_at IS NULL                       -- 1회
```

`findFiltered()` (상태·우선순위·검색 필터)도 동일 패턴으로 JOIN FETCH 포함.

---

## DB 인덱스

```java
// Task.java
@Table(name = "tasks", indexes = {
    @Index(name = "idx_tasks_status",     columnList = "status"),
    @Index(name = "idx_tasks_priority",   columnList = "priority"),
    @Index(name = "idx_tasks_deleted_at", columnList = "deleted_at"),
    @Index(name = "idx_tasks_assignee",   columnList = "assignee_id")
})
```

**대상 쿼리:**

| 인덱스 | 커버하는 쿼리 |
|--------|-------------|
| `status` | `findFiltered(status=...)`, `countByStatus` |
| `priority` | `findFiltered(priority=...)`, `countByPriority` |
| `deleted_at` | 모든 소프트 삭제 필터 (`WHERE deleted_at IS NULL`) |
| `assignee_id` | `findByAssigneeIdAndDeletedAtIsNull` |

> H2 인메모리 테스트 환경에서도 동일 인덱스 DDL이 적용됨.

---

## Redis 캐싱

### 설정

```yaml
# application.yml
spring:
  cache:
    type: ${CACHE_TYPE:simple}    # 기본 in-memory, redis로 전환 가능
    redis:
      time-to-live: 300000        # 5분 (Redis 사용 시)
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

| `CACHE_TYPE` 값 | 사용 CacheManager | 비고 |
|----------------|------------------|------|
| `simple` (기본) | `ConcurrentMapCacheManager` | Redis 불필요 |
| `redis` | `RedisCacheManager` | Redis 서버 필요 |

### 적용 — ProjectService

```java
@Cacheable(value = "projects", key = "#email")
public List<ProjectResponse> getMyProjects(String email) { ... }

@CacheEvict(value = "projects", key = "#email")
@Transactional
public ProjectResponse createProject(ProjectRequest request, String email) { ... }

@CacheEvict(value = "projects", key = "#email")
@Transactional
public ProjectResponse updateProject(Long id, ProjectRequest request, String email) { ... }

@CacheEvict(value = "projects", key = "#email")
@Transactional
public void deleteProject(Long id, String email) { ... }
```

- 캐시 키: `email` — 사용자별 독립 캐시
- 프로젝트 변경 시 해당 사용자 캐시 즉시 제거

### 캐싱 대상 선정 기준

| 후보 | 선택 여부 | 이유 |
|------|----------|------|
| 프로젝트 목록 | ✅ | 변경 드물고 다수 API에서 반복 조회 |
| 태스크 목록 | ❌ | 변경 빈번, TTL 설정 복잡 → TanStack Query로 대응 |
| 통계 데이터 | ❌ | 집계 쿼리 수준 허용 범위 |

---

## EXPLAIN ANALYZE 확인 방법

```sql
-- PostgreSQL (Docker 환경)
EXPLAIN ANALYZE
SELECT t.*, u.email, p.name
FROM tasks t
LEFT JOIN users u ON u.id = t.assignee_id
LEFT JOIN projects p ON p.id = t.project_id
WHERE t.deleted_at IS NULL;
-- 목표: Seq Scan 0개, Index Scan 활용
```

---

## 실행 명령

```bash
# Redis 없이 캐싱 확인 (simple 모드)
mvn spring-boot:run

# Redis 모드 전환
CACHE_TYPE=redis REDIS_HOST=localhost mvn spring-boot:run

# 캐시 테스트
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/projects
# 두 번째 호출: SELECT 쿼리 미발생 (show-sql=true 로그 확인)
```
