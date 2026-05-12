# 인가 (Authorization)

## SecurityConfig 필터 체인

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"error\": \"인증이 필요합니다\"}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"error\": \"접근 권한이 없습니다\"}");
                }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/logout",
                    "/actuator/health"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

## 엔드포인트 권한 매트릭스

| 엔드포인트 | 메서드 | 인증 | 권한 | 설명 |
|-----------|--------|------|------|------|
| `/api/auth/register` | POST | 불필요 | — | 회원가입 |
| `/api/auth/login` | POST | 불필요 | — | 로그인 |
| `/api/auth/refresh` | POST | 불필요 | — | Access Token 재발급 |
| `/api/auth/logout` | POST | 불필요 | — | Refresh Token 무효화 |
| `/api/auth/me` | GET | JWT 필요 | USER+ | 내 정보 조회 |
| `/api/auth/password` | PUT | JWT 필요 | USER+ | 비밀번호 변경 |
| `/actuator/health` | GET | 불필요 | — | 헬스체크 |
| `/api/tasks` | GET | JWT 필요 | USER+ | 내 태스크 목록 |
| `/api/tasks` | POST | JWT 필요 | USER+ | 태스크 생성 |
| `/api/tasks/{id}` | PUT | JWT 필요 | USER+ | 태스크 수정 |
| `/api/tasks/{id}` | DELETE | JWT 필요 | USER+ | 태스크 삭제 |
| `/api/admin/health` | GET | JWT 필요 | **ADMIN** | 관리자 헬스체크 |

## Role 기반 접근 제어

`@EnableMethodSecurity` 활성화로 메서드 수준 권한 제어 가능:

```java
@GetMapping("/health")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> health() { ... }
```

`UserDetailsServiceImpl`이 `user.getRole().name()`을 `.roles()`에 주입하여
`ROLE_ADMIN` / `ROLE_USER` 권한을 자동 부여합니다.

## CORS 설정

```java
@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
    config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);  // HttpOnly 쿠키 전송 필수

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

허용 Origin은 `${CORS_ORIGINS}` 환경 변수로 주입 (쉼표 구분):
- 개발: `http://localhost:5173`
- 프로덕션: `https://taskhive.example.com`

## 리소스 소유권 검증 (예정)

Task 수정·삭제 전 `assignee.email == jwtEmail` 검증 미구현 (Phase 5 예정):

```java
String currentUserEmail = SecurityContextHolder.getContext()
    .getAuthentication().getName();
if (!task.getAssignee().getEmail().equals(currentUserEmail)) {
    throw new AccessDeniedException("이 태스크를 수정할 권한이 없습니다.");
}
```
