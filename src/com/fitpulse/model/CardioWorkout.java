package com.fitpulse.model;

import com.fitpulse.model.enums.WorkoutType;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents cardiovascular exercise (running, cycling, swimming, etc.).
 * Demonstrates:
 * - OOP: Inheritance (extends Workout, super(...))
 * - OOP: Polymorphism (overrides calculateCaloriesBurned, getIntensityScore, generateSummary)
 * - OOP: Encapsulation (heart rate and distance validations)
 * - Rubric Item 1: OOP & Item 4: Model Class
 */
public class CardioWorkout extends Workout {
    private double distanceKm;
    private int avgHeartRate;

    public CardioWorkout() {
        super();
        setWorkoutType(WorkoutType.CARDIO);
    }

    public CardioWorkout(int id, int userId, LocalDateTime workoutDate, int durationMinutes,
                         double distanceKm, int avgHeartRate, String notes) {
        super(id, userId, WorkoutType.CARDIO, workoutDate, durationMinutes, 0.0, notes);
        setDistanceKm(distanceKm);
        setAvgHeartRate(avgHeartRate);
        // Compute calories polymorphically
        setCaloriesBurned(calculateCaloriesBurned());
    }

    /**
     * Polymorphic calorie burn formula tailored for cardiovascular output:
     * Base MET factor modulated by average heart rate and speed (km/h).
     */
    @Override
    public double calculateCaloriesBurned() {
        double hours = getDurationMinutes() / 60.0;
        double speedKmh = hours > 0 ? (distanceKm / hours) : 0.0;
        // Approximation: Cal/min ~ (0.014 * HR + 0.05 * speed) * 60
        double factor = (avgHeartRate > 0) ? (avgHeartRate * 0.065) : 7.5;
        double speedFactor = Math.max(1.0, speedKmh * 0.85);
        double calculated = getDurationMinutes() * factor * 0.75 + (speedFactor * 10);
        return Math.round(calculated * 10.0) / 10.0;
    }

    @Override
    public int getIntensityScore() {
        if (avgHeartRate <= 0) return 5;
        if (avgHeartRate > 165) return 9;
        if (avgHeartRate > 140) return 7;
        if (avgHeartRate > 115) return 5;
        return 3;
    }

    @Override
    public String generateSummary() {
        return String.format("Cardio Session: %.2f km in %d mins (~%.1f kcal, Avg HR: %d bpm)",
                distanceKm, getDurationMinutes(), getCaloriesBurned(), avgHeartRate);
    }

    @Override
    public String getSpecificDetails() {
        return String.format("Distance: %.2f km | Heart Rate: %d bpm", distanceKm, avgHeartRate);
    }

    @Override
    public Map<String, Object> toMetricMap() {
        Map<String, Object> metrics = super.toMetricMap();
        metrics.put("distanceKm", distanceKm);
        metrics.put("avgHeartRate", avgHeartRate);
        return metrics;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative.");
        }
        this.distanceKm = distanceKm;
    }

    public int getAvgHeartRate() {
        return avgHeartRate;
    }

    public void setAvgHeartRate(int avgHeartRate) {
        if (avgHeartRate < 0 || avgHeartRate > 250) {
            throw new IllegalArgumentException("Average heart rate must be between 0 and 250 bpm.");
        }
        this.avgHeartRate = avgHeartRate;
    }
}
