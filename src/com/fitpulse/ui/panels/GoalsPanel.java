package com.fitpulse.ui.panels;

import com.fitpulse.model.FitnessGoal;
import com.fitpulse.model.User;
import com.fitpulse.model.enums.GoalStatus;
import com.fitpulse.service.GoalTrackingService;
import com.fitpulse.ui.components.CircularProgressBar;
import com.fitpulse.ui.components.ModernButton;
import com.fitpulse.ui.dialogs.AddGoalDialog;
import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.swing.*;

/**
 * Visual fitness goal tracking panel featuring circular/linear progress bars, status badges, and milestone tracking.
 */
public class GoalsPanel extends JPanel {
    private final Frame parentFrame;
    private final User currentUser;
    private final GoalTrackingService goalService;

    private JPanel goalsGrid;

    public GoalsPanel(Frame parent, User user, GoalTrackingService goalService) {
        this.parentFrame = parent;
        this.currentUser = user;
        this.goalService = goalService;

        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initHeader();
        initGoalsList();
        loadGoals();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Fitness Goals & Milestones");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Set performance targets and monitor your progress towards completion.");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblSub);

        ModernButton btnAddGoal = new ModernButton("+ Set New Goal", UITheme.ACCENT_PRIMARY);
        btnAddGoal.addActionListener(e -> {
            AddGoalDialog dlg = new AddGoalDialog(parentFrame, currentUser.getId(), goalService, this::loadGoals);
            dlg.setVisible(true);
        });

        header.add(left, BorderLayout.WEST);
        header.add(btnAddGoal, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void initGoalsList() {
        goalsGrid = new JPanel();
        goalsGrid.setLayout(new BoxLayout(goalsGrid, BoxLayout.Y_AXIS));
        goalsGrid.setOpaque(false);

        JScrollPane scroll = new JScrollPane(goalsGrid);
        scroll.getViewport().setBackground(UITheme.BG_DARK);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        add(scroll, BorderLayout.CENTER);
    }

    public void loadGoals() {
        goalsGrid.removeAll();
        try {
            List<FitnessGoal> goals = goalService.getUserGoals(currentUser.getId());
            if (goals.isEmpty()) {
                JLabel emptyLbl = new JLabel("No fitness goals set yet. Click '+ Set New Goal' above to create one!", SwingConstants.CENTER);
                emptyLbl.setFont(UITheme.FONT_SUBTITLE);
                emptyLbl.setForeground(UITheme.TEXT_MUTED);
                emptyLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                goalsGrid.add(Box.createVerticalStrut(50));
                goalsGrid.add(emptyLbl);
            } else {
                for (FitnessGoal g : goals) {
                    goalsGrid.add(createGoalCard(g));
                    goalsGrid.add(Box.createVerticalStrut(12));
                }
            }
        } catch (Exception e) {
            JLabel errLbl = new JLabel("Error loading goals: " + e.getMessage());
            errLbl.setForeground(UITheme.ACCENT_DANGER);
            goalsGrid.add(errLbl);
        }
        goalsGrid.revalidate();
        goalsGrid.repaint();
    }

    private JPanel createGoalCard(FitnessGoal goal) {
        JPanel card = new JPanel(new BorderLayout(16, 8));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(UITheme.createCardBorder());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Circular progress indicator
        CircularProgressBar progressRing = new CircularProgressBar();
        progressRing.setProgress(goal.getProgressPercentage());
        if (goal.getStatus() == GoalStatus.COMPLETED) {
            progressRing.setProgressColor(UITheme.ACCENT_SUCCESS);
        } else {
            progressRing.setProgressColor(UITheme.ACCENT_TEAL);
        }
        card.add(progressRing, BorderLayout.WEST);

        // Center content
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel lblName = new JLabel(goal.getGoalType().getDisplayName());
        lblName.setFont(UITheme.FONT_SUBTITLE);
        lblName.setForeground(UITheme.TEXT_MAIN);

        String progressTxt = String.format("Current Progress: %.1f / %.1f %s",
                goal.getCurrentValue(), goal.getTargetValue(), goal.getGoalType().getUnit());
        JLabel lblProgress = new JLabel(progressTxt);
        lblProgress.setFont(UITheme.FONT_BODY);
        lblProgress.setForeground(UITheme.TEXT_MUTED);

        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), goal.getEndDate());
        String timeTxt = daysLeft >= 0 ? (daysLeft + " days remaining until " + goal.getEndDate())
                                       : ("Ended on " + goal.getEndDate());
        JLabel lblTime = new JLabel(timeTxt);
        lblTime.setFont(UITheme.FONT_SMALL);
        lblTime.setForeground(daysLeft <= 2 ? UITheme.ACCENT_WARNING : UITheme.TEXT_MUTED);

        center.add(lblName);
        center.add(Box.createVerticalStrut(4));
        center.add(lblProgress);
        center.add(Box.createVerticalStrut(4));
        center.add(lblTime);

        card.add(center, BorderLayout.CENTER);

        // Right side badge and delete action
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        right.setOpaque(false);

        JLabel lblBadge = new JLabel(" " + goal.getStatus().name() + " ");
        lblBadge.setFont(UITheme.FONT_BOLD);
        lblBadge.setOpaque(true);
        if (goal.getStatus() == GoalStatus.COMPLETED) {
            lblBadge.setBackground(new Color(6, 78, 59));
            lblBadge.setForeground(UITheme.ACCENT_SUCCESS);
        } else {
            lblBadge.setBackground(new Color(30, 58, 138));
            lblBadge.setForeground(new Color(147, 197, 253));
        }
        lblBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        right.add(lblBadge);

        ModernButton btnDel = new ModernButton("Delete", UITheme.ACCENT_DANGER);
        btnDel.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete Goal #" + goal.getId() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    goalService.deleteGoal(goal.getId());
                    loadGoals();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        right.add(btnDel);

        card.add(right, BorderLayout.EAST);
        return card;
    }
}
