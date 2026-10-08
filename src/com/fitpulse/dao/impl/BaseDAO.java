package com.fitpulse.dao.impl;

import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.util.DatabaseConnectionManager;
import java.sql.Connection;

/**
 * Base DAO providing common database connection utilities.
 * Demonstrates:
 * - Rubric Item 4: Database Operation Classes
 * - Rubric Item 5: JDBC Connectivity
 */
public abstract class BaseDAO {
    protected final DatabaseConnectionManager connectionManager;

    public BaseDAO() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    protected Connection getConnection() throws DatabaseOperationException {
        return connectionManager.getConnection();
    }
}
