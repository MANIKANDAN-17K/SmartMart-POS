package com.supermarketpos.dao;

import com.supermarketpos.database.DatabaseInitializer;
import com.supermarketpos.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDao implements BaseDao<Product, Integer> {

    private static final String FIND_BY_ID_SQL = "SELECT * FROM products WHERE id = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM products";
    private static final String COUNT_ACTIVE_SQL = "SELECT COUNT(*) FROM products WHERE is_active = TRUE";
    private static final String COUNT_LOW_STOCK_SQL =
            "SELECT COUNT(*) FROM products WHERE is_active = TRUE AND quantity_in_stock <= reorder_level";
    private static final String INSERT_SQL =
            "INSERT INTO products (name, sku, category_id, cost_price, selling_price, quantity_in_stock, reorder_level, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE products SET name=?, sku=?, category_id=?, cost_price=?, selling_price=?, quantity_in_stock=?, reorder_level=?, is_active=? WHERE id=?";
    private static final String SOFT_DELETE_SQL = "UPDATE products SET is_active = FALSE WHERE id = ?";

    @Override
    public Optional<Product> findById(Integer id) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find product by id: " + id, e);
        }
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch products", e);
        }
        return products;
    }

    public int countActive() {
        return countWithQuery(COUNT_ACTIVE_SQL);
    }

    public int countLowStock() {
        return countWithQuery(COUNT_LOW_STOCK_SQL);
    }

    private int countWithQuery(String sql) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to run product count query", e);
        }
    }

    @Override
    public Product save(Product product) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bindForWrite(ps, product);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }
            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save product: " + product.getName(), e);
        }
    }

    @Override
    public Product update(Product product) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            bindForWrite(ps, product);
            ps.setInt(9, product.getId());
            ps.executeUpdate();
            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update product: " + product.getId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(SOFT_DELETE_SQL)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate product: " + id, e);
        }
    }

    private void bindForWrite(PreparedStatement ps, Product p) throws SQLException {
        ps.setString(1, p.getName());
        ps.setString(2, p.getSku());
        if (p.getCategoryId() != null) {
            ps.setInt(3, p.getCategoryId());
        } else {
            ps.setNull(3, Types.INTEGER);
        }
        ps.setDouble(4, p.getCostPrice());
        ps.setDouble(5, p.getSellingPrice());
        ps.setInt(6, p.getQuantityInStock());
        ps.setInt(7, p.getReorderLevel());
        ps.setBoolean(8, p.isActive());
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        int categoryIdRaw = rs.getInt("category_id");
        Integer categoryId = rs.wasNull() ? null : categoryIdRaw;
        Timestamp createdTs = rs.getTimestamp("created_at");
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("sku"),
                categoryId,
                rs.getDouble("cost_price"),
                rs.getDouble("selling_price"),
                rs.getInt("quantity_in_stock"),
                rs.getInt("reorder_level"),
                rs.getBoolean("is_active"),
                createdTs != null ? createdTs.toLocalDateTime() : null
        );
    }
}