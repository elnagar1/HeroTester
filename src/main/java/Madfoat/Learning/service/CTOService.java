package Madfoat.Learning.service;

import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CTOService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> getProjectStatistics(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            // Fetch all issues for the project
            List<Map<String, Object>> allIssues = getAllProjectIssues(jiraUrl, projectKey, username, apiToken);
            
            // Calculate statistics
            Map<String, Object> statistics = new HashMap<>();
            
            // Basic counts
            statistics.put("totalIssues", allIssues.size());
            statistics.put("totalBugs", allIssues.stream().filter(issue -> "Bug".equals(issue.get("type"))).count());
            statistics.put("totalStories", allIssues.stream().filter(issue -> "Story".equals(issue.get("type"))).count());
            statistics.put("totalTasks", allIssues.stream().filter(issue -> "Task".equals(issue.get("type"))).count());
            statistics.put("completedIssues", allIssues.stream().filter(issue -> "Done".equals(issue.get("status"))).count());
            
            // Issue types distribution
            Map<String, Long> issueTypesDistribution = allIssues.stream()
                .collect(Collectors.groupingBy(
                    issue -> (String) issue.get("type"),
                    Collectors.counting()
                ));
            statistics.put("issueTypesDistribution", issueTypesDistribution);
            
            // Status distribution
            Map<String, Long> statusDistribution = allIssues.stream()
                .collect(Collectors.groupingBy(
                    issue -> (String) issue.get("status"),
                    Collectors.counting()
                ));
            statistics.put("statusDistribution", statusDistribution);
            
            // Column statistics (status-based)
            Map<String, Long> columnStats = allIssues.stream()
                .collect(Collectors.groupingBy(
                    issue -> (String) issue.get("status"),
                    Collectors.counting()
                ));
            statistics.put("columnStats", columnStats);
            
            // Priority distribution
            Map<String, Long> priorityDistribution = allIssues.stream()
                .collect(Collectors.groupingBy(
                    issue -> (String) issue.get("priority"),
                    Collectors.counting()
                ));
            statistics.put("priorityDistribution", priorityDistribution);
            
            // Assignee distribution
            Map<String, Long> assigneeDistribution = allIssues.stream()
                .filter(issue -> issue.get("assignee") != null && !((String) issue.get("assignee")).isEmpty())
                .collect(Collectors.groupingBy(
                    issue -> (String) issue.get("assignee"),
                    Collectors.counting()
                ));
            statistics.put("assigneeDistribution", assigneeDistribution);
            
            // Sprint information
            List<Map<String, Object>> sprints = getSprints(jiraUrl, projectKey, username, apiToken);
            statistics.put("sprints", sprints);
            
            // Unique values for filters
            Set<String> uniqueAssignees = allIssues.stream()
                .map(issue -> (String) issue.get("assignee"))
                .filter(Objects::nonNull)
                .filter(assignee -> !assignee.isEmpty())
                .collect(Collectors.toSet());
            statistics.put("assignees", new ArrayList<>(uniqueAssignees));
            
            Set<String> uniqueStatuses = allIssues.stream()
                .map(issue -> (String) issue.get("status"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            statistics.put("statuses", new ArrayList<>(uniqueStatuses));
            
            // All issues for detailed view
            statistics.put("issues", allIssues);
            
            // Project information
            Map<String, Object> projectInfo = getProjectInfo(jiraUrl, projectKey, username, apiToken);
            statistics.put("projectInfo", projectInfo);
            
            // CTO specific metrics
            calculateCTOMetrics(statistics, allIssues);
            
            return statistics;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get project statistics: " + e.getMessage(), e);
        }
    }
    
    private List<Map<String, Object>> getAllProjectIssues(String jiraUrl, String projectKey, String username, String apiToken) {
        List<Map<String, Object>> allIssues = new ArrayList<>();
        int startAt = 0;
        int maxResults = 100;
        
        try {
            while (true) {
                String url = jiraUrl + "/rest/api/2/search";
                HttpHeaders headers = createAuthHeaders(username, apiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                // Build JQL query for all project issues
                String jql = String.format("project = %s ORDER BY created DESC", projectKey);
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("jql", jql);
                requestBody.put("maxResults", maxResults);
                requestBody.put("startAt", startAt);
                requestBody.put("fields", Arrays.asList(
                    "summary", "description", "status", "priority", "assignee", 
                    "created", "updated", "issuetype", "project", "sprint"
                ));
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    JsonNode searchResult = objectMapper.readTree(response.getBody());
                    JsonNode issuesNode = searchResult.get("issues");
                    
                    if (issuesNode.size() == 0) {
                        break; // No more issues
                    }
                    
                    for (JsonNode issue : issuesNode) {
                        Map<String, Object> issueData = new HashMap<>();
                        issueData.put("key", issue.get("key").asText());
                        issueData.put("summary", issue.path("fields").path("summary").asText(""));
                        issueData.put("description", issue.path("fields").path("description").asText(""));
                        issueData.put("status", issue.path("fields").path("status").path("name").asText(""));
                        issueData.put("priority", issue.path("fields").path("priority").path("name").asText(""));
                        issueData.put("assignee", issue.path("fields").path("assignee").path("displayName").asText("Unassigned"));
                        issueData.put("created", issue.path("fields").path("created").asText(""));
                        issueData.put("updated", issue.path("fields").path("updated").asText(""));
                        issueData.put("type", issue.path("fields").path("issuetype").path("name").asText(""));
                        
                        allIssues.add(issueData);
                    }
                    
                    startAt += maxResults;
                    
                    // Check if we've reached the total
                    int total = searchResult.get("total").asInt();
                    if (startAt >= total) {
                        break;
                    }
                } else {
                    break;
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch project issues: " + e.getMessage(), e);
        }
        
        return allIssues;
    }
    
    private List<Map<String, Object>> getSprints(String jiraUrl, String projectKey, String username, String apiToken) {
        List<Map<String, Object>> sprints = new ArrayList<>();
        
        try {
            // Get board for the project
            String boardUrl = jiraUrl + "/rest/agile/1.0/board";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            
            ResponseEntity<String> response = restTemplate.exchange(boardUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode boards = objectMapper.readTree(response.getBody());
                
                // Find board for the specific project
                for (JsonNode board : boards.get("values")) {
                    String boardProjectKey = board.path("location").path("projectKey").asText();
                    if (projectKey.equals(boardProjectKey)) {
                        String boardId = board.get("id").asText();
                        sprints = getSprintsForBoard(jiraUrl, boardId, username, apiToken);
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            // If sprint fetching fails, return empty list
            System.err.println("Failed to fetch sprints: " + e.getMessage());
        }
        
        return sprints;
    }
    
    private List<Map<String, Object>> getSprintsForBoard(String jiraUrl, String boardId, String username, String apiToken) {
        List<Map<String, Object>> sprints = new ArrayList<>();
        
        try {
            String url = jiraUrl + "/rest/agile/1.0/board/" + boardId + "/sprint";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode sprintsNode = objectMapper.readTree(response.getBody());
                
                for (JsonNode sprint : sprintsNode.get("values")) {
                    Map<String, Object> sprintData = new HashMap<>();
                    sprintData.put("id", sprint.get("id").asText());
                    sprintData.put("name", sprint.get("name").asText());
                    sprintData.put("state", sprint.get("state").asText());
                    sprintData.put("startDate", sprint.path("startDate").asText(""));
                    sprintData.put("endDate", sprint.path("endDate").asText(""));
                    sprints.add(sprintData);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Failed to fetch sprints for board: " + e.getMessage());
        }
        
        return sprints;
    }
    
    private Map<String, Object> getProjectInfo(String jiraUrl, String projectKey, String username, String apiToken) {
        Map<String, Object> projectInfo = new HashMap<>();
        
        try {
            String url = jiraUrl + "/rest/api/2/project/" + projectKey;
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode project = objectMapper.readTree(response.getBody());
                projectInfo.put("key", project.get("key").asText());
                projectInfo.put("name", project.get("name").asText());
                projectInfo.put("description", project.path("description").asText(""));
                projectInfo.put("lead", project.path("lead").path("displayName").asText(""));
            }
            
        } catch (Exception e) {
            System.err.println("Failed to fetch project info: " + e.getMessage());
        }
        
        return projectInfo;
    }
    
    public Map<String, Object> getProjects(String jiraUrl, String username, String apiToken) {
        try {
            String url = jiraUrl + "/rest/api/2/project";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode projects = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> projectList = new ArrayList<>();
                
                for (JsonNode project : projects) {
                    Map<String, Object> projectData = new HashMap<>();
                    projectData.put("key", project.get("key").asText());
                    projectData.put("name", project.get("name").asText());
                    projectData.put("description", project.path("description").asText(""));
                    projectData.put("lead", project.path("lead").path("displayName").asText(""));
                    projectData.put("projectTypeKey", project.path("projectTypeKey").asText(""));
                    projectData.put("archived", project.path("archived").asBoolean(false));
                    projectList.add(projectData);
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("projects", projectList);
                result.put("total", projectList.size());
                
                return result;
            }
            
            return Map.of("projects", new ArrayList<>(), "total", 0);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get projects: " + e.getMessage(), e);
        }
    }

    private void calculateCTOMetrics(Map<String, Object> statistics, List<Map<String, Object>> allIssues) {
        // Bugs resolved
        long bugsResolved = allIssues.stream()
            .filter(issue -> "Bug".equals(issue.get("type")) && "Done".equals(issue.get("status")))
            .count();
        statistics.put("bugsResolved", bugsResolved);
        
        // Stories completed
        long storiesCompleted = allIssues.stream()
            .filter(issue -> "Story".equals(issue.get("type")) && "Done".equals(issue.get("status")))
            .count();
        statistics.put("storiesCompleted", storiesCompleted);
        
        // Active team members
        long activeTeamMembers = allIssues.stream()
            .map(issue -> (String) issue.get("assignee"))
            .filter(Objects::nonNull)
            .filter(assignee -> !assignee.isEmpty() && !"Unassigned".equals(assignee))
            .distinct()
            .count();
        statistics.put("activeTeamMembers", activeTeamMembers);
        
        // Critical bugs
        long criticalBugs = allIssues.stream()
            .filter(issue -> "Bug".equals(issue.get("type")) && "Highest".equals(issue.get("priority")))
            .count();
        statistics.put("criticalBugs", criticalBugs);
        
        // Average resolution time (simplified calculation)
        long totalResolutionTime = allIssues.stream()
            .filter(issue -> "Done".equals(issue.get("status")))
            .mapToLong(issue -> {
                try {
                    String created = (String) issue.get("created");
                    String updated = (String) issue.get("updated");
                    if (created != null && updated != null) {
                        long createdTime = java.time.Instant.parse(created).toEpochMilli();
                        long updatedTime = java.time.Instant.parse(updated).toEpochMilli();
                        return (updatedTime - createdTime) / (1000 * 60 * 60 * 24); // days
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
                return 0;
            })
            .sum();
        
        long completedIssues = allIssues.stream()
            .filter(issue -> "Done".equals(issue.get("status")))
            .count();
        
        long avgResolutionTime = completedIssues > 0 ? totalResolutionTime / completedIssues : 0;
        statistics.put("avgResolutionTime", avgResolutionTime);
        
        // Productivity index (simplified calculation)
        double productivityIndex = calculateProductivityIndex(allIssues);
        statistics.put("productivityIndex", Math.round(productivityIndex * 100) / 100.0);
        
        // Sprint information
        Map<String, Object> sprintInfo = getCurrentSprintInfo(allIssues);
        statistics.put("sprintInfo", sprintInfo);
    }
    
    private double calculateProductivityIndex(List<Map<String, Object>> allIssues) {
        // Simple productivity calculation based on completion rate and resolution time
        long totalIssues = allIssues.size();
        long completedIssues = allIssues.stream()
            .filter(issue -> "Done".equals(issue.get("status")))
            .count();
        
        if (totalIssues == 0) return 0.0;
        
        double completionRate = (double) completedIssues / totalIssues;
        
        // Factor in resolution time (shorter is better)
        long avgResolutionTime = calculateAvgResolutionTime(allIssues);
        double timeFactor = avgResolutionTime > 0 ? Math.max(0.1, 1.0 - (avgResolutionTime / 30.0)) : 1.0; // 30 days max
        
        return completionRate * timeFactor * 100; // Scale to 0-100
    }
    
    private long calculateAvgResolutionTime(List<Map<String, Object>> allIssues) {
        List<Long> resolutionTimes = allIssues.stream()
            .filter(issue -> "Done".equals(issue.get("status")))
            .map(issue -> {
                try {
                    String created = (String) issue.get("created");
                    String updated = (String) issue.get("updated");
                    if (created != null && updated != null) {
                        long createdTime = java.time.Instant.parse(created).toEpochMilli();
                        long updatedTime = java.time.Instant.parse(updated).toEpochMilli();
                        return (updatedTime - createdTime) / (1000 * 60 * 60 * 24); // days
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
                return 0L;
            })
            .filter(time -> time > 0)
            .collect(Collectors.toList());
        
        return resolutionTimes.isEmpty() ? 0 : resolutionTimes.stream().mapToLong(Long::longValue).sum() / resolutionTimes.size();
    }
    
    private Map<String, Object> getCurrentSprintInfo(List<Map<String, Object>> allIssues) {
        Map<String, Object> sprintInfo = new HashMap<>();
        
        // Count stories and bugs in current sprint (simplified)
        long sprintStories = allIssues.stream()
            .filter(issue -> "Story".equals(issue.get("type")))
            .count();
        
        long sprintBugs = allIssues.stream()
            .filter(issue -> "Bug".equals(issue.get("type")))
            .count();
        
        sprintInfo.put("name", "Current Sprint");
        sprintInfo.put("storiesCount", sprintStories);
        sprintInfo.put("bugsCount", sprintBugs);
        sprintInfo.put("duration", "2 weeks"); // Default sprint duration
        sprintInfo.put("startDate", java.time.LocalDate.now().minusDays(7).toString());
        sprintInfo.put("endDate", java.time.LocalDate.now().plusDays(7).toString());
        
        return sprintInfo;
    }

    private HttpHeaders createAuthHeaders(String username, String apiToken) {
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}