package com.fitpulse.model.enums;

public enum GoalType {
    CALORIE_BURN("Calorie Burn Target", "kcal"),
    WORKOUT_COUNT("Total Workouts Completed", "sessions"),
    WEIGHT_LOSS("Target Body Weight", "kg");

    private final String displayName;
    private final String unit;

    GoalType(String displayName, String unit) {
        this.displayName = displayName;
        this.unit = unit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }
}
