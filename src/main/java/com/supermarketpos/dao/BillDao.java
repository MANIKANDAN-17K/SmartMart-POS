package com.supermarketpos.dao;

import com.supermarketpos.model.Bill;
import com.supermarketpos.model.Bill.PaymentMethod;
import com.supermarketpos.model.Bill.Status;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class BillDao {

    private static final Logger log = Logger.getLogger(BillDao.class.getName());

    public void insert(Connection conn, Bill bill) throws SQLException {
        String sql = """
            INSERT INTO bills
              (invoice_number, customer_id, cashier_id,
               subtotal, discount_amount, gst_amount, grand_total,
               payment_method, cash_paid, card_paid, upi_paid,
               amount_paid, balance, status, created_at, completed_at)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bill.getInvoiceNumber());
            if (bill.getCustomerId() != null) ps.setInt(2, bill.getCustomerId());
            else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, bill.getCashierId());
            ps.setBigDecimal(4,  bill.getSubtotal());
            ps.setBigDecimal(5,  bill.getDiscountAmount());
            ps.setBigDecimal(6,  bill.getGstAmount());
            ps.setBigDecimal(7,  bill.getGrandTotal());
            ps.setString(8,  bill.getPaymentMethod().name());
            ps.setBigDecimal(9,  bill.getCashPaid());
            ps.setBigDecimal(10, bill.getCardPaid());
            ps.setBigDecimal(11, bill.getUpiPaid());
            ps.setBigDecimal(12, bill.getAmountPaid());
            ps.setBigDecimal(13, bill.getBalance());
            ps.setString(14, bill.getStatus().name());
            ps.setTimestamp(15, Timestamp.valueOf(bill.getCreatedAt()));
            ps.setTimestamp(16, bill.getCompletedAt() != null
                    ? Timestamp.valueOf(bill.getCompletedAt()) : null);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Insert bill failed – no rows affected.");
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    bill.setId(rs.getInt(1));
                    log.info("Bill inserted: id=" + bill.getId() + " invoice=" + bill.getInvoiceNumber());
                }
            }
        }
    }

    public void updateStatus(Connection conn, int billId, Status status) throws SQLException {
        String sql = "UPDATE bills SET status = ?, completed_at = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setTimestamp(2, status == Status.COMPLETED
                    ? Timestamp.valueOf(java.time.LocalDateTime.now()) : null);
            ps.setInt(3, billId);
            ps.executeUpdate();
        }
    }

    public Optional<Bill> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM bills WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Bill> findByInvoiceNumber(Connection conn, String invoiceNumber) throws SQLException {
        String sql = "SELECT * FROM bills WHERE invoice_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public boolean invoiceNumberExists(Connection conn, String invoiceNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bills WHERE invoice_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Bill> findByDateRange(Connection conn,
                                      java.time.LocalDate from,
                                      java.time.LocalDate to) throws SQLException {
        String sql = """
            SELECT * FROM bills
            WHERE DATE(created_at) BETWEEN ? AND ?
              AND status = 'COMPLETED'
            ORDER BY created_at DESC
            """;
        List<Bill> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setInvoiceNumber(rs.getString("invoice_number"));
        int custId = rs.getInt("customer_id");
        if (!rs.wasNull()) b.setCustomerId(custId);
        b.setCashierId(rs.getInt("cashier_id"));
        b.setSubtotal(rs.getBigDecimal("subtotal"));
        b.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        b.setGstAmount(rs.getBigDecimal("gst_amount"));
        b.setGrandTotal(rs.getBigDecimal("grand_total"));
        b.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        b.setCashPaid(rs.getBigDecimal("cash_paid"));
        b.setCardPaid(rs.getBigDecimal("card_paid"));
        b.setUpiPaid(rs.getBigDecimal("upi_paid"));
        b.setAmountPaid(rs.getBigDecimal("amount_paid"));
        b.setBalance(rs.getBigDecimal("balance"));
        b.setStatus(Status.valueOf(rs.getString("status")));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) b.setCreatedAt(created.toLocalDateTime());
        Timestamp completed = rs.getTimestamp("completed_at");
        if (completed != null) b.setCompletedAt(completed.toLocalDateTime());
        return b;
    }
}