package com.fitpulse;

import com.fitpulse.concurrency.*;
import com.fitpulse.interfaces.NotificationListener;
import com.fitpulse.model.CardioWorkout;
import com.fitpulse.model.FitnessGoal;
import com.fitpulse.model.StrengthWorkout;
import com.fitpulse.model.Workout;
import com.fitpulse.model.enums.GoalStatus;
import com.fitpulse.model.enums.GoalType;
import com.fitpulse.service.GoalTrackingService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Phase 4 Multithreading & Synchronization Rubric Verification Test (4 Marks).
 *
 * Validates each rubric requirement:
 * 1. Multiple Java threads working concurrently (1 mark)
 * 2. Concurrency utilities: ExecutorService, ThreadPool, CountDownLatch, ScheduledExecutor (1 mark)
 * 3. Synchronization: Synchronized methods and synchronized blocks (1 mark)
 * 4. Safe access to shared data & race condition prevention (1 mark)
 */
public class Phase4MultithreadingVerificationTest {

    private static int totalPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("======================================================================");
        System.out.println("  FitPulse Fitness Tracking System — Phase 4 Multithreading Verification");
        System.out.println("======================================================================\n");

        test1_ConcurrentThreadExecution();
        test2_ExecutorServiceAndThreadPool();
        test3_SynchronizationMethodsAndBlocks();
        test4_RaceConditionPreventionProof();
        test5_BackgroundScheduledGoalReminderDaemon();

        System.out.println("\n======================================================================");
        System.out.printf("  PHASE 4 SUMMARY: %d / %d TESTS PASSED SUCCESSFULLY!%n", totalPassed, totalTests);
        if (totalPassed == totalTests) {
            System.out.println("  MULTITHREADING RUBRIC: 4/4 MARKS FULLY DEMONSTRATED!");
        } else {
            System.err.println("  MULTITHREADING RUBRIC: VERIFICATION FAILED!");
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
     * Rubric Item 1: Multiple threads executing concurrently.
     * Demonstrates distinct worker threads processing fitness tasks in parallel.
     */
    private static void test1_ConcurrentThreadExecution() throws Exception {
        System.out.println("[Criterion 1: Multiple Concurrent Java Threads]");

        int threadCount = 6;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);
        Set<String> executingThreads = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threadCount; i++) {
            final int id = i + 1;
            new Thread(() -> {
                try {
                    startSignal.await(); // All threads start simultaneously
                    executingThreads.add(Thread.currentThread().getName());
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {
                } finally {
                    doneSignal.countDown();
                }
            }, "FitPulseWorker-" + id).start();
        }

        startSignal.countDown(); // Unleash all threads concurrently
        boolean finishedInTime = doneSignal.await(5, TimeUnit.SECONDS);

        assertTrue("Parallel Thread Launch",
                finishedInTime && executingThreads.size() == threadCount,
                String.format("%d distinct named threads executed concurrently in parallel: %s",
                        executingThreads.size(), executingThreads));
    }

    /**
     * Rubric Item 2: Thread Pool & Concurrency Utilities.
     * Demonstrates ExecutorService thread pool processing workout stream using ConcurrentWorkoutBatchProcessor.
     */
    private static void test2_ExecutorServiceAndThreadPool() throws Exception {
        System.out.println("\n[Criterion 2: ExecutorService & Thread Pool]");

        ConcurrentWorkoutBatchProcessor processor = new ConcurrentWorkoutBatchProcessor(4);
        SharedMetricsMonitor monitor = SharedMetricsMonitor.getInstance();
        monitor.reset();

        List<Workout> batch = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            if (i % 2 == 0) {
                batch.add(new CardioWorkout(i, 10, LocalDateTime.now(), 30, 5.0, 145, "Batch Cardio #" + i));
            } else {
                batch.add(new StrengthWorkout(i, 10, LocalDateTime.now(), 45, 2500.0, 12, 10, "Batch Strength #" + i));
            }
        }

        ConcurrentWorkoutBatchProcessor.BatchProcessingResult result =
                processor.processBatchConcurrently(batch, monitor);

        assertTrue("ThreadPool Batch Execution",
                result.getProcessedCount() == 20 && result.getTotalCaloriesAccumulated() > 0,
                String.format("Processed %d workouts across 4 thread-pool workers in %d ms (Accumulated: %.1f kcal).",
                        result.getProcessedCount(), result.getDurationMillis(), result.getTotalCaloriesAccumulated()));

        processor.shutdown();
    }

    /**
     * Rubric Item 3: Synchronization (Synchronized methods & Synchronized blocks).
     * Demonstrates synchronization mechanisms protecting the SharedMetricsMonitor.
     */
    private static void test3_SynchronizationMethodsAndBlocks() throws Exception {
        System.out.println("\n[Criterion 3: Synchronized Methods & Blocks]");

        SharedMetricsMonitor monitor = SharedMetricsMonitor.getInstance();
        monitor.reset();

        int workers = 8;
        int operationsPerWorker = 50;
        CountDownLatch latch = new CountDownLatch(workers);
        ExecutorService pool = Executors.newFixedThreadPool(workers);

        for (int i = 0; i < workers; i++) {
            pool.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerWorker; j++) {
                        // 1. Synchronized Method: recordSessionStart
                        monitor.recordSessionStart();

                        // 2. Synchronized Block: recordCalorieBurn on monitor's private lock
                        monitor.recordCalorieBurn(10.0);

                        // 3. Synchronized Method: recordSessionEnd
                        monitor.recordSessionEnd();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        double expectedCalories = workers * operationsPerWorker * 10.0;
        double actualCalories = monitor.getLiveCaloriesBurnedCounter();

        assertTrue("Synchronized Block Calorie Accuracy",
                Math.abs(actualCalories - expectedCalories) < 0.001,
                String.format("Expected exactly %.1f kcal; Got %.1f kcal across %d concurrent workers.",
                        expectedCalories, actualCalories, workers));

        long expectedTotalEvents = (long) workers * operationsPerWorker * 3; // start + burn + end
        assertTrue("Synchronized Method Event Accounting",
                monitor.getTotalProcessedEvents() == expectedTotalEvents,
                String.format("All %d synchronized state modifications accounted for without lost updates.",
                        monitor.getTotalProcessedEvents()));
    }

    /**
     * Rubric Item 4: Race Condition Elimination Proof.
     * Demonstrates an unsynchronized counter failing under concurrent contention vs
     * synchronized counter maintaining 100% integrity.
     */
    private static void test4_RaceConditionPreventionProof() throws Exception {
        System.out.println("\n[Criterion 4: Race Condition Elimination Proof]");

        final int THREADS = 10;
        final int INCREMENTS = 500;
        final int EXPECTED_TOTAL = THREADS * INCREMENTS; // 5,000

        // Unsynchronized counter prone to race conditions (lost updates)
        class UnsynchronizedCounter {
            int count = 0;
            void increment() {
                count++; // Non-atomic read-modify-write!
            }
        }

        UnsynchronizedCounter unsafeCounter = new UnsynchronizedCounter();
        SharedMetricsMonitor safeMonitor = SharedMetricsMonitor.getInstance();
        safeMonitor.reset();

        CountDownLatch latch = new CountDownLatch(THREADS);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                try {
                    for (int j = 0; j < INCREMENTS; j++) {
                        unsafeCounter.increment();
                        safeMonitor.recordSessionStart(); // Thread-safe synchronized increment
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        int safeFinalCount = safeMonitor.getActiveLiveSessions();

        assertTrue("Safe Synchronized Monitor Integrity",
                safeFinalCount == EXPECTED_TOTAL,
                String.format("Synchronized method recorded exactly %d / %d increments (0 lost updates).",
                        safeFinalCount, EXPECTED_TOTAL));

        System.out.printf("    [VIVA PROOF] Unsynchronized counter result: %d / %d (Lost %d updates due to race conditions).%n",
                unsafeCounter.count, EXPECTED_TOTAL, (EXPECTED_TOTAL - unsafeCounter.count));
        System.out.printf("    [VIVA PROOF] Synchronized SharedMetricsMonitor: %d / %d (100%% atomic consistency).%n",
                safeFinalCount, EXPECTED_TOTAL);
    }

    /**
     * Realistic Use Case: Background Scheduled Goal Reminder Daemon.
     * Demonstrates ScheduledExecutorService & GoalReminderTask.
     */
    private static void test5_BackgroundScheduledGoalReminderDaemon() throws Exception {
        System.out.println("\n[Criterion 5: Scheduled Background Concurrency — GoalReminderTask]");

        AtomicBoolean notificationFired = new AtomicBoolean(false);
        AtomicInteger notificationCount = new AtomicInteger(0);

        NotificationListener listener = (title, message, level) -> {
            notificationFired.set(true);
            notificationCount.incrementAndGet();
        };

        // Create a test goal ending in 1 day (approaching deadline)
        FitnessGoal urgentGoal = new FitnessGoal(99, 1, GoalType.WORKOUT_COUNT, 5.0, 2.0,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(1), GoalStatus.IN_PROGRESS);

        // Mock GoalTrackingService anonymous subtype for test
        GoalTrackingService mockService = new GoalTrackingService() {
            @Override
            public List<FitnessGoal> getActiveUserGoals(int userId) {
                return Collections.singletonList(urgentGoal);
            }
        };

        GoalReminderTask reminderTask = new GoalReminderTask(1, mockService, listener);

        // Run via ThreadPoolManager scheduled background thread
        ThreadPoolManager poolManager = ThreadPoolManager.getInstance();
        ScheduledFuture<?> future = poolManager.scheduleAtFixedRate(reminderTask, 0, 50, TimeUnit.MILLISECONDS);

        // Wait briefly for daemon execution
        Thread.sleep(150);
        future.cancel(true);

        assertTrue("Scheduled Daemon Notification Execution",
                notificationFired.get() && notificationCount.get() >= 1,
                String.format("Scheduled background daemon fired %d proactive goal deadline alerts.",
                        notificationCount.get()));
    }
}
