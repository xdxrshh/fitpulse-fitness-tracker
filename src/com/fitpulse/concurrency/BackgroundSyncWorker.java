package com.fitpulse.concurrency;

import com.fitpulse.model.FitnessGoal;
import com.fitpulse.model.MealLog;
import com.fitpulse.model.Workout;
import com.fitpulse.service.GoalTrackingService;
import com.fitpulse.service.NutritionService;
import com.fitpulse.service.WorkoutService;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/**
 * SwingWorker background task fetching database metrics asynchronously without freezing the Swing EDT.
 * Demonstrates:
 * - Rubric Item 3: Multithreading & Concurrency (SwingWorker, background threads)
 */
public class BackgroundSyncWorker extends SwingWorker<BackgroundSyncWorker.DashboardSnapshot, Void> {

    public static class DashboardSnapshot {
        public final List<Workout> workouts;
        public final List<FitnessGoal> goals;
        public final List<MealLog> todayMeals;
        public final double totalCaloriesBurned;
        public final int totalWorkouts;
        public final int todayCaloriesIntake;

        public DashboardSnapshot(List<Workout> workouts, List<FitnessGoal> goals, List<MealLog> todayMeals,
                                 double totalCaloriesBurned, int totalWorkouts, int todayCaloriesIntake) {
            this.workouts = workouts;
            this.goals = goals;
            this.todayMeals = todayMeals;
            this.totalCaloriesBurned = totalCaloriesBurned;
            this.totalWorkouts = totalWorkouts;
            this.todayCaloriesIntake = todayCaloriesIntake;
        }
    }

    private final int userId;
    private final WorkoutService workoutService;
    private final GoalTrackingService goalService;
    private final NutritionService nutritionService;
    private final Consumer<DashboardSnapshot> onSuccess;
    private final Consumer<Exception> onFailure;

    public BackgroundSyncWorker(int userId,
                                WorkoutService workoutService,
                                GoalTrackingService goalService,
                                NutritionService nutritionService,
                                Consumer<DashboardSnapshot> onSuccess,
                                Consumer<Exception> onFailure) {
        this.userId = userId;
        this.workoutService = workoutService;
        this.goalService = goalService;
        this.nutritionService = nutritionService;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    /**
     * Executes heavy database queries on a background worker thread.
     */
    @Override
    protected DashboardSnapshot doInBackground() throws Exception {
        List<Workout> workouts = workoutService.getUserWorkouts(userId);
        List<FitnessGoal> goals = goalService.getUserGoals(userId);
        List<MealLog> meals = nutritionService.getUserMealsForToday(userId);
        double totalCalories = workoutService.getTotalCaloriesBurned(userId);
        int totalWorkouts = workoutService.getTotalWorkoutsCount(userId);
        int intake = nutritionService.getTodayCalorieIntake(userId);

        return new DashboardSnapshot(workouts, goals, meals, totalCalories, totalWorkouts, intake);
    }

    /**
     * Invoked safely on the Swing Event Dispatch Thread (EDT) once background query finishes.
     */
    @Override
    protected void done() {
        try {
            DashboardSnapshot snapshot = get();
            if (onSuccess != null) {
                onSuccess.accept(snapshot);
            }
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
        }
    }
}
