package Madfoat.Learning.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/performance-test")
    public String performanceTestPage() {
        return "performance-test";
    }

    @GetMapping("/")
    public String homePage() {
        return "performance-test";
    }
}