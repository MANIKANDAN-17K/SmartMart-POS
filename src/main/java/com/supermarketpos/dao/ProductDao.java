package com.supermarketpos.dao;

import com.supermarketpos.model.Product;
import com.supermarketpos.report.StockReport;
import com.supermarketpos.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDao {
    private static final Logger LOGGER = Logger.getLogger(ProductDao.class.getName());

    public Optional<Product> findByBarcode(Connection conn, String barcode) throws SQLException {
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.barcode = ? AND p.status = 'ACTIVE'
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Product findByBarcode(String barcode) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findByBarcode(conn, barcode).orElse(null);
        }
    }

    public List<Product> searchByName(Connection conn, String keyword) throws SQLException {
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.name LIKE ? AND p.status = 'ACTIVE' LIMIT 20
                """;
        List<Product> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(mapRow(rs));
            }
        }
        return result;
    }

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
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Product findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findById(conn, id).orElse(null);
        }
    }

    public boolean existsByBarcode(String barcode, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM products WHERE barcode = ?"
                : "SELECT COUNT(*) FROM products WHERE barcode = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode.trim());
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean existsBySku(String sku, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM products WHERE sku = ?"
                : "SELECT COUNT(*) FROM products WHERE sku = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku.trim());
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int create(Product product) throws SQLException {
        String sql = """
                INSERT INTO products (name, barcode, sku, category_id, cost_price, selling_price, gst_percent, image_path, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getBarcode());
            ps.setString(3, product.getSku());
            if (product.getCategoryId() > 0) {
                ps.setInt(4, product.getCategoryId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setBigDecimal(5, product.getCostPrice());
            ps.setBigDecimal(6, product.getSellingPrice());
            ps.setBigDecimal(7, product.getGstPercentage());
            ps.setString(8, product.getImagePath());
            ps.setString(9, product.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public void update(Product product) throws SQLException {
        String sql = """
                UPDATE products
                SET name = ?, barcode = ?, sku = ?, category_id = ?, cost_price = ?, selling_price = ?,
                    gst_percent = ?, image_path = ?, status = ?
                WHERE id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getBarcode());
            ps.setString(3, product.getSku());
            if (product.getCategoryId() > 0) {
                ps.setInt(4, product.getCategoryId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setBigDecimal(5, product.getCostPrice());
            ps.setBigDecimal(6, product.getSellingPrice());
            ps.setBigDecimal(7, product.getGstPercentage());
            ps.setString(8, product.getImagePath());
            ps.setString(9, product.getStatus());
            ps.setInt(10, product.getId());
            ps.executeUpdate();
        }
    }

    public void setActiveStatus(int id, boolean active) throws SQLException {
        String sql = "UPDATE products SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, active ? "ACTIVE" : "INACTIVE");
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public List<Product> findAll() throws SQLException {
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                ORDER BY p.name
                """;
        List<Product> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<Product> findByCategory(int categoryId) throws SQLException {
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.category_id = ?
                ORDER BY p.name
                """;
        List<Product> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    public Product findBySku(String sku) throws SQLException {
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.sku = ?
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Product> search(String keyword) throws SQLException {
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.name LIKE ? OR p.barcode LIKE ? OR p.sku LIKE ?
                ORDER BY p.name
                """;
        List<Product> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            String wildcard = "%" + keyword.trim() + "%";
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setBarcode(rs.getString("barcode"));
        p.setName(rs.getString("name"));
        p.setSku(rs.getString("sku"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCostPrice(rs.getBigDecimal("cost_price"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setGstPercentage(rs.getBigDecimal("gst_percent"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setStatus(rs.getString("status"));
        p.setImagePath(rs.getString("image_path"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        try {
            p.setCategoryName(rs.getString("category_name"));
        } catch (SQLException e) {
            // column category_name is not present, ignore
        }
        return p;
    }

    public List<StockReport> getStockReport(String category, String stockStatus) {
        List<StockReport> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.id AS product_id,
                    p.name AS product_name,
                    c.name AS category,
                    p.stock_quantity AS current_stock,
                    p.reorder_level AS reorder_level
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (category != null && !category.isEmpty() && !category.equals("All")) {
            sql.append(" AND c.name = ?");
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
        String sql = """
                SELECT DISTINCT c.name AS category
                FROM products p
                JOIN categories c ON p.category_id = c.id
                ORDER BY c.name
                """;
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

    public int getCurrentStock(int productId) throws SQLException {
        String sql = "SELECT stock_quantity FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_quantity");
                }
            }
        }
        return 0;
    }

    public List<InventoryRow> findAllForInventory() throws SQLException {
        String sql = """
                SELECT p.id, p.barcode, p.sku, p.name AS product_name, c.name AS category_name,
                       p.stock_quantity, p.reorder_level, p.status, p.updated_at
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                """;
        List<InventoryRow> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapInventoryRow(rs));
            }
        }
        return list;
    }

    public InventoryRow getStockForUpdate(Connection conn, int productId) throws SQLException {
        String sql = """
                SELECT p.id, p.barcode, p.sku, p.name AS product_name, c.name AS category_name,
                       p.stock_quantity, p.reorder_level, p.status, p.updated_at
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.id = ? FOR UPDATE
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapInventoryRow(rs);
                }
            }
        }
        throw new SQLException("Product not found: " + productId);
    }

    public void increaseStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    public boolean decreaseStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    public List<InventoryRow> searchInventory(String keyword) throws SQLException {
        String sql = """
                SELECT p.id, p.barcode, p.sku, p.name AS product_name, c.name AS category_name,
                       p.stock_quantity, p.reorder_level, p.status, p.updated_at
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.name LIKE ? OR p.sku LIKE ? OR p.barcode LIKE ?
                """;
        List<InventoryRow> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            String wildcard = "%" + keyword + "%";
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInventoryRow(rs));
                }
            }
        }
        return list;
    }

    public List<InventoryRow> findInventoryByCategory(int categoryId) throws SQLException {
        String sql = """
                SELECT p.id, p.barcode, p.sku, p.name AS product_name, c.name AS category_name,
                       p.stock_quantity, p.reorder_level, p.status, p.updated_at
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.category_id = ?
                """;
        List<InventoryRow> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInventoryRow(rs));
                }
            }
        }
        return list;
    }

    private InventoryRow mapInventoryRow(ResultSet rs) throws SQLException {
        InventoryRow row = new InventoryRow();
        row.setProductId(rs.getInt("id"));
        row.setBarcode(rs.getString("barcode"));
        row.setSku(rs.getString("sku"));
        row.setProductName(rs.getString("product_name"));
        row.setCategoryName(rs.getString("category_name"));
        int stock = rs.getInt("stock_quantity");
        int reorder = rs.getInt("reorder_level");
        row.setCurrentStock(stock);
        row.setMinStockQuantity(reorder);
        row.setStatus(rs.getString("status"));

        String stockStatus = "IN_STOCK";
        if (stock == 0) {
            stockStatus = "OUT_OF_STOCK";
        } else if (stock <= reorder) {
            stockStatus = "LOW_STOCK";
        }
        row.setStockStatus(stockStatus);

        Timestamp ts = rs.getTimestamp("updated_at");
        if (ts != null) {
            row.setUpdatedAt(ts.toLocalDateTime());
        }
        return row;
    }

    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countLowStock() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE stock_quantity <= reorder_level AND status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public static class InventoryRow {
        private int productId;
        private String barcode;
        private String sku;
        private String productName;
        private String categoryName;
        private int currentStock;
        private int minStockQuantity;
        private String stockStatus;
        private String status;
        private java.time.LocalDateTime updatedAt;

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public int getCurrentStock() {
            return currentStock;
        }

        public void setCurrentStock(int currentStock) {
            this.currentStock = currentStock;
        }

        public int getMinStockQuantity() {
            return minStockQuantity;
        }

        public void setMinStockQuantity(int minStockQuantity) {
            this.minStockQuantity = minStockQuantity;
        }

        public String getStockStatus() {
            return stockStatus;
        }

        public void setStockStatus(String stockStatus) {
            this.stockStatus = stockStatus;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public java.time.LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public boolean isActive() {
            return "ACTIVE".equalsIgnoreCase(status);
        }
    }
}