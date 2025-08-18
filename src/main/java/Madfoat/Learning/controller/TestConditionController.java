package Madfoat.Learning.controller;

import Madfoat.Learning.service.AIService;
import Madfoat.Learning.service.ImageProcessingService;
import Madfoat.Learning.dto.GenerateRequest;
import Madfoat.Learning.dto.RowQuestion;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class TestConditionController {

    @Autowired
    private AIService aiService;

    @Autowired
    private ImageProcessingService imageProcessingService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/generate-from-text")
    public String generateFromText(
            @RequestParam("businessText") String businessText,
            @RequestParam(value = "generationType", required = false) String generationType,
            @RequestParam(value = "includeAcceptance", required = false, defaultValue = "false") boolean includeAcceptance,
            @RequestParam(value = "selectedTypes", required = false) String[] selectedTypes,
            Model model) {
        if (businessText == null || businessText.trim().isEmpty()) {
            model.addAttribute("error", "Please provide business text or requirements");
            return "index";
        }

        try {
            String genType = generationType == null ? "test_scenarios" : generationType;
            String result = aiService.generateContent(businessText, "text", genType, includeAcceptance, selectedTypes == null ? null : java.util.Arrays.asList(selectedTypes));
            model.addAttribute("input", businessText);
            model.addAttribute("inputType", "Business Text");
            model.addAttribute("generationType", genType);
            model.addAttribute("testConditions", result);
            return "results";
        } catch (Exception e) {
            model.addAttribute("error", "Error generating content: " + e.getMessage());
            return "index";
        }
    }

    @PostMapping("/generate-from-image")
    public String generateFromImage(
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "generationType", required = false) String generationType,
            @RequestParam(value = "includeAcceptance", required = false, defaultValue = "false") boolean includeAcceptance,
            @RequestParam(value = "selectedTypes", required = false) String[] selectedTypes,
            Model model) {
        if (imageFile == null || imageFile.isEmpty()) {
            model.addAttribute("error", "Please select an image file");
            return "index";
        }

        if (!imageProcessingService.isValidImageFile(imageFile)) {
            model.addAttribute("error", "Please upload a valid image file (JPEG, PNG, GIF, BMP)");
            return "index";
        }

        try {
            String imageContext = imageProcessingService.getImageAnalysisContext(imageFile);
            String genType = generationType == null ? "test_scenarios" : generationType;
            String result = aiService.generateContent(imageContext, "image", genType, includeAcceptance, selectedTypes == null ? null : java.util.Arrays.asList(selectedTypes));
            
            model.addAttribute("input", imageContext);
            model.addAttribute("inputType", "Image Analysis");
            model.addAttribute("generationType", genType);
            model.addAttribute("testConditions", result);
            model.addAttribute("fileName", imageFile.getOriginalFilename());
            return "results";
        } catch (Exception e) {
            model.addAttribute("error", "Error processing image and generating content: " + e.getMessage());
            return "index";
        }
    }

    @GetMapping("/api/health")
    @ResponseBody
    public String health() {
        return "Test Analyst AI Assistant is running!";
    }

    // API to request additional generation from results page
    @PostMapping("/api/generate-more")
    @ResponseBody
    public String generateMore(@RequestBody GenerateRequest req) {
        String genType = req.getGenerationType() == null ? "test_scenarios" : req.getGenerationType();
        return aiService.generateContent(
                req.getInput(),
                req.getInputType() == null ? "text" : req.getInputType(),
                genType,
                req.isIncludeAcceptance(),
                req.getSelectedTypes()
        );
    }

    // QA endpoint: ask about a specific table row
    @PostMapping("/api/ask-row")
    @ResponseBody
    public String askRow(@RequestBody RowQuestion rq) {
        String prompt = "You are a helpful QA assistant. Based on the following selected test case row, answer the user's question concisely.\n\n" +
                "Row:\n" + rq.getRowText() + "\n\n" +
                "Question: " + rq.getQuestion();
        return aiService.generateContent(prompt, "text", "user_story", false, null);
    }

    // Unified generate endpoint: accepts text and optional image together
    @PostMapping("/generate")
    public String generateUnified(
            @RequestParam(value = "businessText", required = false) String businessText,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "generationTypes", required = false) String[] generationTypes,
            @RequestParam(value = "includeAcceptance", required = false, defaultValue = "false") boolean includeAcceptance,
            @RequestParam(value = "selectedTypes", required = false) String[] selectedTypes,
            Model model
    ) {
        try {
            String textPart = businessText != null ? businessText.trim() : "";
            String imageContext = "";
            String inputType = "";
            if (imageFile != null && !imageFile.isEmpty()) {
                if (!imageProcessingService.isValidImageFile(imageFile)) {
                    model.addAttribute("error", "Please upload a valid image file (JPEG, PNG, GIF, BMP)");
                    return "index";
                }
                imageContext = imageProcessingService.getImageAnalysisContext(imageFile);
            }
            if (textPart.isEmpty() && imageContext.isEmpty()) {
                model.addAttribute("error", "Please provide text and/or an image");
                return "index";
            }
            String combinedInput;
            if (!textPart.isEmpty() && !imageContext.isEmpty()) {
                combinedInput = textPart + "\n\n" + imageContext;
                inputType = "Text + Image";
            } else if (!textPart.isEmpty()) {
                combinedInput = textPart;
                inputType = "Business Text";
            } else {
                combinedInput = imageContext;
                inputType = "Image Analysis";
            }

            String primaryType = (generationTypes != null && generationTypes.length > 0) ? generationTypes[0] : "test_scenarios";
            String result = aiService.generateContent(combinedInput, imageContext.isEmpty() ? "text" : (textPart.isEmpty() ? "image" : "mixed"), primaryType, includeAcceptance, selectedTypes == null ? null : Arrays.asList(selectedTypes));

            model.addAttribute("input", combinedInput);
            model.addAttribute("inputType", inputType);
            model.addAttribute("generationType", primaryType);
            model.addAttribute("testConditions", result);
            model.addAttribute("fileName", imageFile != null ? imageFile.getOriginalFilename() : null);
            String initialTypesCsv = (generationTypes != null && generationTypes.length > 0) ? Arrays.stream(generationTypes).collect(Collectors.joining(",")) : primaryType;
            model.addAttribute("initialTypes", initialTypesCsv);
            model.addAttribute("includeAcceptanceFlag", includeAcceptance);
            String selectedFilters = (selectedTypes != null && selectedTypes.length > 0) ? Arrays.stream(selectedTypes).collect(Collectors.joining(",")) : "";
            model.addAttribute("selectedTypeFilters", selectedFilters);

            return "results";
        } catch (Exception e) {
            model.addAttribute("error", "Error generating content: " + e.getMessage());
            return "index";
        }
    }
}
