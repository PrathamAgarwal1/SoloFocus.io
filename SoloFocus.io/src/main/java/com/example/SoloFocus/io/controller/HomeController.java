package com.example.SoloFocus.io.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            return "redirect:/timer";
        }
        return "redirect:/auth/login";
    }
}

