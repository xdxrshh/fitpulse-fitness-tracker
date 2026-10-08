package com.fitpulse;

import com.fitpulse.model.User;
import com.fitpulse.service.GoalTrackingService;
import com.fitpulse.service.NutritionService;
import com.fitpulse.service.UserService;
import com.fitpulse.service.WorkoutService;
import com.fitpulse.ui.MainFrame;
import com.fitpulse.ui.dialogs.LoginDialog;
import com.fitpulse.util.DatabaseConnectionManager;
import javax.swing.*;

/**
 * Main application launcher for FitPulse Fitness Tracking System.
 */
public class App {

    private static UserService userService;
    private static WorkoutService workoutService;
    private static GoalTrackingService goalService;
    private static NutritionService nutritionService;
    private static MainFrame mainFrame;

    public static void main(String[] args) {
        // Initialize Look and Feel
        setupModernLookAndFeel();

        // Initialize Services
        userService = new UserService();
        workoutService = new WorkoutService();
        goalService = new GoalTrackingService();
        nutritionService = new NutritionService();

        // Check DB connection on launch
        SwingUtilities.invokeLater(() -> {
            boolean dbOnline = DatabaseConnectionManager.getInstance().testConnection();
            if (!dbOnline) {
                System.out.println("[INFO] MySQL connection test failed or MySQL service not yet started.");
                System.out.println("[INFO] URL: " + DatabaseConnectionManager.getInstance().getUrl());
                System.out.println("[INFO] FitPulse GUI will still open. Please start MySQL and import sql/fitpulse_schema.sql.");
            }
            showAuthentication();
        });
    }

    private static void setupModernLookAndFeel() {
        try {
            // Attempt FlatLaf Dark theme
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception e) {
            try {
                // Fallback to Nimbus
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private static void showAuthentication() {
        if (mainFrame != null) {
            mainFrame.dispose();
            mainFrame = null;
        }

        LoginDialog loginDialog = new LoginDialog(null, userService);
        loginDialog.setVisible(true);

        User user = loginDialog.getAuthenticatedUser();
        if (user != null) {
            mainFrame = new MainFrame(user, workoutService, goalService, nutritionService, App::showAuthentication);
            mainFrame.setVisible(true);
        } else {
            System.exit(0);
        }
    }
}
