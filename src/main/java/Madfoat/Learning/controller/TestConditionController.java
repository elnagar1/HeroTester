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

    @GetMapping("/api/health")
    @ResponseBody
    public String health() {
        return "Test Analyst AI Assistant is running!";
    }
}
