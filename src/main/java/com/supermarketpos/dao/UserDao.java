package com.supermarketpos.dao;

import com.supermarketpos.database.DatabaseInitializer;
import com.supermarketpos.model.Role;
import com.supermarketpos.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persistence for the users table.
 * Note: this class calls DatabaseInitializer.getConnection() from the Sprint 1
 * database module. That file is out of scope for this sprint and is not modified here.
 */
public class UserDao implements BaseDao<User, Integer> {

    private static final String INSERT_SQL =
            "INSERT INTO users (username, password_hash, role, is_active) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE users SET username = ?, password_hash = ?, role = ?, is_active = ? WHERE id = ?";
    private static final String FIND_BY_ID_SQL =
            "SELECT * FROM users WHERE id = ?";
    private static final String FIND_BY_USERNAME_SQL =
            "SELECT * FROM users WHERE username = ? AND is_active = TRUE";
    private static final String FIND_ALL_SQL =
            "SELECT * FROM users";
    private static final String SOFT_DELETE_SQL =
            "UPDATE users SET is_active = FALSE WHERE id = ?";
    private static final String UPDATE_LAST_LOGIN_SQL =
            "UPDATE users SET last_login = ? WHERE id = ?";
    private static final String USERNAME_EXISTS_SQL =
            "SELECT COUNT(*) FROM users WHERE username = ?";

    @Override
    public Optional<User> findById(Integer id) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id: " + id, e);
        }
    }

    public Optional<User> findByUsername(String username) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_USERNAME_SQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by username: " + username, e);
        }
    }

    public boolean usernameExists(String username) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(USERNAME_EXISTS_SQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check username existence: " + username, e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch users", e);
        }
        return users;
    }

    @Override
    public User save(User user) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, user.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user: " + user.getUsername(), e);
        }
    }

    @Override
    public User update(User user) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user: " + user.getId(), e);
        }
    }

    /**
     * Soft delete only — per the data rules a user row is never hard-deleted,
     * it is deactivated so billing/audit history referencing it stays intact.
     */
    @Override
    public void delete(Integer id) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(SOFT_DELETE_SQL)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate user: " + id, e);
        }
    }

    public void updateLastLogin(int userId) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_LAST_LOGIN_SQL)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update last login for user: " + userId, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        Timestamp createdTs = rs.getTimestamp("created_at");
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")),
                rs.getBoolean("is_active"),
                lastLoginTs != null ? lastLoginTs.toLocalDateTime() : null,
                createdTs != null ? createdTs.toLocalDateTime() : null
        );
    }
}
