package com.fitpulse.dao;

import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.model.User;
import java.util.Optional;

/**
 * Specialized DAO interface for User persistence operations.
 */
public interface UserDAO extends GenericDAO<User, Integer> {
    Optional<User> findByEmail(String email) throws DatabaseOperationException;
    boolean emailExists(String email) throws DatabaseOperationException;
}
