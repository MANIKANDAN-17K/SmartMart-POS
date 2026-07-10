package com.supermarketpos.dao;

import com.supermarketpos.database.DatabaseInitializer;
import com.supermarketpos.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillDao implements BaseDao<Bill, Integer> {

    private static final String FIND_BY_ID_SQL = "SELECT * FROM bills WHERE id = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM bills";
    private static final String SUM_TODAY_SALES_SQL =
            "SELECT COALESCE(SUM(total_amount), 0) FROM bills WHERE DATE(created_at) = CURDATE()";
    private static final String SUM_TODAY_PROFIT_SQL =
            "SELECT COALESCE(SUM(profit_amount), 0) FROM bills WHERE DATE(created_at) = CURDATE()";
    private static final String FIND_RECENT_SQL =
            "SELECT b.invoice_number, COALESCE(c.name, 'Walk-in Customer') AS customer_name, " +
                    "b.total_amount, b.payment_type, b.created_at " +
                    "FROM bills b LEFT JOIN customers c ON b.customer_id = c.id " +
                    "ORDER BY b.created_at DESC LIMIT ?";
    private static final String INSERT_SQL =
            "INSERT INTO bills (invoice_number, customer_id, created_by, subtotal, tax_amount, discount_amount, total_amount, profit_amount, payment_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    public Optional<Bill> findById(Integer id) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find bill by id: " + id, e);
        }
    }

    @Override
    public List<Bill> findAll() {
        List<Bill> bills = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bills.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch bills", e);
        }
        return bills;
    }

    public double sumTodaySales() {
        return sumWithQuery(SUM_TODAY_SALES_SQL);
    }

    public double sumTodayProfit() {
        return sumWithQuery(SUM_TODAY_PROFIT_SQL);
    }

    private double sumWithQuery(String sql) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            double value = rs.next() ? rs.getDouble(1) : 0;
            return Math.max(value, 0); // dashboard must never show negative sales
        } catch (SQLException e) {
            throw new RuntimeException("Failed to run bill sum query", e);
        }
    }

    public List<RecentBill> findRecent(int limit) {
        List<RecentBill> results = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_RECENT_SQL)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdTs = rs.getTimestamp("created_at");
                    results.add(new RecentBill(
                            rs.getString("invoice_number"),
                            rs.getString("customer_name"),
                            rs.getDouble("total_amount"),
                            rs.getString("payment_type"),
                            createdTs != null ? createdTs.toLocalDateTime() : null
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch recent bills", e);
        }
        return results;
    }

    @Override
    public Bill save(Bill bill) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bill.getInvoiceNumber());
            if (bill.getCustomerId() != null) {
                ps.setInt(2, bill.getCustomerId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, bill.getCreatedBy());
            ps.setDouble(4, bill.getSubtotal());
            ps.setDouble(5, bill.getTaxAmount());
            ps.setDouble(6, bill.getDiscountAmount());
            ps.setDouble(7, bill.getTotalAmount());
            ps.setDouble(8, bill.getProfitAmount());
            ps.setString(9, bill.getPaymentType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    bill.setId(keys.getInt(1));
                }
            }
            return bill;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save bill: " + bill.getInvoiceNumber(), e);
        }
    }

    @Override
    public Bill update(Bill bill) {
        throw new UnsupportedOperationException("Bills are immutable once created; use adjustments instead");
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Bills cannot be deleted; use adjustments/refunds instead");
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        int customerIdRaw = rs.getInt("customer_id");
        Integer customerId = rs.wasNull() ? null : customerIdRaw;
        Timestamp createdTs = rs.getTimestamp("created_at");
        return new Bill(
                rs.getInt("id"),
                rs.getString("invoice_number"),
                customerId,
                rs.getInt("created_by"),
                rs.getDouble("subtotal"),
                rs.getDouble("tax_amount"),
                rs.getDouble("discount_amount"),
                rs.getDouble("total_amount"),
                rs.getDouble("profit_amount"),
                rs.getString("payment_type"),
                createdTs != null ? createdTs.toLocalDateTime() : null
        );
    }

    /** Lightweight projection for the Dashboard's recent-bills table. */
    public static final class RecentBill {
        private final String invoiceNumber;
        private final String customerName;
        private final double amount;
        private final String paymentType;
        private final LocalDateTime date;

        public RecentBill(String invoiceNumber, String customerName, double amount,
                          String paymentType, LocalDateTime date) {
            this.invoiceNumber = invoiceNumber;
            this.customerName = customerName;
            this.amount = amount;
            this.paymentType = paymentType;
            this.date = date;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getCustomerName() { return customerName; }
        public double getAmount() { return amount; }
        public String getPaymentType() { return paymentType; }
        public LocalDateTime getDate() { return date; }
    }
}