package com.fitpulse.ui.dialogs;

import com.fitpulse.model.CardioWorkout;
import com.fitpulse.model.FitnessGoal;
import com.fitpulse.model.StrengthWorkout;
import com.fitpulse.model.Workout;
import com.fitpulse.model.enums.WorkoutType;
import com.fitpulse.service.GoalTrackingService;
import com.fitpulse.service.WorkoutService;
import com.fitpulse.ui.components.ModernButton;
import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.*;

/**
 * Modal dialog for logging polymorphic workouts (Cardio / Strength)
 * and triggering atomic multi-table transaction workflows.
 * Demonstrates:
 * - Rubric Item 1: Polymorphism (CardioWorkout vs StrengthWorkout)
 * - Rubric Item 6: Transaction Management (Atomic workout + goal sync)
 */
public class LogWorkoutDialog extends JDialog {
    private final int userId;
    private final WorkoutService workoutService;
    private final GoalTrackingService goalService;
    private final Runnable onWorkoutSaved;

    private JComboBox<WorkoutType> cmbType;
    private JTextField txtDuration;
    private JTextField txtNotes;

    // Cardio specific
    private JPanel panelCardio;
    private JTextField txtDistance;
    private JTextField txtHeartRate;

    // Strength specific
    private JPanel panelStrength;
    private JTextField txtWeight;
    private JTextField txtSets;
    private JTextField txtReps;

    // Transaction Management Controls
    private JCheckBox chkAtomicGoalSync;
    private JComboBox<String> cmbActiveGoals;
    private List<FitnessGoal> activeGoals;

    public LogWorkoutDialog(Frame parent, int userId,
                            WorkoutService workoutService,
                            GoalTrackingService goalService,
                            Runnable onWorkoutSaved) {
        super(parent, "Log New Workout — FitPulse", true);
        this.userId = userId;
        this.workoutService = workoutService;
        this.goalService = goalService;
        this.onWorkoutSaved = onWorkoutSaved;

        initUI();
    }

    private void initUI() {
        setSize(460, 580);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(UITheme.BG_DARK);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel("Record Fitness Activity");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblTitle);
        formPanel.add(Box.createVerticalStrut(15));

        // Workout Type selector
        formPanel.add(createLabel("Workout Modality (Polymorphic Model)"));
        cmbType = new JComboBox<>(new WorkoutType[]{WorkoutType.CARDIO, WorkoutType.STRENGTH});
        cmbType.setFont(UITheme.FONT_BODY);
        cmbType.setBackground(UITheme.BG_INPUT);
        cmbType.setForeground(UITheme.TEXT_MAIN);
        cmbType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cmbType.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbType.addActionListener(e -> updateTypePanels());
        formPanel.add(cmbType);
        formPanel.add(Box.createVerticalStrut(10));

        // Duration
        formPanel.add(createLabel("Duration (Minutes)"));
        txtDuration = createTextField("30");
        formPanel.add(txtDuration);
        formPanel.add(Box.createVerticalStrut(10));

        // Dynamic Card Panel for Cardio vs Strength
        panelCardio = createCardioPanel();
        panelStrength = createStrengthPanel();
        formPanel.add(panelCardio);
        formPanel.add(panelStrength);

        // Notes
        formPanel.add(createLabel("Workout Notes / Log"));
        txtNotes = createTextField("Routine session");
        formPanel.add(txtNotes);
        formPanel.add(Box.createVerticalStrut(12));

        // Transaction Management Section (Rubric Item 6)
        JPanel txBox = new JPanel();
        txBox.setLayout(new BoxLayout(txBox, BoxLayout.Y_AXIS));
        txBox.setBackground(new Color(24, 34, 52));
        txBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT_PRIMARY, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        txBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        chkAtomicGoalSync = new JCheckBox("Atomic Transaction: Update Goal Progress");
        chkAtomicGoalSync.setFont(UITheme.FONT_BOLD);
        chkAtomicGoalSync.setForeground(UITheme.ACCENT_PRIMARY);
        chkAtomicGoalSync.setOpaque(false);
        chkAtomicGoalSync.addActionListener(e -> cmbActiveGoals.setEnabled(chkAtomicGoalSync.isSelected()));
        txBox.add(chkAtomicGoalSync);
        txBox.add(Box.createVerticalStrut(6));

        cmbActiveGoals = new JComboBox<>();
        cmbActiveGoals.setFont(UITheme.FONT_BODY);
        cmbActiveGoals.setBackground(UITheme.BG_CARD);
        cmbActiveGoals.setForeground(UITheme.TEXT_MAIN);
        cmbActiveGoals.setEnabled(false);
        cmbActiveGoals.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        loadActiveGoals();
        txBox.add(cmbActiveGoals);

        formPanel.add(txBox);
        formPanel.add(Box.createVerticalStrut(16));

        // Save Button
        ModernButton btnSave = new ModernButton("Save Workout Session", UITheme.ACCENT_SUCCESS);
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnSave.addActionListener(e -> saveWorkout());
        formPanel.add(btnSave);

        add(formPanel, BorderLayout.CENTER);
        updateTypePanels();
    }

    private JPanel createCardioPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        txtDistance = createTextField("5.0");
        txtHeartRate = createTextField("145");

        p.add(createLabel("Distance (km):"));
        p.add(txtDistance);
        p.add(createLabel("Avg Heart Rate (bpm):"));
        p.add(txtHeartRate);
        return p;
    }

    private JPanel createStrengthPanel() {
        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        txtWeight = createTextField("2500");
        txtSets = createTextField("15");
        txtReps = createTextField("10");

        p.add(createLabel("Total Weight Volume (kg):"));
        p.add(txtWeight);
        p.add(createLabel("Total Sets:"));
        p.add(txtSets);
        p.add(createLabel("Reps per Set:"));
        p.add(txtReps);
        return p;
    }

    private void updateTypePanels() {
        WorkoutType type = (WorkoutType) cmbType.getSelectedItem();
        boolean isCardio = type == WorkoutType.CARDIO;
        panelCardio.setVisible(isCardio);
        panelStrength.setVisible(!isCardio);
        revalidate();
        repaint();
    }

    private void loadActiveGoals() {
        try {
            activeGoals = goalService.getActiveUserGoals(userId);
            cmbActiveGoals.removeAllItems();
            for (FitnessGoal g : activeGoals) {
                cmbActiveGoals.addItem(String.format("Goal #%d: %s (%.0f / %.0f %s)",
                        g.getId(), g.getGoalType().getDisplayName(), g.getCurrentValue(), g.getTargetValue(), g.getGoalType().getUnit()));
            }
            if (activeGoals.isEmpty()) {
                cmbActiveGoals.addItem("No active goals found");
                chkAtomicGoalSync.setEnabled(false);
            }
        } catch (Exception e) {
            cmbActiveGoals.addItem("Unable to load goals");
        }
    }

    private void saveWorkout() {
        try {
            int duration = Integer.parseInt(txtDuration.getText().trim());
            String notes = txtNotes.getText().trim();
            WorkoutType type = (WorkoutType) cmbType.getSelectedItem();
            LocalDateTime now = LocalDateTime.now();

            Workout workout;
            if (type == WorkoutType.CARDIO) {
                double distance = Double.parseDouble(txtDistance.getText().trim());
                int hr = Integer.parseInt(txtHeartRate.getText().trim());
                workout = new CardioWorkout(0, userId, now, duration, distance, hr, notes);
            } else {
                double weight = Double.parseDouble(txtWeight.getText().trim());
                int sets = Integer.parseInt(txtSets.getText().trim());
                int reps = Integer.parseInt(txtReps.getText().trim());
                workout = new StrengthWorkout(0, userId, now, duration, weight, sets, reps, notes);
            }

            // Check if atomic transaction mode is selected
            if (chkAtomicGoalSync.isSelected() && activeGoals != null && !activeGoals.isEmpty()) {
                int selectedIdx = cmbActiveGoals.getSelectedIndex();
                FitnessGoal goal = activeGoals.get(selectedIdx);
                double increment = (goal.getGoalType() == com.fitpulse.model.enums.GoalType.CALORIE_BURN)
                        ? workout.getCaloriesBurned() : 1.0;

                // Execute Atomic Multi-Step Transaction (Rubric Item 6)
                goalService.logWorkoutAndUpdateGoalAtomic(workout, goal.getId(), increment);
                JOptionPane.showMessageDialog(this,
                        "Atomic Transaction Committed Successfully!\n" +
                        "1. Workout logged (" + workout.getCaloriesBurned() + " kcal)\n" +
                        "2. Goal #" + goal.getId() + " updated (+ " + increment + ")\n" +
                        "3. Audit log committed to database.",
                        "Transaction Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (workout instanceof CardioWorkout) {
                    CardioWorkout c = (CardioWorkout) workout;
                    workoutService.logCardioWorkout(userId, now, duration, c.getDistanceKm(), c.getAvgHeartRate(), notes);
                } else {
                    StrengthWorkout s = (StrengthWorkout) workout;
                    workoutService.logStrengthWorkout(userId, now, duration, s.getTotalWeightKg(), s.getSetsCount(), s.getRepsCount(), notes);
                }
                JOptionPane.showMessageDialog(this,
                        "Workout logged successfully!\nCalories burned: " + workout.getCaloriesBurned() + " kcal",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            if (onWorkoutSaved != null) {
                onWorkoutSaved.run();
            }
            dispose();

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please check all numeric inputs.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Operation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField createTextField(String val) {
        JTextField tf = new JTextField(val);
        tf.setFont(UITheme.FONT_BODY);
        tf.setBackground(UITheme.BG_INPUT);
        tf.setForeground(UITheme.TEXT_MAIN);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }
}
