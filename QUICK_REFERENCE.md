# Test Analyst AI Assistant - Quick Reference Guide

## 🚀 Quick Start

```bash
# Clone and run
git clone <repo-url>
cd TestAnalyistAiAssisstant
mvn spring-boot:run

# Access application
http://localhost:8080
```

## 📋 API Endpoints Quick Reference

### Test Generation
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/generate-from-text` | Generate test conditions from text |
| `POST` | `/generate-from-image` | Generate test conditions from image |
| `POST` | `/generate` | Unified generation endpoint |
| `POST` | `/generate-automation` | Generate automation scripts |

### API Testing
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/generate-scenarios` | Generate API test scenarios |
| `POST` | `/api/run-scenario` | Execute test scenario |
| `GET` | `/api/scenario-code` | Get scenario code |
| `GET` | `/api/scenario-detail` | Get scenario details |
| `POST` | `/api/scenario-assert` | Set scenario assertion |

### Performance Testing
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/performance-test/generate-scenarios` | Generate performance test plan |
| `GET` | `/api/performance-test/scenario-types` | Get available scenario types |
| `GET` | `/api/performance-test/system-types` | Get supported system types |

### AI Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/ai` | AI console interface |
| `POST` | `/ai/ingest-text` | Ingest text document |
| `POST` | `/ai/ingest-file` | Ingest file document |
| `POST` | `/ai/connect-jira` | Connect Jira project |
| `POST` | `/ai/ask` | Ask AI question |

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/login` | Login page |
| `GET` | `/register` | Registration page |
| `POST` | `/register` | User registration |
| `GET` | `/api/health` | Health check |

## 🔧 Configuration Quick Reference

### AI Provider Configuration
```properties
# Choose AI provider
ai.provider=demo|ollama|openai|huggingface|gemini

# OpenAI (Paid)
openai.api.key=sk-your-key-here

# Ollama (Free - Local)
ollama.url=http://localhost:11434

# Hugging Face (Free tier)
huggingface.api.key=hf_your-token-here

# Google Gemini (Free tier)
gemini.api.key=your-gemini-key-here
```

### File Upload Limits
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Supported Image Formats
- JPEG/JPG
- PNG
- GIF
- BMP

## 📊 Data Models Quick Reference

### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    private Long id;                    // Primary key
    private String username;            // Unique username
    private String password;            // Encrypted password
    private boolean isFreeTrial;        // Free trial status
    private int remainingRequests;      // Remaining requests
}
```

### Performance Test Request
```java
public class PerformanceTestRequest {
    private int expectedUsers;                           // Concurrent users
    private List<String> commonOperations;               // Operations to test
    private Map<String, Integer> peakTimes;             // Peak usage times
    private String systemType;                           // System type
    private int dataVolume;                              // Data volume
    private String deploymentEnvironment;                // Environment
    private Map<String, Object> additionalRequirements; // Additional reqs
}
```

### Performance Test Plan
```java
public class PerformanceTestPlan {
    private String planId;                              // Unique identifier
    private String planName;                            // Plan name
    private String systemName;                          // System under test
    private List<PerformanceTestScenario> scenarios;    // Test scenarios
    private Map<String, Object> systemRequirements;     // System requirements
    private String testEnvironment;                     // Test environment
    private String executionStrategy;                   // Execution strategy
    private String estimatedDuration;                   // Estimated duration
    private String riskAssessment;                      // Risk assessment
}
```

## 🏗️ Architecture Quick Reference

### Package Structure
```
Madfoat.Learning/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── service/         # Business logic services
├── model/           # Entity classes
├── dto/             # Data transfer objects
├── repository/      # Data access layer
├── exception/       # Exception handling
└── util/            # Utility classes
```

### Key Services
- **AIService**: AI-powered content generation
- **ImageProcessingService**: Image processing and OCR
- **ApiScenarioService**: API test scenario management
- **PerformanceTestScenarioGenerator**: Performance test generation
- **KnowledgeBaseService**: Knowledge base management
- **UserService**: User management

### Design Patterns
- **MVC**: Model-View-Controller separation
- **Service Layer**: Business logic encapsulation
- **Repository**: Data access abstraction
- **Strategy**: AI provider selection
- **Factory**: Object creation patterns

## 🧪 Testing Quick Reference

### Unit Testing
```java
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock private DependencyService dependencyService;
    @InjectMocks private TestService testService;
    
    @Test
    void testMethod_WithValidInput_ReturnsExpectedResult() {
        // Given
        when(dependencyService.method()).thenReturn("expected");
        
        // When
        String result = testService.method();
        
        // Then
        assertThat(result).isEqualTo("expected");
    }
}
```

### Integration Testing
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"ai.provider=demo"})
class IntegrationTest {
    @Autowired private TestRestTemplate restTemplate;
    
    @Test
    void testEndpoint_EndToEnd_ReturnsValidResponse() {
        // Test implementation
    }
}
```

### Test Coverage Requirements
- **Unit tests**: 80% minimum coverage
- **Integration tests**: All major workflows
- **Performance tests**: Response time < 2 seconds

## 🚀 Deployment Quick Reference

### JAR Deployment
```bash
# Build
mvn clean package

# Run
java -jar target/TestAnalyistAiAssisstant-1.0-SNAPSHOT.jar

# With custom config
java -jar app.jar --spring.profiles.active=prod
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=testanalyst
DB_USERNAME=root
DB_PASSWORD=password

# AI Provider
AI_PROVIDER=ollama
OLLAMA_URL=http://ollama:11434

# Security
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin
```

## 🔍 Troubleshooting Quick Reference

### Common Issues

#### AI Service Not Working
```bash
# Check provider configuration
grep "ai.provider" application.properties

# Test Ollama connection
curl http://localhost:11434/api/tags

# Verify API keys
echo $OPENAI_API_KEY
```

#### Image Processing Errors
```bash
# Check Tesseract installation
tesseract --version

# Verify data path
ls -la /usr/share/tesseract-ocr/4.00/tessdata/
```

#### Database Issues
```bash
# Check database status
systemctl status mysql

# Test connection
mysql -u root -p -e "SELECT 1;"
```

### Debug Logging
```properties
# Enable debug logging
logging.level.Madfoat.Learning=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Performance Monitoring
```bash
# JVM tuning
java -Xmx2g -Xms1g -jar app.jar

# GC logging
java -XX:+PrintGCDetails -jar app.jar

# Monitor logs
tail -f logs/application.log
```

## 📚 Service Methods Quick Reference

### AIService
```java
// Core methods
generateTestConditions(String input, String inputType)
generateContent(String input, String inputType, String generationType, 
               boolean includeAcceptance, List<String> selectedTypes)
generateAutomationScripts(String description)
generateWithPrompt(String prompt)
```

### ImageProcessingService
```java
// Image processing methods
extractTextFromImage(MultipartFile imageFile)
isValidImageFile(MultipartFile file)
getImageAnalysisContext(MultipartFile imageFile)
```

### ApiScenarioService
```java
// API testing methods
generateScenarios(String curl, Integer limit, List<String> caseTypes)
runScenario(String id)
getScenarioCode(String id)
getScenarioDetail(String id)
setScenarioAssertion(String id, String expected)
setScenarioAssertions(String id, Map<String, Object> assertions, Integer expectedStatus)
```

### PerformanceTestScenarioGenerator
```java
// Performance testing methods
generateTestPlan(PerformanceTestRequest request)
```

### KnowledgeBaseService
```java
// Knowledge base methods
listDocuments()
ingestText(String title, String text)
ingestFile(MultipartFile file)
ingestJiraProject(String baseUrl, String email, String token, String projectKey, int maxIssues)
ingestSlackChannel(String token, String channel, int limit)
buildContextForQuestion(String question, int maxTokens)
```

## 🎯 Usage Examples Quick Reference

### Generate Test Cases
```bash
# From text
curl -X POST http://localhost:8080/generate-from-text \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "businessText=User login functionality"

# From image
curl -X POST http://localhost:8080/generate-from-image \
  -F "imageFile=@screenshot.png"
```

### Generate Performance Test Plan
```bash
curl -X POST http://localhost:8080/api/performance-test/generate-scenarios \
  -H "Content-Type: application/json" \
  -d '{
    "expectedUsers": 1000,
    "commonOperations": ["Login", "Search"],
    "systemType": "E_COMMERCE",
    "deploymentEnvironment": "PRODUCTION_LIKE"
  }'
```

### Generate API Test Scenarios
```bash
curl -X POST http://localhost:8080/api/generate-scenarios \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "curl=curl -X POST https://api.example.com/login -H 'Content-Type: application/json' -d '{\"username\":\"user\",\"password\":\"pass\"}'"
```

### Ask AI Question
```bash
curl -X POST http://localhost:8080/ai/ask \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "question=What are the performance requirements?"
```

## 🔐 Security Quick Reference

### Authentication
- Form-based login
- BCrypt password encoding
- Session management
- CSRF protection (configurable)

### Authorization
- Public endpoints: `/register`, `/login`, `/css/**`, `/js/**`, `/images/**`
- Protected endpoints: All other endpoints require authentication

### User Management
- Self-service registration
- Free trial with request limits
- User role management (extensible)

## 📊 Performance Quick Reference

### Test Types
- **STRESS**: Breakpoint identification
- **SOAK**: Long-term stability
- **VOLUME**: Large data handling
- **SPIKE**: Sudden load response
- **SCALABILITY**: Performance with resource addition

### System Types
- E_COMMERCE, BANKING, HEALTHCARE, EDUCATION
- GOVERNMENT, SOCIAL_MEDIA, GAMING, ENTERPRISE

### Environments
- DEVELOPMENT, STAGING, PRODUCTION_LIKE, PRODUCTION

### Monitoring Points
- Response time
- Throughput
- Resource utilization
- Error rates
- User experience metrics

## 🛠️ Development Quick Reference

### Code Standards
- Google Java Style Guide
- Meaningful naming conventions
- Comprehensive JavaDoc
- Method length < 50 lines
- Constructor injection preferred

### Git Workflow
```bash
# Create feature branch
git checkout -b feature/new-feature

# Commit changes
git commit -m "Add feature: description"

# Push and create PR
git push origin feature/new-feature
```

### Testing Requirements
- Unit tests: 80% coverage minimum
- Integration tests: All workflows
- Performance tests: < 2 second response
- Security tests: Authentication/authorization

---

## 📞 Support

- **Documentation**: Check API_DOCUMENTATION.md and DEVELOPER_GUIDE.md
- **Issues**: Create issue in repository
- **Logs**: Check application logs for detailed error information
- **Configuration**: Verify application.properties and environment variables

---

*This quick reference guide provides essential information for developers. For comprehensive details, refer to the full API documentation and developer guide.*