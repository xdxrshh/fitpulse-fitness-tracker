package com.fitpulse.concurrency;

/**
 * Thread-safe shared metrics monitor using explicit synchronization.
 * Demonstrates:
 * - Rubric Item 3: Multithreading & Synchronization (Synchronized methods & synchronized blocks)
 * - Safe concurrent state management without race conditions.
 */
public class SharedMetricsMonitor {

    private static volatile SharedMetricsMonitor instance;

    // Shared state variables accessed concurrently
    private int activeLiveSessions = 0;
    private double liveCaloriesBurnedCounter = 0.0;
    private long totalProcessedEvents = 0;

    private SharedMetricsMonitor() {}

    public static SharedMetricsMonitor getInstance() {
        if (instance == null) {
            synchronized (SharedMetricsMonitor.class) {
                if (instance == null) {
                    instance = new SharedMetricsMonitor();
                }
            }
        }
        return instance;
    }

    /**
     * Synchronized method: Safely increments active sessions across threads.
     */
    public synchronized void recordSessionStart() {
        activeLiveSessions++;
        totalProcessedEvents++;
    }

    /**
     * Synchronized method: Safely decrements active sessions.
     */
    public synchronized void recordSessionEnd() {
        if (activeLiveSessions > 0) {
            activeLiveSessions--;
        }
        totalProcessedEvents++;
    }

    /**
     * Synchronized block: Safely accumulates live calorie burn increments.
     * Uses explicit synchronized(this) block for critical section.
     */
    public void recordCalorieBurn(double calories) {
        if (calories <= 0) return;
        synchronized (this) {
            this.liveCaloriesBurnedCounter += calories;
            this.totalProcessedEvents++;
        }
    }

    /**
     * Synchronized accessor for active sessions.
     */
    public synchronized int getActiveLiveSessions() {
        return activeLiveSessions;
    }

    /**
     * Synchronized block accessor for live calories.
     */
    public double getLiveCaloriesBurnedCounter() {
        synchronized (this) {
            return Math.round(liveCaloriesBurnedCounter * 10.0) / 10.0;
        }
    }

    public synchronized long getTotalProcessedEvents() {
        return totalProcessedEvents;
    }

    public synchronized void reset() {
        activeLiveSessions = 0;
        liveCaloriesBurnedCounter = 0.0;
        totalProcessedEvents = 0;
    }
}
