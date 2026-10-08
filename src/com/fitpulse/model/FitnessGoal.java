package com.fitpulse.model;

import com.fitpulse.model.enums.GoalStatus;
import com.fitpulse.model.enums.GoalType;
import java.time.LocalDate;

/**
 * Model class encapsulating user fitness milestones.
 * Demonstrates:
 * - OOP: Encapsulation (progress calculation & constraints)
 * - Rubric Item 4: Database Model Class
 */
public class FitnessGoal {
    private int id;
    private int userId;
    private GoalType goalType;
    private double targetValue;
    private double currentValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private GoalStatus status;

    public FitnessGoal() {
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusWeeks(2);
        this.status = GoalStatus.IN_PROGRESS;
    }

    public FitnessGoal(int id, int userId, GoalType goalType, double targetValue,
                       double currentValue, LocalDate startDate, LocalDate endDate, GoalStatus status) {
        this.id = id;
        this.userId = userId;
        this.goalType = goalType;
        this.targetValue = targetValue;
        this.currentValue = currentValue;
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.endDate = endDate != null ? endDate : LocalDate.now().plusWeeks(2);
        this.status = status != null ? status : GoalStatus.IN_PROGRESS;
    }

    public double getProgressPercentage() {
        if (targetValue <= 0) return 0.0;
        double pct = (currentValue / targetValue) * 100.0;
        return Math.min(100.0, Math.max(0.0, pct));
    }

    public boolean isGoalReached() {
        return currentValue >= targetValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public void setGoalType(GoalType goalType) {
        this.goalType = goalType;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(double targetValue) {
        if (targetValue <= 0) {
            throw new IllegalArgumentException("Goal target value must be positive.");
        }
        this.targetValue = targetValue;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = Math.max(0.0, currentValue);
        if (isGoalReached() && status == GoalStatus.IN_PROGRESS) {
            this.status = GoalStatus.COMPLETED;
        }
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }
}
