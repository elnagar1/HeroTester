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
        // Scenario templates definition
        SCENARIO_TEMPLATES.put("STRESS", Arrays.asList(
            "Gradually increase the number of users until the system breaks",
            "Test system stability under high load",
            "Measure system performance at maximum user capacity"
        ));
        
        SCENARIO_TEMPLATES.put("SOAK", Arrays.asList(
            "Test the system for an extended period (24 hours)",
            "Monitor for memory and resource leaks",
            "Test long-term performance stability"
        ));
        
        SCENARIO_TEMPLATES.put("VOLUME", Arrays.asList(
            "Test the system with large volumes of data",
            "Measure database performance under heavy load",
            "Test system response to big data"
        ));
        
        SCENARIO_TEMPLATES.put("SPIKE", Arrays.asList(
            "Sudden increase in the number of users",
            "Test system response to sudden spikes",
            "Measure recovery time after spikes"
        ));
        
        SCENARIO_TEMPLATES.put("SCALABILITY", Arrays.asList(
            "Test horizontal scalability of the system",
            "Measure performance with added resources",
            "Test load distribution across servers"
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
        plan.setPlanName("Performance Test Plan - " + request.getSystemType());
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
        stressScenario.setScenarioName("Stress Test - Breakpoint");
        stressScenario.setScenarioType("STRESS");
        stressScenario.setDescription("Test the system under high load until the breakpoint is reached");
        stressScenario.setTargetUsers(breakpoint);
        stressScenario.setRampUpTime(calculateRampUpTime(breakpoint));
        stressScenario.setTestDuration(30); // 30 minutes
        stressScenario.setPriority(5);
        
        List<String> steps = new ArrayList<>();
        steps.add("Start with " + (breakpoint * 0.2) + " users");
        steps.add("Gradually increase to " + (breakpoint * 0.5) + " users");
        steps.add("Increase to " + (breakpoint * 0.8) + " users");
        steps.add("Increase to " + breakpoint + " users (expected breakpoint)");
        steps.add("Monitor system response");
        stressScenario.setTestSteps(steps);
        
        Map<String, Object> params = new HashMap<>();
        params.put("breakpoint", breakpoint);
        params.put("ramp_up_percentage", 20);
        params.put("monitoring_interval", 30); // seconds
        stressScenario.setParameters(params);
        
        List<String> successCriteria = Arrays.asList(
            "Accurately identify the system's breakpoint",
            "Measure response time at each level",
            "Detect the first sign of performance degradation"
        );
        stressScenario.setSuccessCriteria(successCriteria);
        
        List<String> monitoringPoints = Arrays.asList(
            "CPU usage",
            "Memory usage",
            "Response time",
            "Error rate",
            "Network usage"
        );
        stressScenario.setMonitoringPoints(monitoringPoints);
        
        stressScenario.setExpectedOutcome("Determine the maximum number of concurrent users the system can handle");
        
        scenarios.add(stressScenario);
        return scenarios;
    }

    private List<PerformanceTestScenario> generateSoakTestScenarios(PerformanceTestRequest request) {
        List<PerformanceTestScenario> scenarios = new ArrayList<>();
        
        PerformanceTestScenario soakScenario = new PerformanceTestScenario();
        soakScenario.setScenarioId("SOAK_001");
        soakScenario.setScenarioName("Soak Test - 24 Hours");
        soakScenario.setScenarioType("SOAK");
        soakScenario.setDescription("Test the system's stability over a long period");
        soakScenario.setTargetUsers((int) (request.getExpectedUsers() * 0.7)); // 70% of expected users
        soakScenario.setRampUpTime(30); // 30 minutes
        soakScenario.setTestDuration(1440); // 24 hours
        soakScenario.setPriority(4);
        
        List<String> steps = new ArrayList<>();
        steps.add("Start with " + (soakScenario.getTargetUsers() * 0.5) + " users");
        steps.add("Increase to " + soakScenario.getTargetUsers() + " users");
        steps.add("Maintain load for 24 hours");
        steps.add("Monitor resource consumption");
        soakScenario.setTestSteps(steps);
        
        Map<String, Object> params = new HashMap<>();
        params.put("monitoring_interval", 300); // 5 minutes
        params.put("memory_leak_threshold", 10); // 10% increase
        params.put("cpu_threshold", 80); // 80%
        soakScenario.setParameters(params);
        
        List<String> successCriteria = Arrays.asList(
            "No memory leaks detected",
            "Stable system response",
            "No increasing error rate"
        );
        soakScenario.setSuccessCriteria(successCriteria);
        
        List<String> monitoringPoints = Arrays.asList(
            "Memory usage over time",
            "CPU usage",
            "Error count",
            "Response time",
            "Disk usage"
        );
        soakScenario.setMonitoringPoints(monitoringPoints);
        
        soakScenario.setExpectedOutcome("Confirm system stability over a long period");
        
        scenarios.add(soakScenario);
        return scenarios;
    }

    private List<PerformanceTestScenario> generateVolumeTestScenarios(PerformanceTestRequest request) {
        List<PerformanceTestScenario> scenarios = new ArrayList<>();
        
        PerformanceTestScenario volumeScenario = new PerformanceTestScenario();
        volumeScenario.setScenarioId("VOLUME_001");
        volumeScenario.setScenarioName("Volume Test - Big Data");
        volumeScenario.setScenarioType("VOLUME");
        volumeScenario.setDescription("Test system performance with large volumes of data");
        volumeScenario.setTargetUsers(request.getExpectedUsers());
        volumeScenario.setRampUpTime(20);
        volumeScenario.setTestDuration(60);
        volumeScenario.setPriority(4);
        
        List<String> steps = new ArrayList<>();
        steps.add("Load " + (request.getDataVolume() * 0.5) + " records into the database");
        steps.add("Start the test with " + request.getExpectedUsers() + " users");
        steps.add("Perform search and read operations");
        steps.add("Monitor database performance");
        volumeScenario.setTestSteps(steps);
        
        Map<String, Object> params = new HashMap<>();
        params.put("data_volume", request.getDataVolume());
        params.put("query_complexity", "HIGH");
        params.put("index_optimization", true);
        volumeScenario.setParameters(params);
        
        List<String> successCriteria = Arrays.asList(
            "Search response time less than 2 seconds",
            "Database usage below 80%",
            "No query errors"
        );
        volumeScenario.setSuccessCriteria(successCriteria);
        
        List<String> monitoringPoints = Arrays.asList(
            "Database response time",
            "Disk usage",
            "Memory usage",
            "Queries per second",
            "Complex query execution time"
        );
        volumeScenario.setMonitoringPoints(monitoringPoints);
        
        volumeScenario.setExpectedOutcome("Confirm the system can handle big data efficiently");
        
        scenarios.add(volumeScenario);
        return scenarios;
    }

    private List<PerformanceTestScenario> generateSpikeTestScenarios(PerformanceTestRequest request) {
        List<PerformanceTestScenario> scenarios = new ArrayList<>();
        
        PerformanceTestScenario spikeScenario = new PerformanceTestScenario();
        spikeScenario.setScenarioId("SPIKE_001");
        spikeScenario.setScenarioName("Spike Test - Sudden Load");
        spikeScenario.setScenarioType("SPIKE");
        spikeScenario.setDescription("Test system response to a sudden increase in load");
        spikeScenario.setTargetUsers((int) (request.getExpectedUsers() * 2)); // double expected users
        spikeScenario.setRampUpTime(5); // 5 minutes
        spikeScenario.setTestDuration(20);
        spikeScenario.setPriority(3);
        
        List<String> steps = new ArrayList<>();
        steps.add("Start with " + request.getExpectedUsers() + " users");
        steps.add("Suddenly increase to " + spikeScenario.getTargetUsers() + " users in 5 minutes");
        steps.add("Maintain high load for 10 minutes");
        steps.add("Gradually decrease the load");
        spikeScenario.setTestSteps(steps);
        
        Map<String, Object> params = new HashMap<>();
        params.put("spike_multiplier", 2.0);
        params.put("recovery_time_threshold", 300); // 5 minutes
        params.put("graceful_degradation", true);
        spikeScenario.setParameters(params);
        
        List<String> successCriteria = Arrays.asList(
            "System does not crash during spike",
            "Recovery time less than 5 minutes",
            "Core functionalities remain available"
        );
        spikeScenario.setSuccessCriteria(successCriteria);
        
        List<String> monitoringPoints = Arrays.asList(
            "Response time during spike",
            "Error rate",
            "Resource usage",
            "Recovery time",
            "Impact on current users"
        );
        spikeScenario.setMonitoringPoints(monitoringPoints);
        
        spikeScenario.setExpectedOutcome("Confirm the system can handle sudden spikes in load");
        
        scenarios.add(spikeScenario);
        return scenarios;
    }

    private List<PerformanceTestScenario> generateScalabilityTestScenarios(PerformanceTestRequest request) {
        List<PerformanceTestScenario> scenarios = new ArrayList<>();
        
        PerformanceTestScenario scalabilityScenario = new PerformanceTestScenario();
        scalabilityScenario.setScenarioId("SCALABILITY_001");
        scalabilityScenario.setScenarioName("Scalability Test");
        scalabilityScenario.setScenarioType("SCALABILITY");
        scalabilityScenario.setDescription("Test system performance with additional resources");
        scalabilityScenario.setTargetUsers(request.getExpectedUsers());
        scalabilityScenario.setRampUpTime(15);
        scalabilityScenario.setTestDuration(45);
        scalabilityScenario.setPriority(3);
        
        List<String> steps = new ArrayList<>();
        steps.add("Start the test with one server");
        steps.add("Add a second server during the test");
        steps.add("Add a third server if needed");
        steps.add("Monitor load distribution");
        scalabilityScenario.setTestSteps(steps);
        
        Map<String, Object> params = new HashMap<>();
        params.put("initial_servers", 1);
        params.put("max_servers", 3);
        params.put("load_balancing", true);
        params.put("auto_scaling", false);
        scalabilityScenario.setParameters(params);
        
        List<String> successCriteria = Arrays.asList(
            "Performance improves with added servers",
            "Balanced load distribution",
            "No service downtime"
        );
        scalabilityScenario.setSuccessCriteria(successCriteria);
        
        List<String> monitoringPoints = Arrays.asList(
            "Load distribution across servers",
            "Response time per server",
            "Resource usage per server",
            "Time to add new servers",
            "Impact of scaling on overall performance"
        );
        scalabilityScenario.setMonitoringPoints(monitoringPoints);
        
        scalabilityScenario.setExpectedOutcome("Confirm the system's horizontal scalability");
        
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
            return "Production-like Environment";
        } else if ("STAGING".equals(request.getDeploymentEnvironment())) {
            return "Staging Environment";
        } else {
            return "Development Environment";
        }
    }

    private String generateExecutionStrategy(PerformanceTestRequest request, List<PerformanceTestScenario> scenarios) {
        StringBuilder strategy = new StringBuilder();
        strategy.append("Execution Strategy:\n");
        strategy.append("1. Execute scenarios in the following order:\n");
        strategy.append("   - Soak Test (SOAK) first\n");
        strategy.append("   - Volume Test (VOLUME)\n");
        strategy.append("   - Stress Test (STRESS)\n");
        strategy.append("   - Spike Test (SPIKE)\n");
        strategy.append("   - Scalability Test (SCALABILITY)\n");
        strategy.append("2. Continuously monitor resources\n");
        strategy.append("3. Stop the test if critical issues occur\n");
        return strategy.toString();
    }

    private List<String> generatePrerequisites(PerformanceTestRequest request) {
        List<String> prerequisites = new ArrayList<>();
        prerequisites.add("Prepare the test environment");
        prerequisites.add("Prepare test data");
        prerequisites.add("Set up monitoring tools");
        prerequisites.add("Ensure sufficient system resources");
        prerequisites.add("Prepare a contingency plan");
        return prerequisites;
    }

    private Map<String, String> generateTestDataRequirements(PerformanceTestRequest request) {
        Map<String, String> requirements = new HashMap<>();
        requirements.put("user_data", "Data for " + request.getExpectedUsers() + " users");
        requirements.put("transaction_data", "Transaction data for common operations");
        requirements.put("search_data", "Diverse search data");
        requirements.put("file_data", "Files of various sizes for testing");
        return requirements;
    }

    private String calculateEstimatedDuration(List<PerformanceTestScenario> scenarios) {
        int totalMinutes = scenarios.stream()
                .mapToInt(PerformanceTestScenario::getTestDuration)
                .sum();
        
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        
        return hours + " hours and " + minutes + " minutes";
    }

    private String assessRisks(PerformanceTestRequest request, List<PerformanceTestScenario> scenarios) {
        StringBuilder risks = new StringBuilder();
        risks.append("Risk Assessment:\n");
        risks.append("1. High risk: Stress test may cause system outage\n");
        risks.append("2. Medium risk: Soak test may consume significant resources\n");
        risks.append("3. Low risk: Volume test may temporarily affect performance\n");
        risks.append("4. Recommendations: Prepare a recovery plan and monitor continuously\n");
        return risks.toString();
    }

    private String generatePlanId() {
        return "PERF_PLAN_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}