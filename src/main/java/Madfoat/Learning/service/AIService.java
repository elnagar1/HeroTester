package Madfoat.Learning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${ai.provider:demo}")
    private String aiProvider;

    @Value("${openai.api.key:your-api-key-here}")
    private String openaiKey;

    @Value("${huggingface.api.key:your-hf-key-here}")
    private String huggingfaceKey;

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${gemini.api.key:your-gemini-key-here}")
    private String geminiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AIService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public String generateTestConditions(String input, String inputType) {
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

    // New unified generator supporting different generation types and options
    public String generateContent(String input, String inputType, String generationType, boolean includeAcceptance, List<String> selectedTypes) {
        String prompt = buildPromptAdvanced(input, inputType, generationType, includeAcceptance, selectedTypes);
        switch (aiProvider.toLowerCase()) {
            case "openai":
                return generateWithOpenAIPrompt(prompt);
            case "ollama":
                return generateWithOllamaPrompt(prompt);
            case "huggingface":
                return generateWithHuggingFacePrompt(prompt);
            case "gemini":
                return generateWithGeminiPrompt(prompt);
            case "demo":
            default:
                return generateDemoContent(input, inputType, generationType, includeAcceptance, selectedTypes);
        }
    }

    // Automation script generator
    public String generateAutomationScripts(String description) {
        switch (aiProvider.toLowerCase()) {
            case "openai":
                return generateAutomationWithOpenAI(description);
            case "ollama":
                return generateAutomationWithOllama(description);
            case "huggingface":
                return generateAutomationWithHuggingFace(description);
            case "gemini":
                return generateAutomationWithGemini(description);
            case "demo":
            default:
                return generateDemoAutomationScripts(description);
        }
    }

    private String generateWithOpenAI(String input, String inputType) {
        if ("your-api-key-here".equals(openaiKey)) {
            return "OpenAI API key not configured. Please set openai.api.key in application.properties";
        }

        try {
            String prompt = buildPrompt(input, inputType);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are an expert software test analyst. Generate comprehensive test conditions based on the provided input."),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.7);

            Mono<String> response = webClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractOpenAIContent(responseBody);

        } catch (Exception e) {
            return "Error with OpenAI: " + e.getMessage();
        }
    }

    // Prompt-based overloads for provider calls
    private String generateWithOpenAIPrompt(String prompt) {
        if ("your-api-key-here".equals(openaiKey)) {
            return "OpenAI API key not configured. Please set openai.api.key in application.properties";
        }
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are an expert software test analyst. Generate based on the user's request."),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.7);

            Mono<String> response = webClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractOpenAIContent(responseBody);
        } catch (Exception e) {
            return "Error with OpenAI: " + e.getMessage();
        }
    }

    private String generateWithOllama(String input, String inputType) {
        try {
            String prompt = buildPrompt(input, inputType);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama2"); // You can change this to other models like "codellama", "mistral"
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            Mono<String> response = webClient.post()
                    .uri(ollamaUrl + "/api/generate")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractOllamaContent(responseBody);

        } catch (Exception e) {
            return "Error with Ollama: " + e.getMessage() + 
                   "\n\nMake sure Ollama is installed and running:\n" +
                   "1. Install Ollama from https://ollama.ai\n" +
                   "2. Run: ollama pull llama2\n" +
                   "3. Start Ollama service";
        }
    }

    private String generateWithOllamaPrompt(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama2");
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            Mono<String> response = webClient.post()
                    .uri(ollamaUrl + "/api/generate")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractOllamaContent(responseBody);
        } catch (Exception e) {
            return "Error with Ollama: " + e.getMessage() + 
                   "\n\nMake sure Ollama is installed and running:\n" +
                   "1. Install Ollama from https://ollama.ai\n" +
                   "2. Run: ollama pull llama2\n" +
                   "3. Start Ollama service";
        }
    }

    private String generateWithHuggingFace(String input, String inputType) {
        if ("your-hf-key-here".equals(huggingfaceKey)) {
            return "Hugging Face API key not configured. Get a free key from https://huggingface.co/settings/tokens";
        }

        try {
            String prompt = buildPrompt(input, inputType);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", prompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 1000,
                "temperature", 0.7,
                "do_sample", true
            ));

            Mono<String> response = webClient.post()
                    .uri("https://api-inference.huggingface.co/models/google/flan-t5-large")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + huggingfaceKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractHuggingFaceContent(responseBody);

        } catch (Exception e) {
            return "Error with Hugging Face: " + e.getMessage() + 
                   "\n\nGet a free API key from: https://huggingface.co/settings/tokens";
        }
    }

    private String generateWithHuggingFacePrompt(String prompt) {
        if ("your-hf-key-here".equals(huggingfaceKey)) {
            return "Hugging Face API key not configured. Get a free key from https://huggingface.co/settings/tokens";
        }
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", prompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 1000,
                "temperature", 0.7,
                "do_sample", true
            ));

            Mono<String> response = webClient.post()
                    .uri("https://api-inference.huggingface.co/models/google/flan-t5-large")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + huggingfaceKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractHuggingFaceContent(responseBody);
        } catch (Exception e) {
            return "Error with Hugging Face: " + e.getMessage() + 
                   "\n\nGet a free API key from: https://huggingface.co/settings/tokens";
        }
    }

    private String generateWithGemini(String input, String inputType) {
        if ("your-gemini-key-here".equals(geminiKey)) {
            return "Google Gemini API key not configured. Get a free key from https://ai.google.dev/";
        }

        try {
            String prompt = buildPrompt(input, inputType);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
            ));

            Mono<String> response = webClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("X-goog-api-key",geminiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractGeminiContent(responseBody);

        } catch (Exception e) {
            return "Error with Gemini: " + e.getMessage() + 
                   "\n\nGet a free API key from: https://ai.google.dev/";
        }
    }

    private String generateWithGeminiPrompt(String prompt) {
        if ("your-gemini-key-here".equals(geminiKey)) {
            return "Google Gemini API key not configured. Get a free key from https://ai.google.dev/";
        }
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
            ));

            Mono<String> response = webClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("X-goog-api-key",geminiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractGeminiContent(responseBody);
        } catch (Exception e) {
            return "Error with Gemini: " + e.getMessage() + 
                   "\n\nGet a free API key from: https://ai.google.dev/";
        }
    }

    private String buildPrompt(String input, String inputType) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert software test analyst. ");
        
        if ("image".equals(inputType)) {
            prompt.append("Based on the following text extracted from an image or document, ");
        } else {
            prompt.append("Based on the following business requirements or description, ");
        }
        
        // Default behavior now targets test scenarios
        prompt.append("generate comprehensive software test scenarios. ");
        prompt.append("Output ONLY a Markdown table with the following headers: ID | Test Scenario | Test Case Title | Steps | Expected | Actual. ");
        prompt.append("Do not include any additional text before or after the table. ");
        prompt.append("Include functional, non-functional, positive, negative, boundary, and edge case scenarios. ");
        prompt.append("Ensure each row is a single scenario with detailed steps, expected, and an empty 'Actual' cell.\n\n");
        prompt.append("Input: ").append(input);
        
        return prompt.toString();
    }

    private String buildPromptAdvanced(String input, String inputType, String generationType, boolean includeAcceptance, List<String> selectedTypes) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert software test analyst. ");
        if ("image".equals(inputType)) {
            prompt.append("Based on the following text extracted from an image or document, ");
        } else {
            prompt.append("Based on the following business requirements or description, ");
        }

        String type = generationType == null ? "test_scenarios" : generationType.toLowerCase();
        if ("user_story".equals(type)) {
            prompt.append("write a high-quality agile user story. ");
            prompt.append("Output ONLY the user story text. ");
            if (includeAcceptance) {
                prompt.append("Then include Acceptance Criteria as a numbered list using clear Given/When/Then phrasing. ");
            } else {
                prompt.append("Do NOT include acceptance criteria. ");
            }
        } else if ("test_cases".equals(type)) {
            String filters = (selectedTypes != null && !selectedTypes.isEmpty()) ? String.join(", ", selectedTypes) : "functional, negative, boundary, security, performance, usability";
            prompt.append("generate comprehensive software test cases. ");
            prompt.append("Focus on these types: " + filters + ". ");
            prompt.append("Output ONLY a Markdown table with headers: ID | Title | Steps | Expected | Type | Priority | Actual. ");
            prompt.append("Each row should be one test case with concise steps; leave 'Actual' empty. ");
        } else { // default fallback -> test_cases
            String filters = (selectedTypes != null && !selectedTypes.isEmpty()) ? String.join(", ", selectedTypes) : "functional, negative, boundary, security, performance, usability";
            prompt.append("generate comprehensive software test cases. ");
            prompt.append("Focus on these types: " + filters + ". ");
            prompt.append("Output ONLY a Markdown table with headers: ID | Title | Steps | Expected | Type | Priority | Actual. ");
            prompt.append("Each row should be one test case with concise steps; leave 'Actual' empty. ");
        }
        prompt.append("\n\nInput: ").append(input);
        return prompt.toString();
    }

    private String generateDemoTestConditions(String input, String inputType) {
        StringBuilder result = new StringBuilder();
        
        result.append("🔧 DEMO MODE - Sample Test Scenarios Generated\n");
        result.append("(Switch to a free AI provider like Ollama or Hugging Face for real AI generation)\n\n");
        
        result.append("📝 INPUT ANALYSIS:\n");
        result.append("Type: ").append(inputType).append("\n");
        result.append("Content Preview: ").append(input.length() > 100 ? input.substring(0, 100) + "..." : input).append("\n\n");
        
        result.append("🧪 FUNCTIONAL TEST SCENARIOS:\n");
        result.append("✓ F001: Verify main functionality works with valid inputs\n");
        result.append("✓ F002: Verify system handles invalid inputs gracefully\n");
        result.append("✓ F003: Verify error messages are displayed for incorrect data\n");
        result.append("✓ F004: Verify success confirmation is shown for valid operations\n");
        result.append("✓ F005: Verify data persistence after successful operations\n\n");
        
        result.append("🔒 SECURITY TEST SCENARIOS:\n");
        result.append("✓ S001: Verify authentication is required for sensitive operations\n");
        result.append("✓ S002: Verify input validation prevents injection attacks\n");
        result.append("✓ S003: Verify session timeout works correctly\n");
        result.append("✓ S004: Verify user permissions are enforced\n\n");
        
        result.append("⚡ PERFORMANCE TEST SCENARIOS:\n");
        result.append("✓ P001: Verify response time is under 3 seconds for normal load\n");
        result.append("✓ P002: Verify system handles 100 concurrent users\n");
        result.append("✓ P003: Verify memory usage stays within acceptable limits\n");
        result.append("✓ P004: Verify system recovers gracefully from high load\n\n");
        
        result.append("📱 USABILITY TEST SCENARIOS:\n");
        result.append("✓ U001: Verify interface is responsive on mobile devices\n");
        result.append("✓ U002: Verify accessibility standards are met\n");
        result.append("✓ U003: Verify user can complete tasks intuitively\n");
        result.append("✓ U004: Verify error messages are user-friendly\n\n");
        
        result.append("🔄 BOUNDARY TEST SCENARIOS:\n");
        result.append("✓ B001: Test with minimum valid input values\n");
        result.append("✓ B002: Test with maximum valid input values\n");
        result.append("✓ B003: Test with values just below minimum threshold\n");
        result.append("✓ B004: Test with values just above maximum threshold\n\n");
        
        result.append("⚠️ NEGATIVE TEST SCENARIOS:\n");
        result.append("✓ N001: Test with null/empty inputs\n");
        result.append("✓ N002: Test with malformed data\n");
        result.append("✓ N003: Test with special characters and Unicode\n");
        result.append("✓ N004: Test system behavior during network failures\n");
        result.append("✓ N005: Test with insufficient system resources\n\n");
        
        result.append("📊 TOTAL: 21 comprehensive test scenarios generated\n");
        result.append("💡 TIP: Set ai.provider=ollama for completely free local AI!");
        
        return result.toString();
    }
    private String generateDemoContent(String input, String inputType, String generationType, boolean includeAcceptance, List<String> selectedTypes) {
        String type = generationType == null ? "test_scenarios" : generationType.toLowerCase();
        if ("user_story".equals(type)) {
            StringBuilder sb = new StringBuilder();
            sb.append("As a user, I want to perform the primary action so that I achieve the desired outcome.\n\n");
            if (includeAcceptance) {
                sb.append("Acceptance Criteria:\n");
                sb.append("1. Given valid inputs, when the action is performed, then the expected result is shown.\n");
                sb.append("2. Given invalid inputs, when the action is performed, then an informative error message is displayed.\n");
                sb.append("3. Given network issues, when the action is retried, then the system handles it gracefully.\n");
            }
            return sb.toString();
        } else if ("test_cases".equals(type)) {
            return "| ID | Title | Steps | Expected | Type | Priority | Actual |\n|---|---|---|---|---|---|---|\n| TC-001 | Login with valid credentials | 1) Open login 2) Enter valid creds 3) Submit | User is logged in | functional | High | |\n| TC-002 | Login with invalid password | 1) Open login 2) Enter invalid password 3) Submit | Error message shown | negative | Medium | |";
        } else { // default fallback
            return generateDemoTestConditions(input, inputType);
        }
    }

    private String buildAutomationPrompt(String description) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a senior QA automation engineer. Based on the following page/scenario description, write Java Selenium + TestNG automation code that matches THIS EXACT STYLE: \n\n");
        prompt.append("Test class requirements:\n");
        prompt.append("- Name: <FeatureName>Test (derive from scenario)\n");
        prompt.append("- Extends: BaseTest\n");
        prompt.append("- Imports: dPhish.core.BaseTest; dPhish.pages.portal.Web1.CampaignsPage; dPhish.pages.portal.Web1.HomePage; dPhish.pages.portal.Web1.LoginPage; org.testng.annotations.BeforeClass; org.testng.annotations.Test;\n");
        prompt.append("- Fields: private LoginPage login; private HomePage home; private CampaignsPage campaignsPage;\n");
        prompt.append("- @BeforeClass: setUp(platformName); new page objects; login.login(); home.openPage(HomePage.MenuItem.Campaigns);\n");
        prompt.append("- Include constants: campaignName, intervals, chunks, attack, difficultyLevel, successCategory, trackerHost (values derived from scenario or placeholders).\n");
        prompt.append("- At least one @Test that chains page methods like: clickNewCampaignButton(), enterCampaignName(...), clickDropDownActions(...), scrollDown(...), enterDate(...), enterIntervals(...), enterChunks(...), clickNext(), selectCampaignType(...), selectSenderByValue(...), assertions with hardAssertion.assertTrue(...), selectTemplateByValue(...), selectMultiplePostCampaignByValue(...).\n\n");
        prompt.append("Page class requirements:\n");
        prompt.append("- Class: CampaignsPage extends BasePage\n");
        prompt.append("- Imports: dPhish.core.BasePage; org.openqa.selenium.*;\n");
        prompt.append("- Provide By locators similar to example and methods with EXACT signatures used by the test: clickNewCampaignButton(), scrollDown(int pixels), groupAction(String campaignGroupName, String action), clickDropDownActions(String dropdownName, String value), enterCampaignName(String name), enterIntervals(String intervals), enterChunks(String chunks), enterTags(String tags), enterDate(String date), selectCampaignType(String type), clickNext(), clickBack(), search(String name), clickNewSenderButton(), selectSenderByValue(String value), isSenderSelected(String value), selectPageByValue(String value), isPageSelected(String value), selectTemplateByValue(String value), isTemplateSelected(String value), selectMultiplePostCampaignByValue(String value), isPostCampaignSelected(String value). Each method returns this (except boolean checks).\n");
        prompt.append("- Include enums inside page class: MenuItem { VIEW, EDIT, DELETE } with getDisplayName(); and CreateCampaignItem { DifficultyLeve, SuccessCategory, Attack, TrackerHost } with getDisplayName();\n");
        prompt.append("- Use an 'element' helper to click/set text, as in example.\n\n");
        prompt.append("Output EXACTLY TWO Markdown code blocks with triple backticks and language 'java': first the test class, then the page class. No extra prose. No package declarations.\n\n");
        prompt.append("Scenario description:\n");
        prompt.append(description);
        return prompt.toString();
    }

    private String generateAutomationWithOpenAI(String description) {
        if ("your-api-key-here".equals(openaiKey)) {
            return generateDemoAutomationScripts(description);
        }
        try {
            String prompt = buildAutomationPrompt(description);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You generate high-quality Java Selenium TestNG automation code."),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.5);

            Mono<String> response = webClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractOpenAIContent(responseBody);
        } catch (Exception e) {
            return "Error with OpenAI: " + e.getMessage();
        }
    }

    private String generateAutomationWithOllama(String description) {
        try {
            String prompt = buildAutomationPrompt(description);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama2");
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            Mono<String> response = webClient.post()
                    .uri(ollamaUrl + "/api/generate")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractOllamaContent(responseBody);
        } catch (Exception e) {
            return "Error with Ollama: " + e.getMessage();
        }
    }

    private String generateAutomationWithHuggingFace(String description) {
        if ("your-hf-key-here".equals(huggingfaceKey)) {
            return generateDemoAutomationScripts(description);
        }
        try {
            String prompt = buildAutomationPrompt(description);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", prompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 1200,
                "temperature", 0.5,
                "do_sample", true
            ));

            Mono<String> response = webClient.post()
                    .uri("https://api-inference.huggingface.co/models/google/flan-t5-large")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + huggingfaceKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractHuggingFaceContent(responseBody);
        } catch (Exception e) {
            return "Error with Hugging Face: " + e.getMessage();
        }
    }

    private String generateAutomationWithGemini(String description) {
        if ("your-gemini-key-here".equals(geminiKey)) {
            return generateDemoAutomationScripts(description);
        }
        try {
            String prompt = buildAutomationPrompt(description);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
            ));

            Mono<String> response = webClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("X-goog-api-key", geminiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            return extractGeminiContent(responseBody);
        } catch (Exception e) {
            return "Error with Gemini: " + e.getMessage();
        }
    }

    private String generateDemoAutomationScripts(String description) {
        String testClass = "" +
            "import dPhish.core.BaseTest;\n" +
            "import dPhish.pages.portal.Web1.CampaignsPage;\n" +
            "import dPhish.pages.portal.Web1.HomePage;\n" +
            "import dPhish.pages.portal.Web1.LoginPage;\n" +
            "import org.testng.annotations.BeforeClass;\n" +
            "import org.testng.annotations.Test;\n\n" +
            "public class CampaignTest extends BaseTest {\n\n" +
            "    private LoginPage login;\n" +
            "    private HomePage home;\n" +
            "    private CampaignsPage campaignsPage;\n\n" +
            "    @BeforeClass\n" +
            "    public void beforeClass() {\n" +
            "        setUp(platformName);\n" +
            "        login = new LoginPage(driver);\n" +
            "        home = new HomePage(driver);\n" +
            "        campaignsPage = new CampaignsPage(driver);\n" +
            "        login.login();\n" +
            "        home.openPage(HomePage.MenuItem.Campaigns);\n" +
            "    }\n\n" +
            "    String campaignName = \"Automation\" + System.currentTimeMillis();\n" +
            "    String intervals = \"30\";\n" +
            "    String chunks = \"150\";\n" +
            "    String attack = \"Regular Attachment\";\n" +
            "    String difficultyLevel = \"Hard\";\n" +
            "    String successCategory = \"Document Opened\";\n" +
            "    String trackerHost = \"testing.winnnig.store\";\n\n" +
            "    @Test\n" +
            "    public void testCreateCampaignWithType_EmailWithAttachment() {\n" +
            "        campaignsPage.clickNewCampaignButton()\n" +
            "                .enterCampaignName(campaignName)\n" +
            "                .clickDropDownActions(CampaignsPage.CreateCampaignItem.SuccessCategory.getDisplayName(), successCategory)\n" +
            "                .clickDropDownActions(CampaignsPage.CreateCampaignItem.Attack.getDisplayName(), attack)\n" +
            "                .clickDropDownActions(CampaignsPage.CreateCampaignItem.DifficultyLeve.getDisplayName(), difficultyLevel)\n" +
            "                .scrollDown(300)\n" +
            "                .clickDropDownActions(CampaignsPage.CreateCampaignItem.TrackerHost.getDisplayName(), trackerHost)\n" +
            "                .enterTags(\"Automation Tag1\" + System.currentTimeMillis())\n" +
            "                .enterTags(\"Automation Tag2\" + System.currentTimeMillis())\n" +
            "                .enterDate(\"28\")\n" +
            "                .enterIntervals(intervals)\n" +
            "                .enterChunks(chunks)\n" +
            "                .clickNext()\n" +
            "                .selectCampaignType(\"SMS\")\n" +
            "                .clickNext()\n" +
            "                .selectSenderByValue(\"SENDER\");\n\n" +
            "        hardAssertion.assertTrue(campaignsPage.isSenderSelected(\"SENDER\"), \"Sender should be selected\");\n\n" +
            "        campaignsPage.clickNext()\n" +
            "                     .selectTemplateByValue(\"summer vacation\");\n" +
            "        hardAssertion.assertTrue(campaignsPage.isTemplateSelected(\"summer vacation\"), \"Template should be selected\");\n\n" +
            "        campaignsPage.clickNext()\n" +
            "                .selectMultiplePostCampaignByValue(\"ffffffffffff…\")\n" +
            "                .selectMultiplePostCampaignByValue(\"s - assign course\")\n" +
            "                .clickNext();\n" +
            "    }\n" +
            "}\n";

        String pageClass = "" +
            "import dPhish.core.BasePage;\n" +
            "import org.openqa.selenium.*;\n\n" +
            "public class CampaignsPage extends BasePage {\n\n" +
            "    public CampaignsPage(WebDriver driver) {\n" +
            "        super(driver);\n" +
            "    }\n\n" +
            "    private final By searchInput = By.cssSelector(\"input[placeholder='Search']\");\n" +
            "    private final By nextButton = By.xpath(\"//button[@type='submit']\");\n" +
            "    private final By backButton = By.xpath(\"//button[text()='Back']\");\n\n" +
            "    private final By newCampaignButton = By.xpath(\"//*[text()='New Campaign']\");\n" +
            "    private final By campaignNameInput = By.id(\"name\");\n" +
            "    private final By intervalsInput = By.id(\"intervals\");\n" +
            "    private final By chunksInput = By.id(\"chunks\");\n" +
            "    private final By tagsTxb = By.xpath(\"//*[@class='v-select vs--multiple tag-input']//input\");\n" +
            "    private final By DatePicker = By.xpath(\"//input[@placeholder='Choose Date' and @name='start']\");\n\n" +
            "    private final By newSenderButton = By.xpath(\"//button[text()= 'New Sender']\");\n\n" +
            "    public CampaignsPage clickNewCampaignButton() {\n" +
            "        element.click(newCampaignButton);\n" +
            "        return this;\n" +
            "    }\n\n" +
            "    public CampaignsPage scrollDown(int pixels) {\n" +
            "        ((JavascriptExecutor) driver).executeScript(\"window.scrollBy(0, arguments[0]);\", pixels);\n" +
            "        return this;\n" +
            "    }\n\n" +
            "    public enum MenuItem {\n" +
            "        VIEW(\"View\"), EDIT(\"Edit\"), DELETE(\"Delete\");\n\n" +
            "        private final String displayName;\n" +
            "        MenuItem(String displayName) { this.displayName = displayName; }\n" +
            "        public String getDisplayName() { return displayName; }\n" +
            "    }\n\n" +
            "    public CampaignsPage groupAction(String campaignGroupName, String action) {\n" +
            "        By elm = By.xpath(\"//*[text()='\" + campaignGroupName + \"']//..//..//..//button\");\n" +
            "        By elm2 = By.xpath(\"//*[text()='\" + campaignGroupName + \"']//..//..//..//*[text()='\" + action + \"']\");\n" +
            "        element.click(elm);\n" +
            "        element.click(elm2);\n" +
            "        return this;\n" +
            "    }\n\n" +
            "    public enum CreateCampaignItem {\n" +
            "        DifficultyLeve(\"level\"), SuccessCategory(\"successCategory\"), Attack(\"attack\"), TrackerHost(\"trackerHost\");\n\n" +
            "        private final String displayName;\n" +
            "        CreateCampaignItem(String displayName) { this.displayName = displayName; }\n" +
            "        public String getDisplayName() { return displayName; }\n" +
            "    }\n\n" +
            "    public CampaignsPage clickDropDownActions(String dropdownName, String value) {\n" +
            "        By elm = By.xpath(\"//*[@name='\" + dropdownName + \"']//*[@class='vs__actions']\");\n" +
            "        By elm2 = By.xpath(\"//*[text()='\" + value + \"']\");\n" +
            "        element.click(elm);\n" +
            "        element.click(elm2);\n" +
            "        return this;\n" +
            "    }\n\n" +
            "    public CampaignsPage enterCampaignName(String name) { element.setText(campaignNameInput, name); return this; }\n" +
            "    public CampaignsPage enterIntervals(String intervals) { element.clearAndSetText(intervalsInput, intervals); return this; }\n" +
            "    public CampaignsPage enterChunks(String chunks) { element.clearAndSetText(chunksInput, chunks); return this; }\n" +
            "    public CampaignsPage enterTags(String tags) { element.setText(tagsTxb, tags, Keys.ENTER); return this; }\n" +
            "    public CampaignsPage enterDate(String date) { By Date = By.xpath(\"//*[@aria-label='July \" + date + \", 2025']\"); element.click(DatePicker); element.click(Date); return this; }\n\n" +
            "    public CampaignsPage selectCampaignType(String type) { By campaignType = By.xpath(\"//*[text()='\" + type + \"']//..//..//input\"); element.click(campaignType); return this; }\n" +
            "    public CampaignsPage clickNext() { element.click(nextButton); return this; }\n" +
            "    public CampaignsPage clickBack() { element.click(backButton); return this; }\n\n" +
            "    public CampaignsPage search(String name) { element.clearAndSetText(searchInput, name); return this; }\n" +
            "    public CampaignsPage clickNewSenderButton() { element.click(newSenderButton); return this; }\n\n" +
            "    public CampaignsPage selectSenderByValue(String value) { element.clearAndSetText(searchInput, value); element.click(By.xpath(\"//input[@name='sender']//..//p[text()='\" + value + \"']\")); return this; }\n" +
            "    public boolean isSenderSelected(String value) { return element.isSelected(By.xpath(\"//input[@name='sender']//..//p[text()='\" + value + \"']//..//..//input\")); }\n\n" +
            "    public CampaignsPage selectPageByValue(String value) { element.clearAndSetText(searchInput, value); element.click(By.xpath(\"//input[@name='page']//..//p[text()='\" + value + \"']\")); return this; }\n" +
            "    public boolean isPageSelected(String value) { return element.isSelected(By.xpath(\"//input[@name='page']//..//p[text()='\" + value + \"']//..//..//input\")); }\n\n" +
            "    public CampaignsPage selectTemplateByValue(String value) { search(value); element.click(By.xpath(\"//input[@name='template']//..//span[text()=' \" + value + \"']\")); return this; }\n" +
            "    public boolean isTemplateSelected(String value) { return element.isSelected(By.xpath(\"//input[@name='template']//..//span[text()=' \" + value + \"']//..//input\")); }\n\n" +
            "    public CampaignsPage selectMultiplePostCampaignByValue(String value) { search(value); element.click(By.xpath(\"//input[@name='posts']//..//span[text()=' \" + value + \"']\")); return this; }\n" +
            "    public boolean isPostCampaignSelected(String value) { return element.isSelected(By.xpath(\"//input[@name='posts']//..//span[text()=' \" + value + \"']//..//input\")); }\n" +
            "}\n";
        return "```java\n" + testClass + "\n```\n\n```java\n" + pageClass + "\n```";
    }
    // Response extraction methods
    private String extractOpenAIContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            return "Error parsing OpenAI response: " + e.getMessage();
        }
    }

    private String extractOllamaContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.get("response").asText();
        } catch (Exception e) {
            return "Error parsing Ollama response: " + e.getMessage();
        }
    }

    private String extractHuggingFaceContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.isArray()) {
                return root.get(0).get("generated_text").asText();
            }
            return root.get("generated_text").asText();
        } catch (Exception e) {
            return "Error parsing Hugging Face response: " + e.getMessage();
        }
    }

    private String extractGeminiContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
        } catch (Exception e) {
            return "Error parsing Gemini response: " + e.getMessage();
        }
    }
}
