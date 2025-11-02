// Pomodoro Timer JavaScript - Pomofocus Style

class PomodoroTimer {
    constructor() {
        this.currentMode = 'pomodoro';
        this.timeLeft = 25 * 60; // in seconds
        this.isRunning = false;
        this.intervalId = null;
        this.currentSessionId = null;
        this.isEditing = false;
        
        this.pomodoroMinutes = 25;
        this.shortBreakMinutes = 5;
        this.longBreakMinutes = 15;
        
        this.totalSeconds = this.pomodoroMinutes * 60;
        this.initialSeconds = this.totalSeconds;
        
        this.initializeElements();
        this.attachEventListeners();
        this.updateDisplay();
    }
    
    initializeElements() {
        this.minutesDisplay = document.getElementById('minutes');
        this.secondsDisplay = document.getElementById('seconds');
        this.timerTimeDisplay = document.getElementById('timerTimeDisplay');
        this.timerTimeEdit = document.getElementById('timerTimeEdit');
        this.startBtn = document.getElementById('startBtn');
        this.pauseBtn = document.getElementById('pauseBtn');
        this.resetBtn = document.getElementById('resetBtn');
        this.progressCircle = document.getElementById('progressCircle');
        this.sessionStatus = document.getElementById('sessionStatus');
        
        this.pomodoroInput = document.getElementById('pomodoroMinutes');
        this.shortBreakInput = document.getElementById('shortBreakMinutes');
        this.longBreakInput = document.getElementById('longBreakMinutes');
        
        this.modeButtons = document.querySelectorAll('.mode-btn');
        this.settingsBtn = document.getElementById('settingsBtn');
        this.settingsPanel = document.getElementById('settingsPanel');
        this.settingsClose = document.getElementById('settingsClose');
    }
    
    attachEventListeners() {
        this.startBtn.addEventListener('click', () => this.start());
        this.pauseBtn.addEventListener('click', () => this.pause());
        this.resetBtn.addEventListener('click', () => this.reset());
        
        this.modeButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.switchMode(e.target.dataset.mode);
            });
        });
        
        // Click on timer to edit (when not running)
        this.timerTimeDisplay.addEventListener('click', () => {
            if (!this.isRunning && !this.isEditing) {
                this.startEditing();
            }
        });
        
        // Handle timer edit input
        this.timerTimeEdit.addEventListener('blur', () => this.finishEditing());
        this.timerTimeEdit.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                this.finishEditing();
            } else if (e.key === 'Escape') {
                e.preventDefault();
                this.cancelEditing();
            }
        });
        
        // Format input as user types (mm:ss)
        this.timerTimeEdit.addEventListener('input', (e) => {
            let value = e.target.value.replace(/\D/g, ''); // Remove non-digits
            if (value.length > 4) value = value.slice(0, 4);
            
            if (value.length >= 2) {
                value = value.slice(0, 2) + ':' + value.slice(2);
            }
            e.target.value = value;
        });
        
        // Settings panel
        this.settingsBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            this.settingsPanel.style.display = this.settingsPanel.style.display === 'none' ? 'block' : 'none';
        });
        
        this.settingsClose.addEventListener('click', () => {
            this.settingsPanel.style.display = 'none';
        });
        
        // Close settings panel when clicking outside
        document.addEventListener('click', (e) => {
            if (this.settingsPanel.style.display === 'block' &&
                !this.settingsPanel.contains(e.target) &&
                !this.settingsBtn.contains(e.target)) {
                this.settingsPanel.style.display = 'none';
            }
        });
        
        // Settings inputs
        this.pomodoroInput.addEventListener('change', () => {
            this.pomodoroMinutes = parseInt(this.pomodoroInput.value) || 25;
            if (this.currentMode === 'pomodoro' && !this.isRunning) {
                this.timeLeft = this.pomodoroMinutes * 60;
                this.totalSeconds = this.timeLeft;
                this.initialSeconds = this.totalSeconds;
                this.updateDisplay();
            }
        });
        
        this.shortBreakInput.addEventListener('change', () => {
            this.shortBreakMinutes = parseInt(this.shortBreakInput.value) || 5;
            if (this.currentMode === 'short_break' && !this.isRunning) {
                this.timeLeft = this.shortBreakMinutes * 60;
                this.totalSeconds = this.timeLeft;
                this.initialSeconds = this.totalSeconds;
                this.updateDisplay();
            }
        });
        
        this.longBreakInput.addEventListener('change', () => {
            this.longBreakMinutes = parseInt(this.longBreakInput.value) || 15;
            if (this.currentMode === 'long_break' && !this.isRunning) {
                this.timeLeft = this.longBreakMinutes * 60;
                this.totalSeconds = this.timeLeft;
                this.initialSeconds = this.totalSeconds;
                this.updateDisplay();
            }
        });
    }
    
    startEditing() {
        if (this.isRunning) return;
        
        this.isEditing = true;
        const minutes = Math.floor(this.timeLeft / 60);
        const seconds = this.timeLeft % 60;
        this.timerTimeEdit.value = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        this.timerTimeDisplay.style.display = 'none';
        this.timerTimeEdit.style.display = 'block';
        this.timerTimeEdit.focus();
        this.timerTimeEdit.select();
    }
    
    finishEditing() {
        if (!this.isEditing) return;
        
        const value = this.timerTimeEdit.value.trim();
        
        // Try to parse different formats: "mm:ss", "mmss", "m:ss"
        let match = value.match(/^(\d{1,2}):(\d{2})$/);
        if (!match) {
            match = value.match(/^(\d{3,4})$/); // mmss format
            if (match) {
                const digits = match[1];
                if (digits.length === 3) {
                    // mss format
                    match = [null, digits[0], digits.slice(1)];
                } else {
                    // mmss format
                    match = [null, digits.slice(0, 2), digits.slice(2)];
                }
            }
        }
        
        if (match) {
            let minutes = parseInt(match[1]) || 0;
            let seconds = parseInt(match[2]) || 0;
            
            // Validate and clamp values
            if (minutes < 0) minutes = 0;
            if (minutes > 99) minutes = 99;
            if (seconds < 0) seconds = 0;
            if (seconds > 59) seconds = 59;
            
            this.timeLeft = minutes * 60 + seconds;
            // Ensure minimum 1 second
            if (this.timeLeft === 0) this.timeLeft = 60;
            
            this.totalSeconds = this.timeLeft;
            this.initialSeconds = this.totalSeconds;
            this.updateDisplay();
        }
        
        this.cancelEditing();
    }
    
    cancelEditing() {
        this.isEditing = false;
        this.timerTimeDisplay.style.display = 'block';
        this.timerTimeEdit.style.display = 'none';
    }
    
    switchMode(mode) {
        if (this.isRunning) {
            this.pause();
        }
        
        if (this.isEditing) {
            this.cancelEditing();
        }
        
        this.currentMode = mode;
        
        // Update active button
        this.modeButtons.forEach(btn => {
            if (btn.dataset.mode === mode) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });
        
        // Set time based on mode
        switch(mode) {
            case 'pomodoro':
                this.timeLeft = this.pomodoroMinutes * 60;
                break;
            case 'short_break':
                this.timeLeft = this.shortBreakMinutes * 60;
                break;
            case 'long_break':
                this.timeLeft = this.longBreakMinutes * 60;
                break;
        }
        
        this.totalSeconds = this.timeLeft;
        this.initialSeconds = this.totalSeconds;
        this.updateDisplay();
    }
    
    async start() {
        if (this.isRunning) return;
        
        if (this.isEditing) {
            this.finishEditing();
        }
        
        this.isRunning = true;
        this.startBtn.style.display = 'none';
        this.pauseBtn.style.display = 'inline-block';
        this.resetBtn.style.display = 'none';
        
        // Start session on server (only for pomodoro)
        if (this.currentMode === 'pomodoro' && !this.currentSessionId) {
            try {
                const response = await fetch('/timer/start', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: `sessionType=${this.currentMode}`
                });
                
                if (response.ok) {
                    const data = await response.json();
                    this.currentSessionId = data.sessionId;
                    this.showStatus('Session started', 'success');
                }
            } catch (error) {
                console.error('Error starting session:', error);
            }
        }
        
        this.intervalId = setInterval(() => {
            this.timeLeft--;
            this.updateDisplay();
            
            if (this.timeLeft <= 0) {
                this.complete();
            }
        }, 1000);
    }
    
    pause() {
        if (!this.isRunning) return;
        
        this.isRunning = false;
        this.startBtn.style.display = 'inline-block';
        this.pauseBtn.style.display = 'none';
        this.resetBtn.style.display = 'inline-block';
        
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
        }
    }
    
    reset() {
        this.pause();
        
        if (this.isEditing) {
            this.cancelEditing();
        }
        
        switch(this.currentMode) {
            case 'pomodoro':
                this.timeLeft = this.pomodoroMinutes * 60;
                break;
            case 'short_break':
                this.timeLeft = this.shortBreakMinutes * 60;
                break;
            case 'long_break':
                this.timeLeft = this.longBreakMinutes * 60;
                break;
        }
        
        this.totalSeconds = this.timeLeft;
        this.initialSeconds = this.totalSeconds;
        this.currentSessionId = null;
        this.startBtn.style.display = 'inline-block';
        this.pauseBtn.style.display = 'none';
        this.resetBtn.style.display = 'none';
        this.updateDisplay();
        this.sessionStatus.textContent = '';
        this.sessionStatus.className = 'session-status';
    }
    
    async complete() {
        this.pause();
        
        // Play notification sound
        this.playNotification();
        
        // End session on server (only for pomodoro)
        if (this.currentMode === 'pomodoro' && this.currentSessionId) {
            try {
                const durationMinutes = this.initialSeconds / 60;
                const response = await fetch('/timer/end', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: `sessionId=${this.currentSessionId}&durationMinutes=${durationMinutes}`
                });
                
                if (response.ok) {
                    const data = await response.json();
                    this.currentSessionId = null;
                    this.showStatus(`Session completed! ${durationMinutes} minutes focused.`, 'info');
                    
                    // Reload page after 3 seconds to update stats
                    setTimeout(() => {
                        window.location.href = '/dashboard';
                    }, 3000);
                }
            } catch (error) {
                console.error('Error ending session:', error);
            }
        } else {
            this.showStatus('Break completed!', 'success');
        }
        
        // Reset timer
        this.reset();
    }
    
    showStatus(message, type) {
        this.sessionStatus.textContent = message;
        this.sessionStatus.className = `session-status session-status-${type}`;
        setTimeout(() => {
            this.sessionStatus.textContent = '';
            this.sessionStatus.className = 'session-status';
        }, 3000);
    }
    
    updateDisplay() {
        const minutes = Math.floor(this.timeLeft / 60);
        const seconds = this.timeLeft % 60;
        
        if (!this.isEditing) {
            this.minutesDisplay.textContent = minutes.toString().padStart(2, '0');
            this.secondsDisplay.textContent = seconds.toString().padStart(2, '0');
        }
        
        // Update progress circle
        const circumference = 2 * Math.PI * 45; // radius = 45
        const progress = (this.initialSeconds - this.timeLeft) / this.initialSeconds;
        const offset = circumference * (1 - progress);
        
        this.progressCircle.style.strokeDashoffset = offset;
        this.progressCircle.style.strokeDasharray = circumference;
    }
    
    playNotification() {
        try {
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();
            
            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);
            
            oscillator.frequency.value = 800;
            oscillator.type = 'sine';
            
            gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);
            
            oscillator.start(audioContext.currentTime);
            oscillator.stop(audioContext.currentTime + 0.5);
        } catch (error) {
            console.log('Audio notification not available');
        }
    }
}

// Initialize timer when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new PomodoroTimer();
});
