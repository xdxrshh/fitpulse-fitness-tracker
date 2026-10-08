package com.fitpulse.ui;

import com.fitpulse.model.User;
import com.fitpulse.service.GoalTrackingService;
import com.fitpulse.service.NutritionService;
import com.fitpulse.service.WorkoutService;
import com.fitpulse.ui.panels.*;
import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * Main application window featuring sidebar navigation and card views.
 */
public class MainFrame extends JFrame {
    private final User currentUser;
    private final WorkoutService workoutService;
    private final GoalTrackingService goalService;
    private final NutritionService nutritionService;
    private final Runnable onLogout;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JPanel sidebarPanel;

    private DashboardPanel dashboardPanel;
    private WorkoutPanel workoutPanel;
    private GoalsPanel goalsPanel;
    private NutritionPanel nutritionPanel;
    private ConcurrencyDemoPanel concurrencyPanel;

    public MainFrame(User user,
                     WorkoutService workoutService,
                     GoalTrackingService goalService,
                     NutritionService nutritionService,
                     Runnable onLogout) {
        super("FitPulse — Fitness Tracking System");
        this.currentUser = user;
        this.workoutService = workoutService;
        this.goalService = goalService;
        this.nutritionService = nutritionService;
        this.onLogout = onLogout;

        initWindow();
        initSidebar();
        initContent();
    }

    private void initWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
    }

    private void initSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BorderLayout());
        sidebarPanel.setBackground(UITheme.BG_SIDEBAR);
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR));

        // App Brand Header
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JLabel lblLogo = new JLabel("⚡ FITPULSE");
        lblLogo.setFont(UITheme.FONT_TITLE);
        lblLogo.setForeground(UITheme.ACCENT_PRIMARY);

        JLabel lblTag = new JLabel("Fitness & Health Tracking");
        lblTag.setFont(UITheme.FONT_SMALL);
        lblTag.setForeground(UITheme.TEXT_MUTED);

        brandPanel.add(lblLogo);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(lblTag);

        // Navigation Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        menuPanel.add(createNavButton("  Dashboard", "DASHBOARD"));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavButton("  Workouts", "WORKOUTS"));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavButton("  Goals & Targets", "GOALS"));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavButton("  Nutrition", "NUTRITION"));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavButton("  Multithreading", "CONCURRENCY"));

        // User profile footer
        JPanel userFooter = new JPanel(new BorderLayout(8, 0));
        userFooter.setBackground(new Color(15, 23, 42));
        userFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JPanel userText = new JPanel();
        userText.setLayout(new BoxLayout(userText, BoxLayout.Y_AXIS));
        userText.setOpaque(false);

        JLabel lblUserName = new JLabel(currentUser.getName());
        lblUserName.setFont(UITheme.FONT_BOLD);
        lblUserName.setForeground(UITheme.TEXT_MAIN);

        JLabel lblUserRole = new JLabel(currentUser.getRole().name());
        lblUserRole.setFont(UITheme.FONT_SMALL);
        lblUserRole.setForeground(UITheme.ACCENT_TEAL);

        userText.add(lblUserName);
        userText.add(Box.createVerticalStrut(2));
        userText.add(lblUserRole);

        JButton btnSignOut = new JButton("Sign Out");
        btnSignOut.setFont(UITheme.FONT_SMALL);
        btnSignOut.setForeground(UITheme.ACCENT_DANGER);
        btnSignOut.setContentAreaFilled(false);
        btnSignOut.setBorderPainted(false);
        btnSignOut.setFocusPainted(false);
        btnSignOut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSignOut.addActionListener(e -> {
            if (onLogout != null) onLogout.run();
        });

        userFooter.add(userText, BorderLayout.WEST);
        userFooter.add(btnSignOut, BorderLayout.EAST);

        sidebarPanel.add(brandPanel, BorderLayout.NORTH);
        sidebarPanel.add(menuPanel, BorderLayout.CENTER);
        sidebarPanel.add(userFooter, BorderLayout.SOUTH);

        add(sidebarPanel, BorderLayout.WEST);
    }

    private void initContent() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UITheme.BG_DARK);

        dashboardPanel = new DashboardPanel(this, currentUser, workoutService, goalService, nutritionService);
        workoutPanel = new WorkoutPanel(this, currentUser, workoutService, goalService);
        goalsPanel = new GoalsPanel(this, currentUser, goalService);
        nutritionPanel = new NutritionPanel(this, currentUser, nutritionService);
        concurrencyPanel = new ConcurrencyDemoPanel(currentUser, goalService);

        mainContentPanel.add(dashboardPanel, "DASHBOARD");
        mainContentPanel.add(workoutPanel, "WORKOUTS");
        mainContentPanel.add(goalsPanel, "GOALS");
        mainContentPanel.add(nutritionPanel, "NUTRITION");
        mainContentPanel.add(concurrencyPanel, "CONCURRENCY");

        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JButton createNavButton(String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(UITheme.TEXT_MUTED);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(Color.WHITE);
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(25, 34, 48));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(UITheme.TEXT_MUTED);
                btn.setContentAreaFilled(false);
            }
        });

        btn.addActionListener(e -> {
            cardLayout.show(mainContentPanel, cardName);
            if ("DASHBOARD".equals(cardName)) dashboardPanel.refreshDataAsync();
            else if ("WORKOUTS".equals(cardName)) workoutPanel.loadWorkouts();
            else if ("GOALS".equals(cardName)) goalsPanel.loadGoals();
            else if ("NUTRITION".equals(cardName)) nutritionPanel.loadNutritionData();
        });

        return btn;
    }
}
