package Madfoat.Learning.dto;

import java.util.List;
import java.util.Map;

public class PerformanceTestScenario {
    private String scenarioId;
    private String scenarioName;
    private String scenarioType; // STRESS, SOAK, VOLUME, SPIKE, etc.
    private String description;
    private int targetUsers;
    private int rampUpTime; // in minutes
    private int testDuration; // in minutes
    private List<String> testSteps;
    private Map<String, Object> parameters;
    private List<String> successCriteria;
    private List<String> monitoringPoints;
    private String expectedOutcome;
    private int priority; // 1-5 (5 being highest)

    // Constructors
    public PerformanceTestScenario() {}

    public PerformanceTestScenario(String scenarioId, String scenarioName, String scenarioType) {
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.scenarioType = scenarioType;
    }

    // Getters and Setters
    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public String getScenarioType() {
        return scenarioType;
    }

    public void setScenarioType(String scenarioType) {
        this.scenarioType = scenarioType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTargetUsers() {
        return targetUsers;
    }

    public void setTargetUsers(int targetUsers) {
        this.targetUsers = targetUsers;
    }

    public int getRampUpTime() {
        return rampUpTime;
    }

    public void setRampUpTime(int rampUpTime) {
        this.rampUpTime = rampUpTime;
    }

    public int getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(int testDuration) {
        this.testDuration = testDuration;
    }

    public List<String> getTestSteps() {
        return testSteps;
    }

    public void setTestSteps(List<String> testSteps) {
        this.testSteps = testSteps;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public List<String> getSuccessCriteria() {
        return successCriteria;
    }

    public void setSuccessCriteria(List<String> successCriteria) {
        this.successCriteria = successCriteria;
    }

    public List<String> getMonitoringPoints() {
        return monitoringPoints;
    }

    public void setMonitoringPoints(List<String> monitoringPoints) {
        this.monitoringPoints = monitoringPoints;
    }

    public String getExpectedOutcome() {
        return expectedOutcome;
    }

    public void setExpectedOutcome(String expectedOutcome) {
        this.expectedOutcome = expectedOutcome;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}