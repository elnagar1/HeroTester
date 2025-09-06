package Madfoat.Learning.controller;

import Madfoat.Learning.service.AIService;
import Madfoat.Learning.service.ImageProcessingService;
import Madfoat.Learning.service.ApiScenarioService;
import Madfoat.Learning.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class TestConditionController {

    @Autowired
    private AIService aiService;

    @Autowired
    private ImageProcessingService imageProcessingService;

    @Autowired
    private ApiScenarioService apiScenarioService;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/cto/results")
    public String ctoResults(Model model) {
        // Sample data for CTO results - in real implementation, this would come from a service
        model.addAttribute("issueTypes", Map.of(
            "Bug", 45,
            "Feature Request", 32,
            "Technical Debt", 18,
            "Performance Issue", 12,
            "Security Issue", 8
        ));
        
        model.addAttribute("statusDistribution", Map.of(
            "Open", 25,
            "In Progress", 35,
            "In Review", 20,
            "Resolved", 15,
            "Closed", 5
        ));
        
        model.addAttribute("priorityDistribution", Map.of(
            "Critical", 8,
            "High", 22,
            "Medium", 45,
            "Low", 25
        ));
        
        model.addAttribute("teamPerformance", Map.of(
            "Frontend Team", 85,
            "Backend Team", 78,
            "DevOps Team", 92,
            "QA Team", 88,
            "Mobile Team", 75
        ));
        
        return "cto-results";
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

    @PostMapping("/api/documentation/group/create")
    @ResponseBody
    public KnowledgeBaseService.DocumentationGroup createDocumentationGroup(@RequestParam("name") String name) {
        return knowledgeBaseService.createDocumentationGroup(name);
    }

    @PostMapping("/api/documentation/group/delete")
    @ResponseBody
    public Map<String, Object> deleteDocumentationGroup(@RequestParam("id") String id) {
        boolean ok = knowledgeBaseService.deleteDocumentationGroup(id);
        return Map.of("status", ok ? "ok" : "error");
    }

    @GetMapping("/api/documentation/groups")
    @ResponseBody
    public List<KnowledgeBaseService.DocumentationGroup> listDocumentationGroups() {
        return knowledgeBaseService.listDocumentationGroups();
    }

    @PostMapping("/api/documentation/delete")
    @ResponseBody
    public Map<String, Object> deleteApiDocumentation(@RequestParam("id") String id) {
        boolean ok = knowledgeBaseService.deleteApiDocumentation(id);
        return Map.of("status", ok ? "ok" : "error");
    }

    @PostMapping("/api/documentation/edit")
    @ResponseBody
    public Map<String, Object> editApiDocumentation(@RequestParam("id") String id,
                                                   @RequestParam(value = "title", required = false) String title,
                                                   @RequestParam(value = "description", required = false) String description,
                                                   @RequestParam(value = "notes", required = false) String notes,
                                                   @RequestParam(value = "tags", required = false) String tags,
                                                   @RequestParam(value = "status", required = false) String status,
                                                   @RequestParam(value = "lastTested", required = false) String lastTested) {
        boolean ok = knowledgeBaseService.updateApiDocumentation(id, title, description, notes, tags, status, lastTested);
        return Map.of("status", ok ? "ok" : "error");
    }

    @PostMapping("/api/documentation/move")
    @ResponseBody
    public Map<String, Object> moveApiDocumentation(@RequestParam("id") String id,
                                                   @RequestParam("groupId") String groupId) {
        boolean ok = knowledgeBaseService.moveApiDocumentation(id, groupId);
        return Map.of("status", ok ? "ok" : "error");
    }

    @PostMapping("/api/documentation/add")
    @ResponseBody
    public Map<String, Object> addApiDocumentation(@RequestParam("id") String id,
                                                  @RequestParam(value = "title", required = false) String title,
                                                  @RequestParam(value = "description", required = false) String description,
                                                  @RequestParam(value = "groupId", required = false) String groupId,
                                                  @RequestParam(value = "tags", required = false) String tags,
                                                  @RequestParam(value = "status", required = false) String status) {
        Map<String, Object> scenario = apiScenarioService.getScenarioDetail(id);
        if (scenario == null || !"ok".equals(scenario.get("status"))) {
            return Map.of("status", "error", "message", "Scenario not found");
        }
        String endpoint = String.valueOf(scenario.getOrDefault("url", ""));
        String method = String.valueOf(scenario.getOrDefault("method", ""));
        String docTitle = title != null ? title : String.valueOf(scenario.getOrDefault("title", method + " " + endpoint));
        StringBuilder content = new StringBuilder();
        content.append("Endpoint: ").append(endpoint).append("\n");
        content.append("Method: ").append(method).append("\n");
        if (description != null && !description.isBlank()) {
            content.append("Description: ").append(description).append("\n");
        } else if (scenario.get("desc") != null) {
            content.append("Description: ").append(scenario.get("desc")).append("\n");
        }
        content.append("Details: ").append(scenario.toString());
        // أضف كل بيانات السيناريو إلى extraMetadata
        Map<String, String> extraMetadata = new HashMap<>();
        extraMetadata.put("scenarioId", id);
        extraMetadata.put("body", String.valueOf(scenario.getOrDefault("body", "")));
        extraMetadata.put("headers", String.valueOf(scenario.getOrDefault("headers", "")));
        extraMetadata.put("expectedStatus", String.valueOf(scenario.getOrDefault("expectedStatus", "")));
        extraMetadata.put("assertions", String.valueOf(scenario.getOrDefault("assertions", "")));
        extraMetadata.put("curl", String.valueOf(scenario.getOrDefault("curl", "")));
        if (tags != null) extraMetadata.put("tags", tags);
        if (status != null) extraMetadata.put("status", status);
        knowledgeBaseService.ingestApiDocumentation(docTitle, content.toString(), endpoint, method, extraMetadata, groupId, description);
        return Map.of("status", "ok");
    }

    @GetMapping("/api/documentation/grouped")
    @ResponseBody
    public Map<String, Map<String, List<KnowledgeBaseService.Document>>> getApiDocumentationGrouped(@RequestParam(value = "groupId", required = false) String groupId) {
        return knowledgeBaseService.getApiDocumentationGrouped(groupId);
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
