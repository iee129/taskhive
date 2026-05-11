# 백엔드 테스트

## 의존성 (pom.xml)

```xml
<!-- 테스트 스코프 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <!-- JUnit 5, Mockito, AssertJ, MockMvc 포함 -->
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

## 단위 테스트 — AuthService

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    @Test
    void 이메일_중복_시_예외_발생() {
        given(userRepository.existsByEmail("dup@example.com")).willReturn(true);

        assertThatThrownBy(() ->
            authService.register(new RegisterRequest("dup@example.com", "pass", "이름")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이미 사용 중인 이메일");
    }

    @Test
    void 회원가입_성공_시_JWT_반환() {
        given(userRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("$hashed");
        given(userRepository.save(any())).willReturn(new User());
        given(jwtUtil.generateToken(any())).willReturn("jwt-token");

        AuthResponse response = authService.register(
            new RegisterRequest("new@example.com", "password", "홍길동"));

        assertThat(response.token()).isEqualTo("jwt-token");
    }
}
```

## 통합 테스트 — AuthController

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setUp() { userRepository.deleteAll(); }

    @Test
    void 회원가입_후_로그인_성공() {
        // 회원가입
        RegisterRequest reg = new RegisterRequest("test@example.com", "pass1234", "테스터");
        ResponseEntity<AuthResponse> regRes =
            restTemplate.postForEntity("/api/auth/register", reg, AuthResponse.class);
        assertThat(regRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(regRes.getBody().token()).isNotBlank();

        // 로그인
        LoginRequest login = new LoginRequest("test@example.com", "pass1234");
        ResponseEntity<AuthResponse> loginRes =
            restTemplate.postForEntity("/api/auth/login", login, AuthResponse.class);
        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false

jwt:
  secret: test-secret-key-at-least-32-chars-long
  expiration: 3600000
```

## 실행

```bash
mvn test -f backend/pom.xml
mvn test -Dtest=AuthServiceTest -f backend/pom.xml   # 단일 테스트
```
