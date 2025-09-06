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
    
    private HttpHeaders createAuthHeaders(String username, String apiToken) {
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}