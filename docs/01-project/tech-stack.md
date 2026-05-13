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

## AI / LLM

| 기술 | 버전 | 선정 이유 |
|------|------|-----------|
| Ollama | 최신 | 로컬 LLM 런타임 — 비용 0, Docker Compose 서비스로 통합 가능 |
| Llama 3.2 3B | - | 경량 한국어 지원 모델, RAM 4GB 동작, 개발 환경 자급자족 |
| Spring RestClient | Boot 3.3 내장 | Ollama HTTP API 호출 (동기, 타임아웃 설정 포함) |

> **선정 배경**: 학생 환경에서 API 키·요금 없이 구동 가능한 로컬 LLM이 목표.  
> Ollama + Llama 3.2 3B 조합이 한국어 태스크 파싱과 요약에 충분한 성능을 제공하며,  
> Docker Compose에 서비스 하나를 추가하는 것으로 전체 AI 스택이 자급자족 구성됨.

## 인프라

| 기술 | 버전 | 용도 |
|------|------|------|
| Docker | 24+ | 컨테이너화 |
| Docker Compose | 3.9 | 로컬 멀티 컨테이너 오케스트레이션 |
| Kubernetes | 1.28+ | 프로덕션 배포 |
| Nginx | alpine | 프론트엔드 정적 파일 서빙 + 리버스 프록시 |
| Redis | 7-alpine | 세션 캐싱, 프로젝트 목록 캐시 |
| GitHub Actions | - | CI/CD 파이프라인 |
