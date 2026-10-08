package com.fitpulse;

import com.fitpulse.dao.GenericDAO;
import com.fitpulse.model.*;
import com.fitpulse.service.FitnessCollectionManager;
import com.fitpulse.util.CollectionsUtil;
import com.fitpulse.util.GenericCache;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Phase 3 Collections & Generics Rubric Verification Test (6 Marks).
 *
 * Demonstrates and validates all required criteria:
 * 1. List: ArrayList<Workout> with iteration, search, and update operations (1.5 marks)
 * 2. Set: HashSet<Challenge> with uniqueness and duplicate elimination (1.5 marks)
 * 3. Map: HashMap<Integer, User> with key-value lookup, entrySet iteration, and aggregation (1.5 marks)
 * 4. Generics: GenericDAO<T, ID>, GenericCache<K, V>, bounded generic methods <T extends Workout> (1.5 marks)
 */
public class Phase3CollectionsVerificationTest {

    private static int totalPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  FitPulse Fitness Tracking System — Phase 3 Collections & Generics");
        System.out.println("======================================================================\n");

        test1_ListCollection();
        test2_SetCollection();
        test3_MapCollection();
        test4_GenericsAndGenericDAO();

        System.out.println("\n======================================================================");
        System.out.printf("  PHASE 3 SUMMARY: %d / %d TESTS PASSED SUCCESSFULLY!%n", totalPassed, totalTests);
        if (totalPassed == totalTests) {
            System.out.println("  COLLECTIONS & GENERICS RUBRIC: 6/6 MARKS FULLY DEMONSTRATED!");
        } else {
            System.err.println("  COLLECTIONS & GENERICS RUBRIC: VERIFICATION FAILED!");
        }
        System.out.println("======================================================================");

        if (totalPassed != totalTests) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String explanation) {
        totalTests++;
        if (condition) {
            totalPassed++;
            System.out.printf("  [PASS] %-42s | %s%n", testName, explanation);
        } else {
            System.err.printf("  [FAIL] %-42s | %s%n", testName, explanation);
        }
    }

    /**
     * Rubric Criterion 1: List (ArrayList<Workout>)
     * Demonstrates insertion order, search/filtering, in-place updates, and element removal.
     */
    private static void test1_ListCollection() {
        System.out.println("[Criterion 1: List — ArrayList<Workout>]");

        FitnessCollectionManager manager = new FitnessCollectionManager();

        Workout w1 = new CardioWorkout(1, 10, LocalDateTime.now().minusDays(2), 30, 4.5, 140, "Warmup Jog");
        Workout w2 = new StrengthWorkout(2, 10, LocalDateTime.now().minusDays(1), 50, 3200.0, 12, 10, "Chest Press");
        Workout w3 = new CardioWorkout(3, 10, LocalDateTime.now(), 60, 8.0, 155, "Endurance Run");

        manager.addWorkout(w1);
        manager.addWorkout(w2);
        manager.addWorkout(w3);

        // 1. Verification of List ordering & size
        List<Workout> workouts = manager.getAllWorkouts();
        assertTrue("ArrayList Storage & Order",
                workouts.size() == 3 && workouts.get(0).getId() == 1 && workouts.get(2).getId() == 3,
                "ArrayList preserves chronological insertion order with 3 workout entries.");

        // 2. Search / Filtering operation
        List<Workout> highBurnWorkouts = manager.searchWorkoutsByMinCalories(300.0);
        assertTrue("ArrayList Search / Filter",
                highBurnWorkouts.size() == 2,
                String.format("Found %d workouts with burn >= 300 kcal using predicate search.", highBurnWorkouts.size()));

        // 3. In-place Update operation
        double oldCal = w1.getCaloriesBurned();
        boolean updated = manager.updateWorkoutDuration(1, 55, "Extended tempo run");
        double newCal = manager.getAllWorkouts().get(0).getCaloriesBurned();
        assertTrue("ArrayList In-Place Update",
                updated && newCal > oldCal && manager.getAllWorkouts().get(0).getDurationMinutes() == 55,
                String.format("Workout #1 duration updated from 30 to 55 mins (burn: %.1f -> %.1f kcal).", oldCal, newCal));

        // 4. Removal operation
        boolean removed = manager.removeWorkoutById(2);
        assertTrue("ArrayList Element Removal",
                removed && manager.getAllWorkouts().size() == 2,
                "Successfully removed Workout #2; list dynamically shrunk to 2 elements.");
    }

    /**
     * Rubric Criterion 2: Set (HashSet<Challenge>)
     * Demonstrates unique membership, duplicate rejection via equals/hashCode, and category iteration.
     */
    private static void test2_SetCollection() {
        System.out.println("\n[Criterion 2: Set — HashSet<Challenge>]");

        FitnessCollectionManager manager = new FitnessCollectionManager();

        Challenge c1 = new Challenge(101, "30-Day Cardio Blast", "CARDIO", 30, 500);
        Challenge c2 = new Challenge(102, "100k Tonnage Club", "STRENGTH", 14, 400);
        Challenge c3 = new Challenge(103, "5k Daily Step Quest", "CARDIO", 7, 200);

        // Enrolling distinct challenges
        manager.enrollChallenge(c1);
        manager.enrollChallenge(c2);
        manager.enrollChallenge(c3);

        assertTrue("HashSet Initial Population",
                manager.getEnrolledChallenges().size() == 3,
                "HashSet successfully enrolled 3 distinct fitness challenges.");

        // Attempting to enroll a duplicate challenge (same ID and Name)
        Challenge duplicateC1 = new Challenge(101, "30-Day Cardio Blast", "CARDIO", 30, 500);
        boolean duplicateAccepted = manager.enrollChallenge(duplicateC1);

        assertTrue("HashSet Duplicate Rejection",
                !duplicateAccepted && manager.getEnrolledChallenges().size() == 3,
                "Duplicate challenge rejected by HashSet using overridden equals() and hashCode().");

        // Filter / Sub-set extraction by category
        Set<Challenge> cardioChallenges = manager.getChallengesByCategory("CARDIO");
        assertTrue("HashSet Category Iteration",
                cardioChallenges.size() == 2,
                String.format("Iterated through set and extracted %d 'CARDIO' challenges.", cardioChallenges.size()));
    }

    /**
     * Rubric Criterion 3: Map (HashMap<Integer, User>)
     * Demonstrates O(1) key lookup, updating entries, Map.Entry iteration, and aggregation.
     */
    private static void test3_MapCollection() {
        System.out.println("\n[Criterion 3: Map — HashMap<Integer, User>]");

        FitnessCollectionManager manager = new FitnessCollectionManager();

        User u1 = new Member(1, "Sarah Connor", "sarah@fitpulse.com", "hash1", 64.0, 168.0, "INTERMEDIATE", 3);
        User u2 = new Member(2, "John Doe", "john@fitpulse.com", "hash2", 78.0, 180.0, "BEGINNER", 3);
        User u3 = new Trainer(3, "Alex Mercer", "alex@fitpulse.com", "hash3", "HIIT", "NASM");

        manager.registerUser(u1);
        manager.registerUser(u2);
        manager.registerUser(u3);

        // 1. O(1) Key-based retrieval
        Optional<User> found = manager.findUserById(2);
        assertTrue("HashMap O(1) Key Lookup",
                found.isPresent() && found.get().getName().equals("John Doe"),
                "Instantly retrieved User #2 ('John Doe') by primary key in O(1) time.");

        // 2. Map entry update
        boolean emailUpdated = manager.updateUserEmail(1, "sarah.connor.updated@fitpulse.com");
        assertTrue("HashMap Entry In-Place Update",
                emailUpdated && manager.findUserById(1).get().getEmail().equals("sarah.connor.updated@fitpulse.com"),
                "User #1 email updated directly in memory via Map lookup.");

        // 3. Map.Entry iteration and grouping count
        Map<String, Integer> counts = manager.countUsersByRole();
        assertTrue("Map.Entry Iteration & Aggregation",
                counts.getOrDefault("MEMBER", 0) == 2 && counts.getOrDefault("TRAINER", 0) == 1,
                String.format("Iterated over entrySet(): %d Members, %d Trainers.",
                        counts.get("MEMBER"), counts.get("TRAINER")));

        // 4. Sorted Map (TreeMap) demonstration: daily calorie burns
        Map<LocalDate, Double> calendarBurnMap = new TreeMap<>();
        LocalDate day1 = LocalDate.of(2026, 9, 25);
        LocalDate day2 = LocalDate.of(2026, 9, 26);
        LocalDate day3 = LocalDate.of(2026, 9, 27);

        calendarBurnMap.put(day2, 450.0);
        calendarBurnMap.put(day1, 380.0);
        calendarBurnMap.put(day3, 620.0);

        // TreeMap automatically sorts by natural order of LocalDate keys
        List<LocalDate> sortedDates = new ArrayList<>(calendarBurnMap.keySet());
        assertTrue("TreeMap Natural Key Sorting",
                sortedDates.get(0).equals(day1) && sortedDates.get(2).equals(day3),
                "TreeMap sorted dates chronologically across keys (2026-09-25 -> 2026-09-27).");
    }

    /**
     * Rubric Criterion 4: Generics
     * GenericDAO<T, ID>, GenericCache<K, V>, and generic methods.
     */
    private static void test4_GenericsAndGenericDAO() {
        System.out.println("\n[Criterion 4: Generics — GenericDAO, GenericCache & Algorithms]");

        // 1. GenericDAO Interface Verification
        assertTrue("Generic Interface: GenericDAO<T, ID>",
                GenericDAO.class.isInterface() && GenericDAO.class.getTypeParameters().length == 2,
                "GenericDAO declares 2 formal type parameters: <T, ID> for reusable CRUD.");

        // 2. Generic In-Memory Cache (GenericCache<K, V>)
        GenericCache<String, Member> memberCache = new GenericCache<>(10_000);
        Member m = new Member(10, "Lisa Ray", "lisa@fitpulse.com", "hash", 58.0, 165.0, "ADVANCED", null);
        memberCache.put("user_10", m);

        assertTrue("GenericCache<K, V> Put & Get",
                memberCache.containsKey("user_10") && memberCache.get("user_10").get().getName().equals("Lisa Ray"),
                "GenericCache stored and retrieved typed entity with zero type casting.");

        // 3. Generic Indexing Algorithm (CollectionsUtil.indexBy)
        List<Workout> workoutList = Arrays.asList(
                new CardioWorkout(1, 5, LocalDateTime.now(), 30, 5.0, 140, "A"),
                new StrengthWorkout(2, 5, LocalDateTime.now(), 45, 2000.0, 10, 10, "B")
        );
        Map<Integer, Workout> indexedMap = CollectionsUtil.indexBy(workoutList, Workout::getId);
        assertTrue("Generic Method: CollectionsUtil.indexBy",
                indexedMap.size() == 2 && indexedMap.get(1).getId() == 1,
                "Generic method indexBy<K, T> successfully indexed workouts by Integer ID.");

        // 4. Bounded Generic Algorithm: findMax with Comparable
        Optional<Workout> maxBurn = CollectionsUtil.findMax(workoutList, Workout::getCaloriesBurned);
        assertTrue("Bounded Generic Method: findMax",
                maxBurn.isPresent() && maxBurn.get().getCaloriesBurned() > 0,
                String.format("Generic findMax successfully determined highest burn: %.1f kcal.",
                        maxBurn.get().getCaloriesBurned()));

        // 5. Bounded Generic Filter by Type (<T extends Workout>)
        List<CardioWorkout> cardioOnly = CollectionsUtil.filter(
                workoutList, w -> w instanceof CardioWorkout
        ).stream().map(w -> (CardioWorkout) w).toList();

        assertTrue("Bounded Type Filter <T extends Workout>",
                cardioOnly.size() == 1,
                "Filtered polymorphic collection returning strictly CardioWorkout subtype.");
    }
}
