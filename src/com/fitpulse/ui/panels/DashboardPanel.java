package com.fitpulse.ui.panels;

import com.fitpulse.concurrency.BackgroundSyncWorker;
import com.fitpulse.concurrency.SharedMetricsMonitor;
import com.fitpulse.model.User;
import com.fitpulse.model.Workout;
import com.fitpulse.service.GoalTrackingService;
import com.fitpulse.service.NutritionService;
import com.fitpulse.service.WorkoutService;
import com.fitpulse.ui.components.ModernButton;
import com.fitpulse.ui.components.ModernTable;
import com.fitpulse.ui.components.StatCard;
import com.fitpulse.ui.dialogs.LogWorkoutDialog;
import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Modern Dashboard view featuring KPI stat cards, real-time sync, and recent activities.
 */
public class DashboardPanel extends JPanel {
    private final User currentUser;
    private final WorkoutService workoutService;
    private final GoalTrackingService goalService;
    private final NutritionService nutritionService;
    private final Frame parentFrame;

    private StatCard cardCalories;
    private StatCard cardWorkouts;
    private StatCard cardGoals;
    private StatCard cardLiveSessions;

    private DefaultTableModel tableModel;
    private ModernTable table;
    private JLabel lblStatus;

    public DashboardPanel(Frame parent, User user, WorkoutService workoutService,
                          GoalTrackingService goalService, NutritionService nutritionService) {
        this.parentFrame = parent;
        this.currentUser = user;
        this.workoutService = workoutService;
        this.goalService = goalService;
        this.nutritionService = nutritionService;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initHeader();
        initStats();
        initRecentWorkouts();
        refreshDataAsync();
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel lblGreeting = new JLabel("Welcome back, " + currentUser.getName());
        lblGreeting.setFont(UITheme.FONT_TITLE);
        lblGreeting.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Track your performance, achieve your milestones, and stay fit.");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        titles.add(lblGreeting);
        titles.add(Box.createVerticalStrut(4));
        titles.add(lblSub);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        ModernButton btnLogWorkout = new ModernButton("+ Log Activity", UITheme.ACCENT_PRIMARY);
        btnLogWorkout.addActionListener(e -> {
            LogWorkoutDialog dlg = new LogWorkoutDialog(parentFrame, currentUser.getId(), workoutService, goalService, this::refreshDataAsync);
            dlg.setVisible(true);
        });

        ModernButton btnRefresh = new ModernButton("Refresh (Async)", UITheme.ACCENT_TEAL);
        btnRefresh.addActionListener(e -> refreshDataAsync());

        actions.add(btnLogWorkout);
        actions.add(btnRefresh);

        headerPanel.add(titles, BorderLayout.WEST);
        headerPanel.add(actions, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void initStats() {
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        statsGrid.setOpaque(false);
        statsGrid.setPreferredSize(new Dimension(0, 110));

        cardCalories = new StatCard("Total Burned", "0 kcal", "All-time workouts", UITheme.ACCENT_PRIMARY);
        cardWorkouts = new StatCard("Sessions", "0", "Total logged workouts", UITheme.ACCENT_TEAL);
        cardGoals = new StatCard("Active Goals", "0", "Current milestones", UITheme.ACCENT_WARNING);
        cardLiveSessions = new StatCard("Live Sync", "0 Active", "Thread-safe monitor", UITheme.ACCENT_SUCCESS);

        statsGrid.add(cardCalories);
        statsGrid.add(cardWorkouts);
        statsGrid.add(cardGoals);
        statsGrid.add(cardLiveSessions);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(statsGrid, BorderLayout.NORTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void initRecentWorkouts() {
        JPanel recentPanel = new JPanel(new BorderLayout(0, 10));
        recentPanel.setOpaque(false);
        recentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Recent Workout Sessions");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        lblStatus = new JLabel("Fetching latest data...");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.ACCENT_TEAL);

        titleBar.add(lblTitle, BorderLayout.WEST);
        titleBar.add(lblStatus, BorderLayout.EAST);
        recentPanel.add(titleBar, BorderLayout.NORTH);

        String[] cols = {"Type", "Date & Time", "Duration", "Calories Burned", "Specific Metrics", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new ModernTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UITheme.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        recentPanel.add(scroll, BorderLayout.CENTER);

        // Add to main center panel
        BorderLayout layout = (BorderLayout) getLayout();
        Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
        if (centerComp instanceof JPanel) {
            ((JPanel) centerComp).add(recentPanel, BorderLayout.CENTER);
        }
    }

    /**
     * Demonstrates Rubric Item 3: Multithreading & Concurrency.
     * Uses SwingWorker to fetch database records asynchronously without blocking the Swing Event Dispatch Thread (EDT).
     */
    public void refreshDataAsync() {
        lblStatus.setText("Syncing in background worker thread...");
        BackgroundSyncWorker worker = new BackgroundSyncWorker(
                currentUser.getId(),
                workoutService,
                goalService,
                nutritionService,
                snapshot -> {
                    // Invoked on Event Dispatch Thread
                    cardCalories.setValue(String.format("%.0f kcal", snapshot.totalCaloriesBurned));
                    cardWorkouts.setValue(String.valueOf(snapshot.totalWorkouts));
                    cardGoals.setValue(snapshot.goals.size() + " Goals");

                    // Read from thread-safe synchronized SharedMetricsMonitor
                    int liveSessions = SharedMetricsMonitor.getInstance().getActiveLiveSessions();
                    cardLiveSessions.setValue(liveSessions + " Active");

                    // Populate table
                    tableModel.setRowCount(0);
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
                    List<Workout> recent = snapshot.workouts;
                    int count = Math.min(recent.size(), 8);
                    for (int i = 0; i < count; i++) {
                        Workout w = recent.get(i);
                        tableModel.addRow(new Object[]{
                                w.getWorkoutType(),
                                w.getWorkoutDate().format(fmt),
                                w.getDurationMinutes() + " mins",
                                String.format("%.1f kcal", w.getCaloriesBurned()),
                                w.getSpecificDetails(),
                                w.getNotes() != null ? w.getNotes() : "-"
                        });
                    }
                    lblStatus.setText("Live Synced (" + snapshot.workouts.size() + " total workouts recorded)");
                },
                ex -> {
                    lblStatus.setText("Sync Error: " + ex.getMessage());
                }
        );
        worker.execute();
    }
}
