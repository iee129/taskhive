# 테스트 전략

## 테스트 피라미드

```
          /\
         /E2E\          소수, 느림, 비용 높음
        /------\
       /통합 테스트\      중간, DB 포함
      /------------\
     / 단위 테스트   \   다수, 빠름, 격리됨
    /________________\
```

## 계층별 전략

| 계층 | 도구 | 범위 | 격리 |
|------|------|------|------|
| 단위 테스트 | JUnit 5 + Mockito | Service, Util 메서드 | Mock 사용 |
| 통합 테스트 | `@SpringBootTest` + H2 | Controller, Repository | 인메모리 DB |
| E2E 테스트 | (예정) Playwright | 전체 플로우 | 실제 브라우저 |

## 현재 구현 상태

> 테스트 코드는 **Phase 3** 이후 본격 작성 예정. 현재 `src/test/` 디렉토리만 존재.

## 커버리지 목표

| 계층 | 목표 커버리지 |
|------|------------|
| Service | 80% 이상 |
| Repository | 주요 쿼리 통합 테스트 |
| Controller | 인증 경로 통합 테스트 |
| 전체 | 70% 이상 (Line Coverage) |

## CI 통합

```yaml
# GitHub Actions — backend-test job
- name: Test with coverage
  run: mvn test jacoco:report -f backend/pom.xml

- name: Coverage gate
  run: |
    COVERAGE=$(cat backend/target/site/jacoco/index.html \
      | grep -o 'Total.*%' | grep -o '[0-9]*%' | head -1)
    echo "Coverage: $COVERAGE"
```

## 테스트 데이터 원칙

- 테스트는 독립적이어야 함 — 실행 순서에 의존 금지
- `@BeforeEach`에서 테스트 데이터 생성, `@AfterEach`에서 정리
- 프로덕션 DB 데이터 사용 금지 — H2 인메모리 사용
- 임의값은 `@Test` 내부에서 명시적으로 설정 (랜덤 금지)
