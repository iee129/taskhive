# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| latest (main) | Yes |

## Reporting a Vulnerability

보안 취약점을 발견하셨다면 **공개 이슈 트래커에 올리지 마세요**.

대신 다음 방법으로 비공개 신고해 주세요:

1. GitHub의 **[Security Advisories](https://github.com/iee129/taskhive/security/advisories/new)** 를 사용해 취약점을 비공개 신고하세요.
2. 또는 이메일로 직접 문의하세요. 레포지토리 프로필의 연락처를 참고하세요.

신고 내용에 포함해 주세요:
- 취약점 설명
- 재현 단계
- 영향 범위 (데이터 노출, 인증 우회 등)
- 가능하다면 수정 방안 제안

**48시간 이내** 응답을 목표로 합니다.

## Known Security Measures

- JWT access token 15분 만료 + HttpOnly Cookie Refresh Token 회전
- Bcrypt 비밀번호 해싱
- bucket4j IP 기반 Rate Limiting (로그인 10회/분, 가입 5회/분)
- Spring Security CORS 설정
- HSTS / CSP / X-Content-Type-Options / Referrer-Policy / X-Frame-Options 헤더
- Sentry 에러 모니터링
