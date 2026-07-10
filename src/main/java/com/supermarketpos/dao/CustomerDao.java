package com.supermarketpos.dao;

import com.supermarketpos.database.DatabaseInitializer;
import com.supermarketpos.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDao implements BaseDao<Customer, Integer> {

    private static final String FIND_BY_ID_SQL = "SELECT * FROM customers WHERE id = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM customers";
    private static final String COUNT_ACTIVE_SQL = "SELECT COUNT(*) FROM customers WHERE is_active = TRUE";
    private static final String INSERT_SQL = "INSERT INTO customers (name, phone, email, is_active) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE customers SET name=?, phone=?, email=?, is_active=? WHERE id=?";
    private static final String SOFT_DELETE_SQL = "UPDATE customers SET is_active = FALSE WHERE id = ?";

    @Override
    public Optional<Customer> findById(Integer id) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer by id: " + id, e);
        }
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch customers", e);
        }
        return customers;
    }

    public int countActive() {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_ACTIVE_SQL);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count active customers", e);
        }
    }

    @Override
    public Customer save(Customer customer) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setBoolean(4, customer.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getInt(1));
                }
            }
            return customer;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save customer: " + customer.getName(), e);
        }
    }

    @Override
    public Customer update(Customer customer) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setBoolean(4, customer.isActive());
            ps.setInt(5, customer.getId());
            ps.executeUpdate();
            return customer;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update customer: " + customer.getId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(SOFT_DELETE_SQL)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate customer: " + id, e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        return new Customer(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getBoolean("is_active"),
                createdTs != null ? createdTs.toLocalDateTime() : null
        );
    }
}