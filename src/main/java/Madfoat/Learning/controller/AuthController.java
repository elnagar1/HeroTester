package Madfoat.Learning.controller;

import Madfoat.Learning.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Refers to login.html
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // You can add an empty User object to the model if you're using Thymeleaf forms
        // model.addAttribute("user", new User());
        return "register"; // Refers to register.html
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            userService.registerNewUser(username, password);
            return "redirect:/login?registered"; // Redirect to login page with success message
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "register"; // Stay on registration page with error
        }
    }
}
