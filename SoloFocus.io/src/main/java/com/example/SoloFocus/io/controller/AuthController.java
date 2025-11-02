package com.example.SoloFocus.io.controller;

import com.example.SoloFocus.io.dto.UserLoginDTO;
import com.example.SoloFocus.io.dto.UserRegistrationDTO;
import com.example.SoloFocus.io.model.User;
import com.example.SoloFocus.io.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        if (!model.containsAttribute("userLogin")) {
            model.addAttribute("userLogin", new UserLoginDTO());
        }
        return "login";
    }
    
    @PostMapping("/login")
    public String login(@Valid UserLoginDTO userLogin, BindingResult result,
                       HttpSession session, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userLogin", result);
            redirectAttributes.addFlashAttribute("userLogin", userLogin);
            return "redirect:/auth/login";
        }
        
        try {
            User user = userService.loginUser(userLogin.getUsername(), userLogin.getPassword());
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            return "redirect:/timer";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("userLogin", userLogin);
            return "redirect:/auth/login";
        }
    }
    
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        if (!model.containsAttribute("userRegistration")) {
            model.addAttribute("userRegistration", new UserRegistrationDTO());
        }
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@Valid UserRegistrationDTO userRegistration, BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegistration", result);
            redirectAttributes.addFlashAttribute("userRegistration", userRegistration);
            return "redirect:/auth/register";
        }
        
        if (!userRegistration.getPassword().equals(userRegistration.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            redirectAttributes.addFlashAttribute("userRegistration", userRegistration);
            return "redirect:/auth/register";
        }
        
        try {
            userService.registerUser(
                userRegistration.getUsername(),
                userRegistration.getEmail(),
                userRegistration.getPassword()
            );
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("userRegistration", userRegistration);
            return "redirect:/auth/register";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}

