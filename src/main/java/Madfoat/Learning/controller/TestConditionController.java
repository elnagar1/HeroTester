package Madfoat.Learning.controller;

import Madfoat.Learning.service.AIService;
import Madfoat.Learning.service.ImageProcessingService;
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
    public String generateFromText(@RequestParam("businessText") String businessText, Model model) {
        if (businessText == null || businessText.trim().isEmpty()) {
            model.addAttribute("error", "Please provide business text or requirements");
            return "index";
        }

        try {
            String testConditions = aiService.generateTestConditions(businessText, "text");
            model.addAttribute("input", businessText);
            model.addAttribute("inputType", "Business Text");
            model.addAttribute("testConditions", testConditions);
            return "results";
        } catch (Exception e) {
            model.addAttribute("error", "Error generating test conditions: " + e.getMessage());
            return "index";
        }
    }

    @PostMapping("/generate-from-image")
    public String generateFromImage(@RequestParam("imageFile") MultipartFile imageFile, Model model) {
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
            String testConditions = aiService.generateTestConditions(imageContext, "image");
            
            model.addAttribute("input", imageContext);
            model.addAttribute("inputType", "Image Analysis");
            model.addAttribute("testConditions", testConditions);
            model.addAttribute("fileName", imageFile.getOriginalFilename());
            return "results";
        } catch (Exception e) {
            model.addAttribute("error", "Error processing image and generating test conditions: " + e.getMessage());
            return "index";
        }
    }

    @GetMapping("/api/health")
    @ResponseBody
    public String health() {
        return "Test Analyst AI Assistant is running!";
    }
}
