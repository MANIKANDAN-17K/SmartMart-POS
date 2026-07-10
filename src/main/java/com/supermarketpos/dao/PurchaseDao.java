package com.supermarketpos.dao;

import com.supermarketpos.report.PurchaseReport;
import com.supermarketpos.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PurchaseDao {

    private static final Logger LOGGER = Logger.getLogger(PurchaseDao.class.getName());

    public List<PurchaseReport> getPurchaseReport(LocalDate startDate, LocalDate endDate, String supplierName) {
        List<PurchaseReport> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT
                pu.id AS purchase_id,
                DATE(pu.purchase_date) AS purchase_date,
                s.name AS supplier_name,
                p.name AS product_name,
                pu.quantity AS quantity,
                pu.total_amount AS purchase_amount
            FROM purchases pu
            JOIN suppliers s ON pu.supplier_id = s.id
            JOIN products p ON pu.product_id = p.id
            WHERE DATE(pu.purchase_date) BETWEEN ? AND ?
            """);
        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(startDate));
        params.add(Date.valueOf(endDate));
        if (supplierName != null && !supplierName.isEmpty() && !supplierName.equals("All")) {
            sql.append(" AND s.name = ?");
            params.add(supplierName);
        }
        sql.append(" ORDER BY pu.purchase_date DESC");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PurchaseReport r = new PurchaseReport();
                r.setPurchaseId(rs.getInt("purchase_id"));
                r.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
                r.setSupplierName(rs.getString("supplier_name"));
                r.setProductName(rs.getString("product_name"));
                r.setQuantity(rs.getInt("quantity"));
                r.setPurchaseAmount(rs.getDouble("purchase_amount"));
                list.add(r);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching purchase report", e);
        }
        return list;
    }

    public List<String> getAllSupplierNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM suppliers ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching supplier names", e);
        }
        return names;
    }
}