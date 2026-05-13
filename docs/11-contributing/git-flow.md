# Git 워크플로

## 브랜치 전략

```
master           ← 프로덕션 기준 브랜치
├── auth         ← Phase 4: 인증 고도화 (완료)
├── architecture ← Phase 5: 아키텍처 고도화 (완료)
├── docs         ← 문서화 (상시)
├── board        ← Phase 6: 칸반·댓글·Audit Log·통계·AI (예정)
├── realtime     ← Phase 6.5: WebSocket 실시간 동기화 (예정)
├── testing      ← Phase 7: 테스트 고도화 JaCoCo·Playwright (예정)
├── performance  ← Phase 8: 성능 최적화 Redis·N+1·TanStack (예정)
├── polish       ← Phase 9: UI/UX 완성도 다크모드·반응형 (예정)
├── docker       ← Phase 10: PostgreSQL + Docker Compose (예정)
├── cicd         ← Phase 11: GitHub Actions CI/CD (예정)
└── kubernetes   ← Phase 12: K8s 배포 (선택)
```

## 브랜치 명명 규칙

- **소문자만** 사용
- **단일 단어** (하이픈·언더스코어 금지)
- 기능명을 직관적으로 표현

```bash
# 올바른 예
git checkout -b auth
git checkout -b taskapi
git checkout -b frontend
git checkout -b payment

# 금지
git checkout -b auth-feature    # 하이픈 금지
git checkout -b task_api        # 언더스코어 금지
git checkout -b AddAuth         # 대문자 금지
```

## 작업 흐름

```bash
# 1. master에서 최신 코드 동기화
git checkout master
git pull origin master

# 2. 기능 브랜치 생성
git checkout -b {feature}

# 3. 개발 + 커밋
git add <files>
git commit -m "feat(auth): JWT 필터 구현"

# 4. 원격 브랜치 푸시
git push -u origin {feature}

# 5. PR 생성 → 코드 리뷰 → master merge
```

## PR 규칙

- PR 제목은 커밋 메시지 형식과 동일
- PR 본문에 변경 사항 요약 + 테스트 방법 포함
- 1개 이상 승인 후 merge (팀 단위 운영 시)
- Squash merge 또는 Rebase merge 권장 (이력 정리)

## 브랜치 보호 (master)

GitHub → Settings → Branches → Branch protection rules:
- Require pull request reviews
- Require status checks to pass (CI)
- Include administrators
