package com.supermarketpos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BillItem {

    private int id;
    private int billId;
    private int productId;
    private String barcode;
    private String productName;
    private BigDecimal unitPrice      = BigDecimal.ZERO;
    private int quantity              = 1;
    private BigDecimal discountPercent = BigDecimal.ZERO;
    private BigDecimal discountAmount  = BigDecimal.ZERO;
    private BigDecimal gstPercent      = BigDecimal.ZERO;
    private BigDecimal gstAmount       = BigDecimal.ZERO;
    private BigDecimal lineTotal       = BigDecimal.ZERO;

    public BillItem() {}

    public BillItem(int productId, String barcode, String productName,
                    BigDecimal unitPrice, BigDecimal gstPercent) {
        this.productId   = productId;
        this.barcode     = barcode;
        this.productName = productName;
        this.unitPrice   = unitPrice;
        this.gstPercent  = gstPercent;
        recalculate();
    }

    public void recalculate() {
        BigDecimal base    = unitPrice.multiply(BigDecimal.valueOf(quantity));
        discountAmount     = base.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = base.subtract(discountAmount);
        gstAmount          = afterDiscount.multiply(gstPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        lineTotal          = afterDiscount.add(gstAmount);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; recalculate(); }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; recalculate(); }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; recalculate(); }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getGstPercent() { return gstPercent; }
    public void setGstPercent(BigDecimal gstPercent) { this.gstPercent = gstPercent; recalculate(); }
    public BigDecimal getGstAmount() { return gstAmount; }
    public BigDecimal getLineTotal() { return lineTotal; }
}