# 릴리즈 가이드

## 버전 정책

**Semantic Versioning** (SemVer): `MAJOR.MINOR.PATCH`

| 버전 유형 | 변경 예시 |
|----------|----------|
| `MAJOR` | 하위 호환 불가 API 변경 |
| `MINOR` | 하위 호환 새 기능 추가 |
| `PATCH` | 버그 수정 |

현재 버전: `0.1.0` (초기 개발 단계)

## CHANGELOG 관리

[Keep a Changelog](https://keepachangelog.com) 형식으로 `docs/01-project/changelog.md` 유지.

```markdown
## [Unreleased]
### Added
- 기능 추가 내용

## [0.2.0] - 2026-06-01
### Added
- 태스크 CRUD API
- 프론트엔드 로그인 UI
### Fixed
- JWT 만료 시 리다이렉트 오류
```

## 릴리즈 절차

```bash
# 1. 버전 번호 결정 (예: 0.2.0)
VERSION=0.2.0

# 2. CHANGELOG 업데이트 — [Unreleased] → [0.2.0] - {날짜}
# docs/01-project/changelog.md 편집

# 3. pom.xml 버전 업데이트
mvn versions:set -DnewVersion=$VERSION -f backend/pom.xml

# 4. 커밋
git add docs/01-project/changelog.md backend/pom.xml
git commit -m "chore: release v$VERSION"

# 5. 태그 생성
git tag -a "v$VERSION" -m "Release v$VERSION"

# 6. 푸시
git push origin master
git push origin "v$VERSION"
```

## GitHub Release 생성

```bash
gh release create "v$VERSION" \
  --title "v$VERSION" \
  --notes-file <(cat docs/01-project/changelog.md | \
    awk "/## \[$VERSION\]/,/## \[/" | head -n -1)
```

## 핫픽스 절차

```bash
# 프로덕션 버그 발생 시
git checkout master
git checkout -b hotfix  # 단일 소문자 단어

# 수정 후
git commit -m "fix: 크리티컬 버그 수정"
git checkout master
git merge hotfix
git tag -a "v0.1.1" -m "Hotfix v0.1.1"
git push origin master --tags
```
