package Madfoat.Learning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import Madfoat.Learning.model.User;

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
    private final UserService userService;

    @Autowired
    public AIService(UserService userService) {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
        this.userService = userService;
    }


    // New unified generator supporting different generation types and options
    public String generateContent(String input, String inputType, String generationType, boolean includeAcceptance, List<String> selectedTypes) {
        if (!deductRequest()) {
            return "You have exceeded your free trial limit. Please contact support for more options.";
        }
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
                return "[DEMO MODE]\n" +
                        "Prompt Summary:\n" + buildPromptAdvanced(input, inputType, generationType, includeAcceptance, selectedTypes) + "\n\n" +
                        "This is a demo response. Configure a free provider like Ollama/HuggingFace or Gemini for real answers.";
        }
    }


    // Generic prompt-based generation (for row Q&A and custom asks)
    public String generateWithPrompt(String prompt) {
        if (!deductRequest()) {
            return "You have exceeded your free trial limit. Please contact support for more options.";
        }
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
                return "[DEMO MODE]\n" +
                        "Prompt Summary:\n" + prompt + "\n\n" +
                        "This is a demo response. Configure a free provider like Ollama/HuggingFace or Gemini for real answers.";
        }
    }

    private boolean deductRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // If not authenticated, assume it's a demo or public access which might not require deduction, or handle as error
            // For now, let's allow it to proceed for unauthenticated users, but ideally, this would be locked down.
            // Or, you could return false and force login.
            return true; // Or false, depending on your public access policy
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);

        if (user == null) {
            // Should not happen if user is authenticated but not found in DB
            return false;
        }

        if (user.isFreeTrial()) {
            if (user.getRemainingRequests() > 0) {
                user.setRemainingRequests(user.getRemainingRequests() - 1);
                userService.saveUser(user);
                return true;
            } else {
                return false; // Free trial requests exhausted
            }
        }
        return true; // Not on free trial, allow unlimited requests (or according to paid plan)
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
