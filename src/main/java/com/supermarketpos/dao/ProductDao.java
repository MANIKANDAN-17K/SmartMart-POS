package com.supermarketpos.dao;

import com.supermarketpos.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

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
    private static final Logger LOGGER = Logger.getLogger(ProductDao.class.getName());

    public List<StockReport> getStockReport(String category, String stockStatus) {
        List<StockReport> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT
                p.id AS product_id,
                p.name AS product_name,
                p.category AS category,
                p.stock_quantity AS current_stock,
                p.reorder_level AS reorder_level
            FROM products p
            WHERE 1=1
            """);
        List<Object> params = new ArrayList<>();
        if (category != null && !category.isEmpty() && !category.equals("All")) {
            sql.append(" AND p.category = ?");
            params.add(category);
        }
        sql.append(" ORDER BY p.name");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StockReport r = new StockReport();
                r.setProductId(rs.getInt("product_id"));
                r.setProductName(rs.getString("product_name"));
                r.setCategory(rs.getString("category"));
                int stock = rs.getInt("current_stock");
                int reorder = rs.getInt("reorder_level");
                r.setCurrentStock(stock);
                r.setReorderLevel(reorder);
                String status = stock == 0 ? "OUT" : stock <= reorder ? "LOW" : "OK";
                r.setStockStatus(status);
                list.add(r);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching stock report", e);
        }
        // Apply stockStatus filter in memory
        if (stockStatus != null && !stockStatus.isEmpty() && !stockStatus.equals("All")) {
            list.removeIf(r -> !r.getStockStatus().equals(stockStatus));
        }
        return list;
    }

    public List<String> getAllCategories() {
        List<String> cats = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM products ORDER BY category";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cats.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching categories", e);
        }
        return cats;
    }
}