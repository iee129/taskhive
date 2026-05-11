# 데이터베이스 마이그레이션

## 현재 전략

개발 환경에서는 `spring.jpa.hibernate.ddl-auto=update`로 Entity 변경 시 스키마 자동 반영.  
프로덕션 전환 시 **Flyway** 도입 예정.

## 마이그레이션 도구 비교

| 도구 | 장점 | 단점 | 선택 여부 |
|------|------|------|----------|
| Flyway | Spring Boot 자동 통합, 단순 SQL 기반 | Java 기반 마이그레이션 제한적 | **예정** |
| Liquibase | XML/YAML/SQL 지원, 롤백 내장 | 설정 복잡 | 미선택 |
| `ddl-auto=update` | 설정 없음 | 컬럼 삭제 불가, 프로덕션 위험 | 개발 전용 |

## Flyway 도입 계획 (Phase 6)

### 의존성 추가 (pom.xml)
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

### 설정 (application.yml)
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: validate   # Flyway 사용 시 validate로 변경
```

### 마이그레이션 파일 명명 규칙

```
src/main/resources/db/migration/
├── V1__create_users.sql
├── V2__create_projects.sql
├── V3__create_tasks.sql
└── V4__add_task_indexes.sql
```

- 접두사: `V{숫자}__` (버전 번호 + 이중 밑줄)
- 숫자는 단조 증가 (건너뛰기 금지)
- 한 번 적용된 파일은 수정 금지

### V1__create_users.sql 예시
```sql
CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

## 롤백 정책

Flyway Community Edition은 자동 롤백 미지원.  
스키마 변경 실패 시:
1. 결함 있는 마이그레이션 파일을 수정한 새 버전(`V{N+1}__fix_...sql`) 작성
2. `flyway repair` 실행하여 체크섬 재계산
3. 다시 `flyway migrate` 실행
