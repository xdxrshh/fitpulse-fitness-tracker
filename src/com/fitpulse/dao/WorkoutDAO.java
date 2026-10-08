package com.fitpulse.dao;

import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.model.Workout;
import java.sql.Connection;
import java.util.List;

/**
 * Specialized DAO interface for Workout persistence operations.
 */
public interface WorkoutDAO extends GenericDAO<Workout, Integer> {
    List<Workout> findByUserId(int userId) throws DatabaseOperationException;
    double getTotalCaloriesBurnedByUser(int userId) throws DatabaseOperationException;
    int getTotalWorkoutsCountByUser(int userId) throws DatabaseOperationException;
    
    // Transactional overload: insert workout within an existing connection/transaction
    Workout createTransactional(Connection conn, Workout workout) throws DatabaseOperationException;
}
