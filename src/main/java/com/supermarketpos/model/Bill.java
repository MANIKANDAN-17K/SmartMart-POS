package com.supermarketpos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bill {

    public enum Status { DRAFT, COMPLETED, CANCELLED }
    public enum PaymentMethod { CASH, CARD, UPI, SPLIT }

    private int id;
    private String invoiceNumber;
    private Integer customerId;
    private String customerName;
    private int cashierId;
    private String cashierName;
    private List<BillItem> items = new ArrayList<>();

    private BigDecimal subtotal       = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal gstAmount      = BigDecimal.ZERO;
    private BigDecimal grandTotal     = BigDecimal.ZERO;

    private PaymentMethod paymentMethod = PaymentMethod.CASH;
    private BigDecimal cashPaid   = BigDecimal.ZERO;
    private BigDecimal cardPaid   = BigDecimal.ZERO;
    private BigDecimal upiPaid    = BigDecimal.ZERO;
    private BigDecimal amountPaid = BigDecimal.ZERO;
    private BigDecimal balance    = BigDecimal.ZERO;

    private Status status = Status.DRAFT;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public Bill() { this.createdAt = LocalDateTime.now(); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public int getCashierId() { return cashierId; }
    public void setCashierId(int cashierId) { this.cashierId = cashierId; }
    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }
    public List<BillItem> getItems() { return items; }
    public void setItems(List<BillItem> items) { this.items = items; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getGstAmount() { return gstAmount; }
    public void setGstAmount(BigDecimal gstAmount) { this.gstAmount = gstAmount; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getCashPaid() { return cashPaid; }
    public void setCashPaid(BigDecimal cashPaid) { this.cashPaid = cashPaid; }
    public BigDecimal getCardPaid() { return cardPaid; }
    public void setCardPaid(BigDecimal cardPaid) { this.cardPaid = cardPaid; }
    public BigDecimal getUpiPaid() { return upiPaid; }
    public void setUpiPaid(BigDecimal upiPaid) { this.upiPaid = upiPaid; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public int getItemCount() { return items.stream().mapToInt(BillItem::getQuantity).sum(); }
    public boolean isDraft()     { return status == Status.DRAFT; }
    public boolean isCompleted() { return status == Status.COMPLETED; }
    public boolean isCancelled() { return status == Status.CANCELLED; }
}