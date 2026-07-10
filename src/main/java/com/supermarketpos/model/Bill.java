package com.supermarketpos.model;

import java.time.LocalDateTime;

public class Bill {
    private int id;
    private String invoiceNumber;
    private Integer customerId;
    private int createdBy;
    private double subtotal;
    private double taxAmount;
    private double discountAmount;
    private double totalAmount;
    private double profitAmount;
    private String paymentType;
    private LocalDateTime createdAt;

    public Bill() {
    }

    public Bill(int id, String invoiceNumber, Integer customerId, int createdBy, double subtotal,
                double taxAmount, double discountAmount, double totalAmount, double profitAmount,
                String paymentType, LocalDateTime createdAt) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.createdBy = createdBy;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.profitAmount = profitAmount;
        this.paymentType = paymentType;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getProfitAmount() { return profitAmount; }
    public void setProfitAmount(double profitAmount) { this.profitAmount = profitAmount; }
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}