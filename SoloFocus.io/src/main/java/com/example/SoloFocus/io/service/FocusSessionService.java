package com.example.SoloFocus.io.service;

import com.example.SoloFocus.io.model.FocusSession;
import com.example.SoloFocus.io.model.User;
import com.example.SoloFocus.io.repository.FocusSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FocusSessionService {
    
    @Autowired
    private FocusSessionRepository focusSessionRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StatisticsService statisticsService;
    
    public FocusSession startSession(Long userId, String sessionType) {
        User user = userService.findById(userId);
        FocusSession session = new FocusSession(user, LocalDateTime.now(), sessionType);
        return focusSessionRepository.save(session);
    }
    
    public FocusSession endSession(Long sessionId, Integer durationMinutes) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        session.setEndTime(LocalDateTime.now());
        session.setDurationMinutes(durationMinutes);
        
        FocusSession savedSession = focusSessionRepository.save(session);
        
        // Update user statistics
        statisticsService.updateUserStatistics(savedSession.getUser().getId());
        
        return savedSession;
    }
    
    public List<FocusSession> getUserSessions(Long userId) {
        User user = userService.findById(userId);
        return focusSessionRepository.findByUserOrderByStartTimeDesc(user);
    }
    
    public List<FocusSession> getSessionsByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        User user = userService.findById(userId);
        return focusSessionRepository.findByUserAndStartTimeBetween(user, start, end);
    }
    
    public FocusSession getSessionById(Long sessionId) {
        return focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }
}

