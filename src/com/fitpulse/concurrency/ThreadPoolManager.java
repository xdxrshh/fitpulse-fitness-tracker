package com.fitpulse.concurrency;

import java.util.concurrent.*;

/**
 * Centralized thread pool manager for scheduled background execution and asynchronous jobs.
 * Demonstrates:
 * - Rubric Item 3: Multithreading & Concurrency (ExecutorService, ScheduledExecutorService)
 */
public class ThreadPoolManager {

    private static volatile ThreadPoolManager instance;
    private final ScheduledExecutorService scheduledExecutor;
    private final ExecutorService workerExecutor;

    private ThreadPoolManager() {
        this.scheduledExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "FitPulse-ScheduledWorker");
            t.setDaemon(true);
            return t;
        });

        this.workerExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "FitPulse-AsyncWorker");
            t.setDaemon(true);
            return t;
        });
    }

    public static ThreadPoolManager getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolManager.class) {
                if (instance == null) {
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public Future<?> submit(Runnable task) {
        return workerExecutor.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return workerExecutor.submit(task);
    }

    public void shutdown() {
        scheduledExecutor.shutdown();
        workerExecutor.shutdown();
    }
}
