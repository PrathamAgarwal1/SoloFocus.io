package com.example.SoloFocus.io.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_session_time")
    private LocalDateTime lastSessionTime;
    
    @Column(name = "current_streak")
    private Integer currentStreak = 0;
    
    @Column(name = "max_streak")
    private Integer maxStreak = 0;
    
    @Column(name = "total_focus_hours")
    private Double totalFocusHours = 0.0;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FocusSession> focusSessions = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public User() {
    }
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastSessionTime() {
        return lastSessionTime;
    }
    
    public void setLastSessionTime(LocalDateTime lastSessionTime) {
        this.lastSessionTime = lastSessionTime;
    }
    
    public Integer getCurrentStreak() {
        return currentStreak;
    }
    
    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }
    
    public Integer getMaxStreak() {
        return maxStreak != null ? maxStreak : 0;
    }
    
    public void setMaxStreak(Integer maxStreak) {
        this.maxStreak = maxStreak;
    }
    
    public Double getTotalFocusHours() {
        return totalFocusHours;
    }
    
    public void setTotalFocusHours(Double totalFocusHours) {
        this.totalFocusHours = totalFocusHours;
    }
    
    public List<FocusSession> getFocusSessions() {
        return focusSessions;
    }
    
    public void setFocusSessions(List<FocusSession> focusSessions) {
        this.focusSessions = focusSessions;
    }
}

