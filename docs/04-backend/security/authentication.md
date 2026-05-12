# 인증 (Authentication)

## JWT 인증 흐름

```
클라이언트
  → Authorization: Bearer {accessToken}
    → JwtFilter.doFilterInternal()
      → JwtUtil.isValid(token)
        → SecurityContextHolder.setAuthentication(...)
          → Controller (인증된 요청)
```

Refresh Token은 `HttpOnly; SameSite=Lax` 쿠키로 전달되며 Access Token 재발급에만 사용됩니다.

## 구현 파일

| 파일 | 역할 |
|------|------|
| `config/JwtUtil.java` | JWT 생성·파싱·검증 |
| `config/JwtFilter.java` | 요청마다 토큰 추출 및 검증 |
| `config/SecurityConfig.java` | 필터 등록, 공개 경로 설정, @EnableMethodSecurity |
| `service/RefreshTokenService.java` | Refresh Token 발급·Rotation·무효화 |

## JwtUtil 핵심 메서드

```java
public String generateToken(String email) {
    return Jwts.builder()
        .subject(email)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
}

public String extractEmail(String token) {
    return getClaims(token).getSubject();
}

public boolean isValid(String token) {
    try {
        getClaims(token);
        return true;
    } catch (JwtException | IllegalArgumentException e) {
        return false;
    }
}
```

## 토큰 설정

| 항목 | 값 |
|------|-----|
| 알고리즘 | HMAC-SHA256 (HS256) |
| Access Token 유효기간 | **15분 (900,000ms)** |
| Refresh Token 유효기간 | **7일 (604,800,000ms)** |
| Subject | 사용자 이메일 |
| Secret 출처 | `${JWT_SECRET}` 환경 변수 |
| Access Token 전달 | 응답 JSON (`token` 필드) |
| Refresh Token 전달 | `HttpOnly; SameSite=Lax` 쿠키 |

## Refresh Token Rotation

`POST /api/auth/refresh` 호출 시:
1. 쿠키에서 Refresh Token 추출
2. DB 조회 + `PESSIMISTIC_WRITE` 잠금으로 중복 회전 방지
3. 기존 토큰 삭제 + 새 토큰 발급 (Rotation)
4. 새 Access Token + 새 Refresh Token 쿠키 응답

## 공개 엔드포인트 (인증 불필요)

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /actuator/health`
