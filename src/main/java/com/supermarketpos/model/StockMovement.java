package com.supermarketpos.model;

import java.time.LocalDateTime;

public class StockMovement {

    public static final String TYPE_ADJUSTMENT_INCREASE = "ADJUSTMENT_INCREASE";
    public static final String TYPE_ADJUSTMENT_DECREASE = "ADJUSTMENT_DECREASE";

    private int id;
    private int productId;
    private String productName;
    private String movementType;
    private int quantity;
    private int previousStock;
    private int currentStock;
    private String referenceNumber;
    private String performedBy;
    private LocalDateTime movementDate;

    public StockMovement() {
    }

    public StockMovement(int productId, String movementType, int quantity, int previousStock, int currentStock,
            String referenceNumber, String performedBy) {
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.currentStock = currentStock;
        this.referenceNumber = referenceNumber;
        this.performedBy = performedBy;
        this.movementDate = LocalDateTime.now();
    }

    // Getters and Setters
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

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(int previousStock) {
        this.previousStock = previousStock;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }
}