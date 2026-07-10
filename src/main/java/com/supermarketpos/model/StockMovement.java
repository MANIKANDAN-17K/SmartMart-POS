package com.supermarketpos.dao;

import com.supermarketpos.model.StockMovement;
import com.supermarketpos.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockMovementDao {

    private static final String SELECT_BASE =
            "SELECT sm.*, p.name AS product_name FROM stock_movements sm " +
                    "JOIN products p ON sm.product_id = p.id ";

    /**
     * Inserts using the caller's connection/transaction so it commits or rolls back
     * atomically with whatever stock change triggered it.
     */
    public int insert(Connection conn, StockMovement movement) throws SQLException {
        String sql = "INSERT INTO stock_movements " +
                "(product_id, movement_type, quantity, previous_stock, current_stock, " +
                "reference_number, performed_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, movement.getProductId());
            ps.setString(2, movement.getMovementType());
            ps.setInt(3, movement.getQuantity());
            ps.setInt(4, movement.getPreviousStock());
            ps.setInt(5, movement.getCurrentStock());
            ps.setString(6, movement.getReferenceNumber());
            ps.setString(7, movement.getPerformedBy());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<StockMovement> findAll() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY sm.movement_date DESC, sm.id DESC";
        List<StockMovement> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<StockMovement> findByProductId(int productId) throws SQLException {
        String sql = SELECT_BASE + "WHERE sm.product_id = ? ORDER BY sm.movement_date DESC, sm.id DESC";
        List<StockMovement> list = new ArrayList<>();
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

    public List<StockMovement> findByDateRange(LocalDate fromDate, LocalDate toDate) throws SQLException {
        String sql = SELECT_BASE +
                "WHERE DATE(sm.movement_date) BETWEEN ? AND ? ORDER BY sm.movement_date DESC, sm.id DESC";
        List<StockMovement> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private StockMovement mapRow(ResultSet rs) throws SQLException {
        StockMovement m = new StockMovement();
        m.setId(rs.getInt("id"));
        m.setProductId(rs.getInt("product_id"));
        m.setProductName(rs.getString("product_name"));
        m.setMovementType(rs.getString("movement_type"));
        m.setQuantity(rs.getInt("quantity"));
        m.setPreviousStock(rs.getInt("previous_stock"));
        m.setCurrentStock(rs.getInt("current_stock"));
        m.setReferenceNumber(rs.getString("reference_number"));
        m.setPerformedBy(rs.getString("performed_by"));
        Timestamp movementDate = rs.getTimestamp("movement_date");
        if (movementDate != null) m.setMovementDate(movementDate.toLocalDateTime());
        return m;
    }
}