package Madfoat.Learning.controller;

import Madfoat.Learning.dto.PerformanceTestRequest;
import Madfoat.Learning.dto.PerformanceTestPlan;
import Madfoat.Learning.service.PerformanceTestScenarioGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
            throw new RuntimeException("خطأ في توليد سيناريوهات الاختبار: " + e.getMessage());
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

    @GetMapping("/system-types")
    public ResponseEntity<Map<String, String>> getSystemTypes() {
        Map<String, String> systemTypes = new HashMap<>();
        systemTypes.put("E_COMMERCE", "E-Commerce Platform");
        systemTypes.put("BANKING", "Banking System");
        systemTypes.put("HEALTHCARE", "Healthcare System");
        systemTypes.put("EDUCATION", "Educational System");
        systemTypes.put("GOVERNMENT", "Government System");
        systemTypes.put("SOCIAL_MEDIA", "Social Media Platform");
        systemTypes.put("GAMING", "Gaming System");
        systemTypes.put("ENTERPRISE", "Enterprise System");
        return ResponseEntity.ok(systemTypes);
    }

    @GetMapping("/deployment-environments")
    public ResponseEntity<Map<String, String>> getDeploymentEnvironments() {
        Map<String, String> environments = new HashMap<>();
        environments.put("DEVELOPMENT", "Development Environment");
        environments.put("STAGING", "Staging Environment");
        environments.put("PRODUCTION_LIKE", "Production-like Environment");
        environments.put("PRODUCTION", "Production Environment");
        return ResponseEntity.ok(environments);
    }
}