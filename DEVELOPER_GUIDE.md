# Test Analyst AI Assistant - Developer Guide

## Table of Contents
1. [Getting Started](#getting-started)
2. [Project Structure](#project-structure)
3. [Development Environment Setup](#development-environment-setup)
4. [Architecture Overview](#architecture-overview)
5. [Core Components](#core-components)
6. [Service Layer Implementation](#service-layer-implementation)
7. [Controller Layer](#controller-layer)
8. [Data Models](#data-models)
9. [Configuration Management](#configuration-management)
10. [Testing](#testing)
11. [Deployment](#deployment)
12. [Troubleshooting](#troubleshooting)
13. [Contributing](#contributing)

## Getting Started

### Prerequisites
- **Java 17 or higher** - Required for Spring Boot 3.x
- **Maven 3.6+** - Build tool and dependency management
- **IDE** - IntelliJ IDEA, Eclipse, or VS Code with Java extensions
- **Git** - Version control

### Quick Start
```bash
# Clone the repository
git clone <repository-url>
cd TestAnalyistAiAssisstant

# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run

# Access the application
open http://localhost:8080
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── Madfoat/
│   │       └── Learning/
│   │           ├── Main.java                 # Spring Boot entry point
│   │           ├── config/                   # Configuration classes
│   │           │   ├── SecurityConfig.java   # Security configuration
│   │           │   └── WebConfig.java        # Web configuration
│   │           ├── controller/               # REST controllers
│   │           │   ├── TestConditionController.java    # Main test generation
│   │           │   ├── AIModelController.java          # AI model management
│   │           │   ├── PerformanceTestController.java  # Performance testing
│   │           │   ├── AuthController.java             # Authentication
│   │           │   └── WebController.java              # Web pages
│   │           ├── service/                  # Business logic services
│   │           │   ├── AIService.java                  # AI integration
│   │           │   ├── ImageProcessingService.java     # Image processing
│   │           │   ├── ApiScenarioService.java         # API testing
│   │           │   ├── PerformanceTestScenarioGenerator.java # Performance testing
│   │           │   ├── KnowledgeBaseService.java       # Knowledge management
│   │           │   └── UserService.java                # User management
│   │           ├── model/                    # Entity classes
│   │           │   └── User.java                       # User entity
│   │           ├── dto/                      # Data transfer objects
│   │           │   ├── GenerateRequest.java            # Generation requests
│   │           │   ├── PerformanceTestRequest.java     # Performance test requests
│   │           │   ├── PerformanceTestPlan.java        # Performance test plans
│   │           │   ├── PerformanceTestScenario.java    # Test scenarios
│   │           │   └── RowQuestion.java                # Row questions
│   │           ├── repository/               # Data access layer
│   │           │   └── UserRepository.java             # User repository
│   │           ├── exception/                # Exception handling
│   │           │   └── GlobalExceptionHandler.java     # Global exception handler
│   │           └── util/                     # Utility classes
│   │               └── RestAssuredAssertionUtil.java   # Testing utilities
│   └── resources/
│       ├── application.properties            # Application configuration
│       └── templates/                        # Thymeleaf templates
│           ├── index.html                    # Main interface
│           ├── results.html                  # Results display
│           ├── performance-test.html         # Performance testing
│           ├── ai-model.html                 # AI management
│           ├── api-scenarios.html            # API scenarios
│           ├── automation.html               # Automation scripts
│           ├── login.html                    # Login page
│           └── register.html                 # Registration page
```

## Development Environment Setup

### 1. IDE Configuration

#### IntelliJ IDEA
1. Open the project as a Maven project
2. Ensure Java 17+ is configured as project SDK
3. Enable annotation processing
4. Configure Spring Boot run configuration

#### Eclipse
1. Import as Maven project
2. Install Spring Tools Suite (STS) plugin
3. Configure Java 17+ as project JRE
4. Enable Spring Boot support

#### VS Code
1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Install Maven for Java extension

### 2. Maven Configuration

The project uses Maven for dependency management. Key dependencies include:

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Thymeleaf Template Engine -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    
    <!-- Tesseract OCR -->
    <dependency>
        <groupId>net.sourceforge.tess4j</groupId>
        <artifactId>tess4j</artifactId>
        <version>5.8.0</version>
    </dependency>
    
    <!-- WebClient for HTTP requests -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
</dependencies>
```

### 3. Database Configuration

The application supports multiple database options:

#### H2 (Development)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

#### MySQL (Production)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/testanalyst
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

#### PostgreSQL (Production)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/testanalyst
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## Architecture Overview

### 1. Layered Architecture

The application follows a standard layered architecture:

```
┌─────────────────────────────────────┐
│           Presentation Layer        │
│        (Controllers + Views)        │
├─────────────────────────────────────┤
│            Business Layer           │
│            (Services)               │
├─────────────────────────────────────┤
│            Data Access Layer       │
│          (Repositories)            │
├─────────────────────────────────────┤
│            Data Layer               │
│         (Database + External APIs) │
└─────────────────────────────────────┘
```

### 2. Design Patterns

- **MVC Pattern**: Separation of concerns between Model, View, and Controller
- **Service Layer Pattern**: Business logic encapsulation
- **Repository Pattern**: Data access abstraction
- **Strategy Pattern**: AI provider selection
- **Factory Pattern**: Object creation for different scenarios

### 3. Dependency Injection

The application uses Spring's dependency injection container:

```java
@Service
public class AIService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Autowired
    public AIService(UserService userService) {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
        this.userService = userService;
    }
}
```

## Core Components

### 1. Main Application Class

```java
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```

**Key Features:**
- Auto-configuration of Spring Boot
- Component scanning
- Embedded server configuration

### 2. Configuration Classes

#### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }
}
```

#### Web Configuration
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));
        return factory.createMultipartConfig();
    }
}
```

## Service Layer Implementation

### 1. AIService

The core service for AI-powered content generation:

```java
@Service
public class AIService {
    
    @Value("${ai.provider:demo}")
    private String aiProvider;
    
    public String generateTestConditions(String input, String inputType) {
        if (!deductRequest()) {
            return "You have exceeded your free trial limit.";
        }
        
        switch (aiProvider.toLowerCase()) {
            case "openai":
                return generateWithOpenAI(input, inputType);
            case "ollama":
                return generateWithOllama(input, inputType);
            case "huggingface":
                return generateWithHuggingFace(input, inputType);
            case "gemini":
                return generateWithGemini(input, inputType);
            case "demo":
            default:
                return generateDemoTestConditions(input, inputType);
        }
    }
    
    private String generateWithOpenAI(String input, String inputType) {
        // OpenAI API integration
        String prompt = buildPrompt(input, inputType);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", "You are an expert software test analyst..."),
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", 2000);
        requestBody.put("temperature", 0.7);
        
        // WebClient implementation
        Mono<String> response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
        
        String responseBody = response.block();
        return extractOpenAIContent(responseBody);
    }
}
```

### 2. ImageProcessingService

Service for processing images using OCR:

```java
@Service
public class ImageProcessingService {
    
    private final ITesseract tesseract;
    
    public ImageProcessingService() {
        this.tesseract = new Tesseract();
        // Configure Tesseract data path if needed
        // tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
    }
    
    public String extractTextFromImage(MultipartFile imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("Invalid image file");
            }
            
            String extractedText = tesseract.doOCR(image);
            
            if (extractedText == null || extractedText.trim().isEmpty()) {
                return "No text could be extracted from the image.";
            }
            
            return extractedText.trim();
            
        } catch (Exception e) {
            return "Error processing image: " + e.getMessage();
        }
    }
    
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif") ||
            contentType.equals("image/bmp")
        );
    }
}
```

### 3. PerformanceTestScenarioGenerator

Service for generating performance test plans:

```java
@Service
public class PerformanceTestScenarioGenerator {
    
    public PerformanceTestPlan generateTestPlan(PerformanceTestRequest request) {
        PerformanceTestPlan plan = new PerformanceTestPlan();
        plan.setPlanId("perf_" + System.currentTimeMillis());
        plan.setPlanName(request.getSystemType() + " Performance Test Plan");
        plan.setSystemName(request.getSystemType());
        plan.setCreatedDate(LocalDateTime.now());
        plan.setCreatedBy("system");
        
        List<PerformanceTestScenario> scenarios = new ArrayList<>();
        
        // Generate different types of scenarios
        scenarios.add(generateStressTestScenario(request));
        scenarios.add(generateSoakTestScenario(request));
        scenarios.add(generateVolumeTestScenario(request));
        scenarios.add(generateSpikeTestScenario(request));
        scenarios.add(generateScalabilityTestScenario(request));
        
        plan.setScenarios(scenarios);
        
        // Set system requirements and other metadata
        plan.setSystemRequirements(buildSystemRequirements(request));
        plan.setTestEnvironment(request.getDeploymentEnvironment());
        plan.setExecutionStrategy("Phased approach with monitoring");
        plan.setPrerequisites(buildPrerequisites(request));
        plan.setTestDataRequirements(buildTestDataRequirements(request));
        plan.setEstimatedDuration("4 hours");
        plan.setRiskAssessment("Medium - Monitor system resources");
        
        return plan;
    }
    
    private PerformanceTestScenario generateStressTestScenario(PerformanceTestRequest request) {
        PerformanceTestScenario scenario = new PerformanceTestScenario();
        scenario.setScenarioId("stress_" + System.currentTimeMillis());
        scenario.setScenarioName("Stress Test - Breakpoint Identification");
        scenario.setScenarioType("STRESS");
        scenario.setDescription("Identify the maximum load the system can handle before performance degrades");
        scenario.setTargetUsers((int) (request.getExpectedUsers() * 1.5));
        scenario.setRampUpTime(300); // 5 minutes
        scenario.setTestDuration(1800); // 30 minutes
        scenario.setPriority(1);
        
        // Set test steps and success criteria
        scenario.setTestSteps(Arrays.asList(
            "Ramp up to " + scenario.getTargetUsers() + " users over " + scenario.getRampUpTime() + " seconds",
            "Maintain load for " + scenario.getTestDuration() + " seconds",
            "Monitor system performance metrics",
            "Identify performance degradation points"
        ));
        
        scenario.setSuccessCriteria(Arrays.asList(
            "System remains stable under expected load",
            "Response time stays within acceptable limits",
            "No memory leaks or resource exhaustion",
            "Clear identification of performance bottlenecks"
        ));
        
        return scenario;
    }
}
```

## Controller Layer

### 1. TestConditionController

Main controller for test condition generation:

```java
@Controller
public class TestConditionController {
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Autowired
    private ApiScenarioService apiScenarioService;
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @PostMapping("/generate-from-text")
    public String generateFromText(@RequestParam("businessText") String businessText, Model model) {
        if (businessText == null || businessText.trim().isEmpty()) {
            model.addAttribute("error", "Please provide business text or requirements");
            return "index";
        }
        
        try {
            String testConditions = aiService.generateTestConditions(businessText, "text");
            model.addAttribute("input", businessText);
            model.addAttribute("inputType", "Business Text");
            model.addAttribute("testConditions", testConditions);
            return "results";
        } catch (Exception e) {
            model.addAttribute("error", "Error generating test conditions: " + e.getMessage());
            return "index";
        }
    }
    
    @PostMapping("/generate")
    public String generateUnified(@RequestParam("generationTypes") List<String> generationTypes,
                                  @RequestParam(value = "includeAcceptance", required = false, defaultValue = "false") boolean includeAcceptance,
                                  @RequestParam(value = "selectedTypes", required = false) List<String> selectedTypes,
                                  @RequestParam(value = "businessText", required = false) String businessText,
                                  @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                  Model model) {
        try {
            // Determine input type and content
            String input; String inputType;
            if (businessText != null && !businessText.trim().isEmpty()) {
                input = businessText; inputType = "text";
            } else if (imageFile != null && !imageFile.isEmpty()) {
                input = imageProcessingService.getImageAnalysisContext(imageFile); inputType = "image";
            } else {
                model.addAttribute("error", "Please provide text or an image/document");
                return "index";
            }
            
            // Generate content for the first type
            String firstType = generationTypes == null || generationTypes.isEmpty() ? "test_cases" : generationTypes.get(0);
            String content = aiService.generateContent(input, inputType, firstType, includeAcceptance, selectedTypes);
            
            // Set model attributes
            model.addAttribute("input", input);
            model.addAttribute("inputType", "text".equals(inputType) ? "Business Text" : "Image Analysis");
            model.addAttribute("testConditions", content);
            model.addAttribute("generationType", firstType);
            if (imageFile != null && !imageFile.isEmpty()) {
                model.addAttribute("fileName", imageFile.getOriginalFilename());
            }
            return "results";
        } catch (Exception e) {
            model.addAttribute("error", "Error generating: " + e.getMessage());
            return "index";
        }
    }
}
```

### 2. PerformanceTestController

REST controller for performance testing:

```java
@RestController
@RequestMapping("/api/performance-test")
@CrossOrigin(origins = "*")
public class PerformanceTestController {
    
    @Autowired
    private PerformanceTestScenarioGenerator scenarioGenerator;
    
    @PostMapping("/generate-scenarios")
    public ResponseEntity<PerformanceTestPlan> generateTestScenarios(@RequestBody PerformanceTestRequest request) {
        try {
            PerformanceTestPlan plan = scenarioGenerator.generateTestPlan(request);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            throw new RuntimeException("Error generating test scenarios: " + e.getMessage());
        }
    }
    
    @GetMapping("/scenario-types")
    public ResponseEntity<Map<String, Object>> getScenarioTypes() {
        Map<String, Object> response = new HashMap<>();
        response.put("scenarioTypes", Map.of(
            "STRESS", "Stress Test - Breakpoint Identification",
            "SOAK", "Soak Test - Long-term Stability",
            "VOLUME", "Volume Test - Large Data Handling",
            "SPIKE", "Spike Test - Sudden Load Response",
            "SCALABILITY", "Scalability Test - Performance with Resource Addition"
        ));
        response.put("commonOperations", Map.of(
            "Login", "Login and Authentication Operations",
            "Search", "Search and Query Operations",
            "Add Product", "Data Addition and Modification Operations",
            "View Page", "Page and Interface Viewing Operations",
            "File Upload", "File Upload and Download Operations"
        ));
        return ResponseEntity.ok(response);
    }
}
```

## Data Models

### 1. User Entity

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    private boolean isFreeTrial;
    private int remainingRequests;
    
    // Constructors
    public User() {}
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.isFreeTrial = true;
        this.remainingRequests = 100;
    }
    
    // Getters and Setters
    // ... (standard getters and setters)
}
```

### 2. DTOs

#### GenerateRequest
```java
public class GenerateRequest {
    private String input;
    private String inputType;
    private String generationType;
    private boolean includeAcceptance;
    private List<String> selectedTypes;
    
    // Getters and Setters
    // ... (standard getters and setters)
}
```

#### PerformanceTestRequest
```java
public class PerformanceTestRequest {
    private int expectedUsers;
    private List<String> commonOperations;
    private Map<String, Integer> peakTimes;
    private String systemType;
    private int dataVolume;
    private String deploymentEnvironment;
    private Map<String, Object> additionalRequirements;
    
    // Constructors
    public PerformanceTestRequest(int expectedUsers, List<String> commonOperations,
                                 Map<String, Integer> peakTimes, String systemType,
                                 int dataVolume, String deploymentEnvironment,
                                 Map<String, Object> additionalRequirements) {
        this.expectedUsers = expectedUsers;
        this.commonOperations = commonOperations;
        this.peakTimes = peakTimes;
        this.systemType = systemType;
        this.dataVolume = dataVolume;
        this.deploymentEnvironment = deploymentEnvironment;
        this.additionalRequirements = additionalRequirements;
    }
    
    // Getters and Setters
    // ... (standard getters and setters)
}
```

## Configuration Management

### 1. Application Properties

```properties
# Application Configuration
spring.application.name=Test Analyst AI Assistant
server.port=8080

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true

# AI Provider Configuration
ai.provider=gemini

# OpenAI Configuration
openai.api.key=your-api-key-here

# Ollama Configuration
ollama.url=http://localhost:11434

# Hugging Face Configuration
huggingface.api.key=your-hf-key-here

# Google Gemini Configuration
gemini.api.key=your-gemini-key-here

# Thymeleaf Configuration
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Logging Configuration
logging.level.Madfoat.Learning=DEBUG
logging.level.org.springframework.web=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Error Handling
server.error.whitelabel.enabled=false
server.error.include-message=always
server.error.include-binding-errors=always

# OCR Configuration
tesseract.data.path=/usr/share/tesseract-ocr/4.00/tessdata
tesseract.language=eng
```

### 2. Environment-Specific Configuration

Create environment-specific property files:

#### application-dev.properties
```properties
# Development Configuration
logging.level.Madfoat.Learning=DEBUG
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=create-drop
```

#### application-prod.properties
```properties
# Production Configuration
logging.level.Madfoat.Learning=WARN
spring.jpa.hibernate.ddl-auto=validate
spring.thymeleaf.cache=true
```

### 3. Configuration Classes

```java
@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AIConfiguration {
    private String provider = "demo";
    private String openaiApiKey;
    private String huggingfaceApiKey;
    private String geminiApiKey;
    private String ollamaUrl = "http://localhost:11434";
}
```

## Testing

### 1. Unit Testing

#### Service Layer Tests
```java
@ExtendWith(MockitoExtension.class)
class AIServiceTest {
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private AIService aiService;
    
    @Test
    void generateTestConditions_WithValidInput_ReturnsTestConditions() {
        // Given
        String input = "User login functionality";
        String inputType = "text";
        
        // When
        String result = aiService.generateTestConditions(input, inputType);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("Functional Test Conditions");
    }
}
```

#### Controller Tests
```java
@WebMvcTest(TestConditionController.class)
class TestConditionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AIService aiService;
    
    @Test
    void generateFromText_WithValidInput_ReturnsResultsPage() throws Exception {
        // Given
        String businessText = "User login functionality";
        when(aiService.generateTestConditions(businessText, "text"))
            .thenReturn("Generated test conditions");
        
        // When & Then
        mockMvc.perform(post("/generate-from-text")
                .param("businessText", businessText))
                .andExpect(status().isOk())
                .andExpect(view().name("results"))
                .andExpect(model().attribute("testConditions", "Generated test conditions"));
    }
}
```

### 2. Integration Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "ai.provider=demo",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TestConditionGenerationIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void generateTestConditions_EndToEnd_ReturnsValidResponse() {
        // Given
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("businessText", "User login functionality");
        
        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/generate-from-text", request, String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Functional Test Conditions");
    }
}
```

### 3. Test Data Management

```java
@TestConfiguration
public class TestDataConfiguration {
    
    @Bean
    public User testUser() {
        User user = new User("testuser", "password");
        user.setId(1L);
        user.setFreeTrial(true);
        user.setRemainingRequests(100);
        return user;
    }
    
    @Bean
    public PerformanceTestRequest testPerformanceRequest() {
        return new PerformanceTestRequest(
            1000,
            Arrays.asList("Login", "Search"),
            Map.of("morning", 300, "afternoon", 500),
            "E_COMMERCE",
            10000,
            "PRODUCTION_LIKE",
            Map.of("responseTime", "< 2 seconds")
        );
    }
}
```

## Deployment

### 1. JAR Packaging

```bash
# Build executable JAR
mvn clean package

# Run the JAR
java -jar target/TestAnalyistAiAssisstant-1.0-SNAPSHOT.jar
```

### 2. Docker Deployment

#### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/TestAnalyistAiAssisstant-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Docker Compose
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - AI_PROVIDER=ollama
    depends_on:
      - ollama
      - mysql
  
  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
  
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: testanalyst
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  ollama_data:
  mysql_data:
```

### 3. Production Configuration

#### application-prod.properties
```properties
# Production Configuration
server.port=8080
spring.profiles.active=prod

# Database Configuration
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:testanalyst}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:password}

# AI Provider Configuration
ai.provider=${AI_PROVIDER:ollama}
ollama.url=${OLLAMA_URL:http://ollama:11434}

# Security Configuration
spring.security.user.name=${ADMIN_USERNAME:admin}
spring.security.user.password=${ADMIN_PASSWORD:admin}

# Logging Configuration
logging.level.root=WARN
logging.level.Madfoat.Learning=INFO
logging.file.name=logs/application.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Performance Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

## Troubleshooting

### 1. Common Issues

#### AI Service Errors
```bash
# Check AI provider configuration
grep "ai.provider" src/main/resources/application.properties

# Verify API keys
echo $OPENAI_API_KEY
echo $GEMINI_API_KEY

# Test Ollama connection
curl http://localhost:11434/api/tags
```

#### Image Processing Errors
```bash
# Check Tesseract installation
tesseract --version

# Verify Tesseract data path
ls -la /usr/share/tesseract-ocr/4.00/tessdata/

# Test OCR functionality
tesseract test-image.png stdout
```

#### Database Connection Issues
```bash
# Check database status
systemctl status mysql
systemctl status postgresql

# Test database connection
mysql -u root -p -e "SELECT 1;"
psql -U postgres -c "SELECT 1;"
```

### 2. Logging and Debugging

#### Enable Debug Logging
```properties
# application.properties
logging.level.Madfoat.Learning=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.security=DEBUG
```

#### Log Analysis
```bash
# View application logs
tail -f logs/application.log

# Search for errors
grep "ERROR" logs/application.log

# Monitor specific components
grep "AIService" logs/application.log
```

### 3. Performance Issues

#### JVM Tuning
```bash
# Increase heap size
java -Xmx2g -Xms1g -jar app.jar

# Enable GC logging
java -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -jar app.jar

# Profile with JProfiler
java -agentpath:/path/to/jprofiler/bin/linux-x64/libjprofilerti.so=port=8849 -jar app.jar
```

#### Database Performance
```sql
-- Check slow queries
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- Analyze table performance
ANALYZE TABLE users;
SHOW INDEX FROM users;
```

## Contributing

### 1. Development Workflow

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/new-feature
   ```
3. **Make your changes**
4. **Write tests**
5. **Commit your changes**
   ```bash
   git commit -m "Add new feature: description"
   ```
6. **Push to your fork**
   ```bash
   git push origin feature/new-feature
   ```
7. **Create a pull request**

### 2. Code Standards

#### Java Code Style
- Follow Google Java Style Guide
- Use meaningful variable and method names
- Add comprehensive JavaDoc for public methods
- Keep methods under 50 lines
- Use meaningful exception messages

#### Spring Boot Best Practices
- Use constructor injection over field injection
- Implement proper exception handling
- Use configuration properties for externalized configuration
- Follow REST API design principles
- Implement proper validation

### 3. Testing Requirements

- **Unit tests**: Minimum 80% code coverage
- **Integration tests**: Test all major workflows
- **Performance tests**: Ensure response times under 2 seconds
- **Security tests**: Verify authentication and authorization

### 4. Documentation

- Update API documentation for new endpoints
- Add examples for new features
- Update configuration documentation
- Maintain troubleshooting guides

---

*This developer guide provides comprehensive information for developers working with the Test Analyst AI Assistant application. For additional information, refer to the API documentation and project README.*