# ADR-001: Spring Boot 3 백엔드 프레임워크 선택

**날짜**: 2026-05-12  
**상태**: 수락됨

## Context (배경)

Java 기반 웹 API 서버가 필요하며, Docker 컨테이너화와 Kubernetes 배포를 목표로 합니다. 학습 목적과 실무 활용 가능성을 동시에 고려해야 합니다.

## Decision (결정)

**Spring Boot 3.3 (Java 21)** 을 백엔드 프레임워크로 선택합니다.

## Alternatives (고려한 대안)

| 대안 | 장점 | 단점 |
|------|------|------|
| Quarkus | 컨테이너 이미지 크기 작음, 빠른 시작 | 레퍼런스 적음, 생태계 협소 |
| Micronaut | 컴파일 타임 DI, GraalVM 최적화 | 러닝 커브 높음 |
| Spring Boot 3 | 방대한 생태계, 레퍼런스, 팀 친숙도 | 이미지 크기 상대적으로 큼 |

## Consequences (결과)

- `spring-boot-starter-web`, `data-jpa`, `security`, `actuator` 로 빠른 기능 구현 가능
- Auto-configuration으로 설정 최소화
- Spring Security와의 긴밀한 통합으로 JWT 필터 체인 구성 용이
- `spring-boot-maven-plugin`으로 실행 가능 JAR 단일 패키징
