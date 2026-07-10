package com.supermarketpos.dao;

import com.supermarketpos.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Billing-relevant product queries.
 * Merge these methods into your existing ProductDao.
 */
public class ProductDao {

    public Optional<Product> findByBarcode(Connection conn, String barcode) throws SQLException {
        String sql = "SELECT * FROM products WHERE barcode = ? AND status = 'ACTIVE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Product> searchByName(Connection conn, String keyword) throws SQLException {
        String sql = "SELECT * FROM products WHERE name LIKE ? AND status = 'ACTIVE' LIMIT 20";
        List<Product> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    /** Deducts stock. Called inside the billing transaction — do NOT commit here. */
    public void deductStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = """
            UPDATE products
               SET stock_quantity = stock_quantity - ?
             WHERE id = ?
               AND stock_quantity >= ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insufficient stock for product id=" + productId);
            }
        }
    }

    public Optional<Product> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setBarcode(rs.getString("barcode"));
        p.setName(rs.getString("name"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setGstPercent(rs.getBigDecimal("gst_percent"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setStatus(rs.getString("status"));
        return p;
    }
}