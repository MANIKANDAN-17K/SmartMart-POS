package com.supermarketpos.service;

import com.supermarketpos.dao.ProductDao;
import com.supermarketpos.dao.StockAdjustmentDao;
import com.supermarketpos.dao.StockMovementDao;
import com.supermarketpos.event.StockUpdateEvent;
import com.supermarketpos.model.StockAdjustment;
import com.supermarketpos.model.StockMovement;
import com.supermarketpos.util.DBConnection;
import com.supermarketpos.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class InventoryService {

    private static final Logger LOGGER = Logger.getLogger(InventoryService.class.getName());
    private static final int REASON_MAX_LENGTH = 255;
    private static final int REMARKS_MAX_LENGTH = 500;

    private final ProductDao productDao = new ProductDao();
    private final StockMovementDao stockMovementDao = new StockMovementDao();
    private final StockAdjustmentDao stockAdjustmentDao = new StockAdjustmentDao();

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    // ---------- Public API required by module contract ----------

    public int getCurrentStock(int productId) throws SQLException {
        return productDao.getCurrentStock(productId);
    }

    public List<ProductDao.InventoryRow> getLowStockProducts() throws SQLException {
        List<ProductDao.InventoryRow> all = productDao.findAllForInventory();
        List<ProductDao.InventoryRow> lowStock = new ArrayList<>();
        for (ProductDao.InventoryRow row : all) {
            if ("LOW_STOCK".equals(row.getStockStatus())) {
                lowStock.add(row);
            }
        }
        return lowStock;
    }

    public List<ProductDao.InventoryRow> getOutOfStockProducts() throws SQLException {
        List<ProductDao.InventoryRow> all = productDao.findAllForInventory();
        List<ProductDao.InventoryRow> outOfStock = new ArrayList<>();
        for (ProductDao.InventoryRow row : all) {
            if ("OUT_OF_STOCK".equals(row.getStockStatus())) {
                outOfStock.add(row);
            }
        }
        return outOfStock;
    }

    /**
     * Performs a manual stock adjustment (increase or decrease) inside a single transaction:
     * updates product stock, records a StockMovement, and records a StockAdjustment.
     * Rolls back entirely if any step fails. Stock is never allowed to go negative.
     */
    public StockAdjustment adjustStock(int productId, String adjustmentType, int adjustmentQuantity,
                                       String reason, String remarks, String performedBy)
            throws ValidationException, SQLException {

        String trimmedReason = ValidationUtil.trimOrEmpty(reason);
        String trimmedRemarks = ValidationUtil.trimOrEmpty(remarks);

        validateAdjustmentInput(adjustmentType, adjustmentQuantity, trimmedReason, trimmedRemarks);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            ProductDao.InventoryRow snapshot = productDao.getStockForUpdate(conn, productId);
            if (!snapshot.isActive()) {
                throw new ValidationException("Cannot adjust stock for an inactive product.");
            }

            int previousStock = snapshot.getCurrentStock();
            int newStock;
            String movementType;

            if (StockAdjustment.TYPE_INCREASE.equals(adjustmentType)) {
                productDao.increaseStock(conn, productId, adjustmentQuantity);
                newStock = previousStock + adjustmentQuantity;
                movementType = StockMovement.TYPE_ADJUSTMENT_INCREASE;
            } else {
                boolean success = productDao.decreaseStock(conn, productId, adjustmentQuantity);
                if (!success) {
                    throw new ValidationException("Insufficient stock. Current stock is " +
                            previousStock + ", cannot decrease by " + adjustmentQuantity + ".");
                }
                newStock = previousStock - adjustmentQuantity;
                movementType = StockMovement.TYPE_ADJUSTMENT_DECREASE;
            }

            StockMovement movement = new StockMovement(
                    productId, movementType, adjustmentQuantity, previousStock, newStock,
                    null, performedBy);
            int movementId = stockMovementDao.insert(conn, movement);

            StockAdjustment adjustment = new StockAdjustment(
                    productId, adjustmentType, adjustmentQuantity, trimmedReason, trimmedRemarks, performedBy);
            adjustment.setStockMovementId(movementId);
            int adjustmentId = stockAdjustmentDao.insert(conn, adjustment);
            adjustment.setId(adjustmentId);

            conn.commit();

            LOGGER.info("Stock adjusted: product id=" + productId + ", type=" + adjustmentType +
                    ", qty=" + adjustmentQuantity + ", " + previousStock + " -> " + newStock);
            if (StockAdjustment.TYPE_INCREASE.equals(adjustmentType)) {
                LOGGER.info("Stock increased: product id=" + productId);
            } else {
                LOGGER.info("Stock decreased: product id=" + productId);
            }

            StockUpdateEvent.publish(new StockUpdateEvent(
                    productId, snapshot.getProductName(), previousStock, newStock, movementType));

            return adjustment;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.severe("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                    // cleanup failure is non-fatal here
                }
            }
        }
    }

    public List<StockMovement> getStockMovements() throws SQLException {
        return stockMovementDao.findAll();
    }

    public List<StockMovement> getStockMovements(LocalDate fromDate, LocalDate toDate) throws SQLException {
        return stockMovementDao.findByDateRange(fromDate, toDate);
    }

    /**
     * Full movement history for a single product — used by the "View History" drill-down.
     */
    public List<StockMovement> getStockHistory(int productId) throws SQLException {
        LOGGER.info("Movement history viewed: product id=" + productId);
        return stockMovementDao.findByProductId(productId);
    }

    public List<StockAdjustment> getAdjustmentHistory(int productId) throws SQLException {
        return stockAdjustmentDao.findByProductId(productId);
    }

    public List<StockAdjustment> getAllAdjustments() throws SQLException {
        return stockAdjustmentDao.findAll();
    }

    public List<ProductDao.InventoryRow> searchInventory(String keyword) throws SQLException {
        if (ValidationUtil.isNullOrEmpty(keyword)) {
            return getCurrentInventory();
        }
        return productDao.searchInventory(keyword.trim());
    }

    // ---------- Additional supporting methods for UI (filters, summary cards) ----------

    public List<ProductDao.InventoryRow> getCurrentInventory() throws SQLException {
        LOGGER.info("Inventory viewed.");
        return productDao.findAllForInventory();
    }

    public List<ProductDao.InventoryRow> getInventoryByCategory(int categoryId) throws SQLException {
        return productDao.findInventoryByCategory(categoryId);
    }

    public List<ProductDao.InventoryRow> getInventoryByStatus(String status) throws SQLException {
        List<ProductDao.InventoryRow> all = productDao.findAllForInventory();
        List<ProductDao.InventoryRow> filtered = new ArrayList<>();
        for (ProductDao.InventoryRow row : all) {
            if (row.getStockStatus().equals(status)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    public InventorySummary getInventorySummary() throws SQLException {
        List<ProductDao.InventoryRow> all = productDao.findAllForInventory();
        int totalProducts = all.size();
        int totalStockItems = 0;
        int lowStockCount = 0;
        int outOfStockCount = 0;

        for (ProductDao.InventoryRow row : all) {
            totalStockItems += row.getCurrentStock();
            String status = row.getStockStatus();
            if ("LOW_STOCK".equals(status)) lowStockCount++;
            if ("OUT_OF_STOCK".equals(status)) outOfStockCount++;
        }

        return new InventorySummary(totalProducts, totalStockItems, lowStockCount, outOfStockCount);
    }

    public static class InventorySummary {
        private final int totalProducts;
        private final int totalStockItems;
        private final int lowStockCount;
        private final int outOfStockCount;

        public InventorySummary(int totalProducts, int totalStockItems, int lowStockCount, int outOfStockCount) {
            this.totalProducts = totalProducts;
            this.totalStockItems = totalStockItems;
            this.lowStockCount = lowStockCount;
            this.outOfStockCount = outOfStockCount;
        }

        public int getTotalProducts() { return totalProducts; }
        public int getTotalStockItems() { return totalStockItems; }
        public int getLowStockCount() { return lowStockCount; }
        public int getOutOfStockCount() { return outOfStockCount; }
    }

    private void validateAdjustmentInput(String adjustmentType, int quantity, String reason, String remarks)
            throws ValidationException {

        if (!StockAdjustment.TYPE_INCREASE.equals(adjustmentType) &&
                !StockAdjustment.TYPE_DECREASE.equals(adjustmentType)) {
            throw new ValidationException("Adjustment type must be INCREASE or DECREASE.");
        }
        if (quantity <= 0) {
            throw new ValidationException("Adjustment quantity must be greater than zero.");
        }
        if (ValidationUtil.isNullOrEmpty(reason)) {
            throw new ValidationException("Adjustment reason is required.");
        }
        if (ValidationUtil.exceedsMaxLength(reason, REASON_MAX_LENGTH)) {
            throw new ValidationException("Reason cannot exceed " + REASON_MAX_LENGTH + " characters.");
        }
        if (ValidationUtil.exceedsMaxLength(remarks, REMARKS_MAX_LENGTH)) {
            throw new ValidationException("Remarks cannot exceed " + REMARKS_MAX_LENGTH + " characters.");
        }
    }
}