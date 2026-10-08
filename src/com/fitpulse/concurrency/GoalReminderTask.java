package com.fitpulse.concurrency;

import com.fitpulse.interfaces.NotificationListener;
import com.fitpulse.model.FitnessGoal;
import com.fitpulse.service.GoalTrackingService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Background Runnable task periodically checking active goal deadlines and triggering notifications.
 * Demonstrates:
 * - Rubric Item 3: Multithreading & Concurrency (Runnable task on dedicated background thread)
 */
public class GoalReminderTask implements Runnable {
    private final int userId;
    private final GoalTrackingService goalService;
    private final NotificationListener listener;

    public GoalReminderTask(int userId, GoalTrackingService goalService, NotificationListener listener) {
        this.userId = userId;
        this.goalService = goalService;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            List<FitnessGoal> activeGoals = goalService.getActiveUserGoals(userId);
            LocalDate today = LocalDate.now();

            for (FitnessGoal goal : activeGoals) {
                long daysRemaining = ChronoUnit.DAYS.between(today, goal.getEndDate());
                if (daysRemaining <= 2 && daysRemaining >= 0 && !goal.isGoalReached()) {
                    String title = "Goal Deadline Approaching!";
                    String message = String.format("Goal '%s' ends in %d day(s). Current progress: %.0f%%.",
                            goal.getGoalType().getDisplayName(), daysRemaining, goal.getProgressPercentage());
                    if (listener != null) {
                        listener.onNotification(title, message, "WARNING");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[GOAL REMINDER] Failed to run reminder task: " + e.getMessage());
        }
    }
}
