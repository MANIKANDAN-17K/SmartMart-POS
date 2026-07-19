package com.supermarketpos.dao;

import com.supermarketpos.model.Customer;
import com.supermarketpos.model.Customer.Status;
import com.supermarketpos.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class CustomerDao {
    private static final Logger log = Logger.getLogger(CustomerDao.class.getName());

    public void insert(Connection conn, Customer customer) throws SQLException {
        if (customer.getCreatedAt() == null) {
            customer.setCreatedAt(java.time.LocalDateTime.now());
        }
        String sql = """
                INSERT INTO customers (name, phone, email, address, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getStatus().name());
            ps.setTimestamp(6, Timestamp.valueOf(customer.getCreatedAt()));
            ps.setTimestamp(7, Timestamp.valueOf(customer.getCreatedAt()));
            int rows = ps.executeUpdate();
            if (rows == 0)
                throw new SQLException("Insert customer failed – no rows affected.");
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    customer.setId(rs.getInt(1));
                    log.info("Customer created: id=" + customer.getId() + " phone=" + customer.getPhone());
                }
            }
        }
    }

    public void update(Connection conn, Customer customer) throws SQLException {
        String sql = """
                UPDATE customers
                   SET name = ?, phone = ?, email = ?, address = ?, status = ?, updated_at = ?
                 WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getStatus().name());
            ps.setTimestamp(6, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(7, customer.getId());
            ps.executeUpdate();
            log.info("Customer updated: id=" + customer.getId());
        }
    }

    public void setStatus(Connection conn, int customerId, Status status) throws SQLException {
        String sql = "UPDATE customers SET status = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setTimestamp(2, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(3, customerId);
            ps.executeUpdate();
            log.info("Customer status set to " + status + ": id=" + customerId);
        }
    }

    public Optional<Customer> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Customer> findByPhone(Connection conn, String phone) throws SQLException {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public boolean phoneExistsForOther(Connection conn, String phone, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE phone = ? AND id != ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean phoneExists(Connection conn, String phone) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE phone = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Customer> searchByName(Connection conn, String keyword) throws SQLException {
        String sql = "SELECT * FROM customers WHERE name LIKE ? ORDER BY name LIMIT 50";
        List<Customer> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<Customer> searchByPhone(Connection conn, String phone) throws SQLException {
        String sql = "SELECT * FROM customers WHERE phone LIKE ? ORDER BY name LIMIT 50";
        List<Customer> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + phone.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<Customer> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY name";
        List<Customer> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                result.add(mapRow(rs));
        }
        return result;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setStatus(Status.valueOf(rs.getString("status")));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null)
            c.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null)
            c.setUpdatedAt(updated.toLocalDateTime());
        return c;
    }

    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}