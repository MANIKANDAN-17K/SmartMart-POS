package com.supermarketpos.controller;

import com.supermarketpos.dao.BillDao.PurchaseHistoryRow;
import com.supermarketpos.model.Customer;
import com.supermarketpos.service.CustomerService;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.util.CurrencyUtil;
import com.supermarketpos.util.DateUtil;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomerController implements Initializable {

    private static final Logger log = Logger.getLogger(CustomerController.class.getName());

    private final CustomerService customerService = new CustomerService();
    private final ObservableList<Customer>           customerList = FXCollections.observableArrayList();
    private final ObservableList<PurchaseHistoryRow> historyList  = FXCollections.observableArrayList();

    private Customer selectedCustomer = null;

    // ── FXML – form ───────────────────────────────────────────────────────────
    @FXML private TextField tfName;
    @FXML private TextField tfPhone;
    @FXML private TextField tfEmail;
    @FXML private TextArea  taAddress;
    @FXML private Label     lblFormTitle;

    // ── FXML – search ─────────────────────────────────────────────────────────
    @FXML private TextField tfSearch;

    // ── FXML – customer table ─────────────────────────────────────────────────
    @FXML private TableView<Customer>             tblCustomers;
    @FXML private TableColumn<Customer, String>   colName;
    @FXML private TableColumn<Customer, String>   colPhone;
    @FXML private TableColumn<Customer, String>   colEmail;
    @FXML private TableColumn<Customer, String>   colStatus;
    @FXML private TableColumn<Customer, String>   colCreatedAt;

    // ── FXML – buttons ────────────────────────────────────────────────────────
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnActivate;
    @FXML private Button btnDeactivate;
    @FXML private Button btnViewHistory;

    // ── FXML – summary ────────────────────────────────────────────────────────
    @FXML private Label lblSummaryName;
    @FXML private Label lblSummaryPhone;
    @FXML private Label lblSummaryEmail;
    @FXML private Label lblSummaryAddress;
    @FXML private Label lblSummaryStatus;
    @FXML private Label lblSummaryJoined;

    // ── FXML – purchase history ───────────────────────────────────────────────
    @FXML private VBox  pnlHistory;
    @FXML private TableView<PurchaseHistoryRow>             tblHistory;
    @FXML private TableColumn<PurchaseHistoryRow, String>   colHInvoice;
    @FXML private TableColumn<PurchaseHistoryRow, String>   colHDate;
    @FXML private TableColumn<PurchaseHistoryRow, Integer>  colHItems;
    @FXML private TableColumn<PurchaseHistoryRow, BigDecimal> colHTotal;
    @FXML private TableColumn<PurchaseHistoryRow, String>   colHPayment;
    @FXML private TableColumn<PurchaseHistoryRow, String>   colHCashier;

    // ── init ──────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupCustomerTable();
        setupHistoryTable();
        setupTableSelection();
        loadAllCustomers();
        setButtonState(false);
        pnlHistory.setVisible(false);
        pnlHistory.setManaged(false);
    }

    // ── table setup ───────────────────────────────────────────────────────────

    private void setupCustomerTable() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEmail() != null ? c.getValue().getEmail() : ""));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        colCreatedAt.setCellValueFactory(c -> new SimpleStringProperty(
                DateUtil.formatDateTime(c.getValue().getCreatedAt())));

        // Colour-code status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                setStyle("ACTIVE".equals(status)
                        ? "-fx-text-fill: #16a34a; -fx-font-weight: bold;"
                        : "-fx-text-fill: #dc2626; -fx-font-weight: bold;");
            }
        });

        tblCustomers.setItems(customerList);
    }

    private void setupHistoryTable() {
        colHInvoice.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInvoiceNumber()));
        colHDate.setCellValueFactory(c -> new SimpleStringProperty(
                DateUtil.formatDateTime(c.getValue().getInvoiceDate())));
        colHItems.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getItemCount()));
        colHTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getGrandTotal()));
        colHTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : CurrencyUtil.format(val));
            }
        });
        colHPayment.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMethod()));
        colHCashier.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCashierName()));
        tblHistory.setItems(historyList);
        tblHistory.setEditable(false);
    }

    private void setupTableSelection() {
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, old, customer) -> {
            selectedCustomer = customer;
            if (customer != null) {
                populateForm(customer);
                updateSummary(customer);
                setButtonState(true);
                pnlHistory.setVisible(false);
                pnlHistory.setManaged(false);
                historyList.clear();
            } else {
                setButtonState(false);
            }
        });
    }

    // ── FXML actions ──────────────────────────────────────────────────────────

    @FXML
    private void onAdd() {
        String name    = tfName.getText();
        String phone   = tfPhone.getText();
        String email   = tfEmail.getText();
        String address = taAddress.getText();

        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            Customer c = customerService.createCustomer(conn, name, phone, email, address);
            customerList.add(c);
            clearForm();
            tblCustomers.getSelectionModel().select(c);
            AlertUtil.showInfo("Customer Added", c.getName() + " registered successfully.");
        } catch (IllegalArgumentException ex) {
            AlertUtil.showWarning("Validation Error", ex.getMessage());
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to create customer.", ex);
            AlertUtil.showError("Error", "Could not save customer. Please try again.");
        }
    }

    @FXML
    private void onUpdate() {
        if (selectedCustomer == null) { AlertUtil.showWarning("No Selection", "Select a customer to update."); return; }

        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            Customer c = customerService.updateCustomer(conn,
                    selectedCustomer.getId(),
                    tfName.getText(), tfPhone.getText(),
                    tfEmail.getText(), taAddress.getText());
            int idx = customerList.indexOf(selectedCustomer);
            if (idx >= 0) customerList.set(idx, c);
            selectedCustomer = c;
            updateSummary(c);
            AlertUtil.showInfo("Updated", "Customer updated successfully.");
        } catch (IllegalArgumentException ex) {
            AlertUtil.showWarning("Validation Error", ex.getMessage());
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to update customer.", ex);
            AlertUtil.showError("Error", "Could not update customer.");
        }
    }

    @FXML
    private void onActivate() {
        if (selectedCustomer == null) return;
        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            customerService.activateCustomer(conn, selectedCustomer.getId());
            selectedCustomer.setStatus(Customer.Status.ACTIVE);
            refreshRow(selectedCustomer);
            updateSummary(selectedCustomer);
            AlertUtil.showInfo("Activated", selectedCustomer.getName() + " is now ACTIVE.");
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to activate customer.", ex);
            AlertUtil.showError("Error", "Could not activate customer.");
        }
    }

    @FXML
    private void onDeactivate() {
        if (selectedCustomer == null) return;
        if (!AlertUtil.confirm("Deactivate", "Deactivate " + selectedCustomer.getName() + "?")) return;
        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            customerService.deactivateCustomer(conn, selectedCustomer.getId());
            selectedCustomer.setStatus(Customer.Status.INACTIVE);
            refreshRow(selectedCustomer);
            updateSummary(selectedCustomer);
            AlertUtil.showInfo("Deactivated", selectedCustomer.getName() + " is now INACTIVE.");
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to deactivate customer.", ex);
            AlertUtil.showError("Error", "Could not deactivate customer.");
        }
    }

    @FXML
    private void onViewHistory() {
        if (selectedCustomer == null) return;
        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            List<PurchaseHistoryRow> history =
                    customerService.getPurchaseHistory(conn, selectedCustomer.getId());
            historyList.setAll(history);
            pnlHistory.setVisible(true);
            pnlHistory.setManaged(true);
            if (history.isEmpty()) {
                AlertUtil.showInfo("No History", selectedCustomer.getName() + " has no purchase history.");
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to load purchase history.", ex);
            AlertUtil.showError("Error", "Could not load purchase history.");
        }
    }

    @FXML
    private void onSearch() {
        String keyword = tfSearch.getText().trim();
        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            List<Customer> results = customerService.searchCustomer(conn, keyword);
            customerList.setAll(results);
            if (results.isEmpty()) AlertUtil.showInfo("No Results", "No customers found for: " + keyword);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Customer search failed.", ex);
            AlertUtil.showError("Error", "Search failed.");
        }
    }

    @FXML
    private void onRefresh() {
        tfSearch.clear();
        clearForm();
        loadAllCustomers();
        pnlHistory.setVisible(false);
        pnlHistory.setManaged(false);
        historyList.clear();
    }

    @FXML
    private void onClear() {
        clearForm();
        tblCustomers.getSelectionModel().clearSelection();
        selectedCustomer = null;
        setButtonState(false);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void loadAllCustomers() {
        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            customerList.setAll(customerService.getAllCustomers(conn));
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to load customers.", ex);
            AlertUtil.showError("Database Error", "Could not load customer list.");
        }
    }

    private void populateForm(Customer c) {
        lblFormTitle.setText("Edit Customer");
        tfName.setText(c.getName());
        tfPhone.setText(c.getPhone());
        tfEmail.setText(c.getEmail() != null ? c.getEmail() : "");
        taAddress.setText(c.getAddress() != null ? c.getAddress() : "");
    }

    private void updateSummary(Customer c) {
        lblSummaryName.setText(c.getName());
        lblSummaryPhone.setText(c.getPhone());
        lblSummaryEmail.setText(c.getEmail() != null ? c.getEmail() : "—");
        lblSummaryAddress.setText(c.getAddress() != null ? c.getAddress() : "—");
        lblSummaryStatus.setText(c.getStatus().name());
        lblSummaryStatus.setStyle(c.isActive()
                ? "-fx-text-fill: #16a34a; -fx-font-weight: bold;"
                : "-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        lblSummaryJoined.setText(DateUtil.formatDateTime(c.getCreatedAt()));
    }

    private void clearForm() {
        lblFormTitle.setText("New Customer");
        tfName.clear(); tfPhone.clear(); tfEmail.clear(); taAddress.clear();
    }

    private void setButtonState(boolean hasSelection) {
        btnUpdate.setDisable(!hasSelection);
        btnActivate.setDisable(!hasSelection);
        btnDeactivate.setDisable(!hasSelection);
        btnViewHistory.setDisable(!hasSelection);
    }

    private void refreshRow(Customer updated) {
        int idx = customerList.indexOf(updated);
        if (idx >= 0) {
            customerList.remove(idx);
            customerList.add(idx, updated);
            tblCustomers.getSelectionModel().select(idx);
        }
    }
}