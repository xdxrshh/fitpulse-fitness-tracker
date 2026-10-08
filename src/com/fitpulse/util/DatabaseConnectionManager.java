package com.fitpulse.util;

import com.fitpulse.exception.DatabaseOperationException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Singleton database connection manager.
 * Attempts MySQL first; if unavailable, automatically falls back to
 * an embedded H2 database (MySQL compatibility mode) so the app runs
 * without any external database installation.
 *
 * Demonstrates:
 * - Rubric Item 5: JDBC Connectivity
 * - Rubric Item 6: Transaction Management support
 */
public class DatabaseConnectionManager {

    private static volatile DatabaseConnectionManager instance;

    // MySQL settings
    private String mysqlUrl;
    private String mysqlUsername;
    private String mysqlPassword;

    // Active connection settings (mysql or h2 fallback)
    private String url;
    private String username;
    private String password;
    private String driverClass;

    private boolean isDriverLoaded = false;
    private boolean usingH2 = false;

    private DatabaseConnectionManager() {
        loadConfiguration();
        resolveDriver();
    }

    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionManager();
                }
            }
        }
        return instance;
    }

    private void loadConfiguration() {
        // MySQL defaults
        this.mysqlUrl      = "jdbc:mysql://localhost:3306/fitpulse_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true";
        this.mysqlUsername = "root";
        this.mysqlPassword = "";

        // Attempt to load overrides from db.properties
        InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties");
        if (in == null) in = getClass().getResourceAsStream("/resources/db.properties");
        if (in == null) in = getClass().getClassLoader().getResourceAsStream("resources/db.properties");
        if (in == null) {
            java.io.File file = new java.io.File("src/resources/db.properties");
            if (!file.exists()) file = new java.io.File("resources/db.properties");
            if (file.exists()) {
                try { in = new java.io.FileInputStream(file); } catch (Exception ignored) {}
            }
        }
        if (in != null) {
            try (InputStream autoClose = in) {
                Properties props = new Properties();
                props.load(autoClose);
                this.mysqlUrl      = props.getProperty("db.url",      this.mysqlUrl);
                this.mysqlUsername = props.getProperty("db.user",     this.mysqlUsername);
                this.mysqlPassword = props.getProperty("db.password", this.mysqlPassword);
            } catch (Exception e) {
                System.err.println("[WARN] Failed to parse db.properties: " + e.getMessage());
            }
        }
    }

    /**
     * Try MySQL first. If driver is missing or server unreachable, switch to H2.
     */
    private void resolveDriver() {
        // 1. Try MySQL driver
        boolean mysqlDriverAvailable = false;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            mysqlDriverAvailable = true;
        } catch (ClassNotFoundException ignored) {}

        if (mysqlDriverAvailable && canConnectMySQL()) {
            this.url         = this.mysqlUrl;
            this.username    = this.mysqlUsername;
            this.password    = this.mysqlPassword;
            this.driverClass = "com.mysql.cj.jdbc.Driver";
            this.usingH2     = false;
            this.isDriverLoaded = true;
            System.out.println("[INFO] Connected to MySQL at " + this.url);
            return;
        }

        // 2. Fall back to H2 embedded
        try {
            Class.forName("org.h2.Driver");
            String dbPath = System.getProperty("user.home").replace("\\", "/") + "/.fitpulse/fitpulse_db";
            this.url         = "jdbc:h2:" + dbPath + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=VALUE";
            this.username    = "sa";
            this.password    = "";
            this.driverClass = "org.h2.Driver";
            this.usingH2     = true;
            this.isDriverLoaded = true;
            System.out.println("[INFO] MySQL unavailable. Using embedded H2 database at: " + dbPath);
            initializeH2Schema();
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] Neither MySQL nor H2 driver found on classpath.");
            this.isDriverLoaded = false;
        }
    }

    private boolean canConnectMySQL() {
        try (Connection c = DriverManager.getConnection(mysqlUrl, mysqlUsername, mysqlPassword)) {
            return c != null && !c.isClosed();
        } catch (Exception e) {
            System.out.println("[INFO] MySQL not reachable: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates tables and seed data in H2 if they don't already exist.
     */
    private void initializeH2Schema() {
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            // Users
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  name VARCHAR(100) NOT NULL," +
                "  email VARCHAR(120) NOT NULL UNIQUE," +
                "  password_hash VARCHAR(256) NOT NULL," +
                "  role VARCHAR(20) NOT NULL DEFAULT 'MEMBER'," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            // Trainers
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS trainers (" +
                "  user_id INT PRIMARY KEY," +
                "  specialization VARCHAR(100) NOT NULL," +
                "  certification VARCHAR(100) NOT NULL," +
                "  CONSTRAINT fk_trainers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")"
            );
            // Members
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS members (" +
                "  user_id INT PRIMARY KEY," +
                "  weight_kg DECIMAL(5,2) NOT NULL," +
                "  height_cm DECIMAL(5,2) NOT NULL," +
                "  fitness_level VARCHAR(30) DEFAULT 'BEGINNER'," +
                "  trainer_id INT NULL," +
                "  CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "  CONSTRAINT fk_members_trainer FOREIGN KEY (trainer_id) REFERENCES trainers(user_id) ON DELETE SET NULL" +
                ")"
            );
            // Workouts
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS workouts (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  workout_type VARCHAR(20) NOT NULL," +
                "  workout_date DATETIME NOT NULL," +
                "  duration_min INT NOT NULL," +
                "  calories_burned DOUBLE NOT NULL," +
                "  distance_km DOUBLE NULL," +
                "  avg_heart_rate INT NULL," +
                "  total_weight_kg DOUBLE NULL," +
                "  sets_count INT NULL," +
                "  reps_count INT NULL," +
                "  notes VARCHAR(255) NULL," +
                "  CONSTRAINT fk_workouts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")"
            );
            // Fitness Goals
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS fitness_goals (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  goal_type VARCHAR(40) NOT NULL," +
                "  target_value DOUBLE NOT NULL," +
                "  current_value DOUBLE DEFAULT 0.0," +
                "  start_date DATE NOT NULL," +
                "  end_date DATE NOT NULL," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS'," +
                "  CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")"
            );
            // Meal Logs
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS meal_logs (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  meal_name VARCHAR(100) NOT NULL," +
                "  meal_type VARCHAR(20) NOT NULL," +
                "  calories INT NOT NULL," +
                "  protein_g DOUBLE DEFAULT 0.0," +
                "  carbs_g DOUBLE DEFAULT 0.0," +
                "  fat_g DOUBLE DEFAULT 0.0," +
                "  logged_at DATETIME NOT NULL," +
                "  CONSTRAINT fk_meals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")"
            );
            // Audit Logs
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS audit_logs (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NULL," +
                "  action VARCHAR(100) NOT NULL," +
                "  details TEXT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Seed data — only if users table is empty
            java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            int userCount = rs.getInt(1);
            rs.close();

            if (userCount == 0) {
                System.out.println("[INFO] Seeding H2 database with demo data...");
                // password123 -> ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
                stmt.execute(
                    "INSERT INTO users (id, name, email, password_hash, role) VALUES " +
                    "(1, 'Alex Mercer',  'alex@fitpulse.com',  'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'TRAINER')," +
                    "(2, 'Sarah Connor', 'sarah@fitpulse.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'MEMBER')," +
                    "(3, 'John Doe',     'john@fitpulse.com',  'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'MEMBER')"
                );
                stmt.execute(
                    "INSERT INTO trainers (user_id, specialization, certification) VALUES " +
                    "(1, 'High Intensity Interval Training & Strength', 'NASM-CPT #88291')"
                );
                stmt.execute(
                    "INSERT INTO members (user_id, weight_kg, height_cm, fitness_level, trainer_id) VALUES " +
                    "(2, 64.5, 168.0, 'INTERMEDIATE', 1)," +
                    "(3, 78.0, 180.5, 'BEGINNER',     1)"
                );
                stmt.execute(
                    "INSERT INTO workouts (user_id, workout_type, workout_date, duration_min, calories_burned, distance_km, avg_heart_rate, total_weight_kg, sets_count, reps_count, notes) VALUES " +
                    "(2,'CARDIO',   DATEADD('DAY',-3,NOW()), 45, 410.0, 6.2, 148, NULL,   NULL, NULL, 'Morning lakeside run')," +
                    "(2,'STRENGTH', DATEADD('DAY',-2,NOW()), 60, 380.0, NULL,NULL,3200.0, 16,   10,   'Full body compound lifts')," +
                    "(2,'CARDIO',   DATEADD('DAY',-1,NOW()), 30, 290.0, 4.5, 142, NULL,   NULL, NULL, 'Treadmill interval sprints')," +
                    "(3,'CARDIO',   DATEADD('DAY',-2,NOW()), 25, 210.0, 3.0, 135, NULL,   NULL, NULL, 'Warm-up jog')," +
                    "(3,'STRENGTH', DATEADD('DAY',-1,NOW()), 50, 320.0, NULL,NULL,2400.0, 12,   12,   'Leg day & core')"
                );
                stmt.execute(
                    "INSERT INTO fitness_goals (user_id, goal_type, target_value, current_value, start_date, end_date, status) VALUES " +
                    "(2,'CALORIE_BURN',  2500.0, 1080.0, CURDATE(), DATEADD('DAY', 7, CURDATE()), 'IN_PROGRESS')," +
                    "(2,'WORKOUT_COUNT', 5.0,    3.0,    CURDATE(), DATEADD('DAY', 7, CURDATE()), 'IN_PROGRESS')," +
                    "(3,'CALORIE_BURN',  2000.0, 530.0,  CURDATE(), DATEADD('DAY', 7, CURDATE()), 'IN_PROGRESS')"
                );
                stmt.execute(
                    "INSERT INTO meal_logs (user_id, meal_name, meal_type, calories, protein_g, carbs_g, fat_g, logged_at) VALUES " +
                    "(2,'Oatmeal & Whey Protein Shake',   'BREAKFAST', 420, 34.0, 48.0, 8.0,  DATEADD('HOUR',-4,NOW()))," +
                    "(2,'Grilled Chicken Quinoa Bowl',    'LUNCH',     650, 48.0, 62.0, 16.0, DATEADD('HOUR',-1,NOW()))," +
                    "(3,'Avocado Toast with Poached Eggs','BREAKFAST', 480, 22.0, 38.0, 24.0, DATEADD('HOUR',-3,NOW()))"
                );
                System.out.println("[INFO] H2 seed data inserted successfully.");
            } else {
                System.out.println("[INFO] H2 database already has data, skipping seed.");
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] H2 schema initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtains a live connection to the active database (MySQL or H2).
     * @return Connection object
     * @throws DatabaseOperationException if connection fails
     */
    public Connection getConnection() throws DatabaseOperationException {
        if (!isDriverLoaded) {
            resolveDriver();
        }
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Unable to connect to database (" + url + "): " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a database connection can be established.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isUsingH2() { return usingH2; }
    public String  getUrl()    { return url; }
    public void    setUrl(String url) { this.url = url; }
    public String  getUsername() { return username; }
    public void    setUsername(String username) { this.username = username; }
    public void    setPassword(String password) { this.password = password; }
}
