# 제약사항

## 기술적 제약

| 제약 | 내용 |
|------|------|
| **Java 버전** | Java 21 이상 필요 (가상 스레드, 패턴 매칭 사용) |
| **Node.js 버전** | Node.js 20 이상 필요 (Vite 5.x 요구사항) |
| **Docker** | Docker Compose 실행 시 Docker Engine 24+ 필요 |
| **운영체제** | macOS / Linux 환경 기준 (Windows WSL2 가능하나 미검증) |

## 스코프 밖 항목

다음 항목은 **현재 구현 범위에 포함되지 않습니다**:

| 항목 | 이유 |
|------|------|
| FastAPI / Django 백엔드 | Java Spring Boot로 확정 |
| Next.js / Nuxt.js 프론트엔드 | React SPA로 확정 |
| GraphQL API | REST API로 충분, 복잡도 증가 불필요 |
| 실시간 WebSocket 알림 | MVP 범위 초과 |
| OAuth2 / 소셜 로그인 | 이메일 인증으로 1차 구현 |
| 마이크로서비스 분리 | 단일 Spring Boot 모놀리식으로 시작 |
| 클라우드 배포 (AWS, GCP, Azure) | 로컬 K8s + Docker Compose가 목표 |
| 모바일 앱 (iOS / Android) | 웹 전용 |
| PostgreSQL 이외 DB | MySQL / MongoDB 등 미지원 |
| 파일 업로드 / 첨부파일 | MVP 범위 초과 |

## 운영 제약

| 제약 | 내용 |
|------|------|
| **인증 방식** | JWT 단일 방식, Refresh Token 미지원 (24시간 만료) |
| **다국어** | 한국어/영어 UI 혼용, i18n 라이브러리 미적용 |
| **접근성** | WCAG 준수 미보장 (추후 개선 가능) |
| **SEO** | SPA 구조로 서버 사이드 렌더링 없음 |

## 보안 제약

| 제약 | 내용 |
|------|------|
| `JWT_SECRET` | 환경 변수로만 주입, `.env` 파일 git 커밋 절대 금지 |
| K8s Secret | `k8s/secret*.yaml` 파일 `.gitignore` 처리됨 |
| API 키 | Kaggle 등 외부 API 키 절대 커밋 금지 |
