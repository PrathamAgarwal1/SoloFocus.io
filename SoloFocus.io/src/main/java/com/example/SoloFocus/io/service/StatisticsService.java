package com.example.SoloFocus.io.service;

import com.example.SoloFocus.io.model.FocusSession;
import com.example.SoloFocus.io.model.User;
import com.example.SoloFocus.io.repository.FocusSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class StatisticsService {
    
    @Autowired
    private FocusSessionRepository focusSessionRepository;
    
    @Autowired
    private UserService userService;
    
    public void updateUserStatistics(Long userId) {
        User user = userService.findById(userId);
        
        // Calculate total focus hours - only count completed sessions
        List<FocusSession> allSessions = focusSessionRepository.findByUserOrderByStartTimeDesc(user);
        double totalMinutes = 0.0;
        LocalDateTime lastSessionTime = null;
        
        for (FocusSession session : allSessions) {
            if (session.getSessionType().equals("pomodoro") && 
                session.getDurationMinutes() != null && 
                session.getDurationMinutes() > 0) {
                totalMinutes += session.getDurationMinutes();
                // Track most recent completed session
                if (lastSessionTime == null || session.getStartTime().isAfter(lastSessionTime)) {
                    lastSessionTime = session.getStartTime();
                }
            }
        }
        
        user.setTotalFocusHours(totalMinutes / 60.0);
        user.setLastSessionTime(lastSessionTime);
        
        // Calculate and update streak
        int streak = calculateStreak(user);
        user.setCurrentStreak(streak);
        
        // Update max streak if current streak is higher
        Integer currentMaxStreak = user.getMaxStreak();
        if (currentMaxStreak == null || streak > currentMaxStreak) {
            user.setMaxStreak(streak);
        }
        
        userService.save(user);
    }
    
    public int calculateStreak(User user) {
        List<FocusSession> sessions = focusSessionRepository.findByUserOrderByStartTimeDesc(user);
        
        if (sessions.isEmpty()) {
            return 0;
        }
        
        // Filter only pomodoro sessions with completed duration
        List<FocusSession> pomodoroSessions = sessions.stream()
                .filter(s -> s.getSessionType().equals("pomodoro") && 
                           s.getDurationMinutes() != null && 
                           s.getDurationMinutes() > 0)
                .collect(Collectors.toList());
        
        if (pomodoroSessions.isEmpty()) {
            return 0;
        }
        
        LocalDate today = LocalDate.now();
        
        // Get unique dates with at least one session, sorted newest first
        Set<LocalDate> sessionDatesSet = new HashSet<>();
        for (FocusSession session : pomodoroSessions) {
            sessionDatesSet.add(session.getStartTime().toLocalDate());
        }
        
        if (sessionDatesSet.isEmpty()) {
            return 0;
        }
        
        // Find the most recent session date
        LocalDate mostRecentDate = null;
        for (LocalDate date : sessionDatesSet) {
            if (mostRecentDate == null || date.isAfter(mostRecentDate)) {
                mostRecentDate = date;
            }
        }
        
        if (mostRecentDate == null) {
            return 0;
        }
        
        // Check if most recent session is today or yesterday
        // If it's more than 1 day ago, streak is broken
        long daysSinceLastSession = ChronoUnit.DAYS.between(mostRecentDate, today);
        if (daysSinceLastSession > 1) {
            return 0;
        }
        
        // Count consecutive days backwards from most recent date
        int streak = 0;
        LocalDate checkDate = mostRecentDate;
        
        // Count consecutive days going backwards
        while (sessionDatesSet.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }
        
        return streak;
    }
    
    public Map<String, Object> getDashboardStatistics(Long userId) {
        User user = userService.findById(userId);
        
        Map<String, Object> stats = new HashMap<>();
        
        // Get values with defaults
        Double totalHours = user.getTotalFocusHours();
        stats.put("totalHours", totalHours != null ? totalHours : 0.0);
        
        Integer streak = user.getCurrentStreak();
        stats.put("currentStreak", streak != null ? streak : 0);
        
        Integer maxStreak = user.getMaxStreak();
        stats.put("maxStreak", maxStreak != null ? maxStreak : 0);
        
        stats.put("lastSessionTime", user.getLastSessionTime());
        
        return stats;
    }
    
    public Map<String, Integer> getWeeklyStatistics(Long userId) {
        User user = userService.findById(userId);
        LocalDate today = LocalDate.now();
        LocalDateTime sevenDaysAgo = LocalDateTime.of(today.minusDays(6), java.time.LocalTime.MIN);
        
        // Get sessions from the last 7 days (including today)
        List<FocusSession> recentSessions = focusSessionRepository.findByUserAndStartTimeAfter(user, sevenDaysAgo);
        
        // Initialize all 7 days with 0 minutes
        Map<String, Integer> weeklyData = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            weeklyData.put(date.toString(), 0);
        }
        
        // Aggregate minutes by date
        for (FocusSession session : recentSessions) {
            if (session.getSessionType() != null && 
                session.getSessionType().equals("pomodoro") && 
                session.getDurationMinutes() != null && 
                session.getDurationMinutes() > 0) {
                LocalDate sessionDate = session.getStartTime().toLocalDate();
                String dateKey = sessionDate.toString();
                if (weeklyData.containsKey(dateKey)) {
                    weeklyData.put(dateKey, weeklyData.get(dateKey) + session.getDurationMinutes());
                }
            }
        }
        
        return weeklyData;
    }
    
    public Map<String, Integer> getMonthlyStatistics(Long userId) {
        User user = userService.findById(userId);
        LocalDate today = LocalDate.now();
        LocalDateTime thirtyDaysAgo = LocalDateTime.of(today.minusDays(29), java.time.LocalTime.MIN);
        
        // Get sessions from the last 30 days (including today)
        List<FocusSession> recentSessions = focusSessionRepository.findByUserAndStartTimeAfter(user, thirtyDaysAgo);
        
        // Initialize all 30 days with 0 minutes
        Map<String, Integer> monthlyData = new HashMap<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            monthlyData.put(date.toString(), 0);
        }
        
        // Aggregate minutes by date
        for (FocusSession session : recentSessions) {
            if (session.getSessionType() != null && 
                session.getSessionType().equals("pomodoro") && 
                session.getDurationMinutes() != null && 
                session.getDurationMinutes() > 0) {
                LocalDate sessionDate = session.getStartTime().toLocalDate();
                String dateKey = sessionDate.toString();
                if (monthlyData.containsKey(dateKey)) {
                    monthlyData.put(dateKey, monthlyData.get(dateKey) + session.getDurationMinutes());
                }
            }
        }
        
        return monthlyData;
    }
    
    public Map<String, Integer> getYearlyStatistics(Long userId) {
        User user = userService.findById(userId);
        LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(365);
        List<FocusSession> allSessions = focusSessionRepository.findByUserAndStartTimeAfter(user, oneYearAgo);
        
        // Initialize last 12 months with 0 minutes
        Map<String, Integer> yearlyData = new HashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            String monthKey = monthStart.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            yearlyData.put(monthKey, 0);
        }
        
        // Aggregate by month
        for (FocusSession session : allSessions) {
            if (session.getSessionType() != null && 
                session.getSessionType().equals("pomodoro") && 
                session.getDurationMinutes() != null && 
                session.getDurationMinutes() > 0) {
                LocalDate sessionDate = session.getStartTime().toLocalDate();
                String monthKey = sessionDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
                if (yearlyData.containsKey(monthKey)) {
                    yearlyData.put(monthKey, yearlyData.get(monthKey) + session.getDurationMinutes());
                }
            }
        }
        
        return yearlyData;
    }
    
    public Map<String, Integer> getContributionData(Long userId) {
        User user = userService.findById(userId);
        LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(365);
        List<FocusSession> allSessions = focusSessionRepository.findByUserAndStartTimeAfter(user, oneYearAgo);
        
        // Initialize all dates in the last year with 0
        Map<String, Integer> contributionData = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(364);
        
        for (int i = 0; i < 365; i++) {
            LocalDate date = startDate.plusDays(i);
            contributionData.put(date.toString(), 0);
        }
        
        // Fill in actual session minutes
        for (FocusSession session : allSessions) {
            if (session.getSessionType() != null && 
                session.getSessionType().equals("pomodoro") && 
                session.getDurationMinutes() != null && 
                session.getDurationMinutes() > 0) {
                String dateKey = session.getStartTime().toLocalDate().toString();
                if (contributionData.containsKey(dateKey)) {
                    contributionData.put(dateKey, contributionData.get(dateKey) + session.getDurationMinutes());
                }
            }
        }
        
        return contributionData;
    }
}