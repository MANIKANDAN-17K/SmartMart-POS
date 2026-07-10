package com.supermarketpos.dao;

import com.supermarketpos.model.StockAdjustment;
import com.supermarketpos.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockAdjustmentDao {

    private static final String SELECT_BASE =
            "SELECT sa.*, p.name AS product_name FROM stock_adjustments sa " +
                    "JOIN products p ON sa.product_id = p.id ";

    public int insert(Connection conn, StockAdjustment adjustment) throws SQLException {
        String sql = "INSERT INTO stock_adjustments " +
                "(product_id, adjustment_type, adjustment_quantity, reason, remarks, " +
                "stock_movement_id, performed_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, adjustment.getProductId());
            ps.setString(2, adjustment.getAdjustmentType());
            ps.setInt(3, adjustment.getAdjustmentQuantity());
            ps.setString(4, adjustment.getReason());
            ps.setString(5, adjustment.getRemarks());
            if (adjustment.getStockMovementId() != null) {
                ps.setInt(6, adjustment.getStockMovementId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, adjustment.getPerformedBy());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<StockAdjustment> findAll() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY sa.created_at DESC, sa.id DESC";
        List<StockAdjustment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<StockAdjustment> findByProductId(int productId) throws SQLException {
        String sql = SELECT_BASE + "WHERE sa.product_id = ? ORDER BY sa.created_at DESC, sa.id DESC";
        List<StockAdjustment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private StockAdjustment mapRow(ResultSet rs) throws SQLException {
        StockAdjustment a = new StockAdjustment();
        a.setId(rs.getInt("id"));
        a.setProductId(rs.getInt("product_id"));
        a.setProductName(rs.getString("product_name"));
        a.setAdjustmentType(rs.getString("adjustment_type"));
        a.setAdjustmentQuantity(rs.getInt("adjustment_quantity"));
        a.setReason(rs.getString("reason"));
        a.setRemarks(rs.getString("remarks"));
        int movementId = rs.getInt("stock_movement_id");
        a.setStockMovementId(rs.wasNull() ? null : movementId);
        a.setPerformedBy(rs.getString("performed_by"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) a.setCreatedAt(createdAt.toLocalDateTime());
        return a;
    }
}