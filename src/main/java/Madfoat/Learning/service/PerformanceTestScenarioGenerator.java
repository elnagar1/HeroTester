package Madfoat.Learning.service;

import Madfoat.Learning.dto.PerformanceTestRequest;
import Madfoat.Learning.dto.PerformanceTestScenario;
import Madfoat.Learning.dto.PerformanceTestPlan;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PerformanceTestScenarioGenerator {

    private static final Map<String, List<String>> SCENARIO_TEMPLATES = new HashMap<>();
    private static final Map<String, Map<String, Object>> OPERATION_WEIGHTS = new HashMap<>();

    static {
        // تعريف قوالب السيناريوهات
        SCENARIO_TEMPLATES.put("STRESS", Arrays.asList(
            "زيادة تدريجية في عدد المستخدمين حتى نقطة الانهيار",
            "اختبار استقرار النظام تحت الضغط العالي",
            "قياس أداء النظام عند الحد الأقصى للمستخدمين"
        ));
        
        SCENARIO_TEMPLATES.put("SOAK", Arrays.asList(
            "اختبار النظام لفترة طويلة (24 ساعة)",
            "مراقبة تسرب الذاكرة والموارد",
            "اختبار استقرار الأداء على المدى الطويل"
        ));
        
        SCENARIO_TEMPLATES.put("VOLUME", Arrays.asList(
            "اختبار النظام مع كميات كبيرة من البيانات",
            "قياس أداء قاعدة البيانات تحت الحمل العالي",
            "اختبار استجابة النظام للبيانات الضخمة"
        ));
        
        SCENARIO_TEMPLATES.put("SPIKE", Arrays.asList(
            "زيادة مفاجئة في عدد المستخدمين",
            "اختبار استجابة النظام للذروات المفاجئة",
            "قياس وقت التعافي من الذروات"
        ));
        
        SCENARIO_TEMPLATES.put("SCALABILITY", Arrays.asList(
            "اختبار قابلية التوسع الأفقي للنظام",
            "قياس الأداء مع إضافة موارد جديدة",
            "اختبار توزيع الحمل على الخوادم"
        ));

        // تعريف أوزان العمليات المختلفة
        OPERATION_WEIGHTS.put("تسجيل الدخول", Map.of("cpu_weight", 0.3, "memory_weight", 0.2, "db_weight", 0.5));
        OPERATION_WEIGHTS.put("البحث", Map.of("cpu_weight", 0.4, "memory_weight", 0.3, "db_weight", 0.8));
        OPERATION_WEIGHTS.put("إضافة منتج", Map.of("cpu_weight", 0.5, "memory_weight", 0.4, "db_weight", 0.7));
        OPERATION_WEIGHTS.put("عرض الصفحة", Map.of("cpu_weight", 0.2, "memory_weight", 0.3, "db_weight", 0.3));
        OPERATION_WEIGHTS.put("تحميل ملف", Map.of("cpu_weight", 0.6, "memory_weight", 0.7, "db_weight", 0.2));
    }

    public PerformanceTestPlan generateTestPlan(PerformanceTestRequest request) {
        PerformanceTestPlan plan = new PerformanceTestPlan();
        plan.setPlanId(generatePlanId());
        plan.setPlanName("خطة اختبار الأداء - " + request.getSystemType());
        plan.setSystemName(request.getSystemType());
        plan.setCreatedBy("AI Generator");
        
        List<PerformanceTestScenario> scenarios = new ArrayList<>();
        
        // توليد سيناريوهات بناءً على نوع النظام والمتطلبات
        scenarios.addAll(generateStressTestScenarios(request));
        scenarios.addAll(generateSoakTestScenarios(request));
        scenarios.addAll(generateVolumeTestScenarios(request));
        scenarios.addAll(generateSpikeTestScenarios(request));
        scenarios.addAll(generateScalabilityTestScenarios(request));
        
        plan.setScenarios(scenarios);
        plan.setSystemRequirements(analyzeSystemRequirements(request));
        plan.setTestEnvironment(determineTestEnvironment(request));
        plan.setExecutionStrategy(generateExecutionStrategy(request, scenarios));
        plan.setPrerequisites(generatePrerequisites(request));
        plan.setTestDataRequirements(generateTestDataRequirements(request));
        plan.setEstimatedDuration(calculateEstimatedDuration(scenarios));
        plan.setRiskAssessment(assessRisks(request, scenarios));
        
        return plan;
    }

    private List<PerformanceTestScenario> generateStressTestScenarios(PerformanceTestRequest request) {
        List<PerformanceTestScenario> scenarios = new ArrayList<>();
        
        // حساب نقطة الانهيار المتوقعة
        int breakpoint = calculateBreakpoint(request.getExpectedUsers());
        
        PerformanceTestScenario stressScenario = new PerformanceTestScenario();
        stressScenario.setScenarioId("STRESS_001");
        stressScenario.setScenarioName("اختبار الضغط - نقطة الانهيار");
        stressScenario.setScenarioType("STRESS");
        stressScenario.setDescription("اختبار النظام تحت ضغط عالي حتى نقطة الانهيار");
        stressScenario.setTargetUsers(breakpoint);
        stressScenario.setRampUpTime(calculateRampUpTime(breakpoint));
        stressScenario.setTestDuration(30); // 30 دقيقة
        stressScenario.setPriority(5);
        
        List<String> steps = new ArrayList<>();
        steps.add("بدء بـ " + (breakpoint * 0.2) + " مستخدم");
        steps.add("زيادة تدريجية إلى " + (breakpoint * 0.5) + " مستخدم");
        steps.add("زيادة إلى " + (breakpoint * 0.8) + " مستخدم");
        steps.add("زيادة إلى " + breakpoint + " مستخدم (نقطة الانهيار المتوقعة)");
        steps.add("مراقبة استجابة النظام");
        stressScenario.setTestSteps(steps);
        
        Map<String, Object> params = new HashMap<>();
        params.put("breakpoint", breakpoint);
        params.put("ramp_up_percentage", 20);
        params.put("monitoring_interval", 30); // seconds
        stressScenario.setParameters(params);
        
        List<String> successCriteria = Arrays.asList(
            "تحديد نقطة الانهيار بدقة",
            "قياس وقت الاستجابة عند كل مستوى",
            "تحديد أول مؤشر للتراجع في الأداء"
        );
        stressScenario.setSuccessCriteria(successCriteria);
        
        List<String> monitoringPoints = Arrays.asList(
            "استخدام CPU",
            "استخدام الذاكرة",
            "وقت الاستجابة",
            "معدل الأخطاء",
            "استخدام الشبكة"
        );
        stressScenario.setMonitoringPoints(monitoringPoints);
        
        stressScenario.setExpectedOutcome("تحديد الحد الأقصى للمستخدمين المتزامنين");
        
        scenarios.add(stressScenario);
        return scenarios;
    }

    private List<PerformanceTestScenario> generateSoakTestScenarios(PerformanceTestRequest request) {
        List<PerformanceTestScenario> scenarios = new ArrayList<>();
        
        PerformanceTestScenario soakScenario = new PerformanceTestScenario();
        soakScenario.setScenarioId("SOAK_001");
        soakScenario.setScenarioName("اختبار التحمل - 24 ساعة");
        soakScenario.setScenarioType("SOAK");
        soakScenario.setDescription("اختبار استقرار النظام على المدى الطويل");
        soakScenario.setTargetUsers((int) (request.getExpectedUsers() * 0.7)); // 70% من المستخدمين المتوقعين
        soakScenario.setRampUpTime(30); // 30 دقيقة
        soakScenario.setTestDuration(1440); // 24 ساعة
        soakScenario.setPriority(4);
        
        List<String> steps = new ArrayList<>();
        steps.add("بدء بـ " + (soakScenario.getTargetUsers() * 0.5) + " مستخدم");
        steps.add("زيادة إلى " + soakScenario.getTargetUsers() + " مستخدم");
        steps.add("الحفاظ على الحمل لمدة 24 ساعة");
        steps.add("مراقبة استهلاك الموارد");
        soakScenario.setTestSteps(steps);
        
        Map<String, Object> params = new HashMap<>();
        params.put("monitoring_interval", 300); // 5 minutes
        params.put("memory_leak_threshold", 10); // 10% increase
        params.put("cpu_threshold", 80); // 80%
        soakScenario.setParameters(params);
        
        List<String> successCriteria = Arrays.asList(
            "عدم وجود تسرب في الذاكرة",
            "استقرار استجابة النظام",
            "عدم وجود أخطاء متزايدة"
        );
        soakScenario.setSuccessCriteria(successCriteria);
        
        List<String> monitoringPoints = Arrays.asList(
            "استخدام الذاكرة مع مرور الوقت",
            "استخدام CPU",
            "عدد الأخطاء",
            "وقت الاستجابة",
            "استخدام القرص"
        );
        soakScenario.setMonitoringPoints(monitoringPoints);
        
        soakScenario.setExpectedOutcome("تأكيد استقرار النظام على المدى الطويل");
        
        scenarios.add(soakScenario);
        return scenarios;
    }

    private List<PerformanceTestScenario> generateVolumeTestScenarios(PerformanceTestRequest request) {
        List<PerformanceTestScenario> scenarios = new ArrayList<>();
        
        PerformanceTestScenario volumeScenario = new PerformanceTestScenario();
        volumeScenario.setScenarioId("VOLUME_001");
        volumeScenario.setScenarioName("اختبار الحجم - البيانات الضخمة");
        volumeScenario.setScenarioType("VOLUME");
        volumeScenario.setDescription("اختبار أداء النظام مع كميات كبيرة من البيانات");
        volumeScenario.setTargetUsers(request.getExpectedUsers());
        volumeScenario.setRampUpTime(20);
        volumeScenario.setTestDuration(60);
        volumeScenario.setPriority(4);
        
        List<String> steps = new ArrayList<>();
        steps.add("تحميل " + (request.getDataVolume() * 0.5) + " سجل في قاعدة البيانات");
        steps.add("بدء الاختبار بـ " + request.getExpectedUsers() + " مستخدم");
        steps.add("تنفيذ عمليات البحث والقراءة");
        steps.add("مراقبة أداء قاعدة البيانات");
        volumeScenario.setTestSteps(steps);
        
        Map<String, Object> params = new HashMap<>();
        params.put("data_volume", request.getDataVolume());
        params.put("query_complexity", "HIGH");
        params.put("index_optimization", true);
        volumeScenario.setParameters(params);
        
        List<String> successCriteria = Arrays.asList(
            "وقت استجابة البحث أقل من 2 ثانية",
            "استخدام قاعدة البيانات أقل من 80%",
            "عدم وجود أخطاء في الاستعلامات"
        );
        volumeScenario.setSuccessCriteria(successCriteria);
        
        List<String> monitoringPoints = Arrays.asList(
            "وقت استجابة قاعدة البيانات",
            "استخدام القرص",
            "استخدام الذاكرة",
            "عدد الاستعلامات في الثانية",
            "وقت تنفيذ الاستعلامات المعقدة"
        );
        volumeScenario.setMonitoringPoints(monitoringPoints);
        
        volumeScenario.setExpectedOutcome("تأكيد قدرة النظام على التعامل مع البيانات الضخمة");
        
        scenarios.add(volumeScenario);
        return scenarios;
    }

    private List<PerformanceTestScenario> generateSpikeTestScenarios(PerformanceTestRequest request) {
        List<PerformanceTestScenario> scenarios = new ArrayList<>();
        
        PerformanceTestScenario spikeScenario = new PerformanceTestScenario();
        spikeScenario.setScenarioId("SPIKE_001");
        spikeScenario.setScenarioName("اختبار الذروة المفاجئة");
        spikeScenario.setScenarioType("SPIKE");
        spikeScenario.setDescription("اختبار استجابة النظام للزيادة المفاجئة في الحمل");
        spikeScenario.setTargetUsers((int) (request.getExpectedUsers() * 2)); // ضعف المستخدمين المتوقعين
        spikeScenario.setRampUpTime(5); // 5 دقائق فقط
        spikeScenario.setTestDuration(20);
        spikeScenario.setPriority(3);
        
        List<String> steps = new ArrayList<>();
        steps.add("بدء بـ " + request.getExpectedUsers() + " مستخدم");
        steps.add("زيادة مفاجئة إلى " + spikeScenario.getTargetUsers() + " مستخدم في 5 دقائق");
        steps.add("الحفاظ على الحمل العالي لمدة 10 دقائق");
        steps.add("تقليل الحمل تدريجياً");
        spikeScenario.setTestSteps(steps);
        
        Map<String, Object> params = new HashMap<>();
        params.put("spike_multiplier", 2.0);
        params.put("recovery_time_threshold", 300); // 5 minutes
        params.put("graceful_degradation", true);
        spikeScenario.setParameters(params);
        
        List<String> successCriteria = Arrays.asList(
            "النظام لا ينهار عند الذروة",
            "وقت التعافي أقل من 5 دقائق",
            "الحفاظ على الوظائف الأساسية"
        );
        spikeScenario.setSuccessCriteria(successCriteria);
        
        List<String> monitoringPoints = Arrays.asList(
            "وقت الاستجابة أثناء الذروة",
            "معدل الأخطاء",
            "استخدام الموارد",
            "وقت التعافي",
            "تأثير على المستخدمين الحاليين"
        );
        spikeScenario.setMonitoringPoints(monitoringPoints);
        
        spikeScenario.setExpectedOutcome("تأكيد قدرة النظام على التعامل مع الذروات المفاجئة");
        
        scenarios.add(spikeScenario);
        return scenarios;
    }

    private List<PerformanceTestScenario> generateScalabilityTestScenarios(PerformanceTestRequest request) {
        List<PerformanceTestScenario> scenarios = new ArrayList<>();
        
        PerformanceTestScenario scalabilityScenario = new PerformanceTestScenario();
        scalabilityScenario.setScenarioId("SCALABILITY_001");
        scalabilityScenario.setScenarioName("اختبار قابلية التوسع");
        scalabilityScenario.setScenarioType("SCALABILITY");
        scalabilityScenario.setDescription("اختبار أداء النظام مع إضافة موارد جديدة");
        scalabilityScenario.setTargetUsers(request.getExpectedUsers());
        scalabilityScenario.setRampUpTime(15);
        scalabilityScenario.setTestDuration(45);
        scalabilityScenario.setPriority(3);
        
        List<String> steps = new ArrayList<>();
        steps.add("بدء الاختبار مع خادم واحد");
        steps.add("إضافة خادم ثاني أثناء الاختبار");
        steps.add("إضافة خادم ثالث إذا لزم الأمر");
        steps.add("مراقبة توزيع الحمل");
        scalabilityScenario.setTestSteps(steps);
        
        Map<String, Object> params = new HashMap<>();
        params.put("initial_servers", 1);
        params.put("max_servers", 3);
        params.put("load_balancing", true);
        params.put("auto_scaling", false);
        scalabilityScenario.setParameters(params);
        
        List<String> successCriteria = Arrays.asList(
            "تحسن الأداء مع إضافة الخوادم",
            "توزيع متوازن للحمل",
            "عدم وجود توقف في الخدمة"
        );
        scalabilityScenario.setSuccessCriteria(successCriteria);
        
        List<String> monitoringPoints = Arrays.asList(
            "توزيع الحمل على الخوادم",
            "وقت الاستجابة لكل خادم",
            "استخدام الموارد لكل خادم",
            "وقت إضافة الخوادم الجديدة",
            "تأثير التوسع على الأداء العام"
        );
        scalabilityScenario.setMonitoringPoints(monitoringPoints);
        
        scalabilityScenario.setExpectedOutcome("تأكيد قابلية النظام للتوسع الأفقي");
        
        scenarios.add(scalabilityScenario);
        return scenarios;
    }

    private int calculateBreakpoint(int expectedUsers) {
        // حساب نقطة الانهيار بناءً على عدد المستخدمين المتوقعين
        return (int) (expectedUsers * 1.5); // 150% من المستخدمين المتوقعين
    }

    private int calculateRampUpTime(int targetUsers) {
        // حساب وقت الزيادة التدريجية بناءً على عدد المستخدمين
        if (targetUsers <= 100) return 10;
        else if (targetUsers <= 500) return 20;
        else if (targetUsers <= 1000) return 30;
        else return 45;
    }

    private Map<String, Object> analyzeSystemRequirements(PerformanceTestRequest request) {
        Map<String, Object> requirements = new HashMap<>();
        requirements.put("expected_concurrent_users", request.getExpectedUsers());
        requirements.put("system_type", request.getSystemType());
        requirements.put("data_volume", request.getDataVolume());
        requirements.put("deployment_environment", request.getDeploymentEnvironment());
        requirements.put("common_operations", request.getCommonOperations());
        requirements.put("peak_times", request.getPeakTimes());
        return requirements;
    }

    private String determineTestEnvironment(PerformanceTestRequest request) {
        if ("PRODUCTION_LIKE".equals(request.getDeploymentEnvironment())) {
            return "بيئة مشابهة للإنتاج";
        } else if ("STAGING".equals(request.getDeploymentEnvironment())) {
            return "بيئة الاختبار";
        } else {
            return "بيئة التطوير";
        }
    }

    private String generateExecutionStrategy(PerformanceTestRequest request, List<PerformanceTestScenario> scenarios) {
        StringBuilder strategy = new StringBuilder();
        strategy.append("استراتيجية التنفيذ:\n");
        strategy.append("1. تنفيذ السيناريوهات بالترتيب التالي:\n");
        strategy.append("   - اختبار التحمل (SOAK) أولاً\n");
        strategy.append("   - اختبار الحجم (VOLUME)\n");
        strategy.append("   - اختبار الضغط (STRESS)\n");
        strategy.append("   - اختبار الذروة (SPIKE)\n");
        strategy.append("   - اختبار قابلية التوسع (SCALABILITY)\n");
        strategy.append("2. مراقبة مستمرة للموارد\n");
        strategy.append("3. إيقاف الاختبار عند حدوث مشاكل خطيرة\n");
        return strategy.toString();
    }

    private List<String> generatePrerequisites(PerformanceTestRequest request) {
        List<String> prerequisites = new ArrayList<>();
        prerequisites.add("إعداد بيئة الاختبار");
        prerequisites.add("تحضير بيانات الاختبار");
        prerequisites.add("إعداد أدوات المراقبة");
        prerequisites.add("تأمين موارد النظام الكافية");
        prerequisites.add("إعداد خطة الطوارئ");
        return prerequisites;
    }

    private Map<String, String> generateTestDataRequirements(PerformanceTestRequest request) {
        Map<String, String> requirements = new HashMap<>();
        requirements.put("user_data", "بيانات " + request.getExpectedUsers() + " مستخدم");
        requirements.put("transaction_data", "بيانات المعاملات للعمليات الشائعة");
        requirements.put("search_data", "بيانات البحث المتنوعة");
        requirements.put("file_data", "ملفات بأحجام مختلفة للاختبار");
        return requirements;
    }

    private String calculateEstimatedDuration(List<PerformanceTestScenario> scenarios) {
        int totalMinutes = scenarios.stream()
                .mapToInt(PerformanceTestScenario::getTestDuration)
                .sum();
        
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        
        return hours + " ساعة و " + minutes + " دقيقة";
    }

    private String assessRisks(PerformanceTestRequest request, List<PerformanceTestScenario> scenarios) {
        StringBuilder risks = new StringBuilder();
        risks.append("تقييم المخاطر:\n");
        risks.append("1. مخاطر عالية: اختبار الضغط قد يسبب توقف النظام\n");
        risks.append("2. مخاطر متوسطة: اختبار التحمل قد يستهلك موارد كثيرة\n");
        risks.append("3. مخاطر منخفضة: اختبار الحجم قد يؤثر على الأداء مؤقتاً\n");
        risks.append("4. توصيات: إعداد خطة استرداد ومراقبة مستمرة\n");
        return risks.toString();
    }

    private String generatePlanId() {
        return "PERF_PLAN_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}