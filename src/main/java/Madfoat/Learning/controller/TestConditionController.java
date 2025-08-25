package Madfoat.Learning.controller;

import Madfoat.Learning.service.AIService;
import Madfoat.Learning.service.ImageProcessingService;
import Madfoat.Learning.service.ApiScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    // Unified generator backing the UI tab "/generate" form
    @PostMapping("/generate")
    public String generateUnified(@RequestParam("generationTypes") List<String> generationTypes,
                                  @RequestParam(value = "includeAcceptance", required = false, defaultValue = "false") boolean includeAcceptance,
                                  @RequestParam(value = "selectedTypes", required = false) List<String> selectedTypes,
                                  @RequestParam(value = "businessText", required = false) String businessText,
                                  @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                  Model model) {
        try {
            // Prefer text if provided; otherwise try image
            String input; String inputType;
            if (businessText != null && !businessText.trim().isEmpty()) {
                input = businessText; inputType = "text";
            } else if (imageFile != null && !imageFile.isEmpty()) {
                input = imageProcessingService.getImageAnalysisContext(imageFile); inputType = "image";
            } else {
                model.addAttribute("error", "Please provide text or an image/document");
                return "index";
            }

            // For first type in list, generate initial content for results page
            String firstType = generationTypes == null || generationTypes.isEmpty() ? "test_cases" : generationTypes.get(0);
            String content = aiService.generateContent(input, inputType, firstType, includeAcceptance, selectedTypes);

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

    @PostMapping("/generate-from-image")
    public String generateFromImage(@RequestParam("imageFile") MultipartFile imageFile, Model model) {
        if (imageFile == null || imageFile.isEmpty()) {
            model.addAttribute("error", "Please select an image file");
            return "index";
        }

        if (!imageProcessingService.isValidImageFile(imageFile)) {
            model.addAttribute("error", "Please upload a valid image file (JPEG, PNG, GIF, BMP)");
            return "index";
        }

        try {
            String imageContext = imageProcessingService.getImageAnalysisContext(imageFile);
            String testConditions = aiService.generateTestConditions(imageContext, "image");
            model.addAttribute("input", imageContext);
            model.addAttribute("inputType", "Image Analysis");
            model.addAttribute("testConditions", testConditions);
            model.addAttribute("fileName", imageFile.getOriginalFilename());
            return "results";
        } catch (Exception e) {
            model.addAttribute("error", "Error processing image and generating test conditions: " + e.getMessage());
            return "index";
        }
    }

    @PostMapping("/generate-automation")
    public String generateAutomation(@RequestParam("scenarioText") String scenarioText, Model model) {
        if (scenarioText == null || scenarioText.trim().isEmpty()) {
            model.addAttribute("error", "Please provide a scenario description");
            return "index";
        }
        try {
            String automationCode = aiService.generateAutomationScripts(scenarioText);
            model.addAttribute("automationCode", automationCode);
            return "automation";
        } catch (Exception e) {
            model.addAttribute("error", "Error generating automation scripts: " + e.getMessage());
            return "index";
        }
    }

    @PostMapping("/api/generate-scenarios")
    public String generateApiScenarios(@RequestParam("curl") String curl,
                                       @RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "caseTypes", required = false) List<String> caseTypes,
                                       Model model) {
        try {
            List<Map<String, Object>> scenarios = apiScenarioService.generateScenarios(curl, limit, caseTypes);
            model.addAttribute("scenarios", scenarios);
            return "api-scenarios";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }

    @PostMapping("/api/run-scenario")
    @ResponseBody
    public Map<String, Object> runScenario(@RequestParam("id") String id) {
        return apiScenarioService.runScenario(id);
    }

    @GetMapping("/api/scenario-code")
    @ResponseBody
    public Map<String, String> getScenarioCode(@RequestParam("id") String id) {
        return apiScenarioService.getScenarioCode(id);
    }

    @GetMapping("/api/scenario-detail")
    @ResponseBody
    public Map<String, Object> getScenarioDetail(@RequestParam("id") String id) {
        return apiScenarioService.getScenarioDetail(id);
    }

    @PostMapping("/api/scenario-assert")
    @ResponseBody
    public Map<String, String> setScenarioAssert(@RequestParam("id") String id,
                                                 @RequestParam(value = "expected", required = false) String expected) {
        return apiScenarioService.setScenarioAssertion(id, expected);
    }

    @PostMapping("/api/scenario-assertions")
    @ResponseBody
    public Map<String, String> setScenarioAssertions(@RequestBody Map<String, Object> payload) {
        String id = String.valueOf(payload.getOrDefault("id", ""));
        @SuppressWarnings("unchecked")
        Map<String, Object> assertions = (Map<String, Object>) payload.get("assertions");
        Integer expectedStatus = null;
        Object exp = payload.get("expectedStatus");
        if (exp instanceof Number n) expectedStatus = n.intValue();
        else if (exp != null) {
            try { expectedStatus = Integer.parseInt(String.valueOf(exp)); } catch (Exception ignored) {}
        }
        return apiScenarioService.setScenarioAssertions(id, assertions, expectedStatus);
    }

    @GetMapping("/api/health")
    @ResponseBody
    public String health() {
        return "Test Analyst AI Assistant is running!";
    }

    // API used by results.html for generating more content dynamically
    @PostMapping("/api/generate-more")
    @ResponseBody
    public String generateMore(@RequestBody Map<String, Object> payload) {
        String input = String.valueOf(payload.getOrDefault("input", ""));
        String inputType = String.valueOf(payload.getOrDefault("inputType", "text"));
        String generationType = String.valueOf(payload.getOrDefault("generationType", "test_cases"));
        boolean includeAcceptance = Boolean.TRUE.equals(payload.get("includeAcceptance"));
        @SuppressWarnings("unchecked")
        List<String> selectedTypes = (List<String>) payload.get("selectedTypes");
        return aiService.generateContent(input, inputType, generationType, includeAcceptance, selectedTypes);
    }

    // API for row Q&A in results.html
    @PostMapping("/api/ask-row")
    @ResponseBody
    public String askRow(@RequestBody Map<String, String> payload) {
        String rowText = payload.getOrDefault("rowText", "");
        String question = payload.getOrDefault("question", "");
        String prompt = "Given this test row, answer the user's question clearly.\n\n" +
                "Row:\n" + rowText + "\n\n" +
                "Question:\n" + question + "\n\n" +
                "Answer:";
        return aiService.generateWithPrompt(prompt);
    }
}
