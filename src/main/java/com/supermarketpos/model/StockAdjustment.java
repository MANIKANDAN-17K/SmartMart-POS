package com.supermarketpos.model;

import java.time.LocalDateTime;

public class StockAdjustment {

    public static final String TYPE_INCREASE = "INCREASE";
    public static final String TYPE_DECREASE = "DECREASE";

    private int id;
    private int productId;
    private String productName; // display only, populated via JOIN
    private String adjustmentType;
    private int adjustmentQuantity;
    private String reason;
    private String remarks;
    private Integer stockMovementId;
    private String performedBy;
    private LocalDateTime createdAt;

    public StockAdjustment() {
    }

    public StockAdjustment(int productId, String adjustmentType, int adjustmentQuantity,
                           String reason, String remarks, String performedBy) {
        this.productId = productId;
        this.adjustmentType = adjustmentType;
        this.adjustmentQuantity = adjustmentQuantity;
        this.reason = reason;
        this.remarks = remarks;
        this.performedBy = performedBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public int getAdjustmentQuantity() {
        return adjustmentQuantity;
    }

    public void setAdjustmentQuantity(int adjustmentQuantity) {
        this.adjustmentQuantity = adjustmentQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getStockMovementId() {
        return stockMovementId;
    }

    public void setStockMovementId(Integer stockMovementId) {
        this.stockMovementId = stockMovementId;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}