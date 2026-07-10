package com.supermarketpos.dao;

import com.supermarketpos.model.Product;
import com.supermarketpos.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    private static final String SELECT_BASE =
            "SELECT p.*, c.name AS category_name FROM products p " +
                    "JOIN categories c ON p.category_id = c.id ";

    public int create(Product product) throws SQLException {
        String sql = "INSERT INTO products " +
                "(name, barcode, sku, category_id, cost_price, selling_price, gst_percentage, image_path, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindProduct(ps, product);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, barcode = ?, sku = ?, category_id = ?, " +
                "cost_price = ?, selling_price = ?, gst_percentage = ?, image_path = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getBarcode());
            ps.setString(3, product.getSku());
            ps.setInt(4, product.getCategoryId());
            ps.setBigDecimal(5, product.getCostPrice());
            ps.setBigDecimal(6, product.getSellingPrice());
            ps.setBigDecimal(7, product.getGstPercentage());
            ps.setString(8, product.getImagePath());
            ps.setInt(9, product.getId());
            ps.executeUpdate();
        }
    }

    public void setActiveStatus(int id, boolean active) throws SQLException {
        String sql = "UPDATE products SET active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public Product findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.id = ?";
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

    public Product findByBarcode(String barcode) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.barcode = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Product findBySku(String sku) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.sku = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean existsByBarcode(String barcode, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM products WHERE barcode = ?"
                : "SELECT COUNT(*) FROM products WHERE barcode = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barcode);
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

    public boolean existsBySku(String sku, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM products WHERE sku = ?"
                : "SELECT COUNT(*) FROM products WHERE sku = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sku);
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

    public List<Product> findAll() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY p.name ASC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Product> findByCategory(int categoryId) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.category_id = ? ORDER BY p.name ASC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Product> search(String keyword) throws SQLException {
        String sql = SELECT_BASE +
                "WHERE p.name LIKE ? OR p.barcode LIKE ? OR p.sku LIKE ? ORDER BY p.name ASC";
        List<Product> list = new ArrayList<>();
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

    private void bindProduct(PreparedStatement ps, Product product) throws SQLException {
        ps.setString(1, product.getName());
        ps.setString(2, product.getBarcode());
        ps.setString(3, product.getSku());
        ps.setInt(4, product.getCategoryId());
        ps.setBigDecimal(5, product.getCostPrice());
        ps.setBigDecimal(6, product.getSellingPrice());
        ps.setBigDecimal(7, product.getGstPercentage());
        ps.setString(8, product.getImagePath());
        ps.setBoolean(9, product.isActive());
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setBarcode(rs.getString("barcode"));
        p.setSku(rs.getString("sku"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setCostPrice(rs.getBigDecimal("cost_price"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setGstPercentage(rs.getBigDecimal("gst_percentage"));
        p.setImagePath(rs.getString("image_path"));
        p.setActive(rs.getBoolean("active"));
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if (created != null) p.setCreatedAt(created.toLocalDateTime());
        if (updated != null) p.setUpdatedAt(updated.toLocalDateTime());
        return p;
    }
}