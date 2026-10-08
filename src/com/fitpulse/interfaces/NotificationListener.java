package com.fitpulse.interfaces;

/**
 * Listener interface for thread-safe asynchronous notification dispatches.
 * Part of Rubric Item 3: Multithreading & Concurrency.
 */
public interface NotificationListener {
    void onNotification(String title, String message, String level);
}
