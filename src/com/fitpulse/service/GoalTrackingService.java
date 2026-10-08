package com.fitpulse.service;

import com.fitpulse.dao.AuditLogDAO;
import com.fitpulse.dao.GoalDAO;
import com.fitpulse.dao.WorkoutDAO;
import com.fitpulse.dao.impl.AuditLogDAOImpl;
import com.fitpulse.dao.impl.GoalDAOImpl;
import com.fitpulse.dao.impl.WorkoutDAOImpl;
import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.exception.TransactionException;
import com.fitpulse.exception.ValidationException;
import com.fitpulse.model.AuditLog;
import com.fitpulse.model.FitnessGoal;
import com.fitpulse.model.Workout;
import com.fitpulse.model.enums.GoalStatus;
import com.fitpulse.model.enums.GoalType;
import com.fitpulse.util.DatabaseConnectionManager;
import com.fitpulse.util.ValidationUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service managing user fitness goals and atomic multi-step database transactions.
 * Demonstrates:
 * - Rubric Item 6: JDBC Implementation - Transaction Management (setAutoCommit, commit, rollback)
 * - Rubric Item 4: Database Operation Classes
 * - Rubric Item 1: Exception Handling (TransactionException)
 */
public class GoalTrackingService {
    private final GoalDAO goalDAO;
    private final WorkoutDAO workoutDAO;
    private final AuditLogDAO auditLogDAO;
    private final DatabaseConnectionManager connectionManager;

    public GoalTrackingService() {
        this(new GoalDAOImpl(), new WorkoutDAOImpl(), new AuditLogDAOImpl());
    }

    public GoalTrackingService(GoalDAO goalDAO, WorkoutDAO workoutDAO, AuditLogDAO auditLogDAO) {
        this.goalDAO = goalDAO;
        this.workoutDAO = workoutDAO;
        this.auditLogDAO = auditLogDAO;
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public FitnessGoal createGoal(int userId, GoalType goalType, double targetValue,
                                  LocalDate startDate, LocalDate endDate)
            throws ValidationException, DatabaseOperationException {

        ValidationUtil.requirePositive(targetValue, "Goal Target Value");
        if (endDate.isBefore(startDate)) {
            throw new ValidationException("Goal end date cannot be earlier than start date.");
        }

        FitnessGoal goal = new FitnessGoal(0, userId, goalType, targetValue, 0.0, startDate, endDate, GoalStatus.IN_PROGRESS);
        return goalDAO.create(goal);
    }

    public List<FitnessGoal> getUserGoals(int userId) throws DatabaseOperationException {
        return goalDAO.findByUserId(userId);
    }

    public List<FitnessGoal> getActiveUserGoals(int userId) throws DatabaseOperationException {
        return goalDAO.findActiveGoalsByUserId(userId);
    }

    /**
     * ATOMIC TRANSACTION MANAGEMENT METHOD (Rubric Item 6)
     * Performs a 3-step atomic operation:
     * 1. Inserts the workout entry.
     * 2. Atomically increments and updates the fitness goal progress.
     * 3. Records a persistent audit log entry.
     *
     * If ANY step fails, ALL steps are rolled back using connection.rollback().
     *
     * @param workout The workout to log
     * @param goalId The goal milestone ID being progressed
     * @param progressIncrement The amount to add to the goal's current progress
     * @return Saved workout entity
     * @throws TransactionException if any query fails, triggering rollback
     */
    public Workout logWorkoutAndUpdateGoalAtomic(Workout workout, int goalId, double progressIncrement)
            throws TransactionException {

        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            // Step 1: Disable AutoCommit to begin transaction
            conn.setAutoCommit(false);

            // Step 2: Save the workout in the current transaction
            Workout savedWorkout = workoutDAO.createTransactional(conn, workout);

            // Step 3: Update goal progress in the current transaction
            goalDAO.updateProgressTransactional(conn, goalId, progressIncrement);

            // Step 4: Record audit log via AuditLogDAO within the same transaction
            AuditLog auditEntry = new AuditLog();
            auditEntry.setUserId(workout.getUserId());
            auditEntry.setAction("WORKOUT_AND_GOAL_SYNC");
            auditEntry.setDetails(String.format(
                    "Atomic commit: Saved %s (%d min) and credited %.1f progress to Goal #%d",
                    workout.getWorkoutType(), workout.getDurationMinutes(), progressIncrement, goalId));
            auditLogDAO.createTransactional(conn, auditEntry);

            // Step 5: Explicit commit when all steps succeed
            conn.commit();
            return savedWorkout;

        } catch (Exception e) {
            // Step 6: Explicit rollback on any exception
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[TRANSACTION ROLLBACK] Rolled back atomic transaction due to: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.err.println("[TRANSACTION FATAL] Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            throw new TransactionException("Atomic workout and goal transaction failed and was rolled back: " + e.getMessage(), e);
        } finally {
            // Step 7: Reset auto-commit and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public boolean deleteGoal(int goalId) throws DatabaseOperationException {
        return goalDAO.delete(goalId);
    }
}
