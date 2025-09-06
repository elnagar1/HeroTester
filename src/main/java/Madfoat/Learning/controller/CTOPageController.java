package Madfoat.Learning.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CTOPageController {

    @GetMapping("/cto/results")
    public String showResultsPage() {
        return "cto-results";
    }
}