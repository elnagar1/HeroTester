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
        } else { // test_scenarios
            String filters = (selectedTypes != null && !selectedTypes.isEmpty()) ? String.join(", ", selectedTypes) : "functional, negative, boundary, security, performance, usability";
            prompt.append("generate comprehensive software test scenarios. ");
            prompt.append("Focus on these types: " + filters + ". ");
            prompt.append("Output ONLY a Markdown table with headers: ID | Test Scenario | Steps | Expected | Type | Priority | Actual. ");
            prompt.append("Each row should be one scenario with concise steps; leave 'Actual' empty. ");
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
        } else { // test_scenarios
            return generateDemoTestConditions(input, inputType);
        }
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
