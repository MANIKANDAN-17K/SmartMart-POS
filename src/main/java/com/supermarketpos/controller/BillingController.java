package com.supermarketpos.controller;

import com.supermarketpos.dao.CustomerDao;
import com.supermarketpos.dao.ProductDao;
import com.supermarketpos.model.Bill;
import com.supermarketpos.model.Bill.PaymentMethod;
import com.supermarketpos.model.BillItem;
import com.supermarketpos.model.Customer;
import com.supermarketpos.model.Product;
import com.supermarketpos.service.BillingService;
import com.supermarketpos.service.ReceiptService;
import com.supermarketpos.util.*;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.BigDecimalStringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BillingController implements Initializable {

    private static final Logger log = Logger.getLogger(BillingController.class.getName());

    // ── injected via UserSession ──────────────────────────────────────────────
    // Replace with your actual UserSession / SessionManager class
    private int cashierId = com.supermarketpos.session.UserSession.getUserId();
    private String cashierName = com.supermarketpos.session.UserSession.getUserName();

    // ── services / DAOs ───────────────────────────────────────────────────────
    private final BillingService billingService = new BillingService();
    private final ReceiptService receiptService = new ReceiptService();
    private final ProductDao productDao = new ProductDao();
    private final CustomerDao customerDao = new CustomerDao();

    // ── state ─────────────────────────────────────────────────────────────────
    private Bill currentBill;
    private final ObservableList<BillItem> cartItems = FXCollections.observableArrayList();

    // ── FXML – header ─────────────────────────────────────────────────────────
    @FXML
    private Label lblInvoiceNumber;
    @FXML
    private Label lblCashier;
    @FXML
    private Label lblDateTime;
    @FXML
    private TextField tfCustomerSearch;
    @FXML
    private ListView<Customer> lvCustomerResults;
    @FXML
    private Label lblSelectedCustomer;
    @FXML
    private Button btnNewBill;

    // ── FXML – product search ─────────────────────────────────────────────────
    @FXML
    private TextField tfBarcode;
    @FXML
    private TextField tfProductSearch;
    @FXML
    private ListView<Product> lvProductResults;

    // ── FXML – cart table ─────────────────────────────────────────────────────
    @FXML
    private TableView<BillItem> tblCart;
    @FXML
    private TableColumn<BillItem, String> colBarcode;
    @FXML
    private TableColumn<BillItem, String> colProduct;
    @FXML
    private TableColumn<BillItem, BigDecimal> colPrice;
    @FXML
    private TableColumn<BillItem, Integer> colQty;
    @FXML
    private TableColumn<BillItem, BigDecimal> colDiscount;
    @FXML
    private TableColumn<BillItem, BigDecimal> colGst;
    @FXML
    private TableColumn<BillItem, BigDecimal> colLineTotal;
    @FXML
    private TableColumn<BillItem, Void> colRemove;

    // ── FXML – summary ────────────────────────────────────────────────────────
    @FXML
    private Label lblItemCount;
    @FXML
    private Label lblSubtotal;
    @FXML
    private Label lblDiscount;
    @FXML
    private Label lblGst;
    @FXML
    private Label lblGrandTotal;
    @FXML
    private TextField tfBillDiscount;

    // ── FXML – payment ────────────────────────────────────────────────────────
    @FXML
    private ToggleGroup tgPayment;
    @FXML
    private RadioButton rbCash;
    @FXML
    private RadioButton rbCard;
    @FXML
    private RadioButton rbUpi;
    @FXML
    private RadioButton rbSplit;
    @FXML
    private TextField tfCashPaid;
    @FXML
    private TextField tfCardPaid;
    @FXML
    private TextField tfUpiPaid;
    @FXML
    private Label lblBalance;

    // ── FXML – action buttons ─────────────────────────────────────────────────
    @FXML
    private Button btnCompleteBill;
    @FXML
    private Button btnCancelBill;
    @FXML
    private Button btnPrintReceipt;
    @FXML
    private Button btnReprintReceipt;
    @FXML
    private Button btnClearCart;

    // ── init ──────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupCartTable();
        setupPaymentListeners();
        setupCustomerSearch();
        setupProductSearch();
        startNewBill();
        startClock();
        log.info("Billing screen opened.");
    }

    private void startClock() {
        javafx.animation.Timeline clock = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1),
                        e -> lblDateTime.setText(DateUtil.now())));
        clock.setCycleCount(javafx.animation.Animation.INDEFINITE);
        clock.play();
    }

    // ── cart table setup ──────────────────────────────────────────────────────

    private void setupCartTable() {
        colBarcode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBarcode()));
        colProduct.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        colPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getUnitPrice()));
        colQty.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getQuantity()));
        colDiscount.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDiscountAmount()));
        colGst.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getGstAmount()));
        colLineTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getLineTotal()));

        // Editable quantity column
        colQty.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 9999, 1);
            {
                spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    BillItem item = getTableView().getItems().get(getIndex());
                    if (item != null && !newVal.equals(item.getQuantity())) {
                        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
                            Product p = productDao.findById(conn, item.getProductId()).orElseThrow();
                            billingService.updateQuantity(currentBill, item.getProductId(),
                                    newVal, p.getStockQuantity());
                            refreshCart();
                        } catch (Exception ex) {
                            AlertUtil.showWarning("Quantity Error", ex.getMessage());
                            spinner.getValueFactory().setValue(item.getQuantity());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) {
                    setGraphic(null);
                    return;
                }
                spinner.getValueFactory().setValue(qty);
                setGraphic(spinner);
            }
        });

        // Remove button column
        colRemove.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✕");
            {
                btn.setOnAction(e -> {
                    BillItem item = getTableView().getItems().get(getIndex());
                    billingService.removeItem(currentBill, item.getProductId());
                    refreshCart();
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tblCart.setItems(cartItems);
        tblCart.setEditable(true);
    }

    // ── payment listeners ─────────────────────────────────────────────────────

    private void setupPaymentListeners() {
        tfCashPaid.textProperty().addListener((obs, o, n) -> updateBalance());
        tfCardPaid.textProperty().addListener((obs, o, n) -> updateBalance());
        tfUpiPaid.textProperty().addListener((obs, o, n) -> updateBalance());

        rbCash.setSelected(true);
        tgPayment.selectedToggleProperty().addListener((obs, o, n) -> {
            boolean isSplit = rbSplit.isSelected();
            tfCashPaid.setDisable(!rbCash.isSelected() && !isSplit);
            tfCardPaid.setDisable(!rbCard.isSelected() && !isSplit);
            tfUpiPaid.setDisable(!rbUpi.isSelected() && !isSplit);
            if (!isSplit) {
                tfCashPaid.setText("");
                tfCardPaid.setText("");
                tfUpiPaid.setText("");
            }
        });
    }

    private void updateBalance() {
        BigDecimal cash = CurrencyUtil.parse(tfCashPaid.getText());
        BigDecimal card = CurrencyUtil.parse(tfCardPaid.getText());
        BigDecimal upi = CurrencyUtil.parse(tfUpiPaid.getText());
        BigDecimal paid = cash.add(card).add(upi);
        BigDecimal balance = paid.subtract(currentBill.getGrandTotal());
        lblBalance.setText(CurrencyUtil.format(balance.max(BigDecimal.ZERO)));
    }

    // ── customer search ───────────────────────────────────────────────────────

    private void setupCustomerSearch() {
        tfCustomerSearch.textProperty().addListener((obs, o, keyword) -> {
            if (keyword.length() < 2) {
                lvCustomerResults.getItems().clear();
                return;
            }
            try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
                List<Customer> results = customerDao.searchByName(conn, keyword);
                lvCustomerResults.setItems(FXCollections.observableArrayList(results));
                lvCustomerResults.setVisible(!results.isEmpty());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Customer search failed.", ex);
            }
        });

        lvCustomerResults.setOnMouseClicked(e -> {
            Customer selected = lvCustomerResults.getSelectionModel().getSelectedItem();
            if (selected != null) {
                currentBill.setCustomerId(selected.getId());
                currentBill.setCustomerName(selected.getName());
                lblSelectedCustomer.setText(selected.getName()
                        + (selected.getPhone() != null ? " | " + selected.getPhone() : ""));
                lvCustomerResults.setVisible(false);
                tfCustomerSearch.setText(selected.getName());
            }
        });
    }

    // ── product search ────────────────────────────────────────────────────────

    private void setupProductSearch() {
        // Barcode: fires on Enter (scanner sends Enter after barcode)
        tfBarcode.setOnAction(e -> searchByBarcode());

        tfProductSearch.textProperty().addListener((obs, o, keyword) -> {
            if (keyword.length() < 2) {
                lvProductResults.getItems().clear();
                return;
            }
            try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
                List<Product> results = productDao.searchByName(conn, keyword);
                lvProductResults.setItems(FXCollections.observableArrayList(results));
                lvProductResults.setVisible(!results.isEmpty());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Product search failed.", ex);
            }
        });

        lvProductResults.setOnMouseClicked(e -> {
            Product selected = lvProductResults.getSelectionModel().getSelectedItem();
            if (selected != null) {
                addProductToCart(selected, 1);
                lvProductResults.setVisible(false);
                tfProductSearch.clear();
            }
        });
    }

    private void searchByBarcode() {
        String raw = BarcodeUtil.normalize(tfBarcode.getText());
        if (raw.isEmpty())
            return;
        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            productDao.findByBarcode(conn, raw).ifPresentOrElse(
                    p -> addProductToCart(p, 1),
                    () -> AlertUtil.showWarning("Not Found", "No active product found for barcode: " + raw));
        } catch (Exception ex) {
            AlertUtil.showError("Error", "Product lookup failed.");
            log.log(Level.SEVERE, "Barcode search error.", ex);
        } finally {
            tfBarcode.clear();
            tfBarcode.requestFocus();
        }
    }

    private void addProductToCart(Product product, int qty) {
        try {
            billingService.addItem(currentBill, product, qty);
            refreshCart();
        } catch (IllegalArgumentException ex) {
            AlertUtil.showWarning("Cannot Add Product", ex.getMessage());
        }
    }

    // ── bill actions ──────────────────────────────────────────────────────────

    @FXML
    private void onNewBill() {
        if (currentBill.isDraft() && !currentBill.getItems().isEmpty()) {
            if (!AlertUtil.confirm("New Bill", "Discard current bill and start a new one?"))
                return;
        }
        startNewBill();
    }

    @FXML
    private void onApplyBillDiscount() {
        try {
            BigDecimal discount = CurrencyUtil.parse(tfBillDiscount.getText());
            billingService.applyBillDiscount(currentBill, discount);
            refreshSummary();
            log.info("Bill discount applied: " + discount);
        } catch (IllegalArgumentException ex) {
            AlertUtil.showWarning("Invalid Discount", ex.getMessage());
        }
    }

    @FXML
    private void onCompleteBill() {
        if (currentBill.getItems().isEmpty()) {
            AlertUtil.showWarning("Empty Cart", "Add at least one product before completing the bill.");
            return;
        }

        PaymentMethod method = getSelectedPaymentMethod();
        BigDecimal cash = CurrencyUtil.parse(tfCashPaid.getText());
        BigDecimal card = CurrencyUtil.parse(tfCardPaid.getText());
        BigDecimal upi = CurrencyUtil.parse(tfUpiPaid.getText());

        try {
            billingService.processPayment(currentBill, method, cash, card, upi);
        } catch (IllegalArgumentException ex) {
            AlertUtil.showWarning("Payment Error", ex.getMessage());
            return;
        }

        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            billingService.completeBill(conn, currentBill);
            refreshCart();
            refreshSummary();
            AlertUtil.showInfo("Bill Completed",
                    "Invoice " + currentBill.getInvoiceNumber() + " saved successfully.\n"
                            + "Change: " + CurrencyUtil.format(currentBill.getBalance()));
            receiptService.printReceipt(currentBill);
            log.info("Bill completed: " + currentBill.getInvoiceNumber());
        } catch (Exception ex) {
            AlertUtil.showError("Transaction Failed",
                    "Bill could not be saved. All changes have been rolled back.\n" + ex.getMessage());
            log.log(Level.SEVERE, "completeBill failed.", ex);
        }
    }

    @FXML
    private void onCancelBill() {
        if (!AlertUtil.confirm("Cancel Bill", "Cancel the current bill? This cannot be undone."))
            return;
        billingService.cancelBill(currentBill);
        log.info("Bill cancelled: " + currentBill.getInvoiceNumber());
        startNewBill();
    }

    @FXML
    private void onPrintReceipt() {
        if (!currentBill.isCompleted()) {
            AlertUtil.showWarning("Cannot Print", "Complete the bill before printing.");
            return;
        }
        try {
            receiptService.printReceipt(currentBill);
        } catch (Exception ex) {
            AlertUtil.showError("Print Failed", ex.getMessage());
        }
    }

    @FXML
    private void onReprintReceipt() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reprint Receipt");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Invoice Number:");
        dialog.showAndWait().ifPresent(invoiceNum -> {
            try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
                // Find bill id by invoice number then load
                com.supermarketpos.dao.BillDao bd = new com.supermarketpos.dao.BillDao();
                bd.findByInvoiceNumber(conn, invoiceNum).ifPresentOrElse(
                        found -> {
                            try {
                                Bill b = billingService.loadForReprint(conn, found.getId());
                                receiptService.reprintReceipt(b);
                            } catch (Exception e) {
                                AlertUtil.showError("Reprint Failed", e.getMessage());
                            }
                        },
                        () -> AlertUtil.showWarning("Not Found", "Invoice not found: " + invoiceNum));
            } catch (Exception ex) {
                AlertUtil.showError("Error", ex.getMessage());
            }
        });
    }

    @FXML
    private void onClearCart() {
        if (!AlertUtil.confirm("Clear Cart", "Remove all items from the cart?"))
            return;
        billingService.clearCart(currentBill);
        refreshCart();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void startNewBill() {
        currentBill = billingService.createBill(cashierId, cashierName);
        lblInvoiceNumber.setText(currentBill.getInvoiceNumber());
        lblCashier.setText(cashierName);
        lblSelectedCustomer.setText("Walk-in");
        tfCustomerSearch.clear();
        tfBillDiscount.clear();
        tfCashPaid.clear();
        tfCardPaid.clear();
        tfUpiPaid.clear();
        rbCash.setSelected(true);
        refreshCart();
    }

    private void refreshCart() {
        cartItems.setAll(currentBill.getItems());
        refreshSummary();
    }

    private void refreshSummary() {
        lblItemCount.setText(String.valueOf(currentBill.getItemCount()));
        lblSubtotal.setText(CurrencyUtil.format(currentBill.getSubtotal()));
        lblDiscount.setText(CurrencyUtil.format(currentBill.getDiscountAmount()));
        lblGst.setText(CurrencyUtil.format(currentBill.getGstAmount()));
        lblGrandTotal.setText(CurrencyUtil.format(currentBill.getGrandTotal()));
        updateBalance();

        boolean completed = currentBill.isCompleted();
        btnCompleteBill.setDisable(completed);
        btnCancelBill.setDisable(completed);
        btnClearCart.setDisable(completed);
        btnPrintReceipt.setDisable(!completed);
    }

    private PaymentMethod getSelectedPaymentMethod() {
        if (rbCard.isSelected())
            return PaymentMethod.CARD;
        if (rbUpi.isSelected())
            return PaymentMethod.UPI;
        if (rbSplit.isSelected())
            return PaymentMethod.SPLIT;
        return PaymentMethod.CASH;
    }

    @FXML
    private void onBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnNewBill.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Dashboard");
        } catch (java.io.IOException e) {
            log.log(Level.SEVERE, "Could not load Dashboard view", e);
            AlertUtil.showError("Navigation Error", "Could not load Dashboard.");
        }
    }
}