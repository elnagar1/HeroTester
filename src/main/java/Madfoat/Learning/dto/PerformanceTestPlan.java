package Madfoat.Learning.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PerformanceTestPlan {
    private String planId;
    private String planName;
    private String systemName;
    private LocalDateTime createdDate;
    private String createdBy;
    private List<PerformanceTestScenario> scenarios;
    private Map<String, Object> systemRequirements;
    private String testEnvironment;
    private String executionStrategy;
    private List<String> prerequisites;
    private Map<String, String> testDataRequirements;
    private String estimatedDuration;
    private String riskAssessment;

    // Constructors
    public PerformanceTestPlan() {
        this.createdDate = LocalDateTime.now();
    }

    public PerformanceTestPlan(String planId, String planName, String systemName) {
        this.planId = planId;
        this.planName = planName;
        this.systemName = systemName;
        this.createdDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<PerformanceTestScenario> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<PerformanceTestScenario> scenarios) {
        this.scenarios = scenarios;
    }

    public Map<String, Object> getSystemRequirements() {
        return systemRequirements;
    }

    public void setSystemRequirements(Map<String, Object> systemRequirements) {
        this.systemRequirements = systemRequirements;
    }

    public String getTestEnvironment() {
        return testEnvironment;
    }

    public void setTestEnvironment(String testEnvironment) {
        this.testEnvironment = testEnvironment;
    }

    public String getExecutionStrategy() {
        return executionStrategy;
    }

    public void setExecutionStrategy(String executionStrategy) {
        this.executionStrategy = executionStrategy;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public Map<String, String> getTestDataRequirements() {
        return testDataRequirements;
    }

    public void setTestDataRequirements(Map<String, String> testDataRequirements) {
        this.testDataRequirements = testDataRequirements;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(String estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String getRiskAssessment() {
        return riskAssessment;
    }

    public void setRiskAssessment(String riskAssessment) {
        this.riskAssessment = riskAssessment;
    }
}