package com.supermarketpos.service;

import com.supermarketpos.model.Bill;
import com.supermarketpos.model.BillItem;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.util.CurrencyUtil;
import com.supermarketpos.util.DateUtil;
import com.supermarketpos.util.PrintUtil;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiptService {

    private static final Logger log = Logger.getLogger(ReceiptService.class.getName());
    private static final String TEMPLATE_PATH = "/templates/receipt_template.html";

    /**
     * Builds an HTML receipt string from the bill and template,
     * then saves it to a file via PrintUtil.
     */
    public void printReceipt(Bill bill) {
        try {
            String html = buildHtml(bill);
            String filePath = PrintUtil.printHtml(html, "Receipt-" + bill.getInvoiceNumber());
            log.info("Receipt saved: " + bill.getInvoiceNumber());
            javafx.application.Platform.runLater(() ->
                AlertUtil.showInfo("Receipt Saved",
                    "Receipt saved successfully.\nFile: " + filePath)
            );
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to save receipt: " + bill.getInvoiceNumber(), ex);
            javafx.application.Platform.runLater(() ->
                AlertUtil.showError("Receipt Error",
                    "Receipt could not be saved: " + ex.getMessage())
            );
        }
    }

    public void reprintReceipt(Bill bill) {
        log.info("Receipt reprint requested: " + bill.getInvoiceNumber());
        printReceipt(bill);
        log.info("Receipt reprinted: " + bill.getInvoiceNumber());
    }

    /** Returns the rendered HTML string (useful for preview or email). */
    public String buildHtml(Bill bill) throws Exception {
        String template = loadTemplate();
        StringBuilder rows = new StringBuilder();

        for (BillItem item : bill.getItems()) {
            rows.append("<tr>")
                    .append("<td>").append(escape(item.getBarcode())).append("</td>")
                    .append("<td>").append(escape(item.getProductName())).append("</td>")
                    .append("<td class='right'>").append(CurrencyUtil.format(item.getUnitPrice())).append("</td>")
                    .append("<td class='right'>").append(item.getQuantity()).append("</td>")
                    .append("<td class='right'>").append(CurrencyUtil.format(item.getDiscountAmount())).append("</td>")
                    .append("<td class='right'>").append(CurrencyUtil.format(item.getGstAmount())).append("</td>")
                    .append("<td class='right'>").append(CurrencyUtil.format(item.getLineTotal())).append("</td>")
                    .append("</tr>");
        }

        return template
                .replace("{{INVOICE_NUMBER}}", escape(bill.getInvoiceNumber()))
                .replace("{{DATE_TIME}}",      DateUtil.formatDateTime(bill.getCompletedAt()))
                .replace("{{CASHIER}}",        escape(bill.getCashierName()))
                .replace("{{CUSTOMER}}",       bill.getCustomerName() != null ? escape(bill.getCustomerName()) : "Walk-in")
                .replace("{{ITEMS_ROWS}}",     rows.toString())
                .replace("{{ITEM_COUNT}}",     String.valueOf(bill.getItemCount()))
                .replace("{{SUBTOTAL}}",       CurrencyUtil.format(bill.getSubtotal()))
                .replace("{{DISCOUNT}}",       CurrencyUtil.format(bill.getDiscountAmount()))
                .replace("{{GST}}",            CurrencyUtil.format(bill.getGstAmount()))
                .replace("{{GRAND_TOTAL}}",    CurrencyUtil.format(bill.getGrandTotal()))
                .replace("{{AMOUNT_PAID}}",    CurrencyUtil.format(bill.getAmountPaid()))
                .replace("{{BALANCE}}",        CurrencyUtil.format(bill.getBalance()))
                .replace("{{PAYMENT_METHOD}}", bill.getPaymentMethod().name());
    }

    private String loadTemplate() throws Exception {
        try (InputStream is = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (is == null) throw new IllegalStateException("Receipt template not found: " + TEMPLATE_PATH);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}