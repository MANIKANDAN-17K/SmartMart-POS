package com.supermarketpos.dao;

import com.supermarketpos.model.Category;
import com.supermarketpos.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    public int create(Category category) throws SQLException {
        String sql = "INSERT INTO categories (name, description, active) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setBoolean(3, category.isActive());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            // Fallback for schema without description or active columns
            String fallbackSql = "INSERT INTO categories (name) VALUES (?)";
            try (Connection conn = DBConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(fallbackSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, category.getName());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public void update(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            String fallbackSql = "UPDATE categories SET name = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(fallbackSql)) {
                ps.setString(1, category.getName());
                ps.setInt(2, category.getId());
                ps.executeUpdate();
            }
        }
    }

    public void setActiveStatus(int id, boolean active) throws SQLException {
        String sql = "UPDATE categories SET active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException ignored) {
            // Safe fallback if active column is not present
        }
    }

    public Category findById(int id) throws SQLException {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean existsByName(String name, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(?)"
                : "SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(?) AND id != ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Category> findAll() throws SQLException {
        String sql = "SELECT * FROM categories ORDER BY name ASC";
        return queryCategoryList(sql, null);
    }

    public List<Category> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM categories WHERE name LIKE ? ORDER BY name ASC";
        return queryCategoryList(sql, "%" + keyword + "%");
    }

    public List<Category> findAllActive() throws SQLException {
        try {
            String sql = "SELECT * FROM categories WHERE active = TRUE ORDER BY name ASC";
            return queryCategoryList(sql, null);
        } catch (SQLException e) {
            // Fallback query if 'active' column does not exist in categories table
            String fallbackSql = "SELECT * FROM categories ORDER BY name ASC";
            return queryCategoryList(fallbackSql, null);
        }
    }

    private List<Category> queryCategoryList(String sql, String param) throws SQLException {
        List<Category> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        
        try {
            c.setDescription(rs.getString("description"));
        } catch (SQLException ignored) {
            c.setDescription("");
        }

        try {
            c.setActive(rs.getBoolean("active"));
        } catch (SQLException ignored) {
            c.setActive(true);
        }

        try {
            Timestamp created = rs.getTimestamp("created_at");
            if (created != null) c.setCreatedAt(created.toLocalDateTime());
        } catch (SQLException ignored) {}

        try {
            Timestamp updated = rs.getTimestamp("updated_at");
            if (updated != null) c.setUpdatedAt(updated.toLocalDateTime());
        } catch (SQLException ignored) {}

        return c;
    }
}