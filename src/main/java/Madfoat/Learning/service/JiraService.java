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
    
    private HttpHeaders createAuthHeaders(String username, String apiToken) {
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}
