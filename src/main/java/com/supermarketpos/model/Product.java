package com.supermarketpos.model;

import java.time.LocalDateTime;

public class Product {
    private int id;
    private String name;
    private String sku;
    private Integer categoryId;
    private double costPrice;
    private double sellingPrice;
    private int quantityInStock;
    private int reorderLevel;
    private boolean active;
    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(int id, String name, String sku, Integer categoryId, double costPrice,
                   double sellingPrice, int quantityInStock, int reorderLevel, boolean active,
                   LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.categoryId = categoryId;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.quantityInStock = quantityInStock;
        this.reorderLevel = reorderLevel;
        this.active = active;
        this.createdAt = createdAt;
    }

    public boolean isLowStock() {
        return quantityInStock <= reorderLevel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }
    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}