package com.supermarketpos.controller;

import com.supermarketpos.model.Role;
import com.supermarketpos.model.Supplier;
import com.supermarketpos.model.User;
import com.supermarketpos.service.AuthService;
import com.supermarketpos.service.SupplierService;
import com.supermarketpos.session.UserSession;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.util.DateUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

public class SupplierController {

    private static final Logger LOGGER = LogManager.getLogger(SupplierController.class);

    private final SupplierService supplierService = new SupplierService();
    private final AuthService authService = new AuthService();
    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();
    private Supplier selectedSupplier;

    // Header & User Profile
    @FXML private Label loggedUserLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private Label currentTimeLabel;

    // KPI Cards
    @FXML private Label lblTotalSuppliers;
    @FXML private Label lblActiveSuppliers;
    @FXML private Label lblCoveredCities;

    // Form controls
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField contactPersonField;
    @FXML private TextField mobileField;
    @FXML private TextField emailField;
    @FXML private TextField gstField;
    @FXML private TextArea addressField;
    @FXML private TextField cityField;
    @FXML private TextField stateField;
    @FXML private TextField pincodeField;

    // Table
    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, String> codeColumn;
    @FXML private TableColumn<Supplier, String> nameColumn;
    @FXML private TableColumn<Supplier, String> contactColumn;
    @FXML private TableColumn<Supplier, String> mobileColumn;
    @FXML private TableColumn<Supplier, String> emailColumn;
    @FXML private TableColumn<Supplier, String> cityColumn;
    @FXML private TableColumn<Supplier, String> statusColumn;

    // Buttons
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button activateButton;
    @FXML private Button deactivateButton;
    @FXML private Button refreshButton;
    @FXML private Button purchaseHistoryButton;
    @FXML private Button clearButton;
    @FXML private Button logoutButton;

    // Sidebar navigation buttons
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

    private static final String FILTER_ALL = "All";
    private static final String FILTER_ACTIVE = "Active";
    private static final String FILTER_INACTIVE = "Inactive";

    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadUserInfo();
        startClock();

        statusFilter.setItems(FXCollections.observableArrayList(FILTER_ALL, FILTER_ACTIVE, FILTER_INACTIVE));
        statusFilter.setValue(FILTER_ALL);

        supplierTable.setItems(supplierList);
        supplierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedSupplier = newVal;
            populateForm(newVal);
        });

        loadSuppliers();
        LOGGER.info("SupplierController initialized");
    }

    private void setupTableColumns() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("supplierCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        mobileColumn.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));

        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("badge");
                    if ("Active".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("badge-active");
                    } else {
                        badge.getStyleClass().add("badge-inactive");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
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

    private void updateKpis() {
        int total = supplierList.size();
        long active = supplierList.stream().filter(Supplier::isActive).count();
        Set<String> cities = supplierList.stream()
                .map(Supplier::getCity)
                .filter(c -> c != null && !c.trim().isEmpty())
                .collect(Collectors.toSet());

        if (lblTotalSuppliers != null) lblTotalSuppliers.setText(String.valueOf(total));
        if (lblActiveSuppliers != null) lblActiveSuppliers.setText(String.valueOf(active));
        if (lblCoveredCities != null) lblCoveredCities.setText(String.valueOf(cities.size()));
    }

    @FXML
    private void handleSearch() {
        try {
            supplierList.setAll(supplierService.searchSupplier(searchField.getText()));
            updateKpis();
        } catch (SQLException e) {
            LOGGER.error("Unable to search suppliers", e);
            AlertUtil.showError("Database Error", "Unable to search suppliers: " + e.getMessage());
        }
    }

    @FXML
    private void handleStatusFilterChange() {
        String filter = statusFilter.getValue();
        try {
            if (FILTER_ACTIVE.equals(filter)) {
                supplierList.setAll(supplierService.getSuppliersByStatus(true));
            } else if (FILTER_INACTIVE.equals(filter)) {
                supplierList.setAll(supplierService.getSuppliersByStatus(false));
            } else {
                supplierList.setAll(supplierService.getAllSuppliers());
            }
            updateKpis();
        } catch (SQLException e) {
            LOGGER.error("Unable to filter suppliers", e);
            AlertUtil.showError("Database Error", "Unable to filter suppliers: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            supplierService.createSupplier(
                    codeField.getText(), nameField.getText(), contactPersonField.getText(),
                    mobileField.getText(), emailField.getText(), gstField.getText(),
                    addressField.getText(), cityField.getText(), stateField.getText(), pincodeField.getText());

            AlertUtil.showInfo("Success", "Supplier created successfully.");
            clearForm();
            loadSuppliers();
        } catch (SupplierService.ValidationException e) {
            AlertUtil.showWarning("Validation Error", e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("Unable to create supplier", e);
            AlertUtil.showError("Database Error", "Unable to create supplier: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedSupplier == null) {
            AlertUtil.showWarning("No Selection", "Please select a supplier to update.");
            return;
        }
        try {
            supplierService.updateSupplier(
                    selectedSupplier.getId(), codeField.getText(), nameField.getText(),
                    contactPersonField.getText(), mobileField.getText(), emailField.getText(),
                    gstField.getText(), addressField.getText(), cityField.getText(),
                    stateField.getText(), pincodeField.getText());

            AlertUtil.showInfo("Success", "Supplier updated successfully.");
            clearForm();
            loadSuppliers();
        } catch (SupplierService.ValidationException e) {
            AlertUtil.showWarning("Validation Error", e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("Unable to update supplier", e);
            AlertUtil.showError("Database Error", "Unable to update supplier: " + e.getMessage());
        }
    }

    @FXML
    private void handleActivate() {
        if (selectedSupplier == null) {
            AlertUtil.showWarning("No Selection", "Please select a supplier to activate.");
            return;
        }
        try {
            supplierService.activateSupplier(selectedSupplier.getId());
            loadSuppliers();
        } catch (SQLException e) {
            LOGGER.error("Unable to activate supplier", e);
            AlertUtil.showError("Database Error", "Unable to activate supplier: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeactivate() {
        if (selectedSupplier == null) {
            AlertUtil.showWarning("No Selection", "Please select a supplier to deactivate.");
            return;
        }
        boolean confirmed = AlertUtil.showConfirm("Confirm Deactivation",
                "Inactive suppliers cannot be selected in the Purchase module. Continue?");
        if (!confirmed) return;

        try {
            supplierService.deactivateSupplier(selectedSupplier.getId());
            loadSuppliers();
        } catch (SQLException e) {
            LOGGER.error("Unable to deactivate supplier", e);
            AlertUtil.showError("Database Error", "Unable to deactivate supplier: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        clearForm();
        statusFilter.setValue(FILTER_ALL);
        loadSuppliers();
    }

    @FXML
    private void handlePurchaseHistory() {
        if (selectedSupplier == null) {
            AlertUtil.showWarning("No Selection", "Please select a supplier to view purchase orders.");
            return;
        }
        AlertUtil.showInfo("Purchase History",
                "Purchase history for '" + selectedSupplier.getSupplierName() +
                        "' is accessible via the Purchases module.");
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void loadSuppliers() {
        try {
            supplierList.setAll(supplierService.getAllSuppliers());
            updateKpis();
        } catch (SQLException e) {
            LOGGER.error("Unable to load suppliers", e);
            AlertUtil.showError("Database Error", "Unable to load suppliers: " + e.getMessage());
        }
    }

    private void populateForm(Supplier supplier) {
        if (supplier == null) {
            clearForm();
            return;
        }
        codeField.setText(supplier.getSupplierCode());
        nameField.setText(supplier.getSupplierName());
        contactPersonField.setText(supplier.getContactPerson());
        mobileField.setText(supplier.getMobile());
        emailField.setText(supplier.getEmail());
        gstField.setText(supplier.getGstNumber());
        addressField.setText(supplier.getAddress());
        cityField.setText(supplier.getCity());
        stateField.setText(supplier.getState());
        pincodeField.setText(supplier.getPincode());
    }

    private void clearForm() {
        codeField.clear();
        nameField.clear();
        contactPersonField.clear();
        mobileField.clear();
        emailField.clear();
        gstField.clear();
        addressField.clear();
        cityField.clear();
        stateField.clear();
        pincodeField.clear();
        selectedSupplier = null;
        supplierTable.getSelectionModel().clearSelection();
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

    @FXML private void onDashboard() { navigateTo("/fxml/dashboard.fxml", "Dashboard", btnSuppliersNav); }
    @FXML private void onNewBill() { navigateTo("/fxml/billing.fxml", "Billing", btnSuppliersNav); }
    @FXML private void onProducts() { navigateTo("/fxml/product.fxml", "Products", btnSuppliersNav); }
    @FXML private void onInventory() { navigateTo("/fxml/inventory.fxml", "Inventory", btnSuppliersNav); }
    @FXML private void onPurchases() { navigateTo("/fxml/purchase.fxml", "Purchases", btnSuppliersNav); }
    @FXML private void onCustomersNav() { navigateTo("/fxml/customer.fxml", "Customers", btnSuppliersNav); }
    @FXML private void onSuppliersNav() { handleRefresh(); }
    @FXML private void onReports() { navigateTo("/fxml/report.fxml", "Reports", btnSuppliersNav); }
    @FXML private void onSettings() {
        if (UserSession.getInstance().getCurrentRole() != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can access Settings.");
            return;
        }
        navigateTo("/fxml/settings.fxml", "Settings", btnSuppliersNav);
    }
    @FXML private void onUsersNav() {
        if (UserSession.getInstance().getCurrentRole() != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can manage Users.");
            return;
        }
        navigateTo("/fxml/user_management.fxml", "Users Management", btnSuppliersNav);
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