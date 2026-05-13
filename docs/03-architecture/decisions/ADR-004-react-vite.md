# ADR-004: React 18 + Vite 프론트엔드 선택

**날짜**: 2026-05-12  
**상태**: 수락됨

## Context (배경)

백엔드 REST API와 분리된 SPA(Single Page Application) 프론트엔드가 필요합니다. TypeScript 타입 안전성과 빠른 개발 환경이 중요합니다.

## Decision (결정)

**React 18 + TypeScript 5 + Vite 5** 조합을 선택합니다.

## Alternatives (고려한 대안)

| 대안 | 장점 | 단점 |
|------|------|------|
| Next.js | SSR/SSG, SEO | 서버 필요, 백엔드와 역할 혼재 가능성 |
| Vue 3 | 학습 곡선 낮음 | React 대비 생태계 협소 |
| React + CRA | 검증된 방식 | 느린 빌드, 유지 관리 중단 (deprecated) |
| React + Vite | 빠른 HMR, ESM 네이티브 | CRA 대비 설정 필요 |

## Consequences (결과)

- `npm run dev` 로 즉시 HMR 개발 서버 기동
- `npm run build` → `dist/` 정적 파일 → Nginx 서빙
- `vite.config.ts`의 proxy 설정으로 개발 중 `/api/*` → `localhost:8080` 프록시
- TypeScript strict 모드로 런타임 에러 사전 방지
