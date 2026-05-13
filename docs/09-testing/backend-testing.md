# 백엔드 테스트

## 의존성 (pom.xml)

```xml
<!-- 기본 테스트 (Spring Boot Starter) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <!-- JUnit 5, Mockito, AssertJ, MockMvc 포함 -->
</dependency>

<!-- H2 인메모리 DB (단위·슬라이스 테스트용) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Testcontainers (통합 테스트 — Docker 필요) -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

## JaCoCo 커버리지 게이트

`mvn verify` 실행 시 임계값 미달이면 빌드 실패:

| 지표 | 임계값 |
|------|--------|
| 라인 커버리지 | ≥ 80% |
| 브랜치 커버리지 | ≥ 70% |

제외 대상: `dto/**`, `model/**`, `aspect/**`, `filter/**`, `config/SecurityConfig`, `config/JwtFilter`

```bash
mvn verify                    # 전체 빌드 + 커버리지 체크
mvn verify -f auth/pom.xml    # 경로 지정
```

리포트 위치: `auth/target/site/jacoco/index.html`

---

## 단위 테스트

`@ExtendWith(MockitoExtension.class)` — Spring 컨텍스트 없이 실행, 빠름.

### 테스트 클래스별 커버 범위

| 클래스 | 테스트 수 | 주요 시나리오 |
|--------|-----------|--------------|
| `AuthServiceTest` | 6 | register(정상/중복), login(정상/실패), getMe(정상/없음) |
| `RefreshTokenServiceTest` | 8 | issue, rotate, invalidate, 만료 토큰 |
| `TaskServiceTest` | 17 | CRUD 전체, 필터, 기본값(TODO/MEDIUM), 프로젝트·담당자 지정 |
| `CommentServiceTest` | 9 | 조회/등록/삭제, 태스크 없음, 작성자 아님 FORBIDDEN |
| `StatsServiceTest` | 5 | overdue 계산(dueDate null, DONE 제외), 집계 카운트 |
| `ProjectServiceTest` | 13 | CRUD 전체, 소유자 검증 FORBIDDEN |
| `AiServiceTest` | 5 | Ollama 성공 파싱, 연결 실패 fallback, 긴 설명 50자 자름 |
| `UserDetailsServiceImplTest` | 3 | USER/ADMIN 역할 권한, 없는 사용자 예외 |
| `JwtUtilTest` | 8 | 토큰 생성/추출, 유효성 검사(valid/invalid/만료) |

**총 단위 테스트: 74개**

### 예시 — TaskServiceTest

```java
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;

    @InjectMocks TaskService taskService;

    @Test
    void createTask_기본값_TODO_MEDIUM() {
        TaskRequest req = new TaskRequest();
        req.setTitle("새 태스크");

        Task saved = Task.builder().title("새 태스크")
                .status(Task.Status.TODO).priority(Task.Priority.MEDIUM).build();
        when(taskRepository.save(any())).thenReturn(saved);

        TaskResponse result = taskService.createTask(req);

        assertThat(result.getStatus()).isEqualTo(Task.Status.TODO);
        assertThat(result.getPriority()).isEqualTo(Task.Priority.MEDIUM);
    }

    @Test
    void deleteTask_정상삭제_deletedAt설정() {
        Task task = Task.builder().title("삭제할").build();
        when(taskRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        assertThat(task.getDeletedAt()).isNotNull();
    }
}
```

### 예시 — JwtUtilTest (@Spy 없이 직접 생성)

```java
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "test-secret-key-for-testing-only-must-be-at-least-32chars";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 3_600_000L);
    }

    @Test
    void isValid_만료토큰_false() {
        JwtUtil shortLived = new JwtUtil(SECRET, 1L);
        String token = shortLived.generateToken("user@test.com");
        Thread.sleep(10);
        assertThat(shortLived.isValid(token)).isFalse();
    }
}
```

---

## 리포지토리 슬라이스 테스트

`@DataJpaTest` + H2 — Spring Data JPA 레이어만 로드, DB 실제 쿼리 검증.

| 클래스 | 테스트 수 | 주요 검증 |
|--------|-----------|-----------|
| `TaskRepositoryTest` | 12 | findAllByDeletedAtIsNull, findFiltered(상태/우선순위/키워드/복합), count 메서드 |

```java
@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired TaskRepository taskRepository;

    @Test
    void findFiltered_키워드검색() {
        saveTask("Spring Boot 프로젝트", TODO, MEDIUM, false);
        saveTask("React 작업", TODO, MEDIUM, false);

        List<Task> result = taskRepository.findFiltered(null, null, "spring");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).containsIgnoringCase("spring");
    }
}
```

---

## 통합 테스트 (Testcontainers)

`TestcontainersConfig`를 상속해 실제 PostgreSQL 컨테이너에서 전체 Spring Context 실행.

**Docker 없으면 자동 skip** (`@Testcontainers(disabledWithoutDocker = true)`).

### TestcontainersConfig

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("tc")
public abstract class TestcontainersConfig {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
}
```

### application-tc.yml

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
taskhive:
  jwt:
    secret: test-secret-key-for-testing-only-must-be-at-least-32chars
    expiration-ms: 900000
    refresh-expiration-ms: 604800000
```

### AuthIntegrationTest (6개)

```java
class AuthIntegrationTest extends TestcontainersConfig {

    @Autowired TestRestTemplate restTemplate;

    @Test
    void register_정상등록_토큰반환() {
        RegisterRequest req = ...;
        ResponseEntity<AuthResponse> response =
            restTemplate.postForEntity("/api/auth/register", req, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getToken()).isNotBlank();
    }
}
```

### TaskIntegrationTest (7개)

CRUD 전체 플로우 + 필터 + 인증 없음 401 검증.

---

## MockMvc 컨트롤러 테스트

`@SpringBootTest` + `@AutoConfigureMockMvc` + H2 프로파일.

| 클래스 | 테스트 수 |
|--------|-----------|
| `AuthControllerTest` | 8 |
| `AuthRefreshControllerTest` | 9 |
| `AdminControllerTest` | 2 |

---

## 전체 테스트 현황

```
mvn verify 결과 (2026-05-12):
  통과: 105개
  Skip: 13개 (Testcontainers — Docker 없음)
  실패:  0개
  JaCoCo LINE:   ≥ 80% ✅
  JaCoCo BRANCH: ≥ 70% ✅
```

## 실행 명령

```bash
# 전체 테스트
mvn test -f auth/pom.xml

# 커버리지 포함 빌드 게이트
mvn verify -f auth/pom.xml

# 특정 클래스
mvn test -Dtest=TaskServiceTest -f auth/pom.xml
mvn test -Dtest=TaskRepositoryTest -f auth/pom.xml

# 통합 테스트만 (Docker 필요)
mvn test -Dtest="AuthIntegrationTest,TaskIntegrationTest" -f auth/pom.xml
```
