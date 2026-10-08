package com.fitpulse.dao;

import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.model.AuditLog;
import java.sql.Connection;
import java.util.List;

/**
 * Specialized DAO interface for AuditLog persistence operations.
 * Demonstrates:
 * - Rubric Item 4: Database Operation Classes (DAO interface)
 * - Rubric Item 5: JDBC CRUD operations
 * - Rubric Item 6: Transaction Management (createTransactional)
 */
public interface AuditLogDAO extends GenericDAO<AuditLog, Integer> {

    /**
     * Retrieve all audit log entries for a specific user.
     * @param userId the user whose audit history to retrieve
     * @return list of AuditLog records, newest first
     */
    List<AuditLog> findByUserId(int userId) throws DatabaseOperationException;

    /**
     * Insert an audit log entry within an existing, caller-managed transaction.
     * Used by GoalTrackingService to record the audit step atomically alongside
     * other transactional statements.  The caller is responsible for commit/rollback.
     *
     * @param conn   an open Connection whose autoCommit is already false
     * @param log    the AuditLog entity to persist
     * @return the saved entity with generated id populated
     * @throws DatabaseOperationException if the INSERT fails
     */
    AuditLog createTransactional(Connection conn, AuditLog log) throws DatabaseOperationException;
}
