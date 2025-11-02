package com.example.SoloFocus.io.controller;

import com.example.SoloFocus.io.service.StatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @GetMapping
    public String showDashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        try {
            // Always refresh statistics before displaying dashboard
            statisticsService.updateUserStatistics(userId);
            
            // Get all statistics
            Map<String, Object> stats = statisticsService.getDashboardStatistics(userId);
            Map<String, Integer> weeklyData = statisticsService.getWeeklyStatistics(userId);
            Map<String, Integer> monthlyData = statisticsService.getMonthlyStatistics(userId);
            Map<String, Integer> yearlyData = statisticsService.getYearlyStatistics(userId);
            Map<String, Integer> contributionData = statisticsService.getContributionData(userId);
            
            // Add basic stats
            model.addAttribute("totalHours", stats.get("totalHours") != null ? stats.get("totalHours") : 0.0);
            model.addAttribute("currentStreak", stats.get("currentStreak") != null ? stats.get("currentStreak") : 0);
            model.addAttribute("maxStreak", stats.get("maxStreak") != null ? stats.get("maxStreak") : 0);
            model.addAttribute("lastSessionTime", stats.get("lastSessionTime"));
            
            // Add chart data as both raw maps (for Thymeleaf inline JS) and JSON strings (backup)
            model.addAttribute("weeklyData", weeklyData != null ? weeklyData : new HashMap<>());
            model.addAttribute("monthlyData", monthlyData != null ? monthlyData : new HashMap<>());
            model.addAttribute("yearlyData", yearlyData != null ? yearlyData : new HashMap<>());
            model.addAttribute("contributionData", contributionData != null ? contributionData : new HashMap<>());
            
        } catch (Exception e) {
            e.printStackTrace();
            // Provide empty defaults if there's an error
            model.addAttribute("totalHours", 0.0);
            model.addAttribute("currentStreak", 0);
            model.addAttribute("maxStreak", 0);
            model.addAttribute("lastSessionTime", null);
            model.addAttribute("weeklyData", new HashMap<>());
            model.addAttribute("monthlyData", new HashMap<>());
            model.addAttribute("yearlyData", new HashMap<>());
            model.addAttribute("contributionData", new HashMap<>());
        }
        
        return "dashboard";
    }
}