package com.supermarketpos.service;

import com.supermarketpos.dao.BillDao;
import com.supermarketpos.dao.BillItemDao;
import com.supermarketpos.dao.ProductDao;
import com.supermarketpos.event.BillCreatedEvent;
import com.supermarketpos.model.Bill;
import com.supermarketpos.model.Bill.PaymentMethod;
import com.supermarketpos.model.Bill.Status;
import com.supermarketpos.model.BillItem;
import com.supermarketpos.model.Product;
import com.supermarketpos.util.InvoiceNumberUtil;
import com.supermarketpos.util.ValidationUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BillingService {

    private static final Logger log = Logger.getLogger(BillingService.class.getName());

    private final BillDao    billDao    = new BillDao();
    private final BillItemDao billItemDao = new BillItemDao();
    private final ProductDao productDao = new ProductDao();

    // ── bill lifecycle ────────────────────────────────────────────────────────

    public Bill createBill(int cashierId, String cashierName) {
        Bill bill = new Bill();
        bill.setCashierId(cashierId);
        bill.setCashierName(cashierName);
        bill.setInvoiceNumber(InvoiceNumberUtil.generate());
        log.info("Billing screen opened. New bill created: " + bill.getInvoiceNumber());
        return bill;
    }

    // ── item management ───────────────────────────────────────────────────────

    /**
     * Adds a product to the bill by product object.
     * If already in cart, increments quantity instead of adding a duplicate row.
     */
    public void addItem(Bill bill, Product product, int quantity) {
        ValidationUtil.requireTrue(bill.isDraft(), "Cannot modify a completed or cancelled bill.");
        ValidationUtil.requireTrue("ACTIVE".equalsIgnoreCase(product.getStatus()),
                "Product is not active: " + product.getName());
        ValidationUtil.requireTrue(product.getStockQuantity() > 0,
                "Product is out of stock: " + product.getName());
        ValidationUtil.requireTrue(quantity > 0, "Quantity must be greater than zero.");

        // Check if product already in cart
        Optional<BillItem> existing = bill.getItems().stream()
                .filter(i -> i.getProductId() == product.getId())
                .findFirst();

        if (existing.isPresent()) {
            BillItem item = existing.get();
            int newQty = item.getQuantity() + quantity;
            ValidationUtil.requireTrue(newQty <= product.getStockQuantity(),
                    "Insufficient stock. Available: " + product.getStockQuantity());
            item.setQuantity(newQty);
            log.info("Product quantity updated in cart: " + product.getName() + " qty=" + newQty);
        } else {
            ValidationUtil.requireTrue(quantity <= product.getStockQuantity(),
                    "Insufficient stock. Available: " + product.getStockQuantity());
            BillItem item = new BillItem(
                    product.getId(),
                    product.getBarcode(),
                    product.getName(),
                    product.getSellingPrice(),
                    product.getGstPercent()
            );
            item.setQuantity(quantity);
            bill.getItems().add(item);
            log.info("Product added to cart: " + product.getName());
        }
        calculateTotals(bill);
    }

    public void removeItem(Bill bill, int productId) {
        ValidationUtil.requireTrue(bill.isDraft(), "Cannot modify a completed bill.");
        boolean removed = bill.getItems().removeIf(i -> i.getProductId() == productId);
        if (removed) {
            calculateTotals(bill);
            log.info("Product removed from cart. productId=" + productId);
        }
    }

    public void updateQuantity(Bill bill, int productId, int newQuantity,
                               int availableStock) {
        ValidationUtil.requireTrue(bill.isDraft(), "Cannot modify a completed bill.");
        ValidationUtil.requireTrue(newQuantity > 0, "Quantity must be greater than zero.");
        ValidationUtil.requireTrue(newQuantity <= availableStock,
                "Insufficient stock. Available: " + availableStock);

        bill.getItems().stream()
                .filter(i -> i.getProductId() == productId)
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(newQuantity);
                    calculateTotals(bill);
                    log.info("Quantity updated. productId=" + productId + " qty=" + newQuantity);
                });
    }

    public void applyItemDiscount(Bill bill, int productId, BigDecimal discountPercent) {
        ValidationUtil.requireTrue(bill.isDraft(), "Cannot modify a completed bill.");
        ValidationUtil.requireTrue(discountPercent.compareTo(BigDecimal.ZERO) >= 0
                        && discountPercent.compareTo(BigDecimal.valueOf(100)) <= 0,
                "Discount percent must be between 0 and 100.");

        bill.getItems().stream()
                .filter(i -> i.getProductId() == productId)
                .findFirst()
                .ifPresent(item -> {
                    item.setDiscountPercent(discountPercent);
                    calculateTotals(bill);
                    log.info("Item discount applied. productId=" + productId
                            + " discount=" + discountPercent + "%");
                });
    }

    public void applyBillDiscount(Bill bill, BigDecimal discountAmount) {
        ValidationUtil.requireTrue(bill.isDraft(), "Cannot modify a completed bill.");
        ValidationUtil.requireTrue(discountAmount.compareTo(BigDecimal.ZERO) >= 0,
                "Discount cannot be negative.");
        ValidationUtil.requireTrue(discountAmount.compareTo(bill.getSubtotal()) <= 0,
                "Discount cannot exceed subtotal.");
        bill.setDiscountAmount(discountAmount);
        calculateTotals(bill);
        log.info("Bill-level discount applied: " + discountAmount);
    }

    public void clearCart(Bill bill) {
        ValidationUtil.requireTrue(bill.isDraft(), "Cannot clear a completed bill.");
        bill.getItems().clear();
        calculateTotals(bill);
        log.info("Cart cleared for invoice: " + bill.getInvoiceNumber());
    }

    // ── totals ────────────────────────────────────────────────────────────────

    public void calculateTotals(Bill bill) {
        BigDecimal subtotal = bill.getItems().stream()
                .map(BillItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal itemGst = bill.getItems().stream()
                .map(BillItem::getGstAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal afterDiscount = subtotal.subtract(bill.getDiscountAmount())
                .max(BigDecimal.ZERO);

        // Re-derive GST after bill discount (proportional reduction)
        BigDecimal gstAmount = itemGst;
        if (bill.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0
                && subtotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = afterDiscount.divide(subtotal, 10, RoundingMode.HALF_UP);
            gstAmount = itemGst.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
        }

        bill.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        bill.setGstAmount(gstAmount.setScale(2, RoundingMode.HALF_UP));
        bill.setGrandTotal(afterDiscount.setScale(2, RoundingMode.HALF_UP));
    }

    // ── payment ───────────────────────────────────────────────────────────────

    public void processPayment(Bill bill, PaymentMethod method,
                               BigDecimal cashPaid, BigDecimal cardPaid, BigDecimal upiPaid) {
        ValidationUtil.requireTrue(bill.isDraft(), "Bill is not in DRAFT state.");
        ValidationUtil.requireTrue(!bill.getItems().isEmpty(), "Cart is empty.");

        BigDecimal total = cashPaid.add(cardPaid).add(upiPaid);
        ValidationUtil.requireTrue(
                total.compareTo(bill.getGrandTotal()) >= 0,
                "Amount paid (" + total + ") is less than grand total (" + bill.getGrandTotal() + ")."
        );

        bill.setPaymentMethod(method);
        bill.setCashPaid(cashPaid);
        bill.setCardPaid(cardPaid);
        bill.setUpiPaid(upiPaid);
        bill.setAmountPaid(total);
        bill.setBalance(total.subtract(bill.getGrandTotal()).setScale(2, RoundingMode.HALF_UP));
        log.info("Payment processed. method=" + method + " paid=" + total
                + " balance=" + bill.getBalance());
    }

    // ── complete bill (atomic) ────────────────────────────────────────────────

    /**
     * Atomically:
     *   1. Validates the bill
     *   2. Deducts inventory for each item
     *   3. Inserts the bill row
     *   4. Inserts all bill_items rows
     *   5. Marks bill COMPLETED
     *   6. Fires BillCreatedEvent
     *
     * Rolls back if anything fails.
     */
    public Bill completeBill(Connection conn, Bill bill) throws SQLException {
        validateForCompletion(bill);

        conn.setAutoCommit(false);
        try {
            // Check for duplicate invoice number
            if (billDao.invoiceNumberExists(conn, bill.getInvoiceNumber())) {
                bill.setInvoiceNumber(InvoiceNumberUtil.generate());
            }

            // Deduct inventory for each line item
            for (BillItem item : bill.getItems()) {
                productDao.deductStock(conn, item.getProductId(), item.getQuantity());
            }

            // Persist bill
            bill.setStatus(Status.COMPLETED);
            bill.setCompletedAt(LocalDateTime.now());
            billDao.insert(conn, bill);

            // Persist line items
            billItemDao.insertAll(conn, bill.getId(), bill.getItems());

            conn.commit();
            log.info("Bill completed: " + bill.getInvoiceNumber());

            // Fire event for downstream consumers (Reports, Sync, etc.)
            BillCreatedEvent event = new BillCreatedEvent(bill);
            // TODO: EventBus.getInstance().publish(event);

            return bill;

        } catch (Exception ex) {
            conn.rollback();
            log.log(Level.SEVERE, "Bill completion failed. Transaction rolled back. invoice="
                    + bill.getInvoiceNumber(), ex);
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void cancelBill(Bill bill) {
        ValidationUtil.requireTrue(bill.isDraft(), "Only DRAFT bills can be cancelled.");
        bill.setStatus(Status.CANCELLED);
        log.info("Bill cancelled: " + bill.getInvoiceNumber());
    }

    /**
     * Loads a completed bill with its items for reprinting.
     */
    public Bill loadForReprint(Connection conn, int billId) throws SQLException {
        Bill bill = billDao.findById(conn, billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found: id=" + billId));
        List<BillItem> items = billItemDao.findByBillId(conn, billId);
        bill.setItems(items);
        log.info("Receipt reprint loaded: " + bill.getInvoiceNumber());
        return bill;
    }

    // ── validation ────────────────────────────────────────────────────────────

    private void validateForCompletion(Bill bill) {
        ValidationUtil.requireTrue(!bill.getItems().isEmpty(), "Cart is empty.");
        ValidationUtil.requireTrue(bill.getAmountPaid().compareTo(bill.getGrandTotal()) >= 0,
                "Payment not processed or insufficient.");
        for (BillItem item : bill.getItems()) {
            ValidationUtil.requireTrue(item.getQuantity() > 0,
                    "Invalid quantity for: " + item.getProductName());
        }
    }
}