package com.fitpulse.dao.impl;

import com.fitpulse.dao.GoalDAO;
import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.model.FitnessGoal;
import com.fitpulse.model.enums.GoalStatus;
import com.fitpulse.model.enums.GoalType;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of GoalDAO.
 * Demonstrates:
 * - Rubric Item 4: Database Operation Classes
 * - Rubric Item 5: CRUD & PreparedStatement
 * - Rubric Item 6: Transactional milestone updates
 */
public class GoalDAOImpl extends BaseDAO implements GoalDAO {

    @Override
    public FitnessGoal create(FitnessGoal goal) throws DatabaseOperationException {
        String sql = "INSERT INTO fitness_goals (user_id, goal_type, target_value, current_value, start_date, end_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, goal.getUserId());
            stmt.setString(2, goal.getGoalType().name());
            stmt.setDouble(3, goal.getTargetValue());
            stmt.setDouble(4, goal.getCurrentValue());
            stmt.setDate(5, Date.valueOf(goal.getStartDate()));
            stmt.setDate(6, Date.valueOf(goal.getEndDate()));
            stmt.setString(7, goal.getStatus().name());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    goal.setId(rs.getInt(1));
                }
            }
            return goal;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error creating goal: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<FitnessGoal> findById(Integer id) throws DatabaseOperationException {
        String sql = "SELECT * FROM fitness_goals WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGoal(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding goal by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<FitnessGoal> findAll() throws DatabaseOperationException {
        List<FitnessGoal> list = new ArrayList<>();
        String sql = "SELECT * FROM fitness_goals ORDER BY end_date ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToGoal(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching goals: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<FitnessGoal> findByUserId(int userId) throws DatabaseOperationException {
        List<FitnessGoal> list = new ArrayList<>();
        String sql = "SELECT * FROM fitness_goals WHERE user_id = ? ORDER BY status ASC, end_date ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGoal(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching user goals: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<FitnessGoal> findActiveGoalsByUserId(int userId) throws DatabaseOperationException {
        List<FitnessGoal> list = new ArrayList<>();
        String sql = "SELECT * FROM fitness_goals WHERE user_id = ? AND status = 'IN_PROGRESS' ORDER BY end_date ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGoal(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching active goals: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean update(FitnessGoal goal) throws DatabaseOperationException {
        String sql = "UPDATE fitness_goals SET target_value = ?, current_value = ?, end_date = ?, status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, goal.getTargetValue());
            stmt.setDouble(2, goal.getCurrentValue());
            stmt.setDate(3, Date.valueOf(goal.getEndDate()));
            stmt.setString(4, goal.getStatus().name());
            stmt.setInt(5, goal.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating goal: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateProgressTransactional(Connection conn, int goalId, double addedProgress) 
            throws DatabaseOperationException {
        String sql = "UPDATE fitness_goals SET current_value = current_value + ?, " +
                     "status = CASE WHEN (current_value + ?) >= target_value THEN 'COMPLETED' ELSE status END " +
                     "WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, addedProgress);
            stmt.setDouble(2, addedProgress);
            stmt.setInt(3, goalId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating goal progress in transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DatabaseOperationException {
        String sql = "DELETE FROM fitness_goals WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting goal: " + e.getMessage(), e);
        }
    }

    private FitnessGoal mapResultSetToGoal(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        GoalType type = GoalType.valueOf(rs.getString("goal_type"));
        double target = rs.getDouble("target_value");
        double current = rs.getDouble("current_value");
        LocalDate start = rs.getDate("start_date").toLocalDate();
        LocalDate end = rs.getDate("end_date").toLocalDate();
        GoalStatus status = GoalStatus.valueOf(rs.getString("status"));

        return new FitnessGoal(id, userId, type, target, current, start, end, status);
    }
}
