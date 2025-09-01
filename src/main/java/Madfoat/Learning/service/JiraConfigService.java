package Madfoat.Learning.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class JiraConfigService {
    
    @Value("${jira.url:}")
    private String url;
    
    @Value("${jira.project.key:}")
    private String projectKey;
    
    @Value("${jira.username:}")
    private String username;
    
    @Value("${jira.api.token:}")
    private String apiToken;
    
    @Value("${jira.enabled:false}")
    private boolean enabled;
    
    // Getters
    public String getUrl() { return url; }
    public String getProjectKey() { return projectKey; }
    public String getUsername() { return username; }
    public String getApiToken() { return apiToken; }
    public boolean isEnabled() { return enabled; }
    
    // Setters
    public void setUrl(String url) { this.url = url; }
    public void setProjectKey(String projectKey) { this.projectKey = projectKey; }
    public void setUsername(String username) { this.username = username; }
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public Map<String, Object> getCurrentConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("url", url);
        config.put("projectKey", projectKey);
        config.put("username", username);
        config.put("enabled", enabled);
        
        // عرض جزء من API Token (آمن نسبياً)
        if (apiToken != null && !apiToken.trim().isEmpty()) {
            if (apiToken.length() > 10) {
                config.put("apiTokenMasked", apiToken.substring(0, 10) + "***");
            } else {
                config.put("apiTokenMasked", apiToken + "***");
            }
        } else {
            config.put("apiTokenMasked", "غير محدد");
        }
        
        return config;
    }
    
    public void updateConfig(String url, String projectKey, String username, String apiToken) {
        this.url = url;
        this.projectKey = projectKey;
        this.username = username;
        if (apiToken != null && !apiToken.trim().isEmpty()) {
            this.apiToken = apiToken;
        }
    }
    
    public boolean isConfigured() {
        return url != null && !url.trim().isEmpty() &&
               projectKey != null && !projectKey.trim().isEmpty() &&
               username != null && !username.trim().isEmpty() &&
               apiToken != null && !apiToken.trim().isEmpty();
    }
}
