package Madfoat.Learning.service;

import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Sheet;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Cell;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class KnowledgeBaseService {

    public static class Document {
        public String id;
        public String title;
        public String source; // text, file, jira, slack
        public String content;
        public Map<String, String> metadata;
        public Instant createdAt;
    }

    private final List<Document> documents = new CopyOnWriteArrayList<>();
    private final WebClient http = WebClient.builder().build();

    public List<Document> listDocuments() {
        return new ArrayList<>(documents);
    }

    public void ingestText(String title, String text) {
        if (text == null || text.trim().isEmpty()) return;
        Document d = new Document();
        d.id = UUID.randomUUID().toString();
        d.title = (title == null || title.isBlank()) ? ("Text-" + d.id.substring(0, 6)) : title.trim();
        d.source = "text";
        d.content = text;
        d.metadata = Map.of();
        d.createdAt = Instant.now();
        documents.add(d);
    }

    public void ingestFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return;
        String name = file.getOriginalFilename();
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        String content;
        try {
            if (lower.endsWith(".xlsx") || lower.endsWith(".xlsm") || lower.endsWith(".xls")) {
                content = extractExcel(file.getInputStream());
            } else if (lower.endsWith(".csv")) {
                content = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else if (lower.endsWith(".txt") || lower.endsWith(".md")) {
                content = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else {
                content = "[Unsupported file type for preview]";
            }
        } catch (Exception e) {
            content = "[Error reading file: " + e.getMessage() + "]";
        }
        Document d = new Document();
        d.id = UUID.randomUUID().toString();
        d.title = (name == null || name.isBlank()) ? ("File-" + d.id.substring(0, 6)) : name;
        d.source = "file";
        d.content = content;
        d.metadata = Map.of("filename", name == null ? "" : name);
        d.createdAt = Instant.now();
        documents.add(d);
    }

    private String extractExcel(InputStream in) {
        try (ReadableWorkbook wb = new ReadableWorkbook(in)) {
            Sheet sheet = wb.getFirstSheet();
            if (sheet == null) return "[Empty Excel workbook]";
            java.util.List<java.util.List<String>> rowsData = new ArrayList<>();
            try (java.util.stream.Stream<Row> rows = sheet.openStream()) {
                rows.forEach(r -> {
                    java.util.List<String> vals = new ArrayList<>();
                    for (Cell c : r) {
                        String t = c == null ? "" : c.getText();
                        vals.add(t == null ? "" : t);
                    }
                    rowsData.add(vals);
                });
            }
            if (rowsData.isEmpty()) return "[Empty Excel workbook]";
            // Build Markdown table using first row as headers
            java.util.List<String> headers = new ArrayList<>();
            for (String h : rowsData.get(0)) headers.add(h == null || h.isBlank() ? "Col" + (headers.size() + 1) : h.trim());
            int numCols = headers.size();
            StringBuilder md = new StringBuilder();
            md.append("| ").append(String.join(" | ", headers)).append(" |\n");
            md.append("| ");
            for (int i = 0; i < numCols; i++) { md.append("---"); if (i < numCols - 1) md.append(" | "); }
            md.append(" |\n");
            for (int i = 1; i < rowsData.size(); i++) {
                java.util.List<String> rowVals = rowsData.get(i);
                // pad row to numCols
                java.util.List<String> padded = new ArrayList<>();
                for (int c = 0; c < numCols; c++) padded.add(c < rowVals.size() ? rowVals.get(c) : "");
                md.append("| ").append(String.join(" | ", padded)).append(" |\n");
            }
            return md.toString();
        } catch (Exception e) { return "[Error parsing Excel: " + e.getMessage() + "]"; }
    }

    public int ingestJiraProject(String baseUrl, String email, String apiToken, String projectKey, int maxIssues) {
        try {
            String jql = "project=" + projectKey + " ORDER BY updated DESC";
            String url = baseUrl.replaceAll("/+$", "") + "/rest/api/3/search?maxResults=" + Math.max(1, Math.min(100, maxIssues)) + "&jql=" + java.net.URLEncoder.encode(jql, StandardCharsets.UTF_8);
            String basic = Base64.getEncoder().encodeToString((email + ":" + apiToken).getBytes(StandardCharsets.UTF_8));
            String resp = http.get().uri(url)
                    .header("Authorization", "Basic " + basic)
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(ex -> Mono.just("[Jira error: " + ex.getMessage() + "]"))
                    .block();
            Document d = new Document();
            d.id = UUID.randomUUID().toString();
            d.title = "Jira Project " + projectKey;
            d.source = "jira";
            d.content = resp == null ? "" : resp;
            d.metadata = Map.of("projectKey", projectKey);
            d.createdAt = Instant.now();
            documents.add(d);
            return 1;
        } catch (Exception e) { return 0; }
    }

    public int ingestSlackChannel(String botToken, String channelId, int limit) {
        try {
            String url = "https://slack.com/api/conversations.history?limit=" + Math.max(1, Math.min(100, limit)) + "&channel=" + java.net.URLEncoder.encode(channelId, StandardCharsets.UTF_8);
            String resp = http.get().uri(url)
                    .header("Authorization", "Bearer " + botToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(ex -> Mono.just("[Slack error: " + ex.getMessage() + "]"))
                    .block();
            Document d = new Document();
            d.id = UUID.randomUUID().toString();
            d.title = "Slack Channel " + channelId;
            d.source = "slack";
            d.content = resp == null ? "" : resp;
            d.metadata = Map.of("channel", channelId);
            d.createdAt = Instant.now();
            documents.add(d);
            return 1;
        } catch (Exception e) { return 0; }
    }

    public String buildContextForQuestion(String question, int maxChars) {
        if (documents.isEmpty()) return "";
        String[] terms = Arrays.stream(question.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(s -> s.length() > 2).toArray(String[]::new);
        List<Document> ranked = new ArrayList<>(documents);
        ranked.sort((a, b) -> score(b, terms) - score(a, terms));
        StringBuilder ctx = new StringBuilder();
        for (Document d : ranked) {
            if (ctx.length() > maxChars) break;
            ctx.append("Source: ").append(d.source).append(" | Title: ").append(d.title).append("\n");
            String c = d.content == null ? "" : d.content;
            if (c.length() > 2000) c = c.substring(0, 2000) + "...";
            ctx.append(c).append("\n\n");
        }
        if (ctx.length() > maxChars) return ctx.substring(0, maxChars);
        return ctx.toString();
    }

    private int score(Document d, String[] terms) {
        if (terms.length == 0) return 0;
        String c = (d.content == null ? "" : d.content.toLowerCase(Locale.ROOT)) + " " +
                (d.title == null ? "" : d.title.toLowerCase(Locale.ROOT));
        int s = 0;
        for (String t : terms) {
            if (t.isBlank()) continue;
            if (c.contains(t)) s += 2;
        }
        return s;
    }
}

