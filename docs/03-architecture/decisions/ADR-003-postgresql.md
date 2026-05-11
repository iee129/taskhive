# ADR-003: PostgreSQL 데이터베이스 선택

**날짜**: 2026-05-12  
**상태**: 수락됨

## Context (배경)

관계형 데이터 모델이 필요합니다. User ↔ Project ↔ Task 간의 외래키 관계와 트랜잭션 무결성이 중요합니다.

## Decision (결정)

**PostgreSQL 16** 을 메인 데이터베이스로 선택합니다. 테스트 환경에서는 H2 인메모리를 사용합니다.

## Alternatives (고려한 대안)

| 대안 | 장점 | 단점 |
|------|------|------|
| MySQL 8 | 국내 레퍼런스 풍부 | JSON 타입 지원 약함, strict mode 설정 필요 |
| H2 (전체) | 별도 설치 불필요 | 프로덕션 비적합, PostgreSQL 방언 차이 |
| MongoDB | 스키마 유연성 | 관계형 데이터에 JOIN 불편, JPA 통합 별도 라이브러리 |
| PostgreSQL | JSONB, 풀텍스트 검색, K8s 이미지 최적화 | MySQL 대비 DBA 인력 희소 |

## Consequences (결과)

- `spring.jpa.hibernate.ddl-auto=update` 로 Entity 변경 시 자동 스키마 반영 (개발 환경)
- Docker: `postgres:16-alpine` 이미지로 경량화
- K8s: StatefulSet + PVC로 영속성 보장
- 테스트: `com.h2database:h2` (scope=test) 로 빠른 단위 테스트
