package Madfoat.Learning.controller;

import Madfoat.Learning.service.JiraService;
import Madfoat.Learning.service.JiraConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/jira")
@CrossOrigin(origins = "*")
public class JiraController {

    @Autowired
    private JiraService jiraService;
    
    @Autowired
    private JiraConfigService jiraConfigService;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getJiraConfig() {
        try {
            Map<String, Object> config = jiraConfigService.getCurrentConfig();
            config.put("configured", jiraConfigService.isConfigured());
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get Jira configuration: " + e.getMessage()));
        }
    }

    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> updateJiraConfig(@RequestBody Map<String, String> request) {
        try {
            String url = request.get("url");
            String projectKey = request.get("projectKey");
            String username = request.get("username");
            String apiToken = request.get("apiToken");

            if (url == null || projectKey == null || username == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields: url, projectKey, username"));
            }

            jiraConfigService.updateConfig(url, projectKey, username, apiToken);
            
            return ResponseEntity.ok(Map.of(
                "message", "Jira configuration updated successfully",
                "configured", jiraConfigService.isConfigured()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update Jira configuration: " + e.getMessage()));
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeJiraProject(@RequestBody Map<String, String> request) {
        try {
            // Use configuration from properties if not provided in request
            String jiraUrl = request.get("jiraUrl") != null ? request.get("jiraUrl") : jiraConfigService.getUrl();
            String projectKey = request.get("projectKey") != null ? request.get("projectKey") : jiraConfigService.getProjectKey();
            String username = request.get("username") != null ? request.get("username") : jiraConfigService.getUsername();
            String apiToken = request.get("apiToken") != null ? request.get("apiToken") : jiraConfigService.getApiToken();

            if (jiraUrl == null || projectKey == null || username == null || apiToken == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields: jiraUrl, projectKey, username, apiToken"));
            }

            // Remove trailing slash from Jira URL if present
            if (jiraUrl.endsWith("/")) {
                jiraUrl = jiraUrl.substring(0, jiraUrl.length() - 1);
            }

            Map<String, Object> analysis = jiraService.analyzeJiraProject(jiraUrl, projectKey, username, apiToken);
            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to analyze Jira project: " + e.getMessage()));
        }
    }

    @PostMapping("/export")
    public ResponseEntity<String> exportJiraAnalysis(@RequestBody Map<String, Object> analysis) {
        try {
            // Generate CSV content
            StringBuilder csv = new StringBuilder();
            csv.append("Issue Key,Summary,Status,Priority,Assignee,Data Quality Issues,Language Issues\n");

            if (analysis.containsKey("bugs")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> bugs = (java.util.List<Map<String, Object>>) analysis.get("bugs");
                
                for (Map<String, Object> bug : bugs) {
                    String key = (String) bug.get("key");
                    String summary = (String) bug.get("summary");
                    String status = (String) bug.get("status");
                    String priority = (String) bug.get("priority");
                    String assignee = (String) bug.get("assignee");
                    
                    // Find data quality issues for this bug
                    String dataIssues = "";
                    if (analysis.containsKey("dataQualityIssues")) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Map<String, Object>> dataQualityIssues = (java.util.List<Map<String, Object>>) analysis.get("dataQualityIssues");
                        dataIssues = dataQualityIssues.stream()
                            .filter(issue -> key.equals(issue.get("bugKey")))
                            .map(issue -> (String) issue.get("issueType"))
                            .reduce("", (a, b) -> a.isEmpty() ? b : a + "; " + b);
                    }
                    
                    // Find language issues for this bug
                    String languageIssuesStr = "";
                    if (analysis.containsKey("languageIssues")) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Map<String, Object>> languageIssuesList = (java.util.List<Map<String, Object>>) analysis.get("languageIssues");
                        languageIssuesStr = languageIssuesList.stream()
                            .filter(issue -> key.equals(issue.get("bugKey")))
                            .map(issue -> (String) issue.get("issueType"))
                            .reduce("", (a, b) -> a.isEmpty() ? b : a + "; " + b);
                    }
                    
                    csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        key, summary, status, priority, assignee, dataIssues, languageIssuesStr));
                }
            }

            return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"jira-analysis.csv\"")
                .body(csv.toString());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Failed to export analysis: " + e.getMessage());
        }
    }
}
