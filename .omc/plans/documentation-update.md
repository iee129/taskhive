# 문서화 업데이트 계획

## 요약
Phase 13까지 구현된 TaskHive의 README.md를 현행화하고, API 문서와 아키텍처 문서를 신규 작성한다.

## 수용 기준 (Acceptance Criteria)
- [ ] `README.md`: 실제 폴더 구조(auth/, frontend/) 반영, 구현된 기능 목록, 로컬 실행 명령, 환경변수 목록, 배포 정보(Railway+Vercel) 포함
- [ ] `docs/API.md`: 모든 REST 엔드포인트 문서화 (요청/응답 예시 포함)
- [ ] `docs/ARCHITECTURE.md`: 시스템 구성도, 주요 설계 결정, DB 스키마 개요 포함
- [ ] 기존 코드 변경 없음 — 문서 파일만 추가/수정

## 구현 단계

### Step 1: README.md 전면 업데이트
파일: `/Users/iee12/taskhive/README.md`

변경 내용:
- 프로젝트 설명 유지
- 기술 스택 테이블 현행화 (Ant Design, bucket4j, Sentry 추가)
- 실제 폴더 구조 반영 (auth/ → backend, 스크립트 위치)
- **구현된 기능 목록** 추가 (13개 Phase 결과물)
- 로컬 실행 명령 수정 (`cd auth && ./mvnw spring-boot:run`)
- 환경변수 테이블 (백엔드 + 프론트엔드)
- 배포 정보 → Railway (backend) + Vercel (frontend)
- 개발 로드맵 업데이트 (Phase 1~13 체크박스)
- Phase 13 마이그레이션 노트 (기존 DB 데이터 백필 SQL)

### Step 2: docs/API.md 신규 작성
파일: `/Users/iee12/taskhive/docs/API.md`

섹션:
- 인증 방식 (JWT Bearer + Refresh Cookie)
- Auth API (register, login, logout, refresh, verify-email, forgot/reset-password)
- Projects API (CRUD + members)
- Tasks API (CRUD + filter)
- Comments API
- Stats API
- Users API (search)
- 에러 코드 테이블 (ErrorCode enum 기반)

### Step 3: docs/ARCHITECTURE.md 신규 작성
파일: `/Users/iee12/taskhive/docs/ARCHITECTURE.md`

섹션:
- 시스템 구성 개요 (텍스트 다이어그램)
- 백엔드 레이어 구조 (controller → service → repository → model)
- 인증 흐름 (JWT + Refresh Token 순서도)
- 프로젝트 멤버 권한 모델 (OWNER/MEMBER)
- 핵심 DB 엔티티 목록 및 관계
- 주요 기술 결정 사항

## 리스크 & 완화

| 리스크 | 완화 |
|--------|------|
| 실제 코드와 문서 불일치 | 소스 파일 직접 참조하여 작성 |
| API 요청/응답 예시 부정확 | DTO 클래스 기반으로 작성 |

## 검증 단계
- README.md의 모든 파일 경로가 실제 존재하는지 확인
- API 엔드포인트가 컨트롤러 코드와 일치하는지 대조
