package Madfoat.Learning.dto;

import java.util.List;
import java.util.Map;

public class ProjectStatistics {
    private int totalIssues;
    private int totalBugs;
    private int totalStories;
    private int totalTasks;
    private int completedIssues;
    private Map<String, Long> issueTypesDistribution;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> columnStats;
    private Map<String, Long> priorityDistribution;
    private Map<String, Long> assigneeDistribution;
    private List<SprintInfo> sprints;
    private List<String> assignees;
    private List<String> statuses;
    private List<IssueInfo> issues;
    private ProjectInfo projectInfo;

    // Constructors
    public ProjectStatistics() {}

    public ProjectStatistics(int totalIssues, int totalBugs, int totalStories, int totalTasks, int completedIssues) {
        this.totalIssues = totalIssues;
        this.totalBugs = totalBugs;
        this.totalStories = totalStories;
        this.totalTasks = totalTasks;
        this.completedIssues = completedIssues;
    }

    // Getters and Setters
    public int getTotalIssues() { return totalIssues; }
    public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }

    public int getTotalBugs() { return totalBugs; }
    public void setTotalBugs(int totalBugs) { this.totalBugs = totalBugs; }

    public int getTotalStories() { return totalStories; }
    public void setTotalStories(int totalStories) { this.totalStories = totalStories; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedIssues() { return completedIssues; }
    public void setCompletedIssues(int completedIssues) { this.completedIssues = completedIssues; }

    public Map<String, Long> getIssueTypesDistribution() { return issueTypesDistribution; }
    public void setIssueTypesDistribution(Map<String, Long> issueTypesDistribution) { this.issueTypesDistribution = issueTypesDistribution; }

    public Map<String, Long> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Long> statusDistribution) { this.statusDistribution = statusDistribution; }

    public Map<String, Long> getColumnStats() { return columnStats; }
    public void setColumnStats(Map<String, Long> columnStats) { this.columnStats = columnStats; }

    public Map<String, Long> getPriorityDistribution() { return priorityDistribution; }
    public void setPriorityDistribution(Map<String, Long> priorityDistribution) { this.priorityDistribution = priorityDistribution; }

    public Map<String, Long> getAssigneeDistribution() { return assigneeDistribution; }
    public void setAssigneeDistribution(Map<String, Long> assigneeDistribution) { this.assigneeDistribution = assigneeDistribution; }

    public List<SprintInfo> getSprints() { return sprints; }
    public void setSprints(List<SprintInfo> sprints) { this.sprints = sprints; }

    public List<String> getAssignees() { return assignees; }
    public void setAssignees(List<String> assignees) { this.assignees = assignees; }

    public List<String> getStatuses() { return statuses; }
    public void setStatuses(List<String> statuses) { this.statuses = statuses; }

    public List<IssueInfo> getIssues() { return issues; }
    public void setIssues(List<IssueInfo> issues) { this.issues = issues; }

    public ProjectInfo getProjectInfo() { return projectInfo; }
    public void setProjectInfo(ProjectInfo projectInfo) { this.projectInfo = projectInfo; }
}