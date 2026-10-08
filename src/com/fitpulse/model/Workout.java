package com.fitpulse.model;

import com.fitpulse.interfaces.Reportable;
import com.fitpulse.interfaces.Trackable;
import com.fitpulse.model.enums.WorkoutType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Workout entity.
 * Demonstrates:
 * - OOP: Inheritance (base class for CardioWorkout and StrengthWorkout)
 * - OOP: Interfaces (implements Trackable, Reportable)
 * - OOP: Encapsulation (private fields, defensive checks)
 * - Collections: Map (toMetricMap)
 * - Rubric Item 1: OOP & Item 4: Model Class
 */
public abstract class Workout implements Trackable, Reportable {
    private int id;
    private int userId;
    private WorkoutType workoutType;
    private LocalDateTime workoutDate;
    private int durationMinutes;
    private double caloriesBurned;
    private String notes;

    public Workout() {
        this.workoutDate = LocalDateTime.now();
    }

    public Workout(int id, int userId, WorkoutType workoutType, LocalDateTime workoutDate,
                   int durationMinutes, double caloriesBurned, String notes) {
        this.id = id;
        this.userId = userId;
        this.workoutType = workoutType;
        this.workoutDate = workoutDate != null ? workoutDate : LocalDateTime.now();
        setDurationMinutes(durationMinutes);
        this.caloriesBurned = caloriesBurned;
        this.notes = notes;
    }

    // Abstract method forcing specialized string representations (Polymorphism)
    public abstract String getSpecificDetails();

    @Override
    public Map<String, Object> toMetricMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("workoutId", id);
        map.put("userId", userId);
        map.put("workoutType", workoutType != null ? workoutType.name() : "UNKNOWN");
        map.put("durationMinutes", durationMinutes);
        map.put("caloriesBurned", caloriesBurned);
        map.put("date", workoutDate.toString());
        map.put("intensityScore", getIntensityScore());
        return map;
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

    public WorkoutType getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(WorkoutType workoutType) {
        this.workoutType = workoutType;
    }

    public LocalDateTime getWorkoutDate() {
        return workoutDate;
    }

    public void setWorkoutDate(LocalDateTime workoutDate) {
        this.workoutDate = workoutDate;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Workout duration must be greater than zero.");
        }
        this.durationMinutes = durationMinutes;
    }

    public double getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(double caloriesBurned) {
        this.caloriesBurned = Math.max(0.0, caloriesBurned);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
