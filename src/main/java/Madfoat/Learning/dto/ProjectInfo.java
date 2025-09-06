package Madfoat.Learning.dto;

public class ProjectInfo {
    private String key;
    private String name;
    private String description;
    private String lead;

    // Constructors
    public ProjectInfo() {}

    public ProjectInfo(String key, String name) {
        this.key = key;
        this.name = name;
    }

    // Getters and Setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLead() { return lead; }
    public void setLead(String lead) { this.lead = lead; }
}