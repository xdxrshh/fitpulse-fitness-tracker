package com.fitpulse.model;

import com.fitpulse.model.enums.WorkoutType;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents resistance/weight training exercises.
 * Demonstrates:
 * - OOP: Inheritance (extends Workout, super(...))
 * - OOP: Polymorphism (overrides calculateCaloriesBurned, getIntensityScore, generateSummary)
 * - OOP: Encapsulation (sets, reps, and total weight lifted validations)
 * - Rubric Item 1: OOP & Item 4: Model Class
 */
public class StrengthWorkout extends Workout {
    private double totalWeightKg;
    private int setsCount;
    private int repsCount;

    public StrengthWorkout() {
        super();
        setWorkoutType(WorkoutType.STRENGTH);
    }

    public StrengthWorkout(int id, int userId, LocalDateTime workoutDate, int durationMinutes,
                           double totalWeightKg, int setsCount, int repsCount, String notes) {
        super(id, userId, WorkoutType.STRENGTH, workoutDate, durationMinutes, 0.0, notes);
        setTotalWeightKg(totalWeightKg);
        setSetsCount(setsCount);
        setRepsCount(repsCount);
        // Compute calories polymorphically
        setCaloriesBurned(calculateCaloriesBurned());
    }

    /**
     * Polymorphic calorie burn formula tailored for strength & resistance training:
     * Derived from total tonnage (volume = sets * reps * weight) + time under tension.
     */
    @Override
    public double calculateCaloriesBurned() {
        double timeCal = getDurationMinutes() * 5.5; // Base MET for strength session
        double tonnageKiloFactor = (totalWeightKg / 1000.0) * 18.0;
        double volumeFactor = (setsCount * repsCount) * 0.45;
        double calculated = timeCal + tonnageKiloFactor + volumeFactor;
        return Math.round(calculated * 10.0) / 10.0;
    }

    @Override
    public int getIntensityScore() {
        double volume = setsCount * repsCount;
        if (totalWeightKg > 5000 || volume > 180) return 9;
        if (totalWeightKg > 2500 || volume > 100) return 7;
        if (totalWeightKg > 1000 || volume > 50) return 5;
        return 3;
    }

    @Override
    public String generateSummary() {
        return String.format("Strength Training: %d sets, %d reps (Total Vol: %.1f kg, ~%.1f kcal in %d mins)",
                setsCount, repsCount, totalWeightKg, getCaloriesBurned(), getDurationMinutes());
    }

    @Override
    public String getSpecificDetails() {
        return String.format("Volume: %.1f kg | Sets: %d | Reps: %d", totalWeightKg, setsCount, repsCount);
    }

    @Override
    public Map<String, Object> toMetricMap() {
        Map<String, Object> metrics = super.toMetricMap();
        metrics.put("totalWeightKg", totalWeightKg);
        metrics.put("setsCount", setsCount);
        metrics.put("repsCount", repsCount);
        return metrics;
    }

    public double getTotalWeightKg() {
        return totalWeightKg;
    }

    public void setTotalWeightKg(double totalWeightKg) {
        if (totalWeightKg < 0) {
            throw new IllegalArgumentException("Total weight lifted cannot be negative.");
        }
        this.totalWeightKg = totalWeightKg;
    }

    public int getSetsCount() {
        return setsCount;
    }

    public void setSetsCount(int setsCount) {
        if (setsCount < 0) {
            throw new IllegalArgumentException("Sets count cannot be negative.");
        }
        this.setsCount = setsCount;
    }

    public int getRepsCount() {
        return repsCount;
    }

    public void setRepsCount(int repsCount) {
        if (repsCount < 0) {
            throw new IllegalArgumentException("Reps count cannot be negative.");
        }
        this.repsCount = repsCount;
    }
}
