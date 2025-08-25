package Madfoat.Learning.dto;

import java.util.List;
import java.util.Map;

public class PerformanceTestRequest {
    private int expectedUsers;
    private List<String> commonOperations;
    private Map<String, Integer> peakTimes;
    private String systemType;
    private int dataVolume;
    private String deploymentEnvironment;
    private Map<String, Object> additionalRequirements;

    // Constructors
    public PerformanceTestRequest() {}

    public PerformanceTestRequest(int expectedUsers, List<String> commonOperations, 
                                Map<String, Integer> peakTimes, String systemType) {
        this.expectedUsers = expectedUsers;
        this.commonOperations = commonOperations;
        this.peakTimes = peakTimes;
        this.systemType = systemType;
    }

    // Getters and Setters
    public int getExpectedUsers() {
        return expectedUsers;
    }

    public void setExpectedUsers(int expectedUsers) {
        this.expectedUsers = expectedUsers;
    }

    public List<String> getCommonOperations() {
        return commonOperations;
    }

    public void setCommonOperations(List<String> commonOperations) {
        this.commonOperations = commonOperations;
    }

    public Map<String, Integer> getPeakTimes() {
        return peakTimes;
    }

    public void setPeakTimes(Map<String, Integer> peakTimes) {
        this.peakTimes = peakTimes;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public int getDataVolume() {
        return dataVolume;
    }

    public void setDataVolume(int dataVolume) {
        this.dataVolume = dataVolume;
    }

    public String getDeploymentEnvironment() {
        return deploymentEnvironment;
    }

    public void setDeploymentEnvironment(String deploymentEnvironment) {
        this.deploymentEnvironment = deploymentEnvironment;
    }

    public Map<String, Object> getAdditionalRequirements() {
        return additionalRequirements;
    }

    public void setAdditionalRequirements(Map<String, Object> additionalRequirements) {
        this.additionalRequirements = additionalRequirements;
    }
}