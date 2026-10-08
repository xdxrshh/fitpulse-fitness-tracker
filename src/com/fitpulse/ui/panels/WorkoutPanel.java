package com.fitpulse.ui.panels;

import com.fitpulse.model.CardioWorkout;
import com.fitpulse.model.StrengthWorkout;
import com.fitpulse.model.User;
import com.fitpulse.model.Workout;
import com.fitpulse.service.GoalTrackingService;
import com.fitpulse.service.WorkoutService;
import com.fitpulse.ui.components.ModernButton;
import com.fitpulse.ui.components.ModernTable;
import com.fitpulse.ui.dialogs.LogWorkoutDialog;
import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Detailed Workout history view featuring polymorphic filtering, intensity analysis, and record management.
 */
public class WorkoutPanel extends JPanel {
    private final Frame parentFrame;
    private final User currentUser;
    private final WorkoutService workoutService;
    private final GoalTrackingService goalService;

    private DefaultTableModel tableModel;
    private ModernTable table;
    private JComboBox<String> cmbFilter;
    private List<Workout> allUserWorkouts = new ArrayList<>();
    private JLabel lblSummary;

    public WorkoutPanel(Frame parent, User user, WorkoutService workoutService, GoalTrackingService goalService) {
        this.parentFrame = parent;
        this.currentUser = user;
        this.workoutService = workoutService;
        this.goalService = goalService;

        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initToolbar();
        initTable();
        loadWorkouts();
    }

    private void initToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Workout Logs");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        left.add(lblTitle);

        JLabel lblFilter = new JLabel("Filter Modality:");
        lblFilter.setFont(UITheme.FONT_SMALL);
        lblFilter.setForeground(UITheme.TEXT_MUTED);
        left.add(lblFilter);

        cmbFilter = new JComboBox<>(new String[]{"ALL ACTIVITIES", "CARDIO ONLY", "STRENGTH ONLY"});
        cmbFilter.setFont(UITheme.FONT_BODY);
        cmbFilter.setBackground(UITheme.BG_INPUT);
        cmbFilter.setForeground(UITheme.TEXT_MAIN);
        cmbFilter.addActionListener(e -> applyFilter());
        left.add(cmbFilter);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        ModernButton btnAdd = new ModernButton("+ Log Workout", UITheme.ACCENT_PRIMARY);
        btnAdd.addActionListener(e -> {
            LogWorkoutDialog dlg = new LogWorkoutDialog(parentFrame, currentUser.getId(), workoutService, goalService, this::loadWorkouts);
            dlg.setVisible(true);
        });

        ModernButton btnDelete = new ModernButton("Delete Selected", UITheme.ACCENT_DANGER);
        btnDelete.addActionListener(e -> deleteSelectedWorkout());

        right.add(btnAdd);
        right.add(btnDelete);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);
    }

    private void initTable() {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        String[] cols = {"ID", "Type", "Date", "Duration", "Calories (kcal)", "Intensity (1-10)", "Specific Activity Metrics", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setMaxWidth(110);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UITheme.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        centerPanel.add(scroll, BorderLayout.CENTER);

        lblSummary = new JLabel("Loading records...");
        lblSummary.setFont(UITheme.FONT_SMALL);
        lblSummary.setForeground(UITheme.TEXT_MUTED);
        centerPanel.add(lblSummary, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void loadWorkouts() {
        try {
            allUserWorkouts = workoutService.getUserWorkouts(currentUser.getId());
            applyFilter();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load workouts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        String filter = (String) cmbFilter.getSelectedItem();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        double totalCaloriesFiltered = 0;
        int count = 0;

        for (Workout w : allUserWorkouts) {
            if ("CARDIO ONLY".equals(filter) && !(w instanceof CardioWorkout)) continue;
            if ("STRENGTH ONLY".equals(filter) && !(w instanceof StrengthWorkout)) continue;

            tableModel.addRow(new Object[]{
                    w.getId(),
                    w.getWorkoutType(),
                    w.getWorkoutDate().format(fmt),
                    w.getDurationMinutes() + " min",
                    String.format("%.1f", w.getCaloriesBurned()),
                    w.getIntensityScore() + " / 10",
                    w.getSpecificDetails(),
                    w.getNotes() != null ? w.getNotes() : ""
            });

            totalCaloriesFiltered += w.getCaloriesBurned();
            count++;
        }

        lblSummary.setText(String.format("Showing %d records | Aggregate Burn: %.1f kcal | Average: %.1f kcal/session",
                count, totalCaloriesFiltered, count > 0 ? (totalCaloriesFiltered / count) : 0.0));
    }

    private void deleteSelectedWorkout() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a workout to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int workoutId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete workout #" + workoutId + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                workoutService.deleteWorkout(workoutId, currentUser.getId());
                loadWorkouts();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
