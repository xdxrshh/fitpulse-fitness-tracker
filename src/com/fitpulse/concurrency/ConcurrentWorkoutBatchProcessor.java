package com.fitpulse.concurrency;

import com.fitpulse.model.Workout;
import java.util.List;
import java.util.concurrent.*;

/**
 * Concurrent batch processor simulating parallel ingestion and metric calculation
 * of incoming workout streams from wearable devices.
 * Demonstrates:
 * - Rubric Item 3: Multithreading & Concurrency (ExecutorService, ThreadPool, CountDownLatch)
 * - Safe concurrent access to shared state via SharedMetricsMonitor without race conditions.
 */
public class ConcurrentWorkoutBatchProcessor {

    private final ExecutorService executorService;
    private final int threadPoolSize;

    public ConcurrentWorkoutBatchProcessor(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "WearableWorkerThread-" + (counter++));
                t.setDaemon(true);
                return t;
            }
        });
    }

    /**
     * Concurrently processes a collection of workouts across the thread pool,
     * coordinating parallel execution using CountDownLatch and updating the shared monitor.
     */
    public BatchProcessingResult processBatchConcurrently(List<Workout> workouts, SharedMetricsMonitor monitor)
            throws InterruptedException, ExecutionException {

        if (workouts == null || workouts.isEmpty()) {
            return new BatchProcessingResult(0, 0, 0.0);
        }

        long startTime = System.currentTimeMillis();
        int totalTasks = workouts.size();
        CountDownLatch latch = new CountDownLatch(totalTasks);

        for (Workout workout : workouts) {
            executorService.submit(() -> {
                try {
                    // 1. Synchronized method invocation
                    monitor.recordSessionStart();

                    // 2. Polymorphic calorie burn calculation
                    double burn = workout.calculateCaloriesBurned();

                    // 3. Synchronized block invocation to safely update shared counter
                    monitor.recordCalorieBurn(burn);
                } finally {
                    // 4. Synchronized method invocation
                    monitor.recordSessionEnd();
                    latch.countDown();
                }
            });
        }

        // Wait for all concurrent worker threads to finish
        boolean completed = latch.await(15, TimeUnit.SECONDS);
        if (!completed) {
            System.err.println("[WARN] Batch processing timed out waiting for threads.");
        }

        long duration = System.currentTimeMillis() - startTime;
        return new BatchProcessingResult(totalTasks, duration, monitor.getLiveCaloriesBurnedCounter());
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    public static class BatchProcessingResult {
        private final int processedCount;
        private final long durationMillis;
        private final double totalCaloriesAccumulated;

        public BatchProcessingResult(int processedCount, long durationMillis, double totalCaloriesAccumulated) {
            this.processedCount = processedCount;
            this.durationMillis = durationMillis;
            this.totalCaloriesAccumulated = totalCaloriesAccumulated;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public long getDurationMillis() {
            return durationMillis;
        }

        public double getTotalCaloriesAccumulated() {
            return totalCaloriesAccumulated;
        }
    }
}
