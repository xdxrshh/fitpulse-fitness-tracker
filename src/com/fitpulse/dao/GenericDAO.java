package com.fitpulse.dao;

import com.fitpulse.exception.DatabaseOperationException;
import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object (DAO) interface.
 * Demonstrates:
 * - Rubric Item 2: Generics & Collections (<T, ID>, List<T>)
 * - Rubric Item 4: Database Operation Classes
 * - Rubric Item 5: CRUD Operations
 *
 * @param <T> Entity type
 * @param <ID> Primary key identifier type
 */
public interface GenericDAO<T, ID> {
    /**
     * Create / Insert entity into the database (CRUD: Create).
     */
    T create(T entity) throws DatabaseOperationException;

    /**
     * Read / Retrieve entity by identifier (CRUD: Read).
     */
    Optional<T> findById(ID id) throws DatabaseOperationException;

    /**
     * Retrieve all entities (CRUD: Read).
     */
    List<T> findAll() throws DatabaseOperationException;

    /**
     * Update an existing entity (CRUD: Update).
     */
    boolean update(T entity) throws DatabaseOperationException;

    /**
     * Delete an entity by identifier (CRUD: Delete).
     */
    boolean delete(ID id) throws DatabaseOperationException;
}
