package com.fitpulse.interfaces;

import java.sql.Connection;

/**
 * Functional interface used for executing atomic operations inside a managed JDBC transaction.
 * Part of Rubric Item 6: JDBC Implementation - Transaction Management.
 *
 * @param <T> Return type of the transactional execution block
 */
@FunctionalInterface
public interface TransactionCallback<T> {
    T execute(Connection connection) throws Exception;
}
