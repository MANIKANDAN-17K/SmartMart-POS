package com.supermarketpos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private int id;
    private String name;
    private String barcode;
    private String sku;
    private int categoryId;
    private String categoryName;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private BigDecimal gstPercentage;
    private int stockQuantity;
    private String status = "ACTIVE";
    private String imagePath;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Product() {
    }

    public Product(String name, String barcode, String sku, int categoryId,
            BigDecimal costPrice, BigDecimal sellingPrice, BigDecimal gstPercentage,
            String imagePath, boolean active) {
        this.name = name;
        this.barcode = barcode;
        this.sku = sku;
        this.categoryId = categoryId;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.gstPercentage = gstPercentage;
        this.imagePath = imagePath;
        this.status = active ? "ACTIVE" : "INACTIVE";
    }

    public Product(int id, String name, String barcode, String sku, int categoryId,
            BigDecimal costPrice, BigDecimal sellingPrice, BigDecimal gstPercentage,
            int stockQuantity, String status, String imagePath, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.sku = sku;
        this.categoryId = categoryId;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.gstPercentage = gstPercentage;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public boolean isLowStock() {
        // Assume default reorder level of 5 if not config-driven
        return stockQuantity <= 5;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public String getStatusLabel() {
        return isActive() ? "Active" : "Inactive";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getGstPercentage() {
        return gstPercentage;
    }

    public BigDecimal getGstPercent() {
        return gstPercentage;
    }

    public void setGstPercentage(BigDecimal gstPercentage) {
        this.gstPercentage = gstPercentage;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}