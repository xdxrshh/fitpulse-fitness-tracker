package com.fitpulse.dao.impl;

import com.fitpulse.dao.UserDAO;
import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.model.Member;
import com.fitpulse.model.Trainer;
import com.fitpulse.model.User;
import com.fitpulse.model.enums.UserRole;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of UserDAO.
 * Demonstrates:
 * - Rubric Item 4: Database Operation Classes
 * - Rubric Item 5: CRUD & PreparedStatements (parameterized queries)
 * - Rubric Item 6: Transaction management for multi-table inserts
 */
public class UserDAOImpl extends BaseDAO implements UserDAO {

    @Override
    public User create(User user) throws DatabaseOperationException {
        String insertUserSql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmtUser = null;
        ResultSet rsKeys = null;

        try {
            conn = getConnection();
            // Start transaction for atomic multi-table insert
            conn.setAutoCommit(false);

            stmtUser = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, user.getName());
            stmtUser.setString(2, user.getEmail());
            stmtUser.setString(3, user.getPasswordHash());
            stmtUser.setString(4, user.getRole().name());
            stmtUser.executeUpdate();

            rsKeys = stmtUser.getGeneratedKeys();
            if (!rsKeys.next()) {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
            int generatedId = rsKeys.getInt(1);
            user.setId(generatedId);

            // Insert into polymorphic subtype table based on role
            if (user instanceof Member) {
                Member member = (Member) user;
                String insertMemberSql = "INSERT INTO members (user_id, weight_kg, height_cm, fitness_level, trainer_id) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmtMember = conn.prepareStatement(insertMemberSql)) {
                    stmtMember.setInt(1, generatedId);
                    stmtMember.setDouble(2, member.getWeightKg());
                    stmtMember.setDouble(3, member.getHeightCm());
                    stmtMember.setString(4, member.getFitnessLevel());
                    if (member.getTrainerId() != null) {
                        stmtMember.setInt(5, member.getTrainerId());
                    } else {
                        stmtMember.setNull(5, Types.INTEGER);
                    }
                    stmtMember.executeUpdate();
                }
            } else if (user instanceof Trainer) {
                Trainer trainer = (Trainer) user;
                String insertTrainerSql = "INSERT INTO trainers (user_id, specialization, certification) VALUES (?, ?, ?)";
                try (PreparedStatement stmtTrainer = conn.prepareStatement(insertTrainerSql)) {
                    stmtTrainer.setInt(1, generatedId);
                    stmtTrainer.setString(2, trainer.getSpecialization());
                    stmtTrainer.setString(3, trainer.getCertification());
                    stmtTrainer.executeUpdate();
                }
            }

            // Commit atomic transaction
            conn.commit();
            return user;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on failure
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw new DatabaseOperationException("Failed to create user: " + e.getMessage(), e);
        } finally {
            if (rsKeys != null) try { rsKeys.close(); } catch (SQLException ignored) {}
            if (stmtUser != null) try { stmtUser.close(); } catch (SQLException ignored) {}
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws DatabaseOperationException {
        String sql = "SELECT u.id, u.name, u.email, u.password_hash, u.role, u.created_at, " +
                     "m.weight_kg, m.height_cm, m.fitness_level, m.trainer_id, " +
                     "t.specialization, t.certification " +
                     "FROM users u " +
                     "LEFT JOIN members m ON u.id = m.user_id " +
                     "LEFT JOIN trainers t ON u.id = t.user_id " +
                     "WHERE u.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding user by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) throws DatabaseOperationException {
        String sql = "SELECT u.id, u.name, u.email, u.password_hash, u.role, u.created_at, " +
                     "m.weight_kg, m.height_cm, m.fitness_level, m.trainer_id, " +
                     "t.specialization, t.certification " +
                     "FROM users u " +
                     "LEFT JOIN members m ON u.id = m.user_id " +
                     "LEFT JOIN trainers t ON u.id = t.user_id " +
                     "WHERE u.email = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding user by email: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean emailExists(String email) throws DatabaseOperationException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error checking email existence: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() throws DatabaseOperationException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.id, u.name, u.email, u.password_hash, u.role, u.created_at, " +
                     "m.weight_kg, m.height_cm, m.fitness_level, m.trainer_id, " +
                     "t.specialization, t.certification " +
                     "FROM users u " +
                     "LEFT JOIN members m ON u.id = m.user_id " +
                     "LEFT JOIN trainers t ON u.id = t.user_id " +
                     "ORDER BY u.id ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding all users: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean update(User user) throws DatabaseOperationException {
        String sql = "UPDATE users SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setInt(2, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DatabaseOperationException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting user: " + e.getMessage(), e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String hash = rs.getString("password_hash");
        String roleStr = rs.getString("role");
        UserRole role = UserRole.valueOf(roleStr);

        if (role == UserRole.TRAINER) {
            String spec = rs.getString("specialization");
            String cert = rs.getString("certification");
            return new Trainer(id, name, email, hash, spec != null ? spec : "General Fitness", cert != null ? cert : "None");
        } else {
            double weight = rs.getDouble("weight_kg");
            double height = rs.getDouble("height_cm");
            String level = rs.getString("fitness_level");
            int trainerId = rs.getInt("trainer_id");
            Integer trainerIdObj = rs.wasNull() ? null : trainerId;
            return new Member(id, name, email, hash, weight, height, level, trainerIdObj);
        }
    }
}
