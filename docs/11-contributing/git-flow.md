# Git 워크플로

## 브랜치 전략

```
master          ← 프로덕션 기준 브랜치
├── auth        ← 인증 기능 (완료)
├── docs        ← 문서화 (현재)
├── taskapi     ← 태스크 API (예정)
├── frontend    ← 프론트엔드 UI (예정)
└── {feature}   ← 단일 소문자 단어
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
