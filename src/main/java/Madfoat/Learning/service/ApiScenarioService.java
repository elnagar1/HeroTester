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

    public List<Map<String, Object>> generateScenarios(String curl, Integer limit, List<String> caseTypes) {
        int max = limit == null ? 5 : Math.max(1, Math.min(50, limit));
        Set<String> wanted = (caseTypes == null || caseTypes.isEmpty())
                ? Set.of("happy")
                : new LinkedHashSet<>(caseTypes);

        List<Map<String, Object>> scenarios = new ArrayList<>();
        // Base cases
        if (wanted.contains("happy")) scenarios.add(createScenario("Happy Path", curl, null, Map.of("expectedStatus", 200)));
        if (wanted.contains("unauthorized")) scenarios.add(createScenario("Unauthorized", curl + " -H 'Authorization: Bearer invalid'", null, Map.of("expectedStatus", 401)));
        if (wanted.contains("invalid_payload")) scenarios.add(createScenario("Invalid Payload", curl, null, Map.of("expectedStatus", 400, "mutateBody", true)));

        // Derived from original curl
        try {
            RequestParts base = parseCurl(curl);

            if (wanted.contains("pagination")) {
                RequestParts p = cloneParts(base);
                p.url = ensureQueryParam(p.url, Map.of("page", "2", "limit", "10"));
                scenarios.add(createScenario("Pagination (page=2, limit=10)", buildCurl(p), null, Map.of("expectedStatus", 200)));
                RequestParts pInvalid = cloneParts(base);
                pInvalid.url = ensureQueryParam(pInvalid.url, Map.of("page", "-1"));
                scenarios.add(createScenario("Invalid pagination (page=-1)", buildCurl(pInvalid), null, Map.of("expectedStatus", 400)));
            }

            if (wanted.contains("rate_limit")) {
                RequestParts p = cloneParts(base);
                p.headers.put("X-RateLimit-Test", "true");
                scenarios.add(createScenario("Rate limit exceeded", buildCurl(p), null, Map.of("expectedStatus", 429)));
            }

            if (wanted.contains("not_found")) {
                RequestParts p = cloneParts(base);
                p.url = mutatePathToNotFound(p.url);
                scenarios.add(createScenario("Resource not found", buildCurl(p), null, Map.of("expectedStatus", 404)));
            }

            if (wanted.contains("conflict")) {
                RequestParts p = cloneParts(base);
                p.headers.put("X-Conflict-Test", "true");
                scenarios.add(createScenario("Conflict state", buildCurl(p), null, Map.of("expectedStatus", 409)));
            }

            if (wanted.contains("xml")) {
                RequestParts p = cloneParts(base);
                p.headers.put("Accept", "application/xml");
                if (p.body != null && !p.body.trim().startsWith("<")) {
                    p.headers.putIfAbsent("Content-Type", "application/xml");
                    p.body = "<payload>" + escapeXml(p.body) + "</payload>";
                }
                scenarios.add(createScenario("XML response", buildCurl(p), null, Map.of("expectedStatus", 200)));
            }

            if (wanted.contains("missing_required") && base.body != null) {
                RequestParts p = cloneParts(base);
                p.body = mutateBodyRemoveFirstField(p.body);
                scenarios.add(createScenario("Missing required field", buildCurl(p), null, Map.of("expectedStatus", 400)));
            }
        } catch (Exception ignore) {
        }

        if (scenarios.size() >= max) {
            return scenarios.subList(0, max);
        }

        // Fill up to desired max with simple, labeled variants of existing scenarios
        try {
            while (scenarios.size() < max && !scenarios.isEmpty()) {
                int idx = scenarios.size() % scenarios.size();
                Map<String, Object> baseSc = scenarios.get(idx);
                String baseTitle = String.valueOf(baseSc.getOrDefault("title", "Variant"));
                String baseCurl = String.valueOf(baseSc.get("curl"));
                Integer baseExpected = (Integer) baseSc.getOrDefault("expectedStatus", 200);
                RequestParts p = parseCurl(baseCurl);
                // mark variant in URL for uniqueness
                p.url = ensureQueryParam(p.url, Map.of("variant", String.valueOf(scenarios.size() + 1)));
                String title = baseTitle + " (variant " + (scenarios.size() + 1) + ")";
                scenarios.add(createScenario(title, buildCurl(p), null, Map.of("expectedStatus", baseExpected)));
            }
        } catch (Exception ignored) {}

        return scenarios.subList(0, Math.min(max, scenarios.size()));
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
            long start = System.currentTimeMillis();
            Response resp = execute(parts);
            long timeMs = System.currentTimeMillis() - start;
            int status = resp.statusCode();
            String body = resp.asString();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", id);
            result.put("status", "done");
            result.put("httpStatus", status);
            result.put("body", body);
            try { result.put("contentType", resp.getContentType()); } catch (Exception ignored) {}
            try {
                result.put(
                        "headers",
                        resp.getHeaders() == null ? Map.of() : resp.getHeaders().asList().stream().collect(
                                java.util.stream.Collectors.toMap(h -> h.getName(), h -> h.getValue(), (a, b) -> b, LinkedHashMap::new)
                        )
                );
            } catch (Exception ignored) {}
            result.put("timeMs", timeMs);
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

    public Map<String, String> getScenarioCode(String id) {
        Map<String, Object> scenario = idToScenario.get(id);
        if (scenario == null) {
            return Map.of("status", "error", "message", "Scenario not found");
        }
        String curl = (String) scenario.get("curl");
        Integer expected = (Integer) scenario.getOrDefault("expectedStatus", 200);
        String assertContains = (String) scenario.getOrDefault("assertContains", null);
        @SuppressWarnings("unchecked")
        Map<String, Object> assertions = (Map<String, Object>) scenario.get("assertions");
        RequestParts parts = parseCurl(curl);
        String code = buildRestAssuredCode(parts, expected == null ? 200 : expected, assertContains, assertions);
        return Map.of("status", "ok", "code", code);
    }

    public Map<String, Object> getScenarioDetail(String id) {
        Map<String, Object> scenario = idToScenario.get(id);
        if (scenario == null) {
            return Map.of("status", "error", "message", "Scenario not found");
        }
        RequestParts p = parseCurl((String) scenario.get("curl"));
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("status", "ok");
        detail.put("id", id);
        detail.put("title", scenario.get("title"));
        detail.put("method", p.method);
        detail.put("url", p.url);
        detail.put("headers", p.headers);
        detail.put("body", p.body);
        detail.put("expectedStatus", scenario.getOrDefault("expectedStatus", 200));
        if (scenario.containsKey("assertions")) {
            detail.put("assertions", scenario.get("assertions"));
        }
        return detail;
    }

    private String buildRestAssuredCode(RequestParts p, int expectedStatus, String assertContains) {
        StringBuilder b = new StringBuilder();
        b.append("import io.restassured.RestAssured;\n");
        b.append("import io.restassured.http.ContentType;\n");
        b.append("import static io.restassured.RestAssured.*;\n");
        b.append("import static org.hamcrest.Matchers.*;\n\n");
        b.append("public class ApiScenarioTest {\n");
        b.append("    public void run() {\n");
        b.append("        given()\n");
        if (p.headers != null && !p.headers.isEmpty()) {
            for (Map.Entry<String, String> e : p.headers.entrySet()) {
                b.append("            .header(\"").append(escapeJava(e.getKey())).append("\", \"")
                        .append(escapeJava(e.getValue())).append("\")\n");
            }
        }
        if (p.body != null && !p.body.isBlank()) {
            boolean hasCt = p.headers.keySet().stream().map(String::toLowerCase).anyMatch(h -> h.equals("content-type"));
            if (!hasCt) {
                String ct = looksLikeXml(p.body) ? "application/xml" : "application/json";
                b.append("            .contentType(\"").append(ct).append("\")\n");
            }
            // pretty body section
            b.append("            .body(\n");
            String prettyBody = p.body;
            if (!looksLikeXml(p.body)) {
                try {
                    prettyBody = new com.fasterxml.jackson.databind.ObjectMapper().readTree(p.body).toPrettyString();
                } catch (Exception ignored) {}
            }
            String[] bodyLines = prettyBody.split("\n", -1);
            for (int i = 0; i < bodyLines.length; i++) {
                String line = bodyLines[i];
                b.append("                \"").append(escapeJava(line)).append("\"");
                if (i < bodyLines.length - 1) {
                    b.append(" +");
                }
                b.append("\n");
            }
            b.append("            )\n");
        }
        b.append("        .when()\n");
        String method = p.method == null ? "GET" : p.method.toUpperCase(Locale.ROOT);
        b.append("            .").append(method.toLowerCase(Locale.ROOT)).append("(\"")
                .append(escapeJava(p.url)).append("\")\n");
        b.append("        .then()\n");
        b.append("            .statusCode(").append(expectedStatus).append(")\n");
        if (assertContains != null && !assertContains.isBlank()) {
            b.append("            .body(containsString(\"").append(escapeJava(assertContains)).append("\"))\n");
        }
        b.append("        ;\n");
        b.append("    }\n");
        b.append("}\n");
        return b.toString();
    }

    private String buildRestAssuredCode(RequestParts p, int expectedStatus, String assertContains, Map<String, Object> assertions) {
        // Fallback to legacy single contains assertion if no structured assertions provided
        if (assertions == null || assertions.isEmpty()) {
            return buildRestAssuredCode(p, expectedStatus, assertContains);
        }

        StringBuilder b = new StringBuilder();
        b.append("import io.restassured.RestAssured;\n");
        b.append("import io.restassured.http.ContentType;\n");
        b.append("import static io.restassured.RestAssured.*;\n");
        b.append("import static org.hamcrest.Matchers.*;\n\n");
        b.append("public class ApiScenarioTest {\n");
        b.append("    public void run() {\n");
        b.append("        given()\n");
        if (p.headers != null && !p.headers.isEmpty()) {
            for (Map.Entry<String, String> e : p.headers.entrySet()) {
                b.append("            .header(\"").append(escapeJava(e.getKey())).append("\", \"")
                        .append(escapeJava(e.getValue())).append("\")\n");
            }
        }
        if (p.body != null && !p.body.isBlank()) {
            boolean hasCt = p.headers.keySet().stream().map(String::toLowerCase).anyMatch(h -> h.equals("content-type"));
            if (!hasCt) {
                String ct = looksLikeXml(p.body) ? "application/xml" : "application/json";
                b.append("            .contentType(\"").append(ct).append("\")\n");
            }
            b.append("            .body(\n");
            String prettyBody = p.body;
            if (!looksLikeXml(p.body)) {
                try {
                    prettyBody = new com.fasterxml.jackson.databind.ObjectMapper().readTree(p.body).toPrettyString();
                } catch (Exception ignored) {}
            }
            String[] bodyLines = prettyBody.split("\n", -1);
            for (int i = 0; i < bodyLines.length; i++) {
                String line = bodyLines[i];
                b.append("                \"").append(escapeJava(line)).append("\"");
                if (i < bodyLines.length - 1) {
                    b.append(" +");
                }
                b.append("\n");
            }
            b.append("            )\n");
        }
        b.append("        .when()\n");
        String method = p.method == null ? "GET" : p.method.toUpperCase(Locale.ROOT);
        b.append("            .").append(method.toLowerCase(Locale.ROOT)).append("(\"")
                .append(escapeJava(p.url)).append("\")\n");
        b.append("        .then()\n");
        // Status code
        b.append("            .statusCode(").append(extractInt(assertions.getOrDefault("expectedStatus", expectedStatus))).append(")\n");

        // Status family range
        Object statusFamily = assertions.get("expectedStatusRange");
        if (statusFamily instanceof String s && !s.isBlank()) {
            if (s.startsWith("2")) b.append("            .statusCode(greaterThanOrEqualTo(200)).statusCode(lessThan(300))\n");
            else if (s.startsWith("3")) b.append("            .statusCode(greaterThanOrEqualTo(300)).statusCode(lessThan(400))\n");
            else if (s.startsWith("4")) b.append("            .statusCode(greaterThanOrEqualTo(400)).statusCode(lessThan(500))\n");
            else if (s.startsWith("5")) b.append("            .statusCode(greaterThanOrEqualTo(500)).statusCode(lessThan(600))\n");
        }

        // Content-Type
        Object ctAssert = assertions.get("contentType");
        if (ctAssert instanceof String s && !s.isBlank()) {
            b.append("            .contentType(\"").append(escapeJava(s)).append("\")\n");
        }

        // Time less than
        Object timeLt = assertions.get("timeLessThanMs");
        if (timeLt instanceof Number num) {
            b.append("            .time(lessThan(").append(num.longValue()).append("L))\n");
        }

        // Headers equals
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> headerEquals = (List<Map<String, Object>>) assertions.get("headerEquals");
        if (headerEquals != null) {
            for (Map<String, Object> h : headerEquals) {
                String name = String.valueOf(h.getOrDefault("name", ""));
                String value = String.valueOf(h.getOrDefault("value", ""));
                if (!name.isBlank()) {
                    b.append("            .header(\"").append(escapeJava(name)).append("\", \"")
                            .append(escapeJava(value)).append("\")\n");
                }
            }
        }

        // Header exists
        @SuppressWarnings("unchecked")
        List<String> headerExists = (List<String>) assertions.get("headerExists");
        if (headerExists != null) {
            for (String name : headerExists) {
                if (name != null && !name.isBlank()) {
                    b.append("            .header(\"").append(escapeJava(name)).append("\", notNullValue())\n");
                }
            }
        }

        // Header contains substring
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> headerContains = (List<Map<String, Object>>) assertions.get("headerContains");
        if (headerContains != null) {
            for (Map<String, Object> h : headerContains) {
                String name = String.valueOf(h.getOrDefault("name", ""));
                String substring = String.valueOf(h.getOrDefault("substring", ""));
                if (!name.isBlank() && !substring.isBlank()) {
                    b.append("            .header(\"").append(escapeJava(name)).append("\", containsString(\"")
                            .append(escapeJava(substring)).append("\"))\n");
                }
            }
        }

        // Body contains (multiple)
        @SuppressWarnings("unchecked")
        List<String> containsList = (List<String>) assertions.get("containsText");
        if (containsList != null) {
            for (String s : containsList) {
                if (s != null && !s.isBlank()) {
                    b.append("            .body(containsString(\"").append(escapeJava(s)).append("\"))\n");
                }
            }
        } else if (assertContains != null && !assertContains.isBlank()) {
            b.append("            .body(containsString(\"").append(escapeJava(assertContains)).append("\"))\n");
        }

        // Body NOT contains
        @SuppressWarnings("unchecked")
        List<String> notContains = (List<String>) assertions.get("notContainsText");
        if (notContains != null) {
            for (String s : notContains) {
                if (s != null && !s.isBlank()) {
                    b.append("            .body(not(containsString(\"").append(escapeJava(s)).append("\")))\n");
                }
            }
        }

        // JSONPath equals
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> jsonPathEquals = (List<Map<String, Object>>) assertions.get("jsonPathEquals");
        if (jsonPathEquals != null) {
            for (Map<String, Object> j : jsonPathEquals) {
                String path = String.valueOf(j.getOrDefault("path", ""));
                Object value = j.get("value");
                if (!path.isBlank()) {
                    b.append("            .body(\"").append(escapeJava(path)).append("\", ");
                    if (value instanceof Number || value instanceof Boolean) {
                        b.append("equalTo(").append(String.valueOf(value)).append(")");
                    } else if (value == null || "null".equals(String.valueOf(value))) {
                        b.append("equalTo(null)");
                    } else {
                        b.append("equalTo(\"").append(escapeJava(String.valueOf(value))).append("\")");
                    }
                    b.append(")\n");
                }
            }
        }

        // JSONPath size equals
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> jsonPathSizeEquals = (List<Map<String, Object>>) assertions.get("jsonPathSizeEquals");
        if (jsonPathSizeEquals != null) {
            for (Map<String, Object> j : jsonPathSizeEquals) {
                String path = String.valueOf(j.getOrDefault("path", ""));
                Object size = j.get("size");
                if (!path.isBlank() && size instanceof Number num) {
                    b.append("            .body(\"").append(escapeJava(path)).append(".size()\", equalTo(")
                            .append(num.intValue()).append("))\n");
                }
            }
        }

        // JSONPath contains item
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> jsonPathContains = (List<Map<String, Object>>) assertions.get("jsonPathContains");
        if (jsonPathContains != null) {
            for (Map<String, Object> j : jsonPathContains) {
                String path = String.valueOf(j.getOrDefault("path", ""));
                Object value = j.get("value");
                if (!path.isBlank()) {
                    b.append("            .body(\"").append(escapeJava(path)).append("\", hasItem(");
                    if (value instanceof Number || value instanceof Boolean) {
                        b.append(String.valueOf(value));
                    } else if (value == null || "null".equals(String.valueOf(value))) {
                        b.append("null");
                    } else {
                        b.append("\"").append(escapeJava(String.valueOf(value))).append("\"");
                    }
                    b.append("))\n");
                }
            }
        }

        // XPath assertions (basic): if content looks like XML
        boolean looksXml = looksLikeXml(p.body != null ? p.body : "<x/>");
        if (looksXml) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> xPathExists = (List<Map<String, Object>>) assertions.get("xPathExists");
            if (xPathExists != null) {
                for (Map<String, Object> x : xPathExists) {
                    String path = String.valueOf(x.getOrDefault("path", ""));
                    if (!path.isBlank()) {
                        // Rest Assured supports XML path via body
                        b.append("            .body(hasXPath(\"").append(escapeJava(path)).append("\"))\n");
                    }
                }
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> xPathEquals = (List<Map<String, Object>>) assertions.get("xPathEquals");
            if (xPathEquals != null) {
                for (Map<String, Object> x : xPathEquals) {
                    String path = String.valueOf(x.getOrDefault("path", ""));
                    Object value = x.get("value");
                    if (!path.isBlank()) {
                        b.append("            .body(hasXPath(\"").append(escapeJava(path)).append("\", containsString(\"")
                                .append(escapeJava(String.valueOf(value))).append("\")))\n");
                    }
                }
            }
        }

        b.append("        ;\n");
        b.append("    }\n");
        b.append("}\n");
        return b.toString();
    }

    private int extractInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(val)); } catch (Exception ignored) { return 200; }
    }

    public Map<String, String> setScenarioAssertion(String id, String expectedContains) {
        Map<String, Object> scenario = idToScenario.get(id);
        if (scenario == null) {
            return Map.of("status", "error", "message", "Scenario not found");
        }
        if (expectedContains == null) expectedContains = "";
        scenario.put("assertContains", expectedContains);
        return Map.of("status", "ok");
    }

    public Map<String, String> setScenarioAssertions(String id, Map<String, Object> assertions, Integer expectedStatus) {
        Map<String, Object> scenario = idToScenario.get(id);
        if (scenario == null) {
            return Map.of("status", "error", "message", "Scenario not found");
        }
        if (assertions == null) assertions = new LinkedHashMap<>();
        // Normalize containsText if provided as single string
        Object contains = assertions.get("containsText");
        if (contains instanceof String s) {
            if (s.isBlank()) assertions.remove("containsText");
            else assertions.put("containsText", java.util.List.of(s));
        }
        scenario.put("assertions", assertions);
        if (expectedStatus != null) scenario.put("expectedStatus", expectedStatus);
        return Map.of("status", "ok");
    }

    private RequestParts cloneParts(RequestParts src) {
        RequestParts p = new RequestParts();
        p.method = src.method;
        p.url = src.url;
        p.headers = new LinkedHashMap<>(src.headers);
        p.body = src.body;
        return p;
    }

    private String buildCurl(RequestParts p) {
        StringBuilder curl = new StringBuilder("curl");
        if (p.method != null) curl.append(" -X ").append(p.method);
        for (Map.Entry<String, String> e : p.headers.entrySet()) {
            curl.append(" -H '").append(e.getKey()).append(": ").append(e.getValue()).append("'");
        }
        if (p.body != null) curl.append(" -d '").append(p.body.replace("'", "'\\''")).append("'");
        curl.append(" ").append(p.url);
        return curl.toString();
    }

    private String ensureQueryParam(String url, Map<String, String> params) {
        try {
            URI u = URI.create(url);
            String existing = u.getQuery();
            StringBuilder q = new StringBuilder(existing == null ? "" : existing + "&");
            for (Map.Entry<String, String> e : params.entrySet()) {
                if (q.length() > 0 && q.charAt(q.length() - 1) != '&') q.append('&');
                q.append(e.getKey()).append('=').append(e.getValue());
            }
            URI with = new URI(u.getScheme(), u.getAuthority(), u.getPath(), q.toString(), u.getFragment());
            return with.toString();
        } catch (Exception e) {
            return url;
        }
    }

    private String mutatePathToNotFound(String url) {
        try {
            URI u = URI.create(url);
            String path = u.getPath();
            String newPath = path == null ? "/non-existent" : (path.matches(".*/\\d+$") ? path.replaceAll("\\d+$", "999999999") : (path.endsWith("/") ? path + "non-existent" : path + "/non-existent"));
            URI with = new URI(u.getScheme(), u.getAuthority(), newPath, u.getQuery(), u.getFragment());
            return with.toString();
        } catch (Exception e) {
            return url + "/non-existent";
        }
    }

    private String mutateBodyRemoveFirstField(String body) {
        // Very naive removal of the first JSON key-value if body looks like JSON
        String trimmed = body == null ? null : body.trim();
        if (trimmed == null || trimmed.isEmpty()) return body;
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            // remove content between first key and following comma
            String inner = trimmed.substring(1, trimmed.length() - 1);
            int comma = inner.indexOf(',');
            String newInner = comma > 0 ? inner.substring(comma + 1).trim() : "";
            if (newInner.startsWith(",")) newInner = newInner.substring(1).trim();
            return "{" + newInner + "}";
        }
        return ""; // otherwise produce empty to trigger 400
    }

    private boolean looksLikeXml(String s) {
        String t = s == null ? "" : s.trim();
        return t.startsWith("<") && t.endsWith(">");
    }

    private String escapeJava(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
