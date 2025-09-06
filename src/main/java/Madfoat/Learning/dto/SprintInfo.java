package Madfoat.Learning.dto;

public class SprintInfo {
    private String id;
    private String name;
    private String state;
    private String startDate;
    private String endDate;

    // Constructors
    public SprintInfo() {}

    public SprintInfo(String id, String name, String state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}