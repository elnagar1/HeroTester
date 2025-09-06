package Madfoat.Learning.service;

import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class JiraService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> analyzeJiraProject(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            // Fetch current sprint information
            Map<String, Object> sprintInfo = getCurrentSprint(jiraUrl, projectKey, username, apiToken);
            
            // Fetch issues from current sprint
            List<Map<String, Object>> sprintIssues = getSprintIssues(jiraUrl, projectKey, username, apiToken, sprintInfo);
            
            // Analyze issues for data quality and language problems
            Map<String, Object> analysis = analyzeIssues(sprintIssues);
            
            // Add sprint information to analysis
            analysis.put("sprintInfo", sprintInfo);
            analysis.put("totalBugs", sprintIssues.size());
            
            return analysis;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze Jira project: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> getCurrentSprint(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            String url = jiraUrl + "/rest/agile/1.0/board";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode boards = objectMapper.readTree(response.getBody());
                
                // Find board for the specific project
                for (JsonNode board : boards.get("values")) {
                    String boardProjectKey = board.path("location").path("projectKey").asText();
                    if (projectKey.equals(boardProjectKey)) {
                        String boardId = board.get("id").asText();
                        return getActiveSprint(jiraUrl, boardId, username, apiToken);
                    }
                }
            }
            
            // Fallback: return basic sprint info
            Map<String, Object> fallbackSprint = new HashMap<>();
            fallbackSprint.put("name", "Current Sprint");
            fallbackSprint.put("state", "active");
            fallbackSprint.put("startDate", new Date());
            return fallbackSprint;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get current sprint: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> getActiveSprint(String jiraUrl, String boardId, String username, String apiToken) {
        try {
            String url = jiraUrl + "/rest/agile/1.0/board/" + boardId + "/sprint?state=active";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode sprints = objectMapper.readTree(response.getBody());
                if (sprints.has("values") && sprints.get("values").size() > 0) {
                    JsonNode sprint = sprints.get("values").get(0);
                    Map<String, Object> sprintInfo = new HashMap<>();
                    sprintInfo.put("id", sprint.get("id").asText());
                    sprintInfo.put("name", sprint.get("name").asText());
                    sprintInfo.put("state", sprint.get("state").asText());
                    sprintInfo.put("startDate", sprint.get("startDate").asText());
                    sprintInfo.put("endDate", sprint.get("endDate").asText());
                    return sprintInfo;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get active sprint: " + e.getMessage(), e);
        }
    }
    
    private List<Map<String, Object>> getSprintIssues(String jiraUrl, String projectKey, String username, String apiToken, Map<String, Object> sprintInfo) {
        try {
            String url = jiraUrl + "/rest/api/2/search";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Build JQL query for current sprint issues
            String jql = String.format("project = %s AND sprint in openSprints() ORDER BY priority DESC", projectKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("jql", jql);
            requestBody.put("maxResults", 100);
            requestBody.put("fields", Arrays.asList("summary", "description", "status", "priority", "assignee", "created", "comment"));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode searchResult = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> issues = new ArrayList<>();
                
                JsonNode issuesNode = searchResult.get("issues");
                for (JsonNode issue : issuesNode) {
                    Map<String, Object> issueData = new HashMap<>();
                    issueData.put("key", issue.get("key").asText());
                    issueData.put("summary", issue.path("fields").path("summary").asText(""));
                    issueData.put("description", issue.path("fields").path("description").asText(""));
                    issueData.put("status", issue.path("fields").path("status").path("name").asText(""));
                    issueData.put("priority", issue.path("fields").path("priority").path("name").asText(""));
                    issueData.put("assignee", issue.path("fields").path("assignee").path("displayName").asText("Unassigned"));
                    issueData.put("created", issue.path("fields").path("created").asText(""));
                    
                    // Get comments
                    List<String> comments = new ArrayList<>();
                    JsonNode commentNode = issue.path("fields").path("comment");
                    if (commentNode.has("comments")) {
                        for (JsonNode comment : commentNode.get("comments")) {
                            comments.add(comment.path("body").asText(""));
                        }
                    }
                    issueData.put("comments", comments);
                    
                    issues.add(issueData);
                }
                
                return issues;
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get sprint issues: " + e.getMessage(), e);
        }
    }
    
    public Map<String, Object> getSprints(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            // Get board ID first
            String boardUrl = jiraUrl + "/rest/agile/1.0/board";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> boardResponse = restTemplate.exchange(
                boardUrl + "?projectKeyOrId=" + projectKey, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            if (boardResponse.getStatusCode() == HttpStatus.OK) {
                JsonNode boardResult = objectMapper.readTree(boardResponse.getBody());
                if (boardResult.has("values") && boardResult.get("values").size() > 0) {
                    String boardId = boardResult.get("values").get(0).get("id").asText();
                    
                    // Get sprints for this board
                    String sprintUrl = jiraUrl + "/rest/agile/1.0/board/" + boardId + "/sprint";
                    ResponseEntity<String> sprintResponse = restTemplate.exchange(sprintUrl, HttpMethod.GET, request, String.class);
                    
                    if (sprintResponse.getStatusCode() == HttpStatus.OK) {
                        JsonNode sprintResult = objectMapper.readTree(sprintResponse.getBody());
                        List<Map<String, Object>> sprints = new ArrayList<>();
                        
                        if (sprintResult.has("values")) {
                            for (JsonNode sprint : sprintResult.get("values")) {
                                Map<String, Object> sprintInfo = new HashMap<>();
                                sprintInfo.put("id", sprint.get("id").asText());
                                sprintInfo.put("name", sprint.get("name").asText());
                                sprintInfo.put("state", sprint.get("state").asText());
                                sprintInfo.put("startDate", sprint.get("startDate").asText());
                                sprintInfo.put("endDate", sprint.get("endDate").asText());
                                sprints.add(sprintInfo);
                            }
                        }
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("sprints", sprints);
                        return result;
                    }
                }
            }
            
            return Map.of("sprints", new ArrayList<>());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get sprints: " + e.getMessage(), e);
        }
    }
    
    public Map<String, Object> analyzeSprint(String jiraUrl, String projectKey, String username, String apiToken, String sprintId) {
        try {
            // Get sprint issues
            List<Map<String, Object>> sprintIssues = getSprintIssuesById(jiraUrl, projectKey, username, apiToken, sprintId);
            
            // Analyze issues
            Map<String, Object> analysis = analyzeIssues(sprintIssues);
            
            // Add sprint information
            analysis.put("totalBugs", sprintIssues.size());
            analysis.put("sprintId", sprintId);
            
            return analysis;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze sprint: " + e.getMessage(), e);
        }
    }
    
    private List<Map<String, Object>> getSprintIssuesById(String jiraUrl, String projectKey, String username, String apiToken, String sprintId) {
        try {
            String url = jiraUrl + "/rest/api/2/search";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Build JQL query for specific sprint issues
            String jql = String.format("project = %s AND sprint = %s ORDER BY priority DESC", projectKey, sprintId);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("jql", jql);
            requestBody.put("maxResults", 100);
            requestBody.put("fields", Arrays.asList("summary", "description", "status", "priority", "assignee", "created", "comment"));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode searchResult = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> issues = new ArrayList<>();
                
                JsonNode issuesNode = searchResult.get("issues");
                for (JsonNode issue : issuesNode) {
                    Map<String, Object> issueData = new HashMap<>();
                    issueData.put("key", issue.get("key").asText());
                    issueData.put("summary", issue.path("fields").path("summary").asText(""));
                    issueData.put("description", issue.path("fields").path("description").asText(""));
                    issueData.put("status", issue.path("fields").path("status").path("name").asText(""));
                    issueData.put("priority", issue.path("fields").path("priority").path("name").asText(""));
                    issueData.put("assignee", issue.path("fields").path("assignee").path("displayName").asText("Unassigned"));
                    issueData.put("created", issue.path("fields").path("created").asText(""));
                    
                    // Get comments
                    List<String> comments = new ArrayList<>();
                    JsonNode commentNode = issue.path("fields").path("comment");
                    if (commentNode.has("comments")) {
                        for (JsonNode comment : commentNode.get("comments")) {
                            comments.add(comment.path("body").asText(""));
                        }
                    }
                    issueData.put("comments", comments);
                    
                    issues.add(issueData);
                }
                
                return issues;
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get sprint issues: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> analyzeIssues(List<Map<String, Object>> issues) {
        List<Map<String, Object>> dataQualityIssues = new ArrayList<>();
        List<Map<String, Object>> languageIssues = new ArrayList<>();
        
        for (Map<String, Object> issue : issues) {
            String description = (String) issue.get("description");
            String summary = (String) issue.get("summary");
            String key = (String) issue.get("key");
            
            // Analyze data quality
            analyzeDataQuality(issue, key, dataQualityIssues);
            
            // Analyze language quality
            analyzeLanguageQuality(issue, key, languageIssues);
        }
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("dataQualityIssues", dataQualityIssues);
        analysis.put("languageIssues", languageIssues);
        analysis.put("bugs", issues);
        analysis.put("dataIssues", dataQualityIssues.size());
        analysis.put("spellingErrors", languageIssues.stream().filter(issue -> "Spelling Errors".equals(issue.get("issueType"))).count());
        analysis.put("grammarIssues", languageIssues.stream().filter(issue -> "Grammar Issues".equals(issue.get("issueType"))).count());
        
        return analysis;
    }
    
    private void analyzeDataQuality(Map<String, Object> issue, String key, List<Map<String, Object>> dataQualityIssues) {
        String description = (String) issue.get("description");
        String summary = (String) issue.get("summary");
        
        // Check for missing description
        if (description == null || description.trim().isEmpty()) {
            addIssue(dataQualityIssues, key, "Missing description", "Missing Data");
        }
        
        // Check for missing steps to reproduce
        if (description != null && !description.toLowerCase().contains("step") && !description.toLowerCase().contains("reproduce")) {
            addIssue(dataQualityIssues, key, "No clear steps to reproduce", "Missing Steps");
        }
        
        // Check for missing environment info
        if (description != null && !description.toLowerCase().contains("browser") && !description.toLowerCase().contains("os") && !description.toLowerCase().contains("device")) {
            addIssue(dataQualityIssues, key, "Missing environment information", "Missing Environment");
        }
        
        // Check for missing expected vs actual
        if (description != null && !description.toLowerCase().contains("expected") && !description.toLowerCase().contains("actual")) {
            addIssue(dataQualityIssues, key, "Expected vs actual behavior not clearly defined", "Unclear Requirements");
        }
    }
    
    private void analyzeLanguageQuality(Map<String, Object> issue, String key, List<Map<String, Object>> languageIssues) {
        String description = (String) issue.get("description");
        String summary = (String) issue.get("summary");
        
        // Simple spelling check (basic implementation)
        if (description != null) {
            List<String> commonMisspellings = Arrays.asList("recieve", "seperate", "occured", "definately", "accomodate");
            for (String misspelling : commonMisspellings) {
                if (description.toLowerCase().contains(misspelling)) {
                    addIssue(languageIssues, key, "Contains common misspelling: " + misspelling, "Spelling Errors");
                    break;
                }
            }
        }
        
        // Check for poor grammar patterns
        if (description != null) {
            if (description.contains("..") || description.contains("!!") || description.contains("??")) {
                addIssue(languageIssues, key, "Poor punctuation usage", "Grammar Issues");
            }
            
            // Check for run-on sentences (very basic)
            String[] sentences = description.split("[.!?]");
            for (String sentence : sentences) {
                if (sentence.trim().split("\\s+").length > 50) {
                    addIssue(languageIssues, key, "Very long sentence detected", "Grammar Issues");
                    break;
                }
            }
        }
    }
    
    private void addIssue(List<Map<String, Object>> issues, String bugKey, String description, String issueType) {
        Map<String, Object> issue = new HashMap<>();
        issue.put("bugKey", bugKey);
        issue.put("description", description);
        issue.put("issueType", issueType);
        issues.add(issue);
    }
    
    public Map<String, Object> getProjects(String jiraUrl, String username, String apiToken) {
        try {
            String url = jiraUrl + "/rest/api/2/project";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode projects = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> projectList = new ArrayList<>();
                
                for (JsonNode project : projects) {
                    Map<String, Object> projectInfo = new HashMap<>();
                    projectInfo.put("key", project.get("key").asText());
                    projectInfo.put("name", project.get("name").asText());
                    projectInfo.put("id", project.get("id").asText());
                    projectList.add(projectInfo);
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("projects", projectList);
                return result;
            }
            
            return Map.of("projects", new ArrayList<>());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get projects: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getProjectStatistics(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            // Get all issues for the project
            List<Map<String, Object>> issues = getAllProjectIssues(jiraUrl, projectKey, username, apiToken);
            
            // Calculate statistics
            Map<String, Object> stats = new HashMap<>();
            
            // Count by issue type
            Map<String, Integer> issueTypesDistribution = new HashMap<>();
            Map<String, Integer> statusDistribution = new HashMap<>();
            
            int bugsCount = 0;
            int userStoriesCount = 0;
            int tasksCount = 0;
            int totalIssues = issues.size();
            int completedIssues = 0;
            
            for (Map<String, Object> issue : issues) {
                String issueType = (String) issue.get("issueType");
                String status = (String) issue.get("status");
                
                // Count by issue type
                issueTypesDistribution.put(issueType, issueTypesDistribution.getOrDefault(issueType, 0) + 1);
                
                // Count by status
                statusDistribution.put(status, statusDistribution.getOrDefault(status, 0) + 1);
                
                // Count specific types
                if ("Bug".equalsIgnoreCase(issueType)) {
                    bugsCount++;
                } else if ("Story".equalsIgnoreCase(issueType) || "User Story".equalsIgnoreCase(issueType)) {
                    userStoriesCount++;
                } else if ("Task".equalsIgnoreCase(issueType)) {
                    tasksCount++;
                }
                
                // Count completed issues
                if ("Done".equalsIgnoreCase(status) || "Closed".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status)) {
                    completedIssues++;
                }
            }
            
            // Calculate progress percentage
            int progressPercentage = totalIssues > 0 ? (completedIssues * 100) / totalIssues : 0;
            
            stats.put("bugsCount", bugsCount);
            stats.put("userStoriesCount", userStoriesCount);
            stats.put("tasksCount", tasksCount);
            stats.put("totalIssues", totalIssues);
            stats.put("completedIssues", completedIssues);
            stats.put("progressPercentage", progressPercentage);
            stats.put("issueTypesDistribution", issueTypesDistribution);
            stats.put("statusDistribution", statusDistribution);
            
            // Generate velocity data (mock data for now)
            stats.put("velocityData", generateVelocityData());
            
            // Generate burndown data (mock data for now)
            stats.put("burndownData", generateBurndownData());
            
            return stats;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get project statistics: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getSprintStatistics(String jiraUrl, String projectKey, String username, String apiToken, String sprintId) {
        try {
            // Get sprint issues
            List<Map<String, Object>> sprintIssues = getSprintIssuesById(jiraUrl, projectKey, username, apiToken, sprintId);
            
            // Get sprint information
            Map<String, Object> sprintInfo = getSprintInfo(jiraUrl, projectKey, username, apiToken, sprintId);
            
            // Calculate statistics similar to project statistics but for sprint only
            Map<String, Object> stats = new HashMap<>();
            
            Map<String, Integer> issueTypesDistribution = new HashMap<>();
            Map<String, Integer> statusDistribution = new HashMap<>();
            
            int bugsCount = 0;
            int userStoriesCount = 0;
            int tasksCount = 0;
            int totalIssues = sprintIssues.size();
            int completedIssues = 0;
            
            for (Map<String, Object> issue : sprintIssues) {
                String issueType = (String) issue.get("issueType");
                String status = (String) issue.get("status");
                
                issueTypesDistribution.put(issueType, issueTypesDistribution.getOrDefault(issueType, 0) + 1);
                statusDistribution.put(status, statusDistribution.getOrDefault(status, 0) + 1);
                
                if ("Bug".equalsIgnoreCase(issueType)) {
                    bugsCount++;
                } else if ("Story".equalsIgnoreCase(issueType) || "User Story".equalsIgnoreCase(issueType)) {
                    userStoriesCount++;
                } else if ("Task".equalsIgnoreCase(issueType)) {
                    tasksCount++;
                }
                
                if ("Done".equalsIgnoreCase(status) || "Closed".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status)) {
                    completedIssues++;
                }
            }
            
            int progressPercentage = totalIssues > 0 ? (completedIssues * 100) / totalIssues : 0;
            
            stats.put("bugsCount", bugsCount);
            stats.put("userStoriesCount", userStoriesCount);
            stats.put("tasksCount", tasksCount);
            stats.put("totalIssues", totalIssues);
            stats.put("completedIssues", completedIssues);
            stats.put("progressPercentage", progressPercentage);
            stats.put("issueTypesDistribution", issueTypesDistribution);
            stats.put("statusDistribution", statusDistribution);
            
            // Add sprint information
            if (sprintInfo != null) {
                stats.put("startDate", sprintInfo.get("startDate"));
                stats.put("endDate", sprintInfo.get("endDate"));
                stats.put("state", sprintInfo.get("state"));
                stats.put("name", sprintInfo.get("name"));
            }
            
            return stats;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get sprint statistics: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> getAllProjectIssues(String jiraUrl, String projectKey, String username, String apiToken) {
        try {
            String url = jiraUrl + "/rest/api/2/search";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Build JQL query for all project issues
            String jql = String.format("project = %s ORDER BY created DESC", projectKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("jql", jql);
            requestBody.put("maxResults", 1000);
            requestBody.put("fields", Arrays.asList("summary", "description", "status", "priority", "assignee", "created", "issuetype"));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode searchResult = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> issues = new ArrayList<>();
                
                JsonNode issuesNode = searchResult.get("issues");
                for (JsonNode issue : issuesNode) {
                    Map<String, Object> issueData = new HashMap<>();
                    issueData.put("key", issue.get("key").asText());
                    issueData.put("summary", issue.path("fields").path("summary").asText(""));
                    issueData.put("description", issue.path("fields").path("description").asText(""));
                    issueData.put("status", issue.path("fields").path("status").path("name").asText(""));
                    issueData.put("priority", issue.path("fields").path("priority").path("name").asText(""));
                    issueData.put("issueType", issue.path("fields").path("issuetype").path("name").asText(""));
                    issueData.put("assignee", issue.path("fields").path("assignee").path("displayName").asText("Unassigned"));
                    issueData.put("created", issue.path("fields").path("created").asText(""));
                    
                    issues.add(issueData);
                }
                
                return issues;
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get project issues: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> getSprintInfo(String jiraUrl, String projectKey, String username, String apiToken, String sprintId) {
        try {
            // Get board ID first
            String boardUrl = jiraUrl + "/rest/agile/1.0/board";
            HttpHeaders headers = createAuthHeaders(username, apiToken);
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> boardResponse = restTemplate.exchange(
                boardUrl + "?projectKeyOrId=" + projectKey, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            if (boardResponse.getStatusCode() == HttpStatus.OK) {
                JsonNode boardResult = objectMapper.readTree(boardResponse.getBody());
                if (boardResult.has("values") && boardResult.get("values").size() > 0) {
                    String boardId = boardResult.get("values").get(0).get("id").asText();
                    
                    // Get specific sprint info
                    String sprintUrl = jiraUrl + "/rest/agile/1.0/sprint/" + sprintId;
                    ResponseEntity<String> sprintResponse = restTemplate.exchange(sprintUrl, HttpMethod.GET, request, String.class);
                    
                    if (sprintResponse.getStatusCode() == HttpStatus.OK) {
                        JsonNode sprint = objectMapper.readTree(sprintResponse.getBody());
                        Map<String, Object> sprintInfo = new HashMap<>();
                        sprintInfo.put("id", sprint.get("id").asText());
                        sprintInfo.put("name", sprint.get("name").asText());
                        sprintInfo.put("state", sprint.get("state").asText());
                        sprintInfo.put("startDate", sprint.get("startDate").asText());
                        sprintInfo.put("endDate", sprint.get("endDate").asText());
                        return sprintInfo;
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get sprint info: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> generateVelocityData() {
        // Mock velocity data - in real implementation, this would come from historical sprint data
        Map<String, Object> velocityData = new HashMap<>();
        velocityData.put("labels", Arrays.asList("Sprint 1", "Sprint 2", "Sprint 3", "Sprint 4", "Sprint 5"));
        velocityData.put("values", Arrays.asList(25, 30, 28, 35, 32));
        return velocityData;
    }

    private Map<String, Object> generateBurndownData() {
        // Mock burndown data - in real implementation, this would come from sprint progress data
        Map<String, Object> burndownData = new HashMap<>();
        burndownData.put("labels", Arrays.asList("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Day 7", "Day 8", "Day 9", "Day 10"));
        burndownData.put("ideal", Arrays.asList(100, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0));
        burndownData.put("actual", Arrays.asList(100, 95, 88, 82, 75, 68, 60, 52, 45, 38, 30));
        return burndownData;
    }

    private HttpHeaders createAuthHeaders(String username, String apiToken) {
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}
