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
            "STRESS", "اختبار الضغط - تحديد نقطة الانهيار",
            "SOAK", "اختبار التحمل - اختبار الاستقرار على المدى الطويل",
            "VOLUME", "اختبار الحجم - اختبار مع البيانات الضخمة",
            "SPIKE", "اختبار الذروة - اختبار الاستجابة للزيادة المفاجئة",
            "SCALABILITY", "اختبار قابلية التوسع - اختبار الأداء مع إضافة موارد"
        ));
        response.put("commonOperations", Map.of(
            "تسجيل الدخول", "عمليات تسجيل الدخول والمصادقة",
            "البحث", "عمليات البحث والاستعلام",
            "إضافة منتج", "عمليات إضافة وتعديل البيانات",
            "عرض الصفحة", "عمليات عرض الصفحات والواجهات",
            "تحميل ملف", "عمليات رفع وتحميل الملفات"
        ));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/system-types")
    public ResponseEntity<Map<String, String>> getSystemTypes() {
        Map<String, String> systemTypes = new HashMap<>();
        systemTypes.put("E_COMMERCE", "متجر إلكتروني");
        systemTypes.put("BANKING", "نظام مصرفي");
        systemTypes.put("HEALTHCARE", "نظام صحي");
        systemTypes.put("EDUCATION", "نظام تعليمي");
        systemTypes.put("GOVERNMENT", "نظام حكومي");
        systemTypes.put("SOCIAL_MEDIA", "وسائل التواصل الاجتماعي");
        systemTypes.put("GAMING", "نظام ألعاب");
        systemTypes.put("ENTERPRISE", "نظام مؤسسي");
        return ResponseEntity.ok(systemTypes);
    }

    @GetMapping("/deployment-environments")
    public ResponseEntity<Map<String, String>> getDeploymentEnvironments() {
        Map<String, String> environments = new HashMap<>();
        environments.put("DEVELOPMENT", "بيئة التطوير");
        environments.put("STAGING", "بيئة الاختبار");
        environments.put("PRODUCTION_LIKE", "بيئة مشابهة للإنتاج");
        environments.put("PRODUCTION", "بيئة الإنتاج");
        return ResponseEntity.ok(environments);
    }
}