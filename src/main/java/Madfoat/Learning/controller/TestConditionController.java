package Madfoat.Learning.controller;

import Madfoat.Learning.service.AIService;
import Madfoat.Learning.service.ImageProcessingService;
import Madfoat.Learning.dto.GenerateRequest;
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
}
