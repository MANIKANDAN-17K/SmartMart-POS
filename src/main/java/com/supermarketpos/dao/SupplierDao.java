package com.supermarketpos.dao;

import com.supermarketpos.database.DatabaseInitializer;
import com.supermarketpos.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDao implements BaseDao<Supplier, Integer> {

    private static final String FIND_BY_ID_SQL = "SELECT * FROM suppliers WHERE id = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM suppliers";
    private static final String COUNT_ACTIVE_SQL = "SELECT COUNT(*) FROM suppliers WHERE is_active = TRUE";
    private static final String INSERT_SQL = "INSERT INTO suppliers (name, phone, email, is_active) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE suppliers SET name=?, phone=?, email=?, is_active=? WHERE id=?";
    private static final String SOFT_DELETE_SQL = "UPDATE suppliers SET is_active = FALSE WHERE id = ?";

    @Override
    public Optional<Supplier> findById(Integer id) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find supplier by id: " + id, e);
        }
    }

    @Override
    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                suppliers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch suppliers", e);
        }
        return suppliers;
    }

    public int countActive() {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_ACTIVE_SQL);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count active suppliers", e);
        }
    }

    @Override
    public Supplier save(Supplier supplier) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getPhone());
            ps.setString(3, supplier.getEmail());
            ps.setBoolean(4, supplier.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    supplier.setId(keys.getInt(1));
                }
            }
            return supplier;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save supplier: " + supplier.getName(), e);
        }
    }

    @Override
    public Supplier update(Supplier supplier) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getPhone());
            ps.setString(3, supplier.getEmail());
            ps.setBoolean(4, supplier.isActive());
            ps.setInt(5, supplier.getId());
            ps.executeUpdate();
            return supplier;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update supplier: " + supplier.getId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(SOFT_DELETE_SQL)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate supplier: " + id, e);
        }
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        return new Supplier(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getBoolean("is_active"),
                createdTs != null ? createdTs.toLocalDateTime() : null
        );
    }
}