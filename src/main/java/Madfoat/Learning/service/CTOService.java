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
            
            // Get real sprint information
            System.out.println("Getting sprint information for project: " + projectKey);
            Map<String, Object> sprintInfo = getCurrentSprintInfo(jiraUrl, projectKey, username, apiToken);
            System.out.println("Sprint info result: " + sprintInfo);
            statistics.put("sprintInfo", sprintInfo);
            
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
            System.out.println("Fetching projects from: " + url);
            System.out.println("Username: " + username);
            
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            
            // First, let's test authentication with a simple API call
            try {
                String testUrl = jiraUrl + "/rest/api/2/myself";
                ResponseEntity<String> testResponse = restTemplate.exchange(testUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
                System.out.println("Authentication test status: " + testResponse.getStatusCode());
                if (testResponse.getStatusCode() == HttpStatus.OK) {
                    System.out.println("Authentication successful");
                    JsonNode userInfo = objectMapper.readTree(testResponse.getBody());
                    System.out.println("User info: " + userInfo.get("displayName").asText() + " (" + userInfo.get("emailAddress").asText() + ")");
                } else {
                    System.out.println("Authentication failed: " + testResponse.getBody());
                }
            } catch (Exception authError) {
                System.out.println("Authentication test failed: " + authError.getMessage());
                authError.printStackTrace();
            }
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode projects = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> projectList = new ArrayList<>();
                
                System.out.println("Projects response type: " + projects.getNodeType());
                System.out.println("Is array: " + projects.isArray());
                System.out.println("Response size: " + (projects.isArray() ? projects.size() : "N/A"));
                
                if (projects.isArray()) {
                    System.out.println("Processing " + projects.size() + " projects...");
                    for (JsonNode project : projects) {
                        try {
                            Map<String, Object> projectData = new HashMap<>();
                            projectData.put("key", project.get("key").asText());
                            projectData.put("name", project.get("name").asText());
                            projectData.put("description", project.path("description").asText(""));
                            projectData.put("lead", project.path("lead").path("displayName").asText(""));
                            projectData.put("projectTypeKey", project.path("projectTypeKey").asText(""));
                            projectData.put("archived", project.path("archived").asBoolean(false));
                            projectList.add(projectData);
                            System.out.println("Added project: " + project.get("key").asText() + " - " + project.get("name").asText());
                        } catch (Exception e) {
                            System.err.println("Error processing project: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("Unexpected response format - not an array");
                    System.out.println("Response content: " + response.getBody());
                }
                
                System.out.println("Found " + projectList.size() + " projects total");
                
                Map<String, Object> result = new HashMap<>();
                result.put("projects", projectList);
                result.put("total", projectList.size());
                result.put("jiraUrl", jiraUrl);
                result.put("username", username);
                result.put("success", true);
                
                return result;
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                System.out.println("Unauthorized - check credentials");
                return Map.of(
                    "projects", new ArrayList<>(), 
                    "total", 0, 
                    "error", "Unauthorized - Please check your username and API token",
                    "statusCode", response.getStatusCode().value()
                );
            } else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
                System.out.println("Forbidden - no access to projects");
                return Map.of(
                    "projects", new ArrayList<>(), 
                    "total", 0, 
                    "error", "Forbidden - You don't have access to view projects. Please contact your Jira administrator.",
                    "statusCode", response.getStatusCode().value()
                );
            } else {
                System.out.println("Failed to get projects. Status: " + response.getStatusCode());
                return Map.of(
                    "projects", new ArrayList<>(), 
                    "total", 0, 
                    "error", "HTTP " + response.getStatusCode() + " - " + response.getBody(),
                    "statusCode", response.getStatusCode().value()
                );
            }
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("HTTP Client Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return Map.of(
                "projects", new ArrayList<>(), 
                "total", 0, 
                "error", "HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                "statusCode", e.getStatusCode().value()
            );
        } catch (Exception e) {
            System.err.println("Error getting projects: " + e.getMessage());
            e.printStackTrace();
            return Map.of(
                "projects", new ArrayList<>(), 
                "total", 0, 
                "error", "Connection failed: " + e.getMessage()
            );
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
        
        // Sprint information - will be set by the calling method with real sprint data
        // This is just a placeholder, real sprint info will be set in getProjectStatistics
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
    
    // This method is removed - using the public getCurrentSprintInfo with Jira API instead
    
    public Map<String, Object> getCurrentSprintInfo(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            System.out.println("=== SPRINT INFO DEBUG ===");
            System.out.println("Jira URL: " + jiraUrl);
            System.out.println("Project Key: " + projectKey);
            System.out.println("Username: " + username);
            
            Map<String, Object> sprintInfo = new HashMap<>();
            
            // First, try to get boards for the project
            String url = jiraUrl + "/rest/agile/1.0/board?projectKeyOrId=" + projectKey;
            System.out.println("Boards URL: " + url);
            
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            System.out.println("Boards response status: " + response.getStatusCode());
            System.out.println("Boards response body: " + response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode boards = objectMapper.readTree(response.getBody());
                
                if (boards.has("values") && boards.get("values").size() > 0) {
                    System.out.println("Found " + boards.get("values").size() + " boards");
                    
                    for (JsonNode board : boards.get("values")) {
                        String boardId = board.get("id").asText();
                        String boardName = board.get("name").asText();
                        System.out.println("Processing board: " + boardName + " (ID: " + boardId + ")");
                        
                        // Get all sprints for this board (not just active)
                        String sprintUrl = jiraUrl + "/rest/agile/1.0/board/" + boardId + "/sprint";
                        System.out.println("Sprints URL: " + sprintUrl);
                        
                        ResponseEntity<String> sprintResponse = restTemplate.exchange(sprintUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
                        System.out.println("Sprints response status: " + sprintResponse.getStatusCode());
                        System.out.println("Sprints response body: " + sprintResponse.getBody());
                        
                        if (sprintResponse.getStatusCode() == HttpStatus.OK) {
                            JsonNode sprintData = objectMapper.readTree(sprintResponse.getBody());
                            
                            if (sprintData.has("values") && sprintData.get("values").size() > 0) {
                                System.out.println("Found " + sprintData.get("values").size() + " sprints");
                                
                                // Look for active sprint first, then any sprint
                                JsonNode selectedSprint = null;
                                for (JsonNode sprint : sprintData.get("values")) {
                                    String state = sprint.get("state").asText();
                                    System.out.println("Sprint: " + sprint.get("name").asText() + " (State: " + state + ")");
                                    
                                    if ("active".equals(state)) {
                                        selectedSprint = sprint;
                                        break;
                                    } else if (selectedSprint == null) {
                                        selectedSprint = sprint; // Use first sprint if no active one
                                    }
                                }
                                
                                if (selectedSprint != null) {
                                    System.out.println("Selected sprint: " + selectedSprint.get("name").asText());
                                    
                                    sprintInfo.put("name", selectedSprint.get("name").asText());
                                    sprintInfo.put("id", selectedSprint.get("id").asText());
                                    sprintInfo.put("state", selectedSprint.get("state").asText());
                                    
                                    // Get sprint start and end dates
                                    String startDate = selectedSprint.path("startDate").asText("");
                                    String endDate = selectedSprint.path("endDate").asText("");
                                    
                                    sprintInfo.put("startDate", startDate);
                                    sprintInfo.put("endDate", endDate);
                                    
                                    // Calculate duration
                                    if (!startDate.isEmpty() && !endDate.isEmpty()) {
                                        try {
                                            java.time.LocalDate start = java.time.LocalDate.parse(startDate.substring(0, 10));
                                            java.time.LocalDate end = java.time.LocalDate.parse(endDate.substring(0, 10));
                                            long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                                            sprintInfo.put("duration", days + " days");
                                        } catch (Exception e) {
                                            sprintInfo.put("duration", "Unknown");
                                        }
                                    } else {
                                        sprintInfo.put("duration", "Unknown");
                                    }
                                    
                                    // Get issues in this sprint
                                    String sprintId = selectedSprint.get("id").asText();
                                    String issuesUrl = jiraUrl + "/rest/agile/1.0/sprint/" + sprintId + "/issue?fields=issuetype,summary";
                                    System.out.println("Issues URL: " + issuesUrl);
                                    
                                    ResponseEntity<String> issuesResponse = restTemplate.exchange(issuesUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
                                    System.out.println("Issues response status: " + issuesResponse.getStatusCode());
                                    System.out.println("Issues response body: " + issuesResponse.getBody());
                                    
                                    if (issuesResponse.getStatusCode() == HttpStatus.OK) {
                                        JsonNode issuesData = objectMapper.readTree(issuesResponse.getBody());
                                        
                                        long storiesCount = 0;
                                        long bugsCount = 0;
                                        
                                        if (issuesData.has("issues")) {
                                            System.out.println("Found " + issuesData.get("issues").size() + " issues in sprint");
                                            
                                            for (JsonNode issue : issuesData.get("issues")) {
                                                String issueType = issue.path("fields").path("issuetype").path("name").asText("");
                                                String issueKey = issue.get("key").asText();
                                                System.out.println("Issue: " + issueKey + " (Type: " + issueType + ")");
                                                
                                                if ("Story".equals(issueType)) {
                                                    storiesCount++;
                                                } else if ("Bug".equals(issueType)) {
                                                    bugsCount++;
                                                }
                                            }
                                        }
                                        
                                        sprintInfo.put("storiesCount", storiesCount);
                                        sprintInfo.put("bugsCount", bugsCount);
                                        
                                        System.out.println("Final sprint info: " + sprintInfo);
                                        return sprintInfo;
                                    } else {
                                        System.out.println("Failed to get issues for sprint");
                                        sprintInfo.put("storiesCount", 0);
                                        sprintInfo.put("bugsCount", 0);
                                        return sprintInfo;
                                    }
                                }
                            } else {
                                System.out.println("No sprints found for board");
                            }
                        } else {
                            System.out.println("Failed to get sprints for board");
                        }
                    }
                } else {
                    System.out.println("No boards found for project");
                }
            } else {
                System.out.println("Failed to get boards for project");
            }
            
            // Fallback: Try to get sprint info using JQL search
            System.out.println("No sprint found via Agile API, trying JQL fallback");
            try {
                String jqlUrl = jiraUrl + "/rest/api/2/search?jql=project=" + projectKey + " AND sprint in openSprints()&fields=issuetype,sprint&maxResults=1000";
                System.out.println("JQL URL: " + jqlUrl);
                
                ResponseEntity<String> jqlResponse = restTemplate.exchange(jqlUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
                System.out.println("JQL response status: " + jqlResponse.getStatusCode());
                System.out.println("JQL response body: " + jqlResponse.getBody());
                
                if (jqlResponse.getStatusCode() == HttpStatus.OK) {
                    JsonNode jqlData = objectMapper.readTree(jqlResponse.getBody());
                    
                    if (jqlData.has("issues") && jqlData.get("issues").size() > 0) {
                        System.out.println("Found " + jqlData.get("issues").size() + " issues in open sprints");
                        
                        long storiesCount = 0;
                        long bugsCount = 0;
                        String sprintName = "Open Sprint";
                        
                        for (JsonNode issue : jqlData.get("issues")) {
                            String issueType = issue.path("fields").path("issuetype").path("name").asText("");
                            String issueKey = issue.get("key").asText();
                            System.out.println("Issue: " + issueKey + " (Type: " + issueType + ")");
                            
                            if ("Story".equals(issueType)) {
                                storiesCount++;
                            } else if ("Bug".equals(issueType)) {
                                bugsCount++;
                            }
                            
                            // Try to get sprint name from sprint field
                            JsonNode sprintField = issue.path("fields").path("sprint");
                            if (sprintField.isArray() && sprintField.size() > 0) {
                                String sprintInfoStr = sprintField.get(0).asText();
                                // Parse sprint info string to extract name
                                if (sprintInfoStr.contains("name=")) {
                                    String[] parts = sprintInfoStr.split("name=");
                                    if (parts.length > 1) {
                                        String namePart = parts[1].split(",")[0];
                                        sprintName = namePart;
                                    }
                                }
                            }
                        }
                        
                        sprintInfo.put("name", sprintName);
                        sprintInfo.put("storiesCount", storiesCount);
                        sprintInfo.put("bugsCount", bugsCount);
                        sprintInfo.put("duration", "Unknown");
                        sprintInfo.put("startDate", "Unknown");
                        sprintInfo.put("endDate", "Unknown");
                        
                        System.out.println("JQL fallback result: " + sprintInfo);
                        return sprintInfo;
                    }
                }
            } catch (Exception jqlError) {
                System.err.println("JQL fallback also failed: " + jqlError.getMessage());
            }
            
            // Final fallback
            System.out.println("No sprint found, using final fallback");
            sprintInfo.put("name", "No Sprint Found");
            sprintInfo.put("storiesCount", 0);
            sprintInfo.put("bugsCount", 0);
            sprintInfo.put("duration", "N/A");
            sprintInfo.put("startDate", "N/A");
            sprintInfo.put("endDate", "N/A");
            
            return sprintInfo;
            
        } catch (Exception e) {
            System.err.println("Error getting current sprint info: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorSprintInfo = new HashMap<>();
            errorSprintInfo.put("name", "Error: " + e.getMessage());
            errorSprintInfo.put("storiesCount", 0);
            errorSprintInfo.put("bugsCount", 0);
            errorSprintInfo.put("duration", "Error");
            errorSprintInfo.put("startDate", "Error");
            errorSprintInfo.put("endDate", "Error");
            
            return errorSprintInfo;
        }
    }

    public Map<String, Object> getFilterOptions(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            Map<String, Object> filterOptions = new HashMap<>();
            
            // Get sprints
            List<Map<String, Object>> sprints = getSprints(jiraUrl, projectKey, username, apiToken);
            filterOptions.put("sprints", sprints);
            
            // Get assignees
            List<String> assignees = getAssignees(jiraUrl, projectKey, username, apiToken);
            filterOptions.put("assignees", assignees);
            
            // Get issue types
            List<String> issueTypes = getIssueTypes(jiraUrl, projectKey, username, apiToken);
            filterOptions.put("issueTypes", issueTypes);
            
            // Get statuses
            List<String> statuses = getStatuses(jiraUrl, projectKey, username, apiToken);
            filterOptions.put("statuses", statuses);
            
            // Get priorities
            List<String> priorities = getPriorities(jiraUrl, projectKey, username, apiToken);
            filterOptions.put("priorities", priorities);
            
            return filterOptions;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get filter options: " + e.getMessage(), e);
        }
    }
    
    
    private List<String> getAssignees(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            String url = jiraUrl + "/rest/api/2/search?jql=project=" + projectKey + "&fields=assignee&maxResults=1000";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode searchResult = objectMapper.readTree(response.getBody());
                Set<String> assignees = new HashSet<>();
                
                if (searchResult.has("issues")) {
                    for (JsonNode issue : searchResult.get("issues")) {
                        JsonNode assignee = issue.path("fields").path("assignee");
                        if (!assignee.isNull() && assignee.has("displayName")) {
                            assignees.add(assignee.get("displayName").asText());
                        }
                    }
                }
                
                return new ArrayList<>(assignees);
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("Error getting assignees: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private List<String> getIssueTypes(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            String url = jiraUrl + "/rest/api/2/search?jql=project=" + projectKey + "&fields=issuetype&maxResults=1000";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode searchResult = objectMapper.readTree(response.getBody());
                Set<String> issueTypes = new HashSet<>();
                
                if (searchResult.has("issues")) {
                    for (JsonNode issue : searchResult.get("issues")) {
                        JsonNode issueType = issue.path("fields").path("issuetype");
                        if (issueType.has("name")) {
                            issueTypes.add(issueType.get("name").asText());
                        }
                    }
                }
                
                return new ArrayList<>(issueTypes);
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("Error getting issue types: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private List<String> getStatuses(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            String url = jiraUrl + "/rest/api/2/search?jql=project=" + projectKey + "&fields=status&maxResults=1000";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode searchResult = objectMapper.readTree(response.getBody());
                Set<String> statuses = new HashSet<>();
                
                if (searchResult.has("issues")) {
                    for (JsonNode issue : searchResult.get("issues")) {
                        JsonNode status = issue.path("fields").path("status");
                        if (status.has("name")) {
                            statuses.add(status.get("name").asText());
                        }
                    }
                }
                
                return new ArrayList<>(statuses);
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("Error getting statuses: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private List<String> getPriorities(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            String url = jiraUrl + "/rest/api/2/search?jql=project=" + projectKey + "&fields=priority&maxResults=1000";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode searchResult = objectMapper.readTree(response.getBody());
                Set<String> priorities = new HashSet<>();
                
                if (searchResult.has("issues")) {
                    for (JsonNode issue : searchResult.get("issues")) {
                        JsonNode priority = issue.path("fields").path("priority");
                        if (!priority.isNull() && priority.has("name")) {
                            priorities.add(priority.get("name").asText());
                        }
                    }
                }
                
                return new ArrayList<>(priorities);
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("Error getting priorities: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private HttpHeaders createAuthHeaders(String username, String apiToken) {
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}