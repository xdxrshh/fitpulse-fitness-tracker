package com.fitpulse.service;

import com.fitpulse.dao.MealDAO;
import com.fitpulse.dao.impl.MealDAOImpl;
import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.exception.ValidationException;
import com.fitpulse.model.MealLog;
import com.fitpulse.model.enums.MealType;
import com.fitpulse.util.ValidationUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service managing meal logging, calorie balances, and macronutrient distributions.
 * Demonstrates:
 * - Rubric Item 2: Collections (Map for macro breakdown, List for meals)
 */
public class NutritionService {
    private final MealDAO mealDAO;

    public NutritionService() {
        this(new MealDAOImpl());
    }

    public NutritionService(MealDAO mealDAO) {
        this.mealDAO = mealDAO;
    }

    public MealLog logMeal(int userId, String mealName, MealType type,
                           int calories, double protein, double carbs, double fat, LocalDateTime loggedAt)
            throws ValidationException, DatabaseOperationException {

        ValidationUtil.requireNonEmpty(mealName, "Meal Name");
        ValidationUtil.requirePositive(calories, "Calories");
        ValidationUtil.requireNonNegative(protein, "Protein");
        ValidationUtil.requireNonNegative(carbs, "Carbs");
        ValidationUtil.requireNonNegative(fat, "Fat");

        MealLog meal = new MealLog(0, userId, mealName, type, calories, protein, carbs, fat, loggedAt);
        return mealDAO.create(meal);
    }

    public List<MealLog> getUserMeals(int userId) throws DatabaseOperationException {
        return mealDAO.findByUserId(userId);
    }

    public List<MealLog> getUserMealsForToday(int userId) throws DatabaseOperationException {
        return mealDAO.findByUserIdAndDate(userId, LocalDate.now());
    }

    public int getTodayCalorieIntake(int userId) throws DatabaseOperationException {
        return mealDAO.getTotalCaloriesByDate(userId, LocalDate.now());
    }

    /**
     * Demonstrates Collections: Map<String, Double> for aggregating macros
     */
    public Map<String, Double> getTodayMacroBreakdown(int userId) throws DatabaseOperationException {
        List<MealLog> todayMeals = getUserMealsForToday(userId);
        Map<String, Double> macros = new HashMap<>();
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;

        for (MealLog m : todayMeals) {
            totalProtein += m.getProteinGrams();
            totalCarbs += m.getCarbsGrams();
            totalFat += m.getFatGrams();
        }

        macros.put("proteinGrams", totalProtein);
        macros.put("carbsGrams", totalCarbs);
        macros.put("fatGrams", totalFat);
        return macros;
    }

    public boolean deleteMeal(int mealId) throws DatabaseOperationException {
        return mealDAO.delete(mealId);
    }
}
