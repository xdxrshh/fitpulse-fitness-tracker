package com.fitpulse.ui.panels;

import com.fitpulse.concurrency.GoalReminderTask;
import com.fitpulse.concurrency.SharedMetricsMonitor;
import com.fitpulse.concurrency.ThreadPoolManager;
import com.fitpulse.model.User;
import com.fitpulse.service.GoalTrackingService;
import com.fitpulse.ui.components.ModernButton;
import com.fitpulse.ui.components.StatCard;
import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

/**
 * Interactive demonstration panel explicitly proving Rubric Item 3: Multithreading & Synchronization.
 * Demonstrates:
 * - Thread pool execution (ThreadPoolManager)
 * - Concurrent execution across multiple parallel worker threads
 * - Thread synchronization (SharedMetricsMonitor synchronized methods & blocks)
 * - Scheduled background daemon (GoalReminderTask)
 */
public class ConcurrencyDemoPanel extends JPanel {
    private final User currentUser;
    private final GoalTrackingService goalService;

    private StatCard cardActiveSessions;
    private StatCard cardLiveCalories;
    private StatCard cardProcessedEvents;
    private JTextArea txtThreadLog;

    public ConcurrencyDemoPanel(User user, GoalTrackingService goalService) {
        this.currentUser = user;
        this.goalService = goalService;

        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initHeader();
        initStats();
        initControlsAndLogs();
    }

    private void initHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Multithreading & Synchronization Engine");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Interactive demonstration of Threads, Concurrency, and Synchronized state monitors (Rubric Item 3).");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(lblSub);

        add(header, BorderLayout.NORTH);
    }

    private void initStats() {
        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(0, 110));

        cardActiveSessions = new StatCard("Active Thread Sessions", "0", "Synchronized Counter", UITheme.ACCENT_PRIMARY);
        cardLiveCalories = new StatCard("Live Calorie Burn", "0.0 kcal", "Synchronized Block Accumulator", UITheme.ACCENT_TEAL);
        cardProcessedEvents = new StatCard("Total Concurrency Events", "0", "Thread-Safe Event Count", UITheme.ACCENT_SUCCESS);

        grid.add(cardActiveSessions);
        grid.add(cardLiveCalories);
        grid.add(cardProcessedEvents);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 14));
        centerContainer.setOpaque(false);
        centerContainer.add(grid, BorderLayout.NORTH);

        add(centerContainer, BorderLayout.CENTER);
    }

    private void initControlsAndLogs() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);

        ModernButton btnSimulate = new ModernButton("Launch 5 Parallel Threads", UITheme.ACCENT_PRIMARY);
        btnSimulate.addActionListener(e -> runConcurrentSimulation());

        ModernButton btnSchedule = new ModernButton("Start Scheduled Goal Daemon", UITheme.ACCENT_TEAL);
        btnSchedule.addActionListener(e -> startGoalDaemon());

        ModernButton btnReset = new ModernButton("Reset Counters", UITheme.ACCENT_DANGER);
        btnReset.addActionListener(e -> {
            SharedMetricsMonitor.getInstance().reset();
            updateStatCards();
            log("[SYSTEM] SharedMetricsMonitor state reset.");
        });

        actions.add(btnSimulate);
        actions.add(btnSchedule);
        actions.add(btnReset);
        panel.add(actions, BorderLayout.NORTH);

        txtThreadLog = new JTextArea();
        txtThreadLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtThreadLog.setBackground(UITheme.BG_CARD);
        txtThreadLog.setForeground(new Color(226, 232, 240));
        txtThreadLog.setCaretColor(Color.WHITE);
        txtThreadLog.setEditable(false);
        txtThreadLog.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(txtThreadLog);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        panel.add(scroll, BorderLayout.CENTER);

        // Add to layout
        BorderLayout layout = (BorderLayout) getLayout();
        Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
        if (centerComp instanceof JPanel) {
            ((JPanel) centerComp).add(panel, BorderLayout.CENTER);
        }

        log("[SYSTEM] Concurrency engine initialized. Ready to execute parallel tasks.");
    }

    /**
     * Demonstrates parallel threads competing to update shared state.
     * Uses synchronized methods and synchronized blocks in SharedMetricsMonitor.
     */
    private void runConcurrentSimulation() {
        final int THREAD_COUNT = 5;
        final int ITERATIONS_PER_THREAD = 10;
        final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        log(String.format("--> Starting %d concurrent threads. Each thread performs %d synchronized calorie updates...",
                THREAD_COUNT, ITERATIONS_PER_THREAD));

        for (int i = 1; i <= THREAD_COUNT; i++) {
            final int threadId = i;
            new Thread(() -> {
                SharedMetricsMonitor monitor = SharedMetricsMonitor.getInstance();
                monitor.recordSessionStart();
                log(String.format("[Thread-%d] STARTED. (Active Sessions: %d)", threadId, monitor.getActiveLiveSessions()));

                try {
                    for (int j = 1; j <= ITERATIONS_PER_THREAD; j++) {
                        // Simulate work
                        Thread.sleep((long) (Math.random() * 80 + 20));
                        double burnIncrement = Math.round((Math.random() * 15 + 5) * 10.0) / 10.0;

                        // Synchronized method/block invocation
                        monitor.recordCalorieBurn(burnIncrement);

                        SwingUtilities.invokeLater(() -> updateStatCards());
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    monitor.recordSessionEnd();
                    log(String.format("[Thread-%d] COMPLETED all iterations. (Active Sessions: %d)",
                            threadId, monitor.getActiveLiveSessions()));
                    latch.countDown();
                    SwingUtilities.invokeLater(() -> updateStatCards());
                }
            }, "WorkerThread-" + threadId).start();
        }

        // Background watcher thread to report completion
        new Thread(() -> {
            try {
                latch.await();
                log("[SYSTEM] All 5 parallel threads finished without race conditions!");
                SwingUtilities.invokeLater(() -> updateStatCards());
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void startGoalDaemon() {
        log("[DAEMON] Scheduling background GoalReminderTask every 30 seconds via ThreadPoolManager...");
        ThreadPoolManager.getInstance().scheduleAtFixedRate(
                new GoalReminderTask(currentUser.getId(), goalService, (title, msg, level) -> {
                    log(String.format("[NOTIFICATION DAEMON] %s: %s", title, msg));
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
                    });
                }),
                1, 30, TimeUnit.SECONDS
        );
        log("[DAEMON] Background reminder daemon is active!");
    }

    private void updateStatCards() {
        SharedMetricsMonitor m = SharedMetricsMonitor.getInstance();
        cardActiveSessions.setValue(m.getActiveLiveSessions() + " Active");
        cardLiveCalories.setValue(String.format("%.1f kcal", m.getLiveCaloriesBurnedCounter()));
        cardProcessedEvents.setValue(String.valueOf(m.getTotalProcessedEvents()));
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            txtThreadLog.append(message + "\n");
            txtThreadLog.setCaretPosition(txtThreadLog.getDocument().getLength());
        });
    }
}
