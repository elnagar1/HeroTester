package Madfoat.Learning.controller;

import Madfoat.Learning.service.CTOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cto")
@CrossOrigin(origins = "*")
public class CTOController {

    @Autowired
    private CTOService ctoService;
    
    @Value("${jira.url:}")
    private String jiraUrl;
    
    @Value("${jira.username:}")
    private String jiraUsername;
    
    @Value("${jira.api.token:}")
    private String jiraApiToken;

    @PostMapping("/project-stats")
    public ResponseEntity<Map<String, Object>> getProjectStatistics(@RequestBody Map<String, String> request) {
        try {
            String jiraUrl;
            String projectKey = request.get("projectKey");
            String username;
            String apiToken;
            
            // Check if we should use config values
            boolean useConfig = "true".equals(request.get("useConfig"));
            
            if (useConfig) {
                // Use values from properties file
                jiraUrl = this.jiraUrl;
                username = this.jiraUsername;
                apiToken = this.jiraApiToken;
                
                if (jiraUrl == null || jiraUrl.isEmpty() || 
                    username == null || username.isEmpty() || 
                    apiToken == null || apiToken.isEmpty()) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Jira configuration not found in properties file"));
                }
            } else {
                // Use values from request
                jiraUrl = request.get("jiraUrl");
                username = request.get("username");
                apiToken = request.get("apiToken");
                
                if (jiraUrl == null || projectKey == null || username == null || apiToken == null) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required fields: jiraUrl, projectKey, username, apiToken"));
                }
            }

            if (projectKey == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Project key is required"));
            }

            // Remove trailing slash from Jira URL if present
            if (jiraUrl.endsWith("/")) {
                jiraUrl = jiraUrl.substring(0, jiraUrl.length() - 1);
            }

            Map<String, Object> statistics = ctoService.getProjectStatistics(jiraUrl, projectKey, username, apiToken);
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get project statistics: " + e.getMessage()));
        }
    }

    @PostMapping("/export")
    public ResponseEntity<String> exportCTOData(@RequestBody Map<String, Object> data) {
        try {
            // Generate CSV content
            StringBuilder csv = new StringBuilder();
            csv.append("Issue Key,Summary,Type,Status,Priority,Assignee,Created,Updated,Description\n");

            if (data.containsKey("issues")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> issues = (java.util.List<Map<String, Object>>) data.get("issues");
                
                for (Map<String, Object> issue : issues) {
                    String key = (String) issue.get("key");
                    String summary = (String) issue.get("summary");
                    String type = (String) issue.get("type");
                    String status = (String) issue.get("status");
                    String priority = (String) issue.get("priority");
                    String assignee = (String) issue.get("assignee");
                    String created = (String) issue.get("created");
                    String updated = (String) issue.get("updated");
                    String description = (String) issue.get("description");
                    
                    csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        key, summary, type, status, priority, assignee, created, updated, description));
                }
            }

            return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"cto-management-report.csv\"")
                .body(csv.toString());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Failed to export data: " + e.getMessage());
        }
    }

    @PostMapping("/projects")
    public ResponseEntity<Map<String, Object>> getProjects(@RequestBody Map<String, String> request) {
        try {
            String jiraUrl = request.get("jiraUrl");
            String username = request.get("username");
            String apiToken = request.get("apiToken");

            System.out.println("Received request for projects:");
            System.out.println("Jira URL: " + jiraUrl);
            System.out.println("Username: " + username);
            System.out.println("API Token: " + (apiToken != null ? "***" + apiToken.substring(Math.max(0, apiToken.length() - 4)) : "null"));

            if (jiraUrl == null || username == null || apiToken == null) {
                System.out.println("Missing required fields");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields: jiraUrl, username, apiToken"));
            }

            // Remove trailing slash from Jira URL if present
            if (jiraUrl.endsWith("/")) {
                jiraUrl = jiraUrl.substring(0, jiraUrl.length() - 1);
            }

            Map<String, Object> projects = ctoService.getProjects(jiraUrl, username, apiToken);
            System.out.println("Returning projects: " + projects);
            return ResponseEntity.ok(projects);

        } catch (Exception e) {
            System.err.println("Error in getProjects controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get projects: " + e.getMessage()));
        }
    }

    @GetMapping("/test-projects")
    public ResponseEntity<Map<String, Object>> testProjects() {
        try {
            // Test with a sample Jira URL (this will fail but we can see the error)
            Map<String, Object> result = ctoService.getProjects(
                "https://test.atlassian.net", 
                "test@example.com", 
                "test-token"
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "status", "Test endpoint working - error expected"
            ));
        }
    }

    @PostMapping("/debug-projects")
    public ResponseEntity<Map<String, Object>> debugProjects(@RequestBody Map<String, String> request) {
        try {
            String jiraUrl = request.get("jiraUrl");
            String username = request.get("username");
            String apiToken = request.get("apiToken");

            System.out.println("=== DEBUG PROJECTS REQUEST ===");
            System.out.println("Jira URL: " + jiraUrl);
            System.out.println("Username: " + username);
            System.out.println("API Token: " + (apiToken != null ? "***" + apiToken.substring(Math.max(0, apiToken.length() - 4)) : "null"));

            if (jiraUrl == null || username == null || apiToken == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields"));
            }

            // Remove trailing slash from Jira URL if present
            if (jiraUrl.endsWith("/")) {
                jiraUrl = jiraUrl.substring(0, jiraUrl.length() - 1);
            }

            Map<String, Object> projects = ctoService.getProjects(jiraUrl, username, apiToken);
            
            System.out.println("=== DEBUG PROJECTS RESULT ===");
            System.out.println("Result: " + projects);
            
            return ResponseEntity.ok(projects);

        } catch (Exception e) {
            System.err.println("Error in debug-projects: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Debug failed: " + e.getMessage()));
        }
    }

    @GetMapping("/projects")
    public ResponseEntity<Map<String, Object>> getProjectsFromConfig() {
        try {
            if (jiraUrl == null || jiraUrl.isEmpty() || 
                jiraUsername == null || jiraUsername.isEmpty() || 
                jiraApiToken == null || jiraApiToken.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Jira configuration not found in properties file"));
            }

            // Remove trailing slash from Jira URL if present
            String cleanJiraUrl = jiraUrl.endsWith("/") ? jiraUrl.substring(0, jiraUrl.length() - 1) : jiraUrl;

            Map<String, Object> projects = ctoService.getProjects(cleanJiraUrl, jiraUsername, jiraApiToken);
            return ResponseEntity.ok(projects);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get projects: " + e.getMessage()));
        }
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(Map.of(
            "jiraUrl", jiraUrl != null ? jiraUrl : "",
            "username", jiraUsername != null ? jiraUsername : "",
            "hasApiToken", jiraApiToken != null && !jiraApiToken.isEmpty(),
            "configured", jiraUrl != null && !jiraUrl.isEmpty() && 
                        jiraUsername != null && !jiraUsername.isEmpty() && 
                        jiraApiToken != null && !jiraApiToken.isEmpty()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "service", "CTO Management"));
    }
}