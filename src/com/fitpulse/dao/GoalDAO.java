package com.fitpulse.dao;

import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.model.FitnessGoal;
import java.sql.Connection;
import java.util.List;

/**
 * Specialized DAO interface for FitnessGoal persistence operations.
 */
public interface GoalDAO extends GenericDAO<FitnessGoal, Integer> {
    List<FitnessGoal> findByUserId(int userId) throws DatabaseOperationException;
    List<FitnessGoal> findActiveGoalsByUserId(int userId) throws DatabaseOperationException;
    
    // Transactional overload: update progress within an existing transaction
    boolean updateProgressTransactional(Connection conn, int goalId, double addedProgress) 
            throws DatabaseOperationException;
}
