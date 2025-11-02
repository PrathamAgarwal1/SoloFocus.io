package com.example.SoloFocus.io.repository;

import com.example.SoloFocus.io.model.FocusSession;
import com.example.SoloFocus.io.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    
    List<FocusSession> findByUserOrderByStartTimeDesc(User user);
    
    List<FocusSession> findByUserAndStartTimeBetween(User user, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT f FROM FocusSession f WHERE f.user = :user AND f.startTime >= :date ORDER BY f.startTime DESC")
    List<FocusSession> findByUserAndStartTimeAfter(@Param("user") User user, @Param("date") LocalDateTime date);
    
    @Query("SELECT f FROM FocusSession f WHERE f.user = :user AND DATE(f.startTime) = DATE(:date)")
    List<FocusSession> findByUserAndDate(@Param("user") User user, @Param("date") LocalDateTime date);
    
    @Query("SELECT SUM(f.durationMinutes) FROM FocusSession f WHERE f.user = :user AND f.sessionType = 'pomodoro'")
    Integer getTotalFocusMinutes(@Param("user") User user);
}

