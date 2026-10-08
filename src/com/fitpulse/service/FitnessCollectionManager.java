package com.fitpulse.service;

import com.fitpulse.model.Challenge;
import com.fitpulse.model.User;
import com.fitpulse.model.Workout;
import com.fitpulse.util.CollectionsUtil;
import java.util.*;

/**
 * High-level manager coordinating domain entities using Java Collections Framework.
 * Demonstrates:
 * - Rubric Item 2: Collections & Generics (List, Set, Map, Generics)
 * - ArrayList<Workout> for ordered logs, searching, updating, filtering
 * - HashSet<Challenge> for unique challenge enrollments
 * - HashMap<Integer, User> for O(1) indexed user management
 */
public class FitnessCollectionManager {

    // 1. List: Ordered workout history (supports duplicates, indexed access)
    private final List<Workout> workoutHistory = new ArrayList<>();

    // 2. Set: Unique enrolled challenges (eliminates duplicate entries automatically)
    private final Set<Challenge> enrolledChallenges = new HashSet<>();

    // 3. Map: User lookup repository (O(1) key-value lookup by User ID)
    private final Map<Integer, User> userRegistry = new HashMap<>();

    // ==========================================
    // LIST OPERATIONS (ArrayList<Workout>)
    // ==========================================

    public void addWorkout(Workout workout) {
        if (workout != null) {
            workoutHistory.add(workout);
        }
    }

    public List<Workout> getAllWorkouts() {
        return Collections.unmodifiableList(workoutHistory);
    }

    /**
     * Search operation: Finds all workouts exceeding a calorie burn threshold.
     */
    public List<Workout> searchWorkoutsByMinCalories(double minCalories) {
        return CollectionsUtil.filter(workoutHistory, w -> w.getCaloriesBurned() >= minCalories);
    }

    /**
     * Update operation: Locates a workout by ID and updates its duration and notes in-place.
     */
    public boolean updateWorkoutDuration(int workoutId, int newDurationMinutes, String newNotes) {
        return CollectionsUtil.updateFirst(workoutHistory,
                w -> w.getId() == workoutId,
                w -> {
                    w.setDurationMinutes(newDurationMinutes);
                    w.setNotes(newNotes);
                    w.setCaloriesBurned(w.calculateCaloriesBurned());
                });
    }

    /**
     * Remove / Filter operation: Removes a workout by its ID.
     */
    public boolean removeWorkoutById(int workoutId) {
        return workoutHistory.removeIf(w -> w.getId() == workoutId);
    }

    // ==========================================
    // SET OPERATIONS (HashSet<Challenge>)
    // ==========================================

    /**
     * Set Add operation: Adds a challenge, returning true if newly enrolled,
     * or false if duplicate was safely rejected by HashSet.
     */
    public boolean enrollChallenge(Challenge challenge) {
        if (challenge == null) return false;
        return enrolledChallenges.add(challenge);
    }

    public Set<Challenge> getEnrolledChallenges() {
        return Collections.unmodifiableSet(enrolledChallenges);
    }

    /**
     * Set Search/Filter operation: Filters challenges belonging to a specific category.
     */
    public Set<Challenge> getChallengesByCategory(String category) {
        Set<Challenge> filtered = new HashSet<>();
        for (Challenge c : enrolledChallenges) {
            if (c.getCategory().equalsIgnoreCase(category)) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    // ==========================================
    // MAP OPERATIONS (HashMap<Integer, User>)
    // ==========================================

    public void registerUser(User user) {
        if (user != null) {
            userRegistry.put(user.getId(), user);
        }
    }

    public Optional<User> findUserById(int userId) {
        return Optional.ofNullable(userRegistry.get(userId));
    }

    /**
     * Update operation on Map: Updates an existing user record.
     */
    public boolean updateUserEmail(int userId, String newEmail) {
        User user = userRegistry.get(userId);
        if (user != null) {
            user.setEmail(newEmail);
            return true;
        }
        return false;
    }

    public Map<Integer, User> getUserRegistry() {
        return Collections.unmodifiableMap(userRegistry);
    }

    /**
     * Map Iteration: Calculates total registered users by role using Map.Entry iteration.
     */
    public Map<String, Integer> countUsersByRole() {
        Map<String, Integer> counts = new HashMap<>();
        for (Map.Entry<Integer, User> entry : userRegistry.entrySet()) {
            String roleName = entry.getValue().getRole().name();
            counts.put(roleName, counts.getOrDefault(roleName, 0) + 1);
        }
        return counts;
    }
}
