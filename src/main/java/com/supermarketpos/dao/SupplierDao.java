package com.supermarketpos.dao;

import com.supermarketpos.model.Supplier;
import com.supermarketpos.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDao {

    public int create(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO suppliers " +
                "(supplier_code, supplier_name, contact_person, mobile, email, gst_number, " +
                "address, city, state, pincode, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindSupplier(ps, supplier);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public void update(Supplier supplier) throws SQLException {
        String sql = "UPDATE suppliers SET supplier_code = ?, supplier_name = ?, contact_person = ?, " +
                "mobile = ?, email = ?, gst_number = ?, address = ?, city = ?, state = ?, pincode = ? " +
                "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, supplier.getSupplierCode());
            ps.setString(2, supplier.getSupplierName());
            ps.setString(3, supplier.getContactPerson());
            ps.setString(4, supplier.getMobile());
            ps.setString(5, supplier.getEmail());
            ps.setString(6, supplier.getGstNumber());
            ps.setString(7, supplier.getAddress());
            ps.setString(8, supplier.getCity());
            ps.setString(9, supplier.getState());
            ps.setString(10, supplier.getPincode());
            ps.setInt(11, supplier.getId());
            ps.executeUpdate();
        }
    }

    public void setActiveStatus(int id, boolean active) throws SQLException {
        String sql = "UPDATE suppliers SET active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public Supplier findById(int id) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
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

    public boolean existsByCode(String supplierCode, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM suppliers WHERE LOWER(supplier_code) = LOWER(?)"
                : "SELECT COUNT(*) FROM suppliers WHERE LOWER(supplier_code) = LOWER(?) AND id != ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, supplierCode);
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

    public boolean existsByName(String supplierName, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM suppliers WHERE LOWER(supplier_name) = LOWER(?)"
                : "SELECT COUNT(*) FROM suppliers WHERE LOWER(supplier_name) = LOWER(?) AND id != ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, supplierName);
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

    public List<Supplier> findAll() throws SQLException {
        String sql = "SELECT * FROM suppliers ORDER BY supplier_name ASC";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM suppliers WHERE active = true";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<Supplier> findByStatus(boolean active) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE active = ? ORDER BY supplier_name ASC";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Supplier> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE supplier_name LIKE ? OR supplier_code LIKE ? " +
                "OR mobile LIKE ? ORDER BY supplier_name ASC";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private void bindSupplier(PreparedStatement ps, Supplier supplier) throws SQLException {
        ps.setString(1, supplier.getSupplierCode());
        ps.setString(2, supplier.getSupplierName());
        ps.setString(3, supplier.getContactPerson());
        ps.setString(4, supplier.getMobile());
        ps.setString(5, supplier.getEmail());
        ps.setString(6, supplier.getGstNumber());
        ps.setString(7, supplier.getAddress());
        ps.setString(8, supplier.getCity());
        ps.setString(9, supplier.getState());
        ps.setString(10, supplier.getPincode());
        ps.setBoolean(11, supplier.isActive());
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("id"));
        s.setSupplierCode(rs.getString("supplier_code"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setMobile(rs.getString("mobile"));
        s.setEmail(rs.getString("email"));
        s.setGstNumber(rs.getString("gst_number"));
        s.setAddress(rs.getString("address"));
        s.setCity(rs.getString("city"));
        s.setState(rs.getString("state"));
        s.setPincode(rs.getString("pincode"));
        s.setActive(rs.getBoolean("active"));
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if (created != null)
            s.setCreatedAt(created.toLocalDateTime());
        if (updated != null)
            s.setUpdatedAt(updated.toLocalDateTime());
        return s;
    }
}