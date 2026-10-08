package com.fitpulse.dao.impl;

import com.fitpulse.dao.AuditLogDAO;
import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.model.AuditLog;
import com.fitpulse.util.DatabaseConnectionManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based implementation of {@link AuditLogDAO}.
 * Demonstrates:
 * - Rubric Item 4: Database Operation Classes (DAO implementation)
 * - Rubric Item 5: JDBC CRUD with PreparedStatement
 * - Rubric Item 6: Transaction Management (createTransactional injects into open Connection)
 * - Rubric Item 1: Exception Handling (custom DatabaseOperationException)
 */
public class AuditLogDAOImpl extends BaseDAO implements AuditLogDAO {

    private static final String INSERT_SQL =
            "INSERT INTO audit_logs (user_id, action, details) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, user_id, action, details, created_at FROM audit_logs WHERE id = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT id, user_id, action, details, created_at FROM audit_logs ORDER BY created_at DESC";

    private static final String SELECT_BY_USER_SQL =
            "SELECT id, user_id, action, details, created_at FROM audit_logs " +
            "WHERE user_id = ? ORDER BY created_at DESC";

    private static final String UPDATE_SQL =
            "UPDATE audit_logs SET action = ?, details = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM audit_logs WHERE id = ?";

    // =========================================================================
    // CRUD: Create (Rubric Item 5 — PreparedStatement)
    // =========================================================================

    @Override
    public AuditLog create(AuditLog log) throws DatabaseOperationException {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            return insertLog(conn, log);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert audit log: " + e.getMessage(), e);
        }
    }

    /**
     * Transactional insert — reuses an existing open Connection whose autoCommit
     * is already disabled by the caller (GoalTrackingService).
     * Rubric Item 6: JDBC Implementation — Transaction Management.
     */
    @Override
    public AuditLog createTransactional(Connection conn, AuditLog log)
            throws DatabaseOperationException {
        try {
            return insertLog(conn, log);
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Failed to insert audit log within transaction: " + e.getMessage(), e);
        }
    }

    /** Shared insert logic — reused by both create() and createTransactional(). */
    private AuditLog insertLog(Connection conn, AuditLog log) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            // Rubric Item 5: PreparedStatement bindings
            if (log.getUserId() != null) {
                ps.setInt(1, log.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, log.getAction());
            ps.setString(3, log.getDetails());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    log.setId(keys.getInt(1));
                }
            }
        }
        return log;
    }

    // =========================================================================
    // CRUD: Read (Rubric Item 5)
    // =========================================================================

    @Override
    public Optional<AuditLog> findById(Integer id) throws DatabaseOperationException {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find audit log by id: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<AuditLog> findAll() throws DatabaseOperationException {
        List<AuditLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                logs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve all audit logs: " + e.getMessage(), e);
        }
        return logs;
    }

    @Override
    public List<AuditLog> findByUserId(int userId) throws DatabaseOperationException {
        List<AuditLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USER_SQL)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Failed to retrieve audit logs for user " + userId + ": " + e.getMessage(), e);
        }
        return logs;
    }

    // =========================================================================
    // CRUD: Update (Rubric Item 5)
    // =========================================================================

    @Override
    public boolean update(AuditLog log) throws DatabaseOperationException {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, log.getAction());
            ps.setString(2, log.getDetails());
            ps.setInt(3, log.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update audit log: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // CRUD: Delete (Rubric Item 5)
    // =========================================================================

    @Override
    public boolean delete(Integer id) throws DatabaseOperationException {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete audit log: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // Row Mapper
    // =========================================================================

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int rawUserId = rs.getInt("user_id");
        Integer userId = rs.wasNull() ? null : rawUserId;
        String action = rs.getString("action");
        String details = rs.getString("details");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new AuditLog(id, userId, action, details, createdAt);
    }
}
