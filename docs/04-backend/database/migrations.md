# 데이터베이스 마이그레이션

## 현재 전략 (Phase 10 이후)

**Flyway** 기반 버전 관리. `ddl-auto: validate`로 설정해 Flyway 스크립트가 스키마를 관리하고 JPA는 검증만 수행.

| 환경 | 전략 | Flyway |
|------|------|--------|
| prod/Docker | `validate` | `enabled: true` |
| dev (로컬) | `validate` + `baseline-on-migrate: true` | `enabled: true` |
| test (H2) | `create-drop` | `enabled: false` |

## 의존성 (pom.xml)

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

## 설정 (application.yml)

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: validate
```

## 마이그레이션 스크립트 목록

```
auth/src/main/resources/db/migration/
├── V1__init_users.sql
├── V2__add_projects.sql
├── V3__add_tasks.sql
├── V4__add_refresh_tokens.sql
├── V5__add_comments.sql
└── V6__add_task_activities_and_indexes.sql
```

- 접두사: `V{숫자}__` (버전 번호 + 이중 밑줄)
- 숫자는 단조 증가 (건너뛰기 금지)
- **한 번 적용된 파일은 수정 금지** — 새 버전 파일로 변경 사항 추가

## 도구 비교

| 도구 | 선택 여부 | 이유 |
|------|----------|------|
| Flyway | **선택** | Spring Boot 자동 통합, 단순 SQL 기반 |
| Liquibase | 미선택 | 설정 복잡, 현 규모에서 과도 |
| `ddl-auto=update` | 테스트 전용 | 컬럼 삭제 불가, 프로덕션 위험 |

## 롤백 정책

Flyway Community Edition은 자동 롤백 미지원.
스키마 변경 실패 시:
1. 결함 있는 마이그레이션 파일을 수정한 새 버전(`V{N+1}__fix_...sql`) 작성
2. `flyway repair` 실행하여 체크섬 재계산
3. 다시 `flyway migrate` 실행
