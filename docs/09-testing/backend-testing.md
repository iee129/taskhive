# 백엔드 테스트

## 의존성 (pom.xml)

```xml
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

`@ExtendWith(MockitoExtension.class)`로 Spring 컨텍스트 없이 실행.
`AuthenticationManager`도 Mock으로 주입.

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    @Test void register_정상_저장후JWT반환() { ... }
    @Test void register_중복이메일_예외발생() { ... }
    @Test void login_정상_JWT반환() { ... }
    @Test void login_잘못된자격증명_예외발생() { ... }
    @Test void getMe_정상_사용자정보반환() { ... }
    @Test void getMe_없는이메일_예외발생() { ... }
}
```

총 **6개** 테스트.

## 통합 테스트 — AuthController

`@SpringBootTest` + `@AutoConfigureMockMvc`로 전체 Spring 컨텍스트 기동.
H2 인메모리 DB 사용 (`spring.profiles.active=test`).
테스트 격리는 각 테스트마다 고유 이메일 사용으로 처리 (`@DirtiesContext` 불필요).

```java
@SpringBootTest(properties = {"spring.profiles.active=test"})
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test void register_정상_200_token반환() { ... }
    @Test void register_중복이메일_400() { ... }
    @Test void register_빈이름_400() { ... }
    @Test void login_정상_200_token반환() { ... }
    @Test void login_잘못된비밀번호_401() { ... }
    @Test void me_JWT없음_401() { ... }
    @Test void me_변조JWT_401() { ... }
    @Test void me_유효한JWT_200_사용자정보반환() { ... }
}
```

총 **8개** 테스트.

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

taskhive:
  jwt:
    secret: test-secret-key-for-testing-only-must-be-at-least-32chars
    expiration-ms: 3600000
  cors:
    allowed-origins: http://localhost:5173
```

## 실행

```bash
# 전체 테스트
mvn test -f auth/pom.xml

# 단일 클래스
mvn test -Dtest=AuthServiceTest -f auth/pom.xml
mvn test -Dtest=AuthControllerTest -f auth/pom.xml
```

현재 총 **14개** 테스트 (AuthServiceTest 6 + AuthControllerTest 8).
