package Madfoat.Learning.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.*;
import java.util.*;

@Controller
public class WebController {

    @GetMapping("/performance-test")
    public String performanceTestPage() {
        return "performance-test";
    }

    @GetMapping("/performance")
    public String homePage() {
        return "performance-test";
    }

    @GetMapping("/project-structure")
    public String projectStructurePage() {
        return "project-structure";
    }

    @GetMapping("/chart-cleanup")
    public String chartCleanupPage() {
        return "chart-cleanup";
    }

    @GetMapping("/api/settings/properties")
    @ResponseBody
    public Map<String, String> getProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("src/main/resources/application.properties")) {
            props.load(in);
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (String name : props.stringPropertyNames()) {
            map.put(name, props.getProperty(name));
        }
        return map;
    }

    @PostMapping("/api/settings/properties")
    @ResponseBody
    public Map<String, Object> updateProperties(@RequestBody Map<String, String> updates) throws IOException {
        Properties props = new Properties();
        File file = new File("src/main/resources/application.properties");
        try (InputStream in = new FileInputStream(file)) {
            props.load(in);
        }
        for (Map.Entry<String, String> e : updates.entrySet()) {
            props.setProperty(e.getKey(), e.getValue());
        }
        try (OutputStream out = new FileOutputStream(file)) {
            props.store(out, "Application Properties Updated via UI");
        }
        return Map.of("status", "ok");
    }
}