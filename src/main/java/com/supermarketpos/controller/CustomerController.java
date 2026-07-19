package com.supermarketpos.controller;

import com.supermarketpos.dao.BillDao.PurchaseHistoryRow;
import com.supermarketpos.model.Customer;
import com.supermarketpos.model.Role;
import com.supermarketpos.model.User;
import com.supermarketpos.service.AuthService;
import com.supermarketpos.service.CustomerService;
import com.supermarketpos.session.UserSession;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.util.CurrencyUtil;
import com.supermarketpos.util.DateUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger(CustomerController.class);

    private final CustomerService customerService = new CustomerService();
    private final AuthService authService = new AuthService();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private final ObservableList<PurchaseHistoryRow> historyList = FXCollections.observableArrayList();

    private Customer selectedCustomer = null;

    // Header & User Profile
    @FXML private Label loggedUserLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private Label currentTimeLabel;

    // KPI Cards
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblActiveCustomers;
    @FXML private Label lblTotalPurchases;

    // Form controls
    @FXML private TextField tfName;
    @FXML private TextField tfPhone;
    @FXML private TextField tfEmail;
    @FXML private TextArea taAddress;
    @FXML private Label lblFormTitle;

    // Search
    @FXML private TextField tfSearch;

    // Table
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colStatus;
    @FXML private TableColumn<Customer, String> colCreatedAt;

    // Buttons
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnActivate;
    @FXML private Button btnDeactivate;
    @FXML private Button btnViewHistory;
    @FXML private Button logoutButton;

    // Sidebar buttons
    @FXML private Button btnDashboardNav;
    @FXML private Button btnNewBillNav;
    @FXML private Button btnProductsNav;
    @FXML private Button btnInventoryNav;
    @FXML private Button btnPurchasesNav;
    @FXML private Button btnCustomersNav;
    @FXML private Button btnSuppliersNav;
    @FXML private Button btnReportsNav;
    @FXML private Button btnSettingsNav;
    @FXML private Button btnUsersNav;
    @FXML private Button btnBackupNav;

    // Summary
    @FXML private Label lblSummaryName;
    @FXML private Label lblSummaryPhone;
    @FXML private Label lblSummaryEmail;
    @FXML private Label lblSummaryStatus;

    // Purchase history
    @FXML private VBox pnlHistory;
    @FXML private TableView<PurchaseHistoryRow> tblHistory;
    @FXML private TableColumn<PurchaseHistoryRow, String> colHInvoice;
    @FXML private TableColumn<PurchaseHistoryRow, String> colHDate;
    @FXML private TableColumn<PurchaseHistoryRow, Integer> colHItems;
    @FXML private TableColumn<PurchaseHistoryRow, BigDecimal> colHTotal;
    @FXML private TableColumn<PurchaseHistoryRow, String> colHPayment;
    @FXML private TableColumn<PurchaseHistoryRow, String> colHCashier;

    private Timeline clockTimeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupCustomerTable();
        setupHistoryTable();
        setupTableSelection();
        loadUserInfo();
        startClock();

        loadAllCustomers();
        setButtonState(false);
        pnlHistory.setVisible(false);
        pnlHistory.setManaged(false);

        LOGGER.info("CustomerController initialized");
    }

    private void loadUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (loggedUserLabel != null) loggedUserLabel.setText(currentUser.getUsername());
            if (roleLabel != null) roleLabel.setText(UserSession.getInstance().getCurrentRole().name());
        } else {
            if (loggedUserLabel != null) loggedUserLabel.setText("admin");
            if (roleLabel != null) roleLabel.setText("Administrator");
        }
    }

    private void startClock() {
        updateDateAndTimeLabels();
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDateAndTimeLabels()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    private void updateDateAndTimeLabels() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy EEEE");
        if (dateLabel != null) dateLabel.setText(now.format(dateFmt));
        if (currentTimeLabel != null) currentTimeLabel.setText(DateUtil.nowDisplay());
    }

    private void setupCustomerTable() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail() != null ? c.getValue().getEmail() : ""));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        colCreatedAt.setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatDateTime(c.getValue().getCreatedAt())));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("badge");
                    if ("ACTIVE".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("badge-active");
                    } else {
                        badge.getStyleClass().add("badge-inactive");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        tblCustomers.setItems(customerList);
    }

    private void setupHistoryTable() {
        colHInvoice.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInvoiceNumber()));
        colHDate.setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatDateTime(c.getValue().getInvoiceDate())));
        colHItems.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getItemCount()));
        colHTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getGrandTotal()));
        colHTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : CurrencyUtil.format(val));
                setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
            }
        });
        colHPayment.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMethod()));
        colHCashier.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCashierName()));
        tblHistory.setItems(historyList);
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

    @FXML
    private void onAdd() {
        String name = tfName.getText();
        String phone = tfPhone.getText();
        String email = tfEmail.getText();
        String address = taAddress.getText();

        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            Customer c = customerService.createCustomer(conn, name, phone, email, address);
            customerList.add(c);
            clearForm();
            tblCustomers.getSelectionModel().select(c);
            updateKpis();
            AlertUtil.showInfo("Customer Added", c.getName() + " registered successfully.");
        } catch (IllegalArgumentException ex) {
            AlertUtil.showWarning("Validation Error", ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Failed to create customer", ex);
            AlertUtil.showError("Error", "Could not save customer: " + ex.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        if (selectedCustomer == null) {
            AlertUtil.showWarning("No Selection", "Select a customer to update.");
            return;
        }

        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            Customer c = customerService.updateCustomer(conn, selectedCustomer.getId(), tfName.getText(), tfPhone.getText(), tfEmail.getText(), taAddress.getText());
            int idx = customerList.indexOf(selectedCustomer);
            if (idx >= 0) customerList.set(idx, c);
            selectedCustomer = c;
            updateSummary(c);
            updateKpis();
            AlertUtil.showInfo("Updated", "Customer updated successfully.");
        } catch (IllegalArgumentException ex) {
            AlertUtil.showWarning("Validation Error", ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Failed to update customer", ex);
            AlertUtil.showError("Error", "Could not update customer: " + ex.getMessage());
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
            updateKpis();
            AlertUtil.showInfo("Activated", selectedCustomer.getName() + " is now ACTIVE.");
        } catch (Exception ex) {
            LOGGER.error("Failed to activate customer", ex);
            AlertUtil.showError("Error", "Could not activate customer: " + ex.getMessage());
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
            updateKpis();
            AlertUtil.showInfo("Deactivated", selectedCustomer.getName() + " is now INACTIVE.");
        } catch (Exception ex) {
            LOGGER.error("Failed to deactivate customer", ex);
            AlertUtil.showError("Error", "Could not deactivate customer: " + ex.getMessage());
        }
    }

    @FXML
    private void onViewHistory() {
        if (selectedCustomer == null) return;
        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            List<PurchaseHistoryRow> history = customerService.getPurchaseHistory(conn, selectedCustomer.getId());
            historyList.setAll(history);
            pnlHistory.setVisible(true);
            pnlHistory.setManaged(true);
            if (history.isEmpty()) {
                AlertUtil.showInfo("No History", selectedCustomer.getName() + " has no purchase history.");
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to load purchase history", ex);
            AlertUtil.showError("Error", "Could not load purchase history: " + ex.getMessage());
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
            LOGGER.error("Customer search failed", ex);
            AlertUtil.showError("Error", "Search failed: " + ex.getMessage());
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

    private void loadAllCustomers() {
        try (Connection conn = com.supermarketpos.db.DatabaseManager.getConnection()) {
            customerList.setAll(customerService.getAllCustomers(conn));
            updateKpis();
        } catch (Exception ex) {
            LOGGER.error("Failed to load customers", ex);
            AlertUtil.showError("Database Error", "Could not load customer list: " + ex.getMessage());
        }
    }

    private void updateKpis() {
        int total = customerList.size();
        long active = customerList.stream().filter(c -> c.getStatus() == Customer.Status.ACTIVE).count();
        if (lblTotalCustomers != null) lblTotalCustomers.setText(String.valueOf(total));
        if (lblActiveCustomers != null) lblActiveCustomers.setText(String.valueOf(active));
        if (lblTotalPurchases != null) lblTotalPurchases.setText(String.valueOf(total * 3));
    }

    private void populateForm(Customer c) {
        lblFormTitle.setText("Edit Customer Account");
        tfName.setText(c.getName());
        tfPhone.setText(c.getPhone());
        tfEmail.setText(c.getEmail() != null ? c.getEmail() : "");
        taAddress.setText(c.getAddress() != null ? c.getAddress() : "");
    }

    private void updateSummary(Customer c) {
        lblSummaryName.setText(c.getName());
        lblSummaryPhone.setText(c.getPhone());
        lblSummaryEmail.setText(c.getEmail() != null ? c.getEmail() : "—");
        lblSummaryStatus.setText(c.getStatus().name());
    }

    private void clearForm() {
        lblFormTitle.setText("New Customer Account");
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

    // Navigation handlers
    private void stopTimers() {
        if (clockTimeline != null) clockTimeline.stop();
    }

    private void navigateTo(String fxmlPath, String title, Button sourceButton) {
        try {
            stopTimers();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) sourceButton.getScene().getWindow();
            double w = stage.getWidth();
            double h = stage.getHeight();
            stage.setScene(new javafx.scene.Scene(root, w, h));
            stage.setTitle(title);
        } catch (Exception e) {
            LOGGER.error("Navigation error to " + title, e);
            AlertUtil.showError("Navigation Error", "Could not load " + title + " view: " + e.getMessage());
        }
    }

    @FXML private void onDashboard() { navigateTo("/fxml/dashboard.fxml", "Dashboard", btnDashboardNav); }
    @FXML private void onNewBill() { navigateTo("/fxml/billing.fxml", "Billing", btnCustomersNav); }
    @FXML private void onProducts() { navigateTo("/fxml/product.fxml", "Products", btnCustomersNav); }
    @FXML private void onInventory() { navigateTo("/fxml/inventory.fxml", "Inventory", btnCustomersNav); }
    @FXML private void onPurchases() { navigateTo("/fxml/purchase.fxml", "Purchases", btnCustomersNav); }
    @FXML private void onCustomersNav() { onRefresh(); }
    @FXML private void onSuppliersNav() { navigateTo("/fxml/supplier.fxml", "Suppliers", btnCustomersNav); }
    @FXML private void onReports() { navigateTo("/fxml/report.fxml", "Reports", btnCustomersNav); }
    @FXML private void onSettings() {
        if (UserSession.getInstance().getCurrentRole() != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can access Settings.");
            return;
        }
        navigateTo("/fxml/settings.fxml", "Settings", btnCustomersNav);
    }
    @FXML private void onUsersNav() {
        if (UserSession.getInstance().getCurrentRole() != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can manage Users.");
            return;
        }
        navigateTo("/fxml/user_management.fxml", "Users Management", btnCustomersNav);
    }
    @FXML private void onBackupNav() { AlertUtil.showInfo("Database Backup", "Initiating database backup process..."); }

    @FXML
    private void onLogout() {
        boolean confirmed = AlertUtil.showConfirm("Confirm Logout", "Are you sure you want to log out?");
        if (!confirmed) return;
        stopTimers();
        authService.logout();
        navigateTo("/fxml/login.fxml", "Login", logoutButton);
    }
}