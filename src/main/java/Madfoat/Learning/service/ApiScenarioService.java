package Madfoat.Learning.service;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiScenarioService {

    private final Map<String, Map<String, Object>> idToScenario = new ConcurrentHashMap<>();

    public List<Map<String, Object>> generateScenarios(String curl, String description) {
        // For now, create 3 simple scenarios derived from input. In future, call AI to expand
        List<Map<String, Object>> scenarios = new ArrayList<>();
        scenarios.add(createScenario("Happy Path", curl, description, Map.of("expectedStatus", 200)));
        scenarios.add(createScenario("Unauthorized", curl + " -H 'Authorization: Bearer invalid'", description, Map.of("expectedStatus", 401)));
        scenarios.add(createScenario("Invalid Payload", curl, description, Map.of("expectedStatus", 400, "mutateBody", true)));
        return scenarios;
    }

    public Map<String, Object> runScenario(String id) {
        Map<String, Object> scenario = idToScenario.get(id);
        if (scenario == null) {
            return Map.of("id", id, "status", "error", "message", "Scenario not found");
        }
        try {
            String curl = (String) scenario.get("curl");
            RequestParts parts = parseCurl(curl);
            if (Boolean.TRUE.equals(scenario.get("mutateBody")) && parts.body != null) {
                parts.body = parts.body.replaceAll("\\d+", "-1");
            }
            Response resp = execute(parts);
            int status = resp.statusCode();
            String body = resp.asString();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", id);
            result.put("status", "done");
            result.put("httpStatus", status);
            result.put("body", body);
            return result;
        } catch (Exception ex) {
            return Map.of("id", id, "status", "error", "message", ex.getMessage());
        }
    }

    private Map<String, Object> createScenario(String title, String curl, String description, Map<String, Object> extra) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> sc = new LinkedHashMap<>();
        sc.put("id", id);
        sc.put("title", title);
        sc.put("curl", curl);
        if (description != null && !description.isBlank()) sc.put("desc", description);
        if (extra != null) sc.putAll(extra);
        idToScenario.put(id, sc);
        return sc;
    }

    static class RequestParts {
        String method;
        String url;
        Map<String, String> headers = new LinkedHashMap<>();
        String body;
    }

    private RequestParts parseCurl(String curl) {
        // Very simple cURL parser (supports: -X, -H, --header, -d/--data/--data-raw)
        RequestParts p = new RequestParts();
        List<String> tokens = tokenize(curl);
        Iterator<String> it = tokens.iterator();
        while (it.hasNext()) {
            String t = it.next();
            if (t.equalsIgnoreCase("curl")) continue;
            if (t.equals("-X") && it.hasNext()) { p.method = it.next(); continue; }
            if ((t.equals("-H") || t.equals("--header")) && it.hasNext()) {
                String hv = stripQuotes(it.next());
                int idx = hv.indexOf(':');
                if (idx > 0) p.headers.put(hv.substring(0, idx).trim(), hv.substring(idx + 1).trim());
                continue;
            }
            if (t.equals("-d") || t.equals("--data") || t.equals("--data-raw")) {
                if (it.hasNext()) p.body = stripQuotes(it.next());
                continue;
            }
            if (t.startsWith("http://") || t.startsWith("https://")) {
                p.url = stripQuotes(t);
            }
        }
        if (p.method == null) p.method = p.body != null ? "POST" : "GET";
        return p;
    }

    private List<String> tokenize(String s) {
        List<String> out = new ArrayList<>();
        boolean inQuote = false; char quoteChar = 0; StringBuilder cur = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inQuote) {
                if (c == quoteChar) { inQuote = false; out.add(cur.toString()); cur.setLength(0); }
                else cur.append(c);
            } else {
                if (c == '\'' || c == '"') { inQuote = true; quoteChar = c; }
                else if (Character.isWhitespace(c)) { if (cur.length() > 0) { out.add(cur.toString()); cur.setLength(0); } }
                else cur.append(c);
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }

    private String stripQuotes(String s) {
        if (s == null) return null;
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private Response execute(RequestParts p) {
        io.restassured.specification.RequestSpecification req = RestAssured.given();
        if (p.headers != null) p.headers.forEach(req::header);
        if (p.body != null) req.body(p.body);
        String method = p.method == null ? "GET" : p.method.toUpperCase(Locale.ROOT);
        return switch (method) {
            case "POST" -> req.post(URI.create(p.url));
            case "PUT" -> req.put(URI.create(p.url));
            case "PATCH" -> req.patch(URI.create(p.url));
            case "DELETE" -> req.delete(URI.create(p.url));
            case "HEAD" -> req.head(URI.create(p.url));
            case "OPTIONS" -> req.options(URI.create(p.url));
            default -> req.get(URI.create(p.url));
        };
    }
}