package Madfoat.Learning.controller;

import Madfoat.Learning.service.AIService;
import Madfoat.Learning.service.KnowledgeBaseService;
import Madfoat.Learning.service.KnowledgeBaseService.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class AIModelController {

    @Autowired
    private KnowledgeBaseService kb;

    @Autowired
    private AIService aiService;

    @GetMapping("/ai")
    public String aiConsole(Model model) {
        List<Document> docs = kb.listDocuments();
        model.addAttribute("docs", docs);
        return "ai-model";
    }

    @PostMapping("/ai/ingest-text")
    public String ingestText(@RequestParam("title") String title,
                             @RequestParam("text") String text,
                             Model model) {
        kb.ingestText(title, text);
        model.addAttribute("docs", kb.listDocuments());
        model.addAttribute("message", "Text ingested successfully");
        return "ai-model";
    }

    @PostMapping("/ai/ingest-file")
    public String ingestFile(@RequestParam("file") MultipartFile file, Model model) {
        kb.ingestFile(file);
        model.addAttribute("docs", kb.listDocuments());
        model.addAttribute("message", "File ingested successfully");
        return "ai-model";
    }

    @PostMapping("/ai/connect-jira")
    public String connectJira(@RequestParam("baseUrl") String baseUrl,
                              @RequestParam("email") String email,
                              @RequestParam("token") String token,
                              @RequestParam("projectKey") String projectKey,
                              @RequestParam(value = "maxIssues", required = false, defaultValue = "25") int maxIssues,
                              Model model) {
        kb.ingestJiraProject(baseUrl, email, token, projectKey, maxIssues);
        model.addAttribute("docs", kb.listDocuments());
        model.addAttribute("message", "Jira project content fetched");
        return "ai-model";
    }

    @PostMapping("/ai/connect-slack")
    public String connectSlack(@RequestParam("token") String token,
                               @RequestParam("channel") String channel,
                               @RequestParam(value = "limit", required = false, defaultValue = "50") int limit,
                               Model model) {
        kb.ingestSlackChannel(token, channel, limit);
        model.addAttribute("docs", kb.listDocuments());
        model.addAttribute("message", "Slack channel content fetched");
        return "ai-model";
    }

    @PostMapping("/ai/ask")
    public String ask(@RequestParam("question") String question, Model model) {
        String context = kb.buildContextForQuestion(question, 12000);
        String prompt = "You are an AI assistant. Use ONLY the following context to answer.\n\n" +
                "CONTEXT:\n" + context + "\n\n" +
                "QUESTION: " + question + "\n\n" +
                "Answer concisely. If the answer is not in the context, say you don't have enough information.";
        String answer = aiService.generateWithPrompt(prompt);
        model.addAttribute("docs", kb.listDocuments());
        model.addAttribute("answer", answer);
        model.addAttribute("lastQuestion", question);
        return "ai-model";
    }
}

