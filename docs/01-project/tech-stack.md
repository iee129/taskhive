# 기술 스택

## 백엔드

| 기술 | 버전 | 선정 이유 |
|------|------|-----------|
| Java | 21 (LTS) | Virtual Threads, Pattern Matching 등 최신 기능 + 장기 지원 |
| Spring Boot | 3.3.0 | Java 생태계 표준, Auto-configuration, 풍부한 레퍼런스 |
| Spring Security | 6.x (Boot 내장) | JWT 필터 체인 커스터마이징 용이 |
| Spring Data JPA | 3.x (Boot 내장) | Repository 패턴 자동화, JPQL 지원 |
| jjwt | 0.12.5 | JWT 생성·파싱 표준 라이브러리 |
| Lombok | 최신 | 보일러플레이트 코드 제거 |
| Maven | 3.x | 의존성 관리 및 빌드 표준 |

## 프론트엔드

| 기술 | 버전 | 선정 이유 |
|------|------|-----------|
| React | 18.3.1 | 컴포넌트 기반 UI, 가장 큰 생태계 |
| TypeScript | 5.4.5 | 타입 안전성, IDE 자동완성 |
| Vite | 5.3.1 | 빠른 HMR, ESM 네이티브 빌드 |
| React Router | 6.x | SPA 라우팅 표준 |
| Axios | 1.7.x | HTTP 클라이언트, 인터셉터 지원 |

## 데이터베이스

| 기술 | 버전 | 선정 이유 |
|------|------|-----------|
| PostgreSQL | 16 | 안정성, JSON 지원, Docker 이미지 경량 |
| H2 | 최신 | 테스트 전용 인메모리 DB |

## 인프라

| 기술 | 버전 | 용도 |
|------|------|------|
| Docker | 24+ | 컨테이너화 |
| Docker Compose | 3.9 | 로컬 멀티 컨테이너 오케스트레이션 |
| Kubernetes | 1.28+ | 프로덕션 배포 |
| Nginx | alpine | 프론트엔드 정적 파일 서빙 + 리버스 프록시 |
| GitHub Actions | - | CI/CD 파이프라인 |
