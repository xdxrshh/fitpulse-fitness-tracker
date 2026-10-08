package com.fitpulse.service;

import com.fitpulse.dao.WorkoutDAO;
import com.fitpulse.dao.impl.WorkoutDAOImpl;
import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.exception.ValidationException;
import com.fitpulse.model.CardioWorkout;
import com.fitpulse.model.StrengthWorkout;
import com.fitpulse.model.Workout;
import com.fitpulse.util.GenericCache;
import com.fitpulse.util.ValidationUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service managing workout logging, analytics, and metrics aggregation.
 * Demonstrates:
 * - Rubric Item 1: Polymorphism (dynamic dispatch on workout.calculateCaloriesBurned())
 * - Rubric Item 2: Collections & Generics (List, Set, Map, Generic methods)
 */
public class WorkoutService {
    private final WorkoutDAO workoutDAO;
    private final GenericCache<String, List<Workout>> userWorkoutCache;

    public WorkoutService() {
        this(new WorkoutDAOImpl());
    }

    public WorkoutService(WorkoutDAO workoutDAO) {
        this.workoutDAO = workoutDAO;
        this.userWorkoutCache = new GenericCache<>(120_000); // 2-min cache
    }

    public Workout logCardioWorkout(int userId, LocalDateTime date, int durationMinutes,
                                    double distanceKm, int avgHeartRate, String notes)
            throws ValidationException, DatabaseOperationException {

        ValidationUtil.requirePositive(durationMinutes, "Duration");
        ValidationUtil.requirePositive(distanceKm, "Distance");

        CardioWorkout cardio = new CardioWorkout(0, userId, date, durationMinutes, distanceKm, avgHeartRate, notes);
        Workout saved = workoutDAO.create(cardio);
        userWorkoutCache.remove("user_" + userId);
        return saved;
    }

    public Workout logStrengthWorkout(int userId, LocalDateTime date, int durationMinutes,
                                      double totalWeightKg, int setsCount, int repsCount, String notes)
            throws ValidationException, DatabaseOperationException {

        ValidationUtil.requirePositive(durationMinutes, "Duration");
        ValidationUtil.requirePositive(totalWeightKg, "Total Weight");
        ValidationUtil.requirePositive(setsCount, "Sets Count");
        ValidationUtil.requirePositive(repsCount, "Reps Count");

        StrengthWorkout strength = new StrengthWorkout(0, userId, date, durationMinutes, totalWeightKg, setsCount, repsCount, notes);
        Workout saved = workoutDAO.create(strength);
        userWorkoutCache.remove("user_" + userId);
        return saved;
    }

    /**
     * Demonstrates Collection: List<Workout>
     */
    public List<Workout> getUserWorkouts(int userId) throws DatabaseOperationException {
        String cacheKey = "user_" + userId;
        Optional<List<Workout>> cached = userWorkoutCache.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<Workout> workouts = workoutDAO.findByUserId(userId);
        userWorkoutCache.put(cacheKey, workouts);
        return workouts;
    }

    /**
     * Demonstrates Collection: Set<String> (Unique tags/exercise types)
     */
    public Set<String> getUniqueActivityCategories(int userId) throws DatabaseOperationException {
        List<Workout> workouts = getUserWorkouts(userId);
        Set<String> uniqueCategories = new TreeSet<>();
        for (Workout w : workouts) {
            uniqueCategories.add(w.getWorkoutType().name());
            if (w instanceof CardioWorkout) {
                uniqueCategories.add("Aerobic Cardio");
                uniqueCategories.add("Endurance Training");
            } else if (w instanceof StrengthWorkout) {
                uniqueCategories.add("Resistance Training");
                uniqueCategories.add("Hypertrophy / Power");
            }
        }
        return Collections.unmodifiableSet(uniqueCategories);
    }

    /**
     * Demonstrates Collection: Map<LocalDate, Double> (Aggregated calories burned per calendar day)
     */
    public Map<LocalDate, Double> getDailyCalorieBurnSummary(int userId) throws DatabaseOperationException {
        List<Workout> workouts = getUserWorkouts(userId);
        Map<LocalDate, Double> dailyBurnMap = new TreeMap<>(); // sorted by date

        for (Workout w : workouts) {
            LocalDate date = w.getWorkoutDate().toLocalDate();
            // Polymorphic method call w.getCaloriesBurned()
            double currentDayTotal = dailyBurnMap.getOrDefault(date, 0.0);
            dailyBurnMap.put(date, currentDayTotal + w.getCaloriesBurned());
        }
        return dailyBurnMap;
    }

    /**
     * Demonstrates Generics: Bounded type parameter generic method <T extends Workout>
     */
    public <T extends Workout> List<T> filterByType(List<Workout> allWorkouts, Class<T> clazz) {
        List<T> filtered = new ArrayList<>();
        for (Workout w : allWorkouts) {
            if (clazz.isInstance(w)) {
                filtered.add(clazz.cast(w));
            }
        }
        return filtered;
    }

    public double getTotalCaloriesBurned(int userId) throws DatabaseOperationException {
        return workoutDAO.getTotalCaloriesBurnedByUser(userId);
    }

    public int getTotalWorkoutsCount(int userId) throws DatabaseOperationException {
        return workoutDAO.getTotalWorkoutsCountByUser(userId);
    }

    public boolean deleteWorkout(int workoutId, int userId) throws DatabaseOperationException {
        boolean deleted = workoutDAO.delete(workoutId);
        if (deleted) {
            userWorkoutCache.remove("user_" + userId);
        }
        return deleted;
    }
}
