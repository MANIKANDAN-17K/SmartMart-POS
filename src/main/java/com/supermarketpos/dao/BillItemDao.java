package com.supermarketpos.dao;

import com.supermarketpos.model.BillItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillItemDao {

    public void insertAll(Connection conn, int billId, List<BillItem> items) throws SQLException {
        String sql = """
            INSERT INTO bill_items
              (bill_id, product_id, barcode, product_name,
               unit_price, quantity, discount_percent, discount_amount,
               gst_percent, gst_amount, line_total)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (BillItem item : items) {
                ps.setInt(1,    billId);
                ps.setInt(2,    item.getProductId());
                ps.setString(3, item.getBarcode());
                ps.setString(4, item.getProductName());
                ps.setBigDecimal(5,  item.getUnitPrice());
                ps.setInt(6,    item.getQuantity());
                ps.setBigDecimal(7,  item.getDiscountPercent());
                ps.setBigDecimal(8,  item.getDiscountAmount());
                ps.setBigDecimal(9,  item.getGstPercent());
                ps.setBigDecimal(10, item.getGstAmount());
                ps.setBigDecimal(11, item.getLineTotal());
                ps.addBatch();
            }
            ps.executeBatch();

            // Back-fill generated IDs
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int i = 0;
                while (rs.next() && i < items.size()) {
                    items.get(i++).setId(rs.getInt(1));
                }
            }
        }
    }

    public List<BillItem> findByBillId(Connection conn, int billId) throws SQLException {
        String sql = "SELECT * FROM bill_items WHERE bill_id = ? ORDER BY id";
        List<BillItem> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    private BillItem mapRow(ResultSet rs) throws SQLException {
        BillItem item = new BillItem();
        item.setId(rs.getInt("id"));
        item.setBillId(rs.getInt("bill_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setBarcode(rs.getString("barcode"));
        item.setProductName(rs.getString("product_name"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setQuantity(rs.getInt("quantity"));
        item.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        item.setGstPercent(rs.getBigDecimal("gst_percent"));
        // lineTotal is recomputed; discountAmount & gstAmount stored for reference
        item.recalculate();
        return item;
    }
    // ── Purchase history (add to existing BillDao) ────────────────────────────

    /**
     * Returns all COMPLETED bills for a customer, newest first.
     * Joins to users table to get cashier name.
     */
    public List<PurchaseHistoryRow> getPurchaseHistory(Connection conn, int customerId)
            throws SQLException {
        String sql = """
        SELECT b.invoice_number, b.created_at, b.grand_total,
               b.payment_method,
               (SELECT COUNT(*) FROM bill_items bi WHERE bi.bill_id = b.id) AS item_count,
               u.name AS cashier_name
          FROM bills b
          LEFT JOIN users u ON u.id = b.cashier_id
         WHERE b.customer_id = ?
           AND b.status = 'COMPLETED'
         ORDER BY b.created_at DESC
        """;
        List<PurchaseHistoryRow> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseHistoryRow row = new PurchaseHistoryRow();
                    row.setInvoiceNumber(rs.getString("invoice_number"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) row.setInvoiceDate(ts.toLocalDateTime());
                    row.setItemCount(rs.getInt("item_count"));
                    row.setGrandTotal(rs.getBigDecimal("grand_total"));
                    row.setPaymentMethod(rs.getString("payment_method"));
                    row.setCashierName(rs.getString("cashier_name"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

// ── PurchaseHistoryRow (inner/companion class – put in its own file if preferred) ──

    public static class PurchaseHistoryRow {
        private String invoiceNumber;
        private java.time.LocalDateTime invoiceDate;
        private int itemCount;
        private java.math.BigDecimal grandTotal;
        private String paymentMethod;
        private String cashierName;

        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String v) { this.invoiceNumber = v; }
        public java.time.LocalDateTime getInvoiceDate() { return invoiceDate; }
        public void setInvoiceDate(java.time.LocalDateTime v) { this.invoiceDate = v; }
        public int getItemCount() { return itemCount; }
        public void setItemCount(int v) { this.itemCount = v; }
        public java.math.BigDecimal getGrandTotal() { return grandTotal; }
        public void setGrandTotal(java.math.BigDecimal v) { this.grandTotal = v; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String v) { this.paymentMethod = v; }
        public String getCashierName() { return cashierName; }
        public void setCashierName(String v) { this.cashierName = v; }
    }
}