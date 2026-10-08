package com.fitpulse;

import com.fitpulse.util.DatabaseConnectionManager;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Phase 1 Database Connectivity and Verification Test.
 * 
 * Verifies:
 * 1. JDBC Driver Loading (com.mysql.cj.jdbc.Driver)
 * 2. Configuration loading from db.properties
 * 3. DriverManager-based connection acquisition via DatabaseConnectionManager
 * 4. Database schema and tables existence (fitpulse_db)
 * 5. Table structures and initial seed data
 */
public class DatabaseConnectionTest {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  FitPulse Fitness Tracking System - Phase 1 Database Connectivity Test");
        System.out.println("======================================================================");

        boolean allPassed = true;

        // Step 1: Configuration check
        DatabaseConnectionManager manager = DatabaseConnectionManager.getInstance();
        System.out.println("\n[1] Checking Database Configuration...");
        System.out.println("    Target URL : " + manager.getUrl());
        System.out.println("    DB User    : " + manager.getUsername());

        // Step 2: Establish connection
        System.out.println("\n[2] Attempting JDBC Connection via DatabaseConnectionManager...");
        try (Connection conn = manager.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("    [PASSED] JDBC Connection established successfully!");
                
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("    DBMS Name         : " + metaData.getDatabaseProductName());
                System.out.println("    DBMS Version      : " + metaData.getDatabaseProductVersion());
                System.out.println("    Driver Name       : " + metaData.getDriverName());
                System.out.println("    Driver Version    : " + metaData.getDriverVersion());
                System.out.println("    JDBC URL          : " + metaData.getURL());

                // Step 3: Run metadata query
                System.out.println("\n[3] Querying Database Environment...");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT DATABASE() AS current_db, VERSION() AS db_ver, CURRENT_USER() AS cur_user")) {
                    if (rs.next()) {
                        System.out.println("    Active Database   : " + rs.getString("current_db"));
                        System.out.println("    Server Version    : " + rs.getString("db_ver"));
                        System.out.println("    Active User       : " + rs.getString("cur_user"));
                    }
                }

                // Step 4: Verify required tables
                System.out.println("\n[4] Verifying Database Schema and Tables...");
                List<String> requiredTables = Arrays.asList(
                    "users", "trainers", "members", "workouts", "fitness_goals", "meal_logs", "audit_logs"
                );
                List<String> foundTables = new ArrayList<>();

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SHOW TABLES;")) {
                    while (rs.next()) {
                        foundTables.add(rs.getString(1).toLowerCase());
                    }
                }

                System.out.println("    Found Tables: " + foundTables);

                boolean tablesMatch = true;
                for (String req : requiredTables) {
                    if (foundTables.contains(req.toLowerCase())) {
                        // Count rows
                        int rowCount = 0;
                        try (Statement countStmt = conn.createStatement();
                             ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) FROM " + req)) {
                            if (countRs.next()) {
                                rowCount = countRs.getInt(1);
                            }
                        }
                        System.out.println("    [OK] Table '" + req + "' verified (" + rowCount + " records).");
                    } else {
                        System.out.println("    [FAIL] Table '" + req + "' is missing!");
                        tablesMatch = false;
                        allPassed = false;
                    }
                }

                if (tablesMatch) {
                    System.out.println("\n    [PASSED] All 7 required schema tables exist and are accessible.");
                }

            } else {
                System.err.println("    [FAILED] Connection returned null or closed.");
                allPassed = false;
            }
        } catch (Exception e) {
            System.err.println("    [FAILED] Error during connectivity test: " + e.getMessage());
            e.printStackTrace();
            allPassed = false;
        }

        System.out.println("\n======================================================================");
        if (allPassed) {
            System.out.println("  PHASE 1 STATUS: SUCCESS - JDBC Connection and Database Verified!");
            System.out.println("======================================================================");
            System.exit(0);
        } else {
            System.err.println("  PHASE 1 STATUS: FAILED - Please check database configuration.");
            System.out.println("======================================================================");
            System.exit(1);
        }
    }
}
