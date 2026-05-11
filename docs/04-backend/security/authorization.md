# 인가 (Authorization)

## SecurityConfig 필터 체인

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())           // JWT Stateless → CSRF 불필요
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/actuator/health").permitAll()
            .anyRequest().authenticated()       // 나머지 모두 JWT 필요
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

## 엔드포인트 권한 매트릭스

| 엔드포인트 | 메서드 | 인증 | 설명 |
|-----------|--------|------|------|
| `/api/auth/register` | POST | 불필요 | 회원가입 |
| `/api/auth/login` | POST | 불필요 | 로그인 |
| `/actuator/health` | GET | 불필요 | K8s Liveness Probe |
| `/api/tasks` | GET | JWT 필요 | 내 태스크 목록 |
| `/api/tasks` | POST | JWT 필요 | 태스크 생성 |
| `/api/tasks/{id}` | PUT | JWT 필요 | 태스크 수정 |
| `/api/tasks/{id}` | DELETE | JWT 필요 | 태스크 삭제 |

## CORS 설정

```java
@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(corsOrigins.split(",")));
    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

허용 Origin은 `${CORS_ORIGINS}` 환경 변수로 주입 (쉼표 구분):
- 개발: `http://localhost:3000`
- 프로덕션: `https://taskhive.example.com`

## 리소스 소유권 검증 (예정)

현재 JWT에서 추출한 이메일과 태스크 `assignee.email`을 비교하는 소유권 검증이 미구현.  
우선순위 높은 보안 개선 항목:

```java
// TaskService.updateTask() 예정 구현
String currentUserEmail = SecurityContextHolder.getContext()
    .getAuthentication().getName();
if (!task.getAssignee().getEmail().equals(currentUserEmail)) {
    throw new AccessDeniedException("이 태스크를 수정할 권한이 없습니다.");
}
```
