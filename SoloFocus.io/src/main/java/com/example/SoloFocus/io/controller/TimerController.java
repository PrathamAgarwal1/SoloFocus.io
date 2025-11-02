package com.example.SoloFocus.io.controller;

import com.example.SoloFocus.io.model.FocusSession;
import com.example.SoloFocus.io.service.FocusSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/timer")
public class TimerController {
    
    @Autowired
    private FocusSessionService focusSessionService;
    
    @GetMapping
    public String showTimerPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        return "timer";
    }
    
    @PostMapping("/start")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startSession(
            @RequestParam String sessionType,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(error);
        }
        
        FocusSession focusSession = focusSessionService.startSession(userId, sessionType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", focusSession.getId());
        response.put("startTime", focusSession.getStartTime().toString());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/end")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> endSession(
            @RequestParam Long sessionId,
            @RequestParam Integer durationMinutes,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(error);
        }
        
        FocusSession focusSession = focusSessionService.endSession(sessionId, durationMinutes);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("sessionId", focusSession.getId());
        response.put("durationMinutes", focusSession.getDurationMinutes());
        
        return ResponseEntity.ok(response);
    }
}

