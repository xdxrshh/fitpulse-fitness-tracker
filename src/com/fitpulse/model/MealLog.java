package com.fitpulse.model;

import com.fitpulse.model.enums.MealType;
import java.time.LocalDateTime;

/**
 * Model class for tracking meals and macronutrient distributions.
 * Part of Rubric Item 4: Model Class.
 */
public class MealLog {
    private int id;
    private int userId;
    private String mealName;
    private MealType mealType;
    private int calories;
    private double proteinGrams;
    private double carbsGrams;
    private double fatGrams;
    private LocalDateTime loggedAt;

    public MealLog() {
        this.loggedAt = LocalDateTime.now();
        this.mealType = MealType.LUNCH;
    }

    public MealLog(int id, int userId, String mealName, MealType mealType,
                   int calories, double proteinGrams, double carbsGrams, double fatGrams,
                   LocalDateTime loggedAt) {
        this.id = id;
        this.userId = userId;
        this.mealName = mealName;
        this.mealType = mealType != null ? mealType : MealType.LUNCH;
        setCalories(calories);
        this.proteinGrams = Math.max(0.0, proteinGrams);
        this.carbsGrams = Math.max(0.0, carbsGrams);
        this.fatGrams = Math.max(0.0, fatGrams);
        this.loggedAt = loggedAt != null ? loggedAt : LocalDateTime.now();
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

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        if (calories < 0) {
            throw new IllegalArgumentException("Calories cannot be negative.");
        }
        this.calories = calories;
    }

    public double getProteinGrams() {
        return proteinGrams;
    }

    public void setProteinGrams(double proteinGrams) {
        this.proteinGrams = Math.max(0.0, proteinGrams);
    }

    public double getCarbsGrams() {
        return carbsGrams;
    }

    public void setCarbsGrams(double carbsGrams) {
        this.carbsGrams = Math.max(0.0, carbsGrams);
    }

    public double getFatGrams() {
        return fatGrams;
    }

    public void setFatGrams(double fatGrams) {
        this.fatGrams = Math.max(0.0, fatGrams);
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }
}
