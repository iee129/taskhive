# 인증 (Authentication)

## JWT 인증 흐름

```
클라이언트
  → Authorization: Bearer {token}
    → JwtFilter.doFilterInternal()
      → JwtUtil.isValid(token)
        → SecurityContextHolder.setAuthentication(...)
          → Controller (인증된 요청)
```

## 구현 파일

| 파일 | 역할 |
|------|------|
| `config/JwtUtil.java` | JWT 생성·파싱·검증 |
| `config/JwtFilter.java` | 요청마다 토큰 추출 및 검증 |
| `config/SecurityConfig.java` | 필터 등록, 공개 경로 설정 |

## JwtUtil 핵심 메서드

```java
// JWT 생성 (로그인·회원가입 성공 시)
public String generateToken(String email) {
    return Jwts.builder()
        .subject(email)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
}

// 토큰에서 이메일 추출
public String extractEmail(String token) {
    return getClaims(token).getSubject();
}

// 토큰 유효성 검사 (서명 + 만료 여부)
public boolean isValid(String token) {
    try {
        getClaims(token);
        return true;
    } catch (JwtException | IllegalArgumentException e) {
        return false;
    }
}
```

## JwtFilter 처리 순서

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain chain) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
        String token = header.substring(7);
        if (jwtUtil.isValid(token)) {
            String email = jwtUtil.extractEmail(token);
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
    chain.doFilter(request, response);
}
```

## 토큰 설정

| 항목 | 값 |
|------|-----|
| 알고리즘 | HMAC-SHA256 (HS256) |
| 유효기간 | 24시간 (86,400,000ms) |
| Subject | 사용자 이메일 |
| Secret 출처 | `${JWT_SECRET}` 환경 변수 |
| 저장 위치 (클라이언트) | `localStorage` |

## 공개 엔드포인트 (인증 불필요)

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /actuator/health`
