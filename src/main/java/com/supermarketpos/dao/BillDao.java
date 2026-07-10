package com.supermarketpos.dao;

import com.supermarketpos.model.Bill;
import com.supermarketpos.model.Bill.PaymentMethod;
import com.supermarketpos.model.Bill.Status;
import com.supermarketpos.report.*;
import com.supermarketpos.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
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
            if (bill.getCustomerId() != null)
                ps.setInt(2, bill.getCustomerId());
            else
                ps.setNull(2, Types.INTEGER);
            ps.setInt(3, bill.getCashierId());
            ps.setBigDecimal(4, bill.getSubtotal());
            ps.setBigDecimal(5, bill.getDiscountAmount());
            ps.setBigDecimal(6, bill.getGstAmount());
            ps.setBigDecimal(7, bill.getGrandTotal());
            ps.setString(8, bill.getPaymentMethod().name());
            ps.setBigDecimal(9, bill.getCashPaid());
            ps.setBigDecimal(10, bill.getCardPaid());
            ps.setBigDecimal(11, bill.getUpiPaid());
            ps.setBigDecimal(12, bill.getAmountPaid());
            ps.setBigDecimal(13, bill.getBalance());
            ps.setString(14, bill.getStatus().name());
            ps.setTimestamp(15, Timestamp.valueOf(bill.getCreatedAt()));
            ps.setTimestamp(16, bill.getCompletedAt() != null
                    ? Timestamp.valueOf(bill.getCompletedAt())
                    : null);

            int rows = ps.executeUpdate();
            if (rows == 0)
                throw new SQLException("Insert bill failed – no rows affected.");
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
                    ? Timestamp.valueOf(java.time.LocalDateTime.now())
                    : null);
            ps.setInt(3, billId);
            ps.executeUpdate();
        }
    }

    public Optional<Bill> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM bills WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Bill> findByInvoiceNumber(Connection conn, String invoiceNumber) throws SQLException {
        String sql = "SELECT * FROM bills WHERE invoice_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
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
                while (rs.next())
                    result.add(mapRow(rs));
            }
        }
        return result;
    }

    private static final Logger LOGGER = Logger.getLogger(BillDao.class.getName());

    public DailySalesReport getDailySalesReport(LocalDate date) {
        DailySalesReport report = new DailySalesReport();
        report.setDate(date);
        String sql = """
                SELECT
                    COUNT(*) AS total_bills,
                    COALESCE(SUM(total_amount), 0) AS total_sales,
                    COALESCE(SUM(gst_amount), 0) AS total_gst,
                    COALESCE(SUM(discount_amount), 0) AS total_discount,
                    COALESCE(AVG(total_amount), 0) AS avg_bill
                FROM bills
                WHERE DATE(bill_date) = ?
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                report.setTotalBills(rs.getInt("total_bills"));
                report.setTotalSales(rs.getDouble("total_sales"));
                report.setTotalGst(rs.getDouble("total_gst"));
                report.setTotalDiscount(rs.getDouble("total_discount"));
                report.setAverageBillValue(rs.getDouble("avg_bill"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching daily sales report", e);
        }
        return report;
    }

    public MonthlySalesReport getMonthlySalesReport(int month, int year) {
        MonthlySalesReport report = new MonthlySalesReport();
        report.setMonth(month);
        report.setYear(year);
        String sql = """
                SELECT
                    COUNT(*) AS total_bills,
                    COALESCE(SUM(total_amount), 0) AS total_revenue
                FROM bills
                WHERE MONTH(bill_date) = ? AND YEAR(bill_date) = ?
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                report.setTotalBills(rs.getInt("total_bills"));
                report.setTotalRevenue(rs.getDouble("total_revenue"));
                int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
                report.setAverageDailySales(report.getTotalRevenue() / daysInMonth);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching monthly sales report", e);
        }
        return report;
    }

    public List<ProfitReport> getProfitReport(LocalDate startDate, LocalDate endDate) {
        List<ProfitReport> list = new ArrayList<>();
        String sql = """
                SELECT
                    DATE(b.bill_date) AS report_date,
                    COALESCE(SUM(b.total_amount), 0) AS revenue,
                    COALESCE(SUM(bi.quantity * p.purchase_price), 0) AS cost
                FROM bills b
                JOIN bill_items bi ON b.id = bi.bill_id
                JOIN products p ON bi.product_id = p.id
                WHERE DATE(b.bill_date) BETWEEN ? AND ?
                GROUP BY DATE(b.bill_date)
                ORDER BY report_date
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProfitReport r = new ProfitReport();
                r.setDate(rs.getDate("report_date").toLocalDate());
                double revenue = rs.getDouble("revenue");
                double cost = rs.getDouble("cost");
                double profit = revenue - cost;
                double pct = revenue > 0 ? (profit / revenue) * 100 : 0;
                r.setRevenue(revenue);
                r.setCost(cost);
                r.setGrossProfit(profit);
                r.setProfitPercentage(pct);
                list.add(r);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching profit report", e);
        }
        return list;
    }

    public List<ProductSalesReport> getProductSalesReport(LocalDate startDate, LocalDate endDate,
            String category, String productName) {
        List<ProductSalesReport> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.id AS product_id,
                    p.name AS product_name,
                    p.category AS category,
                    COALESCE(SUM(bi.quantity), 0) AS quantity_sold,
                    COALESCE(SUM(bi.quantity * bi.unit_price), 0) AS revenue
                FROM bill_items bi
                JOIN products p ON bi.product_id = p.id
                JOIN bills b ON bi.bill_id = b.id
                WHERE DATE(b.bill_date) BETWEEN ? AND ?
                """);
        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(startDate));
        params.add(Date.valueOf(endDate));
        if (category != null && !category.isEmpty() && !category.equals("All")) {
            sql.append(" AND p.category = ?");
            params.add(category);
        }
        if (productName != null && !productName.isEmpty()) {
            sql.append(" AND p.name LIKE ?");
            params.add("%" + productName + "%");
        }
        sql.append(" GROUP BY p.id, p.name, p.category ORDER BY quantity_sold DESC");
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductSalesReport r = new ProductSalesReport();
                r.setProductId(rs.getInt("product_id"));
                r.setProductName(rs.getString("product_name"));
                r.setCategory(rs.getString("category"));
                r.setQuantitySold(rs.getInt("quantity_sold"));
                r.setRevenue(rs.getDouble("revenue"));
                list.add(r);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching product sales report", e);
        }
        return list;
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setInvoiceNumber(rs.getString("invoice_number"));
        int custId = rs.getInt("customer_id");
        if (!rs.wasNull())
            b.setCustomerId(custId);
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
        if (created != null)
            b.setCreatedAt(created.toLocalDateTime());
        Timestamp completed = rs.getTimestamp("completed_at");
        if (completed != null)
            b.setCompletedAt(completed.toLocalDateTime());
        return b;
    }

    public double sumTodaySales() {
        String sql = "SELECT COALESCE(SUM(grand_total), 0) FROM bills WHERE DATE(created_at) = CURDATE() AND status = 'COMPLETED'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error summing today's sales", e);
        }
        return 0.0;
    }

    public double sumTodayProfit() {
        String sql = """
                SELECT COALESCE(SUM(bi.quantity * (bi.unit_price - p.cost_price)), 0)
                FROM bill_items bi
                JOIN bills b ON bi.bill_id = b.id
                JOIN products p ON bi.product_id = p.id
                WHERE DATE(b.created_at) = CURDATE() AND b.status = 'COMPLETED'
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error summing today's profit", e);
        }
        return 0.0;
    }

    public static class RecentBill {
        private String invoiceNumber;
        private String customerName;
        private double amount;
        private String paymentType;
        private LocalDateTime date;

        public String getInvoiceNumber() {
            return invoiceNumber;
        }

        public void setInvoiceNumber(String val) {
            this.invoiceNumber = val;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String val) {
            this.customerName = val;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double val) {
            this.amount = val;
        }

        public String getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(String val) {
            this.paymentType = val;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public void setDate(LocalDateTime val) {
            this.date = val;
        }
    }

    public List<RecentBill> findRecent(int limit) {
        List<RecentBill> list = new ArrayList<>();
        String sql = """
                SELECT b.invoice_number, COALESCE(c.name, 'Walk-in') AS customer_name, b.grand_total, b.payment_method, b.created_at
                FROM bills b
                LEFT JOIN customers c ON b.customer_id = c.id
                ORDER BY b.created_at DESC
                LIMIT ?
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RecentBill rb = new RecentBill();
                    rb.setInvoiceNumber(rs.getString("invoice_number"));
                    rb.setCustomerName(rs.getString("customer_name"));
                    rb.setAmount(rs.getDouble("grand_total"));
                    rb.setPaymentType(rs.getString("payment_method"));
                    rb.setDate(rs.getTimestamp("created_at").toLocalDateTime());
                    list.add(rb);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching recent bills", e);
        }
        return list;
    }

    public List<PurchaseHistoryRow> getPurchaseHistory(Connection conn, int customerId)
            throws SQLException {
        String sql = """
                SELECT b.invoice_number, b.created_at, b.grand_total,
                       b.payment_method,
                       (SELECT COUNT(*) FROM bill_items bi WHERE bi.bill_id = b.id) AS item_count,
                       u.username AS cashier_name
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
                    if (ts != null)
                        row.setInvoiceDate(ts.toLocalDateTime());
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

    public static class PurchaseHistoryRow {
        private String invoiceNumber;
        private java.time.LocalDateTime invoiceDate;
        private int itemCount;
        private java.math.BigDecimal grandTotal;
        private String paymentMethod;
        private String cashierName;

        public String getInvoiceNumber() {
            return invoiceNumber;
        }

        public void setInvoiceNumber(String v) {
            this.invoiceNumber = v;
        }

        public java.time.LocalDateTime getInvoiceDate() {
            return invoiceDate;
        }

        public void setInvoiceDate(java.time.LocalDateTime v) {
            this.invoiceDate = v;
        }

        public int getItemCount() {
            return itemCount;
        }

        public void setItemCount(int v) {
            this.itemCount = v;
        }

        public java.math.BigDecimal getGrandTotal() {
            return grandTotal;
        }

        public void setGrandTotal(java.math.BigDecimal v) {
            this.grandTotal = v;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String v) {
            this.paymentMethod = v;
        }

        public String getCashierName() {
            return cashierName;
        }

        public void setCashierName(String v) {
            this.cashierName = v;
        }
    }
}