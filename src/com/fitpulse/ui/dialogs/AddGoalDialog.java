package com.fitpulse.ui.dialogs;

import com.fitpulse.model.enums.GoalType;
import com.fitpulse.service.GoalTrackingService;
import com.fitpulse.ui.components.ModernButton;
import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;

/**
 * Modal dialog for establishing user fitness goals.
 */
public class AddGoalDialog extends JDialog {
    private final int userId;
    private final GoalTrackingService goalService;
    private final Runnable onGoalAdded;

    private JComboBox<GoalType> cmbGoalType;
    private JTextField txtTarget;
    private JTextField txtDays;
    private JLabel lblUnit;

    public AddGoalDialog(Frame parent, int userId, GoalTrackingService goalService, Runnable onGoalAdded) {
        super(parent, "Set Fitness Goal — FitPulse", true);
        this.userId = userId;
        this.goalService = goalService;
        this.onGoalAdded = onGoalAdded;
        initUI();
    }

    private void initUI() {
        setSize(400, 360);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Set New Milestone");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        // Goal Type
        JLabel lblType = new JLabel("Milestone Target Type:");
        lblType.setFont(UITheme.FONT_SMALL);
        lblType.setForeground(UITheme.TEXT_MUTED);
        panel.add(lblType);

        cmbGoalType = new JComboBox<>(GoalType.values());
        cmbGoalType.setFont(UITheme.FONT_BODY);
        cmbGoalType.setBackground(UITheme.BG_INPUT);
        cmbGoalType.setForeground(UITheme.TEXT_MAIN);
        cmbGoalType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cmbGoalType.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbGoalType.addActionListener(e -> updateUnit());
        panel.add(cmbGoalType);
        panel.add(Box.createVerticalStrut(10));

        // Target Value
        lblUnit = new JLabel("Target Goal Value (kcal):");
        lblUnit.setFont(UITheme.FONT_SMALL);
        lblUnit.setForeground(UITheme.TEXT_MUTED);
        panel.add(lblUnit);

        txtTarget = new JTextField("3000");
        styleInput(txtTarget);
        panel.add(txtTarget);
        panel.add(Box.createVerticalStrut(10));

        // Days duration
        JLabel lblDays = new JLabel("Deadline Horizon (Days from now):");
        lblDays.setFont(UITheme.FONT_SMALL);
        lblDays.setForeground(UITheme.TEXT_MUTED);
        panel.add(lblDays);

        txtDays = new JTextField("14");
        styleInput(txtDays);
        panel.add(txtDays);
        panel.add(Box.createVerticalStrut(18));

        ModernButton btnSave = new ModernButton("Create Milestone Goal", UITheme.ACCENT_TEAL);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.addActionListener(e -> saveGoal());
        panel.add(btnSave);

        add(panel);
        updateUnit();
    }

    private void updateUnit() {
        GoalType gt = (GoalType) cmbGoalType.getSelectedItem();
        if (gt != null) {
            lblUnit.setText(String.format("Target Goal Value (%s):", gt.getUnit()));
        }
    }

    private void saveGoal() {
        try {
            double target = Double.parseDouble(txtTarget.getText().trim());
            int days = Integer.parseInt(txtDays.getText().trim());
            GoalType type = (GoalType) cmbGoalType.getSelectedItem();

            LocalDate start = LocalDate.now();
            LocalDate end = start.plusDays(days);

            goalService.createGoal(userId, type, target, start, end);
            JOptionPane.showMessageDialog(this, "Fitness goal created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            if (onGoalAdded != null) onGoalAdded.run();
            dispose();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.", "Validation", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleInput(JTextField tf) {
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
    }
}
