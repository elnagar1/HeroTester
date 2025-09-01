# Test Analyst AI Assistant - Comprehensive API Documentation

## Table of Contents
1. [Overview](#overview)
2. [REST API Endpoints](#rest-api-endpoints)
3. [Service Layer APIs](#service-layer-apis)
4. [Data Transfer Objects (DTOs)](#data-transfer-objects-dtos)
5. [Model Classes](#model-classes)
6. [Configuration](#configuration)
7. [Exception Handling](#exception-handling)
8. [Utility Classes](#utility-classes)
9. [Frontend Templates](#frontend-templates)
10. [Usage Examples](#usage-examples)
11. [Configuration Options](#configuration-options)

## Overview

The Test Analyst AI Assistant is a Spring Boot web application that generates comprehensive software test conditions from business text or images using various AI providers. The application provides both web-based UI and REST API endpoints for test generation, performance testing, and AI-powered analysis.

**Technology Stack:**
- Java 17+
- Spring Boot 3.2.0
- Spring Security
- Thymeleaf (Template Engine)
- Tesseract OCR (Image Processing)
- Multiple AI Providers (OpenAI, Ollama, Hugging Face, Google Gemini)

## REST API Endpoints

### 1. Test Condition Generation

#### Generate Test Conditions from Text
```http
POST /generate-from-text
Content-Type: application/x-www-form-urlencoded

businessText=User login functionality with username and password
```

**Parameters:**
- `businessText` (required): Business requirements or feature description

**Response:** HTML page with generated test conditions

#### Generate Test Conditions from Image
```http
POST /generate-from-image
Content-Type: multipart/form-data

imageFile: [binary image file]
```

**Parameters:**
- `imageFile` (required): Image file (JPEG, PNG, GIF, BMP)

**Response:** HTML page with extracted text and generated test conditions

#### Unified Generation Endpoint
```http
POST /generate
Content-Type: multipart/form-data

generationTypes: test_cases,acceptance_criteria,user_stories
includeAcceptance: true
selectedTypes: functional,non-functional,security
businessText: User authentication system
imageFile: [optional binary file]
```

**Parameters:**
- `generationTypes` (required): List of generation types
- `includeAcceptance` (optional): Include acceptance criteria (default: false)
- `selectedTypes` (optional): Specific test types to include
- `businessText` (optional): Text input (preferred over image)
- `imageFile` (optional): Image file for analysis

**Response:** HTML page with generated content

#### Generate Automation Scripts
```http
POST /generate-automation
Content-Type: application/x-www-form-urlencoded

scenarioText=User login with valid credentials
```

**Parameters:**
- `scenarioText` (required): Test scenario description

**Response:** HTML page with generated automation code

### 2. API Testing Scenarios

#### Generate API Test Scenarios
```http
POST /api/generate-scenarios
Content-Type: application/x-www-form-urlencoded

curl=curl -X POST https://api.example.com/login -H "Content-Type: application/json" -d '{"username":"user","password":"pass"}'
limit: 10
caseTypes: positive,negative,boundary
```

**Parameters:**
- `curl` (required): cURL command to analyze
- `limit` (optional): Maximum number of scenarios (default: unlimited)
- `caseTypes` (optional): Types of test cases to generate

**Response:** HTML page with generated API test scenarios

#### Run API Test Scenario
```http
POST /api/run-scenario
Content-Type: application/x-www-form-urlencoded

id: scenario_123
```

**Parameters:**
- `id` (required): Scenario identifier

**Response:** JSON with execution results
```json
{
  "status": "success",
  "responseTime": 245,
  "statusCode": 200,
  "body": "..."
}
```

#### Get Scenario Code
```http
GET /api/scenario-code?id=scenario_123
```

**Response:** JSON with scenario code
```json
{
  "code": "// Generated test code...",
  "language": "java"
}
```

#### Get Scenario Details
```http
GET /api/scenario-detail?id=scenario_123
```

**Response:** JSON with complete scenario information

#### Set Scenario Assertions
```http
POST /api/scenario-assert
Content-Type: application/x-www-form-urlencoded

id: scenario_123
expected: 200
```

**Parameters:**
- `id` (required): Scenario identifier
- `expected` (optional): Expected status code

#### Set Multiple Assertions
```http
POST /api/scenario-assertions
Content-Type: application/json

{
  "id": "scenario_123",
  "expectedStatus": 200,
  "assertions": {
    "responseTime": "< 1000",
    "contentType": "application/json",
    "bodyContains": "success"
  }
}
```

### 3. Performance Testing

#### Generate Performance Test Scenarios
```http
POST /api/performance-test/generate-scenarios
Content-Type: application/json

{
  "expectedUsers": 1000,
  "commonOperations": ["Login", "Search", "Add Product"],
  "peakTimes": {
    "morning": 300,
    "afternoon": 500,
    "evening": 800
  },
  "systemType": "E_COMMERCE",
  "dataVolume": 10000,
  "deploymentEnvironment": "PRODUCTION_LIKE",
  "additionalRequirements": {
    "responseTime": "< 2 seconds",
    "throughput": "> 1000 TPS"
  }
}
```

**Response:** JSON with complete performance test plan
```json
{
  "planId": "perf_001",
  "planName": "E-Commerce Performance Test Plan",
  "systemName": "E-Commerce Platform",
  "scenarios": [...],
  "systemRequirements": {...},
  "testEnvironment": "Production-like Environment",
  "executionStrategy": "Phased approach with monitoring",
  "prerequisites": [...],
  "testDataRequirements": {...},
  "estimatedDuration": "4 hours",
  "riskAssessment": "Medium - Monitor system resources"
}
```

#### Get Performance Test Configuration
```http
GET /api/performance-test/scenario-types
```

**Response:** Available scenario types and common operations

```http
GET /api/performance-test/system-types
```

**Response:** Supported system types

```http
GET /api/performance-test/deployment-environments
```

**Response:** Available deployment environments

### 4. AI Model Management

#### AI Console Interface
```http
GET /ai
```

**Response:** HTML page with AI model management interface

#### Ingest Text Document
```http
POST /ai/ingest-text
Content-Type: application/x-www-form-urlencoded

title: API Documentation
text: Complete API specification for the system...
```

**Parameters:**
- `title` (required): Document title
- `text` (required): Document content

#### Ingest File Document
```http
POST /ai/ingest-file
Content-Type: multipart/form-data

file: [document file]
```

**Parameters:**
- `file` (required): Document file to ingest

#### Connect Jira Project
```http
POST /ai/connect-jira
Content-Type: application/x-www-form-urlencoded

baseUrl: https://company.atlassian.net
email: user@company.com
token: jira_api_token
projectKey: PROJ
maxIssues: 50
```

**Parameters:**
- `baseUrl` (required): Jira instance URL
- `email` (required): Jira user email
- `token` (required): Jira API token
- `projectKey` (required): Project key
- `maxIssues` (optional): Maximum issues to fetch (default: 25)

#### Connect Slack Channel
```http
POST /ai/connect-slack
Content-Type: application/x-www-form-urlencoded

token: slack_bot_token
channel: #general
limit: 100
```

**Parameters:**
- `token` (required): Slack bot token
- `channel` (required): Channel name
- `limit` (optional): Maximum messages to fetch (default: 50)

#### Ask AI Question
```http
POST /ai/ask
Content-Type: application/x-www-form-urlencoded

question: What are the main features of the system?
```

**Parameters:**
- `question` (required): Question to ask the AI

### 5. Authentication & User Management

#### User Registration
```http
POST /register
Content-Type: application/x-www-form-urlencoded

username: newuser
password: securepassword
```

**Parameters:**
- `username` (required): Unique username
- `password` (required): Secure password

#### User Login
```http
POST /login
Content-Type: application/x-www-form-urlencoded

username: user
password: password
```

**Parameters:**
- `username` (required): Username
- `password` (required): Password

#### Health Check
```http
GET /api/health
```

**Response:** Application status message

### 6. Dynamic Content Generation

#### Generate Additional Content
```http
POST /api/generate-more
Content-Type: application/json

{
  "input": "User authentication system",
  "inputType": "text",
  "generationType": "test_cases",
  "includeAcceptance": true,
  "selectedTypes": ["functional", "security"]
}
```

**Response:** Generated content as string

#### Ask Question About Test Row
```http
POST /api/ask-row
Content-Type: application/json

{
  "rowText": "Verify user can login with valid credentials",
  "question": "What are the test data requirements?"
}
```

**Response:** AI-generated answer as string

## Service Layer APIs

### AIService

The core service for AI-powered content generation with support for multiple providers.

#### Public Methods

```java
// Generate test conditions from input
public String generateTestConditions(String input, String inputType)

// Generate content with advanced options
public String generateContent(String input, String inputType, String generationType, 
                            boolean includeAcceptance, List<String> selectedTypes)

// Generate automation scripts
public String generateAutomationScripts(String description)

// Generate content with custom prompt
public String generateWithPrompt(String prompt)
```

**Supported AI Providers:**
- OpenAI (GPT-3.5-turbo)
- Ollama (Local AI - Free)
- Hugging Face (Free tier available)
- Google Gemini (Free tier available)
- Demo mode (Pre-generated content)

### ImageProcessingService

Service for processing images and extracting text using OCR.

#### Public Methods

```java
// Extract text from image
public String extractTextFromImage(MultipartFile imageFile)

// Validate image file
public boolean isValidImageFile(MultipartFile file)

// Get comprehensive image analysis context
public String getImageAnalysisContext(MultipartFile imageFile)
```

**Supported Image Formats:**
- JPEG/JPG
- PNG
- GIF
- BMP

### ApiScenarioService

Service for generating and managing API test scenarios.

#### Public Methods

```java
// Generate test scenarios from cURL command
public List<Map<String, Object>> generateScenarios(String curl, Integer limit, List<String> caseTypes)

// Execute a test scenario
public Map<String, Object> runScenario(String id)

// Get scenario code
public Map<String, String> getScenarioCode(String id)

// Get scenario details
public Map<String, Object> getScenarioDetail(String id)

// Set scenario assertion
public Map<String, String> setScenarioAssertion(String id, String expected)

// Set multiple assertions
public Map<String, String> setScenarioAssertions(String id, Map<String, Object> assertions, Integer expectedStatus)
```

### PerformanceTestScenarioGenerator

Service for generating comprehensive performance test plans.

#### Public Methods

```java
// Generate complete performance test plan
public PerformanceTestPlan generateTestPlan(PerformanceTestRequest request)
```

**Supported Test Types:**
- STRESS: Breakpoint identification
- SOAK: Long-term stability
- VOLUME: Large data handling
- SPIKE: Sudden load response
- SCALABILITY: Performance with resource addition

**Supported System Types:**
- E_COMMERCE: E-commerce platforms
- BANKING: Banking systems
- HEALTHCARE: Healthcare systems
- EDUCATION: Educational systems
- GOVERNMENT: Government systems
- SOCIAL_MEDIA: Social media platforms
- GAMING: Gaming systems
- ENTERPRISE: Enterprise systems

### KnowledgeBaseService

Service for managing knowledge base documents and AI context.

#### Public Methods

```java
// List all documents
public List<Document> listDocuments()

// Ingest text document
public void ingestText(String title, String text)

// Ingest file document
public void ingestFile(MultipartFile file)

// Ingest Jira project
public void ingestJiraProject(String baseUrl, String email, String token, String projectKey, int maxIssues)

// Ingest Slack channel
public void ingestSlackChannel(String token, String channel, int limit)

// Build context for question
public String buildContextForQuestion(String question, int maxTokens)
```

### UserService

Service for user management and authentication.

#### Public Methods

```java
// Register new user
public void registerNewUser(String username, String password)

// Find user by username
public Optional<User> findByUsername(String username)

// Save user
public void saveUser(User user)
```

## Data Transfer Objects (DTOs)

### GenerateRequest

Request object for content generation.

```java
public class GenerateRequest {
    private String input;                    // Input text or context
    private String inputType;                // Type of input (text/image)
    private String generationType;           // Type of content to generate
    private boolean includeAcceptance;       // Include acceptance criteria
    private List<String> selectedTypes;      // Specific types to include
}
```

### PerformanceTestRequest

Request object for performance test generation.

```java
public class PerformanceTestRequest {
    private int expectedUsers;                           // Expected concurrent users
    private List<String> commonOperations;               // Common operations to test
    private Map<String, Integer> peakTimes;             // Peak usage times
    private String systemType;                           // Type of system
    private int dataVolume;                              // Data volume to test
    private String deploymentEnvironment;                // Target environment
    private Map<String, Object> additionalRequirements; // Additional requirements
}
```

### PerformanceTestPlan

Complete performance test plan with scenarios.

```java
public class PerformanceTestPlan {
    private String planId;                              // Unique plan identifier
    private String planName;                            // Plan name
    private String systemName;                          // System under test
    private LocalDateTime createdDate;                  // Creation date
    private String createdBy;                           // Creator username
    private List<PerformanceTestScenario> scenarios;    // Test scenarios
    private Map<String, Object> systemRequirements;     // System requirements
    private String testEnvironment;                     // Test environment
    private String executionStrategy;                   // Execution strategy
    private List<String> prerequisites;                 // Prerequisites
    private Map<String, String> testDataRequirements;   // Test data needs
    private String estimatedDuration;                   // Estimated duration
    private String riskAssessment;                      // Risk assessment
}
```

### PerformanceTestScenario

Individual performance test scenario.

```java
public class PerformanceTestScenario {
    private String scenarioId;                          // Unique scenario identifier
    private String scenarioName;                        // Scenario name
    private String scenarioType;                        // Type of test
    private String description;                         // Detailed description
    private int targetUsers;                            // Target user count
    private int rampUpTime;                             // Ramp-up time in seconds
    private int testDuration;                           // Test duration in seconds
    private List<String> testSteps;                     // Test execution steps
    private Map<String, Object> parameters;             // Test parameters
    private List<String> successCriteria;               // Success criteria
    private List<String> monitoringPoints;              // Monitoring points
    private String expectedOutcome;                     // Expected outcome
    private int priority;                               // Priority level
}
```

### RowQuestion

Question about a specific test row.

```java
public class RowQuestion {
    private String rowText;                             // Test row text
    private String question;                            // User question
}
```

## Model Classes

### User

User entity for authentication and request tracking.

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                    // Unique identifier
    
    @Column(unique = true, nullable = false)
    private String username;                            // Unique username
    
    @Column(nullable = false)
    private String password;                            // Encrypted password
    
    private boolean isFreeTrial;                        // Free trial status
    private int remainingRequests;                      // Remaining free requests
}
```

## Configuration

### Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Password encoding with BCrypt
    // Form-based authentication
    // Custom login/register pages
    // CSRF protection (configurable)
}
```

**Security Features:**
- BCrypt password encoding
- Form-based authentication
- Custom login/register pages
- Configurable CSRF protection
- Role-based access control

### Web Configuration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // File upload configuration
    // Maximum file size: 10MB
    // Maximum request size: 10MB
}
```

**File Upload Limits:**
- Maximum file size: 10MB
- Maximum request size: 10MB
- Supported formats: Images, documents

### Application Properties

```properties
# AI Provider Configuration
ai.provider=gemini                    # Options: demo, ollama, openai, huggingface, gemini

# OpenAI Configuration
openai.api.key=your-api-key-here

# Ollama Configuration (Free)
ollama.url=http://localhost:11434

# Hugging Face Configuration
huggingface.api.key=your-hf-key-here

# Google Gemini Configuration
gemini.api.key=your-gemini-key-here

# Server Configuration
server.port=8080
spring.application.name=Test Analyst AI Assistant

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# OCR Configuration (Optional)
tesseract.data.path=/path/to/tessdata
tesseract.language=eng

# Logging Configuration
logging.level.Madfoat.Learning=DEBUG
```

## Exception Handling

### Global Exception Handler

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    // Handle file upload size exceeded
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e, Model model)
    
    // Handle invalid arguments
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, Model model)
    
    // Handle generic exceptions
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, Model model)
}
```

**Handled Exceptions:**
- File upload size exceeded
- Invalid arguments
- Generic exceptions
- AI service errors
- Image processing errors

## Utility Classes

### RestAssuredAssertionUtil

Utility class for REST API testing assertions.

```java
public class RestAssuredAssertionUtil {
    
    // Assert response contains substring
    public static ValidatableResponse assertContains(Response response, String expectedSubstring)
    
    // Assert header equals expected value
    public static ValidatableResponse assertHeaderEquals(Response response, String headerName, String expectedValue)
    
    // Assert JSON path equals expected value
    public static ValidatableResponse assertJsonPathEquals(Response response, String jsonPath, Object expectedValue)
    
    // Assert content type
    public static ValidatableResponse assertContentType(Response response, String expectedContentType)
    
    // Assert JSON path exists
    public static ValidatableResponse assertJsonPathExists(Response response, String jsonPath)
    
    // Assert on ValidatableResponse stage
    public static ValidatableResponse assertContains(ValidatableResponse thenStage, String expectedSubstring)
    public static ValidatableResponse assertJsonPathEquals(ValidatableResponse thenStage, String jsonPath, Object expectedValue)
    public static ValidatableResponse assertContentType(ValidatableResponse thenStage, String expectedContentType)
    public static ValidatableResponse assertJsonPathExists(ValidatableResponse thenStage, String jsonPath)
}
```

## Frontend Templates

### Main Interface (index.html)
- Text input for business requirements
- File upload for images/documents
- Generation type selection
- Advanced options configuration

### Results Display (results.html)
- Generated test conditions display
- Export options (copy, download, print)
- Dynamic content generation
- Row-specific Q&A functionality

### Performance Testing (performance-test.html)
- Performance test configuration form
- System type selection
- Environment configuration
- Results visualization

### AI Model Management (ai-model.html)
- Document ingestion interface
- Jira/Slack integration
- Knowledge base management
- AI question interface

### API Scenarios (api-scenarios.html)
- API test scenario display
- Execution controls
- Assertion configuration
- Results analysis

### Automation (automation.html)
- Generated automation code display
- Code formatting
- Export options

## Usage Examples

### 1. Generate Test Cases from Text

**Request:**
```bash
curl -X POST http://localhost:8080/generate-from-text \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "businessText=User login functionality with username and password authentication"
```

**Expected Response:** HTML page with comprehensive test cases including:
- Functional test conditions
- Non-functional test conditions
- Security test conditions
- Boundary test conditions
- Edge case scenarios

### 2. Generate Test Cases from Image

**Request:**
```bash
curl -X POST http://localhost:8080/generate-from-image \
  -F "imageFile=@screenshot.png"
```

**Expected Response:** HTML page with:
- Extracted text from image
- Generated test conditions based on image content
- File analysis information

### 3. Generate Performance Test Plan

**Request:**
```bash
curl -X POST http://localhost:8080/api/performance-test/generate-scenarios \
  -H "Content-Type: application/json" \
  -d '{
    "expectedUsers": 1000,
    "commonOperations": ["Login", "Search", "Checkout"],
    "systemType": "E_COMMERCE",
    "deploymentEnvironment": "PRODUCTION_LIKE"
  }'
```

**Expected Response:** JSON with complete performance test plan including:
- Multiple test scenarios (STRESS, SOAK, VOLUME, SPIKE, SCALABILITY)
- System requirements
- Test environment configuration
- Execution strategy
- Risk assessment

### 4. Generate API Test Scenarios

**Request:**
```bash
curl -X POST http://localhost:8080/api/generate-scenarios \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "curl=curl -X POST https://api.example.com/login -H 'Content-Type: application/json' -d '{\"username\":\"user\",\"password\":\"pass\"}'&limit=5&caseTypes=positive,negative"
```

**Expected Response:** HTML page with:
- Positive test scenarios
- Negative test scenarios
- Test data requirements
- Expected outcomes

### 5. AI Knowledge Base Integration

**Request:**
```bash
curl -X POST http://localhost:8080/ai/ingest-text \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "title=System Requirements&text=The system must support 1000 concurrent users..."
```

**Request:**
```bash
curl -X POST http://localhost:8080/ai/ask \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "question=What are the performance requirements for the system?"
```

## Configuration Options

### AI Provider Selection

1. **Demo Mode** (Default)
   - No setup required
   - Pre-generated sample content
   - Good for testing and demonstration

2. **Ollama** (Recommended for Free Use)
   - Completely free
   - Runs locally on your machine
   - No API keys required
   - Works offline
   - Requires local installation

3. **Hugging Face** (Free Tier)
   - Free tier available
   - Easy setup with API key
   - Cloud-based service
   - Rate limits on free tier

4. **Google Gemini** (Free Tier)
   - Generous free tier
   - High quality results
   - Easy setup with API key
   - Some rate limits

5. **OpenAI** (Paid)
   - Most advanced capabilities
   - Pay-per-use pricing
   - Highest quality results
   - Requires API key and billing setup

### File Upload Configuration

- **Maximum file size:** 10MB
- **Supported image formats:** JPEG, PNG, GIF, BMP
- **OCR support:** Tesseract integration
- **Document processing:** Text extraction and analysis

### Security Configuration

- **Authentication:** Form-based login
- **Password encoding:** BCrypt
- **CSRF protection:** Configurable
- **Session management:** Spring Security
- **User registration:** Self-service

### Performance Testing Configuration

- **Test types:** STRESS, SOAK, VOLUME, SPIKE, SCALABILITY
- **System types:** E-commerce, Banking, Healthcare, Education, Government, Social Media, Gaming, Enterprise
- **Environments:** Development, Staging, Production-like, Production
- **Monitoring:** Response time, throughput, resource utilization

---

## Support and Troubleshooting

### Common Issues

1. **AI Service Errors**
   - Check API key configuration
   - Verify service availability
   - Check rate limits and quotas

2. **Image Processing Errors**
   - Install Tesseract OCR
   - Verify image format support
   - Check file size limits

3. **Authentication Issues**
   - Verify user credentials
   - Check database connectivity
   - Review security configuration

4. **Performance Issues**
   - Monitor system resources
   - Check database performance
   - Review logging levels

### Getting Help

- Check application logs for detailed error information
- Review configuration files for proper setup
- Consult the troubleshooting section in the README
- Create issues in the project repository

---

*This documentation covers all public APIs, functions, and components of the Test Analyst AI Assistant application. For additional information, refer to the project README and source code.*