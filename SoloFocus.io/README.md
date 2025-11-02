# SoloFocus.io - Pomodoro Timer Application

A full-stack Pomodoro timer web application built with Spring Boot, MySQL, HTML, CSS, and JavaScript. Track your focus sessions, maintain daily streaks, and visualize your productivity with interactive charts.

## Features

- **User Authentication**: Secure login and registration system
- **Customizable Pomodoro Timer**: Adjustable focus, short break, and long break durations
- **Session Tracking**: Automatic recording of focus sessions with start/end times
- **Statistics Dashboard**: View total focus hours, daily streaks, and performance charts
- **Responsive Design**: Beautiful, modern UI that works on all devices
- **Performance Charts**: Visualize your focus patterns using Chart.js

## Technology Stack

- **Backend**: Spring Boot 3.5.7
- **Database**: MySQL
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Templating**: Thymeleaf
- **Data Visualization**: Chart.js

## Prerequisites

Before running the application, ensure you have the following installed:

1. **Java Development Kit (JDK) 17 or higher**
   - Download from: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
   - Verify installation: `java -version`

2. **MySQL Database Server 8.0 or higher**
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Create a MySQL user account with appropriate permissions

3. **Maven 3.6+** (included via Maven Wrapper)
   - The project includes `mvnw` (Maven Wrapper) for Unix/Mac and `mvnw.cmd` for Windows

## Database Setup

📖 **For detailed MySQL setup instructions, see [MYSQL_SETUP.md](MYSQL_SETUP.md)**

### Quick Setup Steps:

1. **Install MySQL** (if not already installed)
   - **Windows**: Download from https://dev.mysql.com/downloads/installer/
   - **macOS**: `brew install mysql`
   - **Linux**: `sudo apt install mysql-server`

2. **Start MySQL Server**
   - **Windows**: `net start MySQL80` or use XAMPP
   - **macOS**: `brew services start mysql`
   - **Linux**: `sudo systemctl start mysql`

3. **Update Database Credentials**
   - Open `src/main/resources/application.properties`
   - **Find these lines:**
     ```properties
     spring.datasource.username=root
     spring.datasource.password=root
     ```
   - **Replace with YOUR MySQL credentials:**
     ```properties
     spring.datasource.username=root
     spring.datasource.password=YOUR_MYSQL_PASSWORD
     ```
   - **Note:** If using XAMPP with no password, leave it empty: `spring.datasource.password=`

4. **Test Connection** (Optional)
   ```bash
   mysql -u root -p
   # Enter your password
   ```

The application will **automatically create** the database `solofocus_db` on first run (no manual creation needed).

**Need help?** Check [MYSQL_SETUP.md](MYSQL_SETUP.md) for detailed instructions and troubleshooting.

## Project Structure

```
SoloFocus.io/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/SoloFocus/io/
│   │   │       ├── Application.java          # Main Spring Boot application
│   │   │       ├── controller/               # REST and MVC controllers
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── TimerController.java
│   │   │       │   ├── DashboardController.java
│   │   │       │   └── HomeController.java
│   │   │       ├── model/                    # JPA entities
│   │   │       │   ├── User.java
│   │   │       │   └── FocusSession.java
│   │   │       ├── repository/               # Data access layer
│   │   │       │   ├── UserRepository.java
│   │   │       │   └── FocusSessionRepository.java
│   │   │       ├── service/                  # Business logic
│   │   │       │   ├── UserService.java
│   │   │       │   ├── FocusSessionService.java
│   │   │       │   └── StatisticsService.java
│   │   │       └── dto/                      # Data transfer objects
│   │   │           ├── UserLoginDTO.java
│   │   │           └── UserRegistrationDTO.java
│   │   └── resources/
│   │       ├── application.properties        # Configuration
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── style.css            # Main stylesheet
│   │       │   └── js/
│   │       │       └── timer.js             # Timer functionality
│   │       └── templates/                   # Thymeleaf templates
│   │           ├── login.html
│   │           ├── register.html
│   │           ├── timer.html
│   │           └── dashboard.html
│   └── test/                                # Test files
├── pom.xml                                   # Maven dependencies
└── README.md
```

## Building and Running the Application

### Option 1: Using Maven Wrapper (Recommended)

**Windows:**
```bash
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

### Option 2: Using Maven (if installed)

```bash
mvn spring-boot:run
```

### Option 3: Build JAR and Run

**Build:**
```bash
mvn clean package
```

**Run:**
```bash
java -jar target/SoloFocus.io-0.0.1-SNAPSHOT.jar
```

## Accessing the Application

Once the application starts successfully, open your web browser and navigate to:

- **Home/Login**: http://localhost:8080/
- **Login Page**: http://localhost:8080/auth/login
- **Register Page**: http://localhost:8080/auth/register
- **Timer**: http://localhost:8080/timer (requires login)
- **Dashboard**: http://localhost:8080/dashboard (requires login)

## Usage Instructions

### 1. Register a New Account
- Click "Register here" on the login page or navigate to `/auth/register`
- Fill in username, email, and password
- Confirm your password
- Click "Register"
- After successful registration, you'll be redirected to the login page

### 2. Login
- Enter your username and password
- Click "Login"
- You'll be redirected to the Pomodoro timer page

### 3. Using the Pomodoro Timer
- **Adjust Settings**: Modify Pomodoro, Short Break, and Long Break durations (in minutes)
- **Select Mode**: Choose between Pomodoro, Short Break, or Long Break
- **Start Timer**: Click "Start" to begin your focus session
- **Pause**: Click "Pause" to temporarily stop the timer
- **Reset**: Click "Reset" to restart the current timer
- **Auto-tracking**: Pomodoro sessions are automatically saved when completed

### 4. View Statistics
- Navigate to the Dashboard from the navigation menu
- View your:
  - **Total Focus Hours**: Cumulative time focused
  - **Current Streak**: Consecutive days with focus sessions
  - **Last Session Time**: When you last completed a session
  - **Weekly Performance Chart**: Bar chart showing focus minutes per day for the last 7 days

## Database Tables

The application automatically creates the following tables:

### `users`
- `id` (Primary Key)
- `username` (Unique)
- `email` (Unique)
- `password`
- `created_at`
- `last_session_time`
- `current_streak`
- `total_focus_hours`

### `focus_sessions`
- `id` (Primary Key)
- `user_id` (Foreign Key to users)
- `start_time`
- `end_time`
- `duration_minutes`
- `session_type` (pomodoro, short_break, long_break)
- `created_at`

## Key Features Implementation

### Streak Calculation
- Calculates consecutive days with completed Pomodoro sessions
- Resets if more than one day passes without a session
- Updates automatically when sessions are completed

### Total Focus Hours
- Accumulates all completed Pomodoro sessions (excludes breaks)
- Stored in hours with decimal precision
- Updates in real-time after each session

### Performance Charts
- Weekly bar chart using Chart.js
- Shows focus minutes per day for the last 7 days
- Responsive and interactive visualization

## Troubleshooting

### Issue: Application won't start
- **Check MySQL**: Ensure MySQL server is running
- **Verify Credentials**: Double-check database username/password in `application.properties`
- **Check Port**: Ensure port 8080 is not in use by another application

### Issue: Database connection error
- Verify MySQL is running: `mysql --version`
- Check connection string in `application.properties`
- Ensure database exists or `createDatabaseIfNotExist=true` is set
- Verify user has CREATE DATABASE permissions if auto-creating

### Issue: Static files not loading
- Clear browser cache
- Ensure files are in `src/main/resources/static/`
- Check browser console for 404 errors

### Issue: Session not saving
- Check browser console for JavaScript errors
- Verify you're logged in (check session)
- Check server logs for backend errors

## Development Notes

### Password Security
⚠️ **Important**: The current implementation uses plain text password comparison. For production use:
- Implement BCrypt password hashing
- Add password encryption
- Use Spring Security for authentication

### Session Management
- Uses HttpSession for user sessions
- Session expires on browser close (default behavior)
- Consider implementing persistent sessions for production

### Error Handling
- Basic error handling is implemented
- Production should include more comprehensive error handling and logging

## Future Enhancements

- [ ] Password encryption with BCrypt
- [ ] Email verification for registration
- [ ] Password reset functionality
- [ ] Export statistics to CSV/PDF
- [ ] Daily/weekly/monthly statistics
- [ ] Sound notifications customization
- [ ] Themes and customization options
- [ ] Social features (sharing achievements)
- [ ] Mobile app integration

## License

This project is open source and available for educational purposes.

## Support

For issues, questions, or contributions, please check the project repository or create an issue.

---

**Happy Focusing! 🍅**

