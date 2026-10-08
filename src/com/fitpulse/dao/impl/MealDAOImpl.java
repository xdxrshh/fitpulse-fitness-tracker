package com.fitpulse.dao.impl;

import com.fitpulse.dao.MealDAO;
import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.model.MealLog;
import com.fitpulse.model.enums.MealType;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of MealDAO.
 * Demonstrates:
 * - Rubric Item 4: Database Operation Classes
 * - Rubric Item 5: CRUD & PreparedStatement
 */
public class MealDAOImpl extends BaseDAO implements MealDAO {

    @Override
    public MealLog create(MealLog meal) throws DatabaseOperationException {
        String sql = "INSERT INTO meal_logs (user_id, meal_name, meal_type, calories, protein_g, carbs_g, fat_g, logged_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, meal.getUserId());
            stmt.setString(2, meal.getMealName());
            stmt.setString(3, meal.getMealType().name());
            stmt.setInt(4, meal.getCalories());
            stmt.setDouble(5, meal.getProteinGrams());
            stmt.setDouble(6, meal.getCarbsGrams());
            stmt.setDouble(7, meal.getFatGrams());
            stmt.setTimestamp(8, Timestamp.valueOf(meal.getLoggedAt()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    meal.setId(rs.getInt(1));
                }
            }
            return meal;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error logging meal: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<MealLog> findById(Integer id) throws DatabaseOperationException {
        String sql = "SELECT * FROM meal_logs WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMeal(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding meal by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<MealLog> findAll() throws DatabaseOperationException {
        List<MealLog> list = new ArrayList<>();
        String sql = "SELECT * FROM meal_logs ORDER BY logged_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToMeal(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error retrieving meals: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<MealLog> findByUserId(int userId) throws DatabaseOperationException {
        List<MealLog> list = new ArrayList<>();
        String sql = "SELECT * FROM meal_logs WHERE user_id = ? ORDER BY logged_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMeal(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error retrieving meals for user: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<MealLog> findByUserIdAndDate(int userId, LocalDate date) throws DatabaseOperationException {
        List<MealLog> list = new ArrayList<>();
        String sql = "SELECT * FROM meal_logs WHERE user_id = ? AND DATE(logged_at) = ? ORDER BY logged_at ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMeal(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error retrieving meals by date: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int getTotalCaloriesByDate(int userId, LocalDate date) throws DatabaseOperationException {
        String sql = "SELECT COALESCE(SUM(calories), 0) AS total_cal FROM meal_logs WHERE user_id = ? AND DATE(logged_at) = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_cal");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error summing calories: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(MealLog meal) throws DatabaseOperationException {
        String sql = "UPDATE meal_logs SET meal_name = ?, meal_type = ?, calories = ?, protein_g = ?, carbs_g = ?, fat_g = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, meal.getMealName());
            stmt.setString(2, meal.getMealType().name());
            stmt.setInt(3, meal.getCalories());
            stmt.setDouble(4, meal.getProteinGrams());
            stmt.setDouble(5, meal.getCarbsGrams());
            stmt.setDouble(6, meal.getFatGrams());
            stmt.setInt(7, meal.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating meal: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DatabaseOperationException {
        String sql = "DELETE FROM meal_logs WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting meal: " + e.getMessage(), e);
        }
    }

    private MealLog mapResultSetToMeal(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        String name = rs.getString("meal_name");
        MealType type = MealType.valueOf(rs.getString("meal_type"));
        int calories = rs.getInt("calories");
        double protein = rs.getDouble("protein_g");
        double carbs = rs.getDouble("carbs_g");
        double fat = rs.getDouble("fat_g");
        LocalDateTime loggedAt = rs.getTimestamp("logged_at").toLocalDateTime();

        return new MealLog(id, userId, name, type, calories, protein, carbs, fat, loggedAt);
    }
}
