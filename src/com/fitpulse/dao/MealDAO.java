package com.fitpulse.dao;

import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.model.MealLog;
import java.time.LocalDate;
import java.util.List;

/**
 * Specialized DAO interface for MealLog persistence operations.
 */
public interface MealDAO extends GenericDAO<MealLog, Integer> {
    List<MealLog> findByUserId(int userId) throws DatabaseOperationException;
    List<MealLog> findByUserIdAndDate(int userId, LocalDate date) throws DatabaseOperationException;
    int getTotalCaloriesByDate(int userId, LocalDate date) throws DatabaseOperationException;
}
