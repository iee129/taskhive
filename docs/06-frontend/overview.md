# 프론트엔드 개요

## 기술 스택

| 항목 | 선택 | 버전 |
|------|------|------|
| UI 라이브러리 | React | 18.x |
| 언어 | TypeScript | 5.x |
| 빌드 도구 | Vite | 5.x |
| 패키지 관리 | npm | 10.x |
| HTTP 클라이언트 | Axios | 1.x |
| 라우팅 | React Router | 6.x |
| UI 컴포넌트 | Ant Design | 5.x |

## 구현 상태

| 기능 | 상태 |
|------|------|
| Vite + React 프로젝트 초기화 | ✅ 완료 |
| Axios 클라이언트 + JWT 인터셉터 | ✅ 완료 |
| 로그인·회원가입 페이지 | ✅ 완료 |
| 태스크 목록 + CRUD | ✅ 완료 |
| 내 정보 페이지 | ✅ 완료 |
| PrivateRoute (비인증 접근 차단) | ✅ 완료 |

## 아키텍처 원칙

- **SPA (Single Page Application)**: 서버 렌더링 없음 — React Router로 클라이언트 라우팅
- **API 분리**: 백엔드와 완전히 분리된 독립 컨테이너. `/api/*` 는 Vite proxy → Nginx로 라우팅
- **TypeScript strict 모드**: 런타임 에러 사전 방지
- **JWT 클라이언트 저장**: `localStorage['token']`, Axios 인터셉터로 자동 주입

## 개발 서버

```bash
cd frontend
npm install
npm run dev       # http://localhost:5173
```

`vite.config.ts`의 proxy 설정으로 개발 중 `/api/*` → `http://localhost:8080` 자동 프록시.

## 빌드 및 서빙

```bash
npm run build     # dist/ 정적 파일 생성
# Nginx가 dist/ 서빙, /api/* 는 backend:8080으로 프록시
```
