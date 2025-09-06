package Madfoat.Learning.dto;

public class IssueInfo {
    private String key;
    private String summary;
    private String description;
    private String status;
    private String priority;
    private String assignee;
    private String created;
    private String updated;
    private String type;

    // Constructors
    public IssueInfo() {}

    public IssueInfo(String key, String summary, String type, String status, String priority) {
        this.key = key;
        this.summary = summary;
        this.type = type;
        this.status = status;
        this.priority = priority;
    }

    // Getters and Setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }

    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }

    public String getUpdated() { return updated; }
    public void setUpdated(String updated) { this.updated = updated; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}