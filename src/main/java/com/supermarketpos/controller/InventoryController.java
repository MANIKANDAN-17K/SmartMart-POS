package com.supermarketpos.controller;

import com.supermarketpos.dao.ProductDao;
import com.supermarketpos.model.Category;
import com.supermarketpos.model.Role;
import com.supermarketpos.model.StockAdjustment;
import com.supermarketpos.model.StockMovement;
import com.supermarketpos.model.User;
import com.supermarketpos.service.AuthService;
import com.supermarketpos.service.CategoryService;
import com.supermarketpos.service.InventoryService;
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
import java.util.List;

public class InventoryController {

    private static final Logger LOGGER = LogManager.getLogger(InventoryController.class);

    // Header & User Profile
    @FXML private Label loggedUserLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private Label currentTimeLabel;

    // KPI Summary Labels
    @FXML private Label totalProductsLabel;
    @FXML private Label totalStockItemsLabel;
    @FXML private Label lowStockCountLabel;
    @FXML private Label outOfStockCountLabel;

    // Filter controls
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private ComboBox<String> statusFilter;

    // Inventory Table
    @FXML private TableView<ProductDao.InventoryRow> inventoryTable;
    @FXML private TableColumn<ProductDao.InventoryRow, String> barcodeColumn;
    @FXML private TableColumn<ProductDao.InventoryRow, String> skuColumn;
    @FXML private TableColumn<ProductDao.InventoryRow, String> nameColumn;
    @FXML private TableColumn<ProductDao.InventoryRow, String> categoryColumn;
    @FXML private TableColumn<ProductDao.InventoryRow, Number> currentStockColumn;
    @FXML private TableColumn<ProductDao.InventoryRow, Number> minStockColumn;
    @FXML private TableColumn<ProductDao.InventoryRow, String> statusColumn;
    @FXML private TableColumn<ProductDao.InventoryRow, String> lastUpdatedColumn;

    // Adjustment Form
    @FXML private ComboBox<String> adjustmentTypeDropdown;
    @FXML private TextField adjustmentQuantityField;
    @FXML private TextField reasonField;
    @FXML private TextArea remarksField;
    @FXML private Label selectedProductLabel;
    @FXML private Label currentStockLabel;

    // Buttons
    @FXML private Button refreshButton;
    @FXML private Button searchButton;
    @FXML private Button adjustStockButton;
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

    private final InventoryService inventoryService = new InventoryService();
    private final CategoryService categoryService = new CategoryService();
    private final AuthService authService = new AuthService();

    private final ObservableList<ProductDao.InventoryRow> inventoryList = FXCollections.observableArrayList();
    private ProductDao.InventoryRow selectedRow;

    private static final String STATUS_ALL = "All";
    private static final String STATUS_IN_STOCK = "In Stock";
    private static final String STATUS_LOW_STOCK = "Low Stock";
    private static final String STATUS_OUT_OF_STOCK = "Out of Stock";

    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadUserInfo();
        startClock();

        statusFilter.setItems(FXCollections.observableArrayList(STATUS_ALL, STATUS_IN_STOCK, STATUS_LOW_STOCK, STATUS_OUT_OF_STOCK));
        statusFilter.setValue(STATUS_ALL);

        adjustmentTypeDropdown.setItems(FXCollections.observableArrayList(
                StockAdjustment.TYPE_INCREASE, StockAdjustment.TYPE_DECREASE));

        inventoryTable.setItems(inventoryList);
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedRow = newVal;
            populateAdjustmentPanel(newVal);
        });

        loadCategoryFilter();
        loadInventory();
        loadSummary();
        LOGGER.info("InventoryController initialized");
    }

    private void setupTableColumns() {
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        currentStockColumn.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        minStockColumn.setCellValueFactory(new PropertyValueFactory<>("minStockAlert"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("stockStatus"));

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
                    if ("IN_STOCK".equalsIgnoreCase(status) || "In Stock".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("badge-active");
                    } else if ("LOW_STOCK".equalsIgnoreCase(status) || "Low Stock".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("badge-cashier");
                    } else {
                        badge.getStyleClass().add("badge-inactive");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        lastUpdatedColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        DateUtil.formatDateTime(cellData.getValue().getUpdatedAt())));
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

    @FXML
    private void handleSearch() {
        try {
            inventoryList.setAll(inventoryService.searchInventory(searchField.getText()));
        } catch (SQLException e) {
            LOGGER.error("Search failed", e);
            AlertUtil.showError("Database Error", "Unable to search inventory: " + e.getMessage());
        }
    }

    @FXML
    private void handleCategoryFilterChange() {
        filterInventory();
    }

    @FXML
    private void handleStatusFilterChange() {
        filterInventory();
    }

    private void filterInventory() {
        Category selectedCategory = categoryFilter.getValue();
        Integer categoryId = selectedCategory != null ? selectedCategory.getId() : null;
        String status = statusFilter.getValue();
        if (STATUS_ALL.equals(status)) {
            status = null;
        }

        try {
            if (categoryId != null && categoryId > 0) {
                inventoryList.setAll(inventoryService.getInventoryByCategory(categoryId));
            } else if (status != null && !status.isBlank()) {
                inventoryList.setAll(inventoryService.getInventoryByStatus(status));
            } else {
                inventoryList.setAll(inventoryService.getCurrentInventory());
            }
        } catch (SQLException e) {
            LOGGER.error("Filter failed", e);
            AlertUtil.showError("Database Error", "Unable to filter inventory: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdjustStock() {
        if (selectedRow == null) {
            AlertUtil.showWarning("No Selection", "Please select a product from the table first.");
            return;
        }

        String type = adjustmentTypeDropdown.getValue();
        String qtyText = adjustmentQuantityField.getText();
        String reason = reasonField.getText();
        String remarks = remarksField.getText();

        if (type == null || qtyText == null || qtyText.isBlank() || reason == null || reason.isBlank()) {
            AlertUtil.showWarning("Validation Error", "Please fill in all required fields (Type, Quantity, Reason).");
            return;
        }

        try {
            int quantity = Integer.parseInt(qtyText.trim());
            String performedBy = UserSession.getUserName() != null ? UserSession.getUserName() : "admin";
            inventoryService.adjustStock(selectedRow.getProductId(), type, quantity, reason, remarks, performedBy);
            AlertUtil.showInfo("Success", "Stock adjustment recorded successfully.");
            clearAdjustmentForm();
            loadInventory();
            loadSummary();
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation Error", "Quantity must be a valid integer.");
        } catch (Exception e) {
            LOGGER.error("Stock adjustment failed", e);
            AlertUtil.showError("Stock Adjustment Error", "Unable to adjust stock: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        categoryFilter.setValue(null);
        statusFilter.setValue(STATUS_ALL);
        loadInventory();
        loadSummary();
    }

    private void loadCategoryFilter() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            categoryFilter.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            LOGGER.error("Failed to load categories", e);
            AlertUtil.showError("Database Error", "Unable to load categories: " + e.getMessage());
        }
    }

    private void loadInventory() {
        try {
            inventoryList.setAll(inventoryService.getCurrentInventory());
        } catch (SQLException e) {
            LOGGER.error("Failed to load inventory", e);
            AlertUtil.showError("Database Error", "Unable to load inventory: " + e.getMessage());
        }
    }

    private void loadSummary() {
        try {
            InventoryService.InventorySummary summary = inventoryService.getInventorySummary();
            totalProductsLabel.setText(String.valueOf(summary.getTotalProducts()));
            totalStockItemsLabel.setText(String.valueOf(summary.getTotalStockItems()));
            lowStockCountLabel.setText(String.valueOf(summary.getLowStockCount()));
            outOfStockCountLabel.setText(String.valueOf(summary.getOutOfStockCount()));
        } catch (SQLException e) {
            LOGGER.error("Failed to load inventory summary", e);
            AlertUtil.showError("Database Error", "Unable to load inventory summary: " + e.getMessage());
        }
    }

    private void populateAdjustmentPanel(ProductDao.InventoryRow row) {
        if (row == null) {
            clearAdjustmentForm();
            return;
        }
        selectedProductLabel.setText(row.getProductName());
        currentStockLabel.setText(String.valueOf(row.getCurrentStock()));
    }

    private void clearAdjustmentForm() {
        adjustmentTypeDropdown.setValue(null);
        adjustmentQuantityField.clear();
        reasonField.clear();
        remarksField.clear();
        selectedProductLabel.setText("Select a product from table");
        currentStockLabel.setText("—");
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

    @FXML private void onDashboard() { navigateTo("/fxml/dashboard.fxml", "Dashboard", btnInventoryNav); }
    @FXML private void onNewBill() { navigateTo("/fxml/billing.fxml", "Billing", btnInventoryNav); }
    @FXML private void onProducts() { navigateTo("/fxml/product.fxml", "Products", btnInventoryNav); }
    @FXML private void onInventory() { handleResetFilters(); }
    @FXML private void onPurchases() { navigateTo("/fxml/purchase.fxml", "Purchases", btnInventoryNav); }
    @FXML private void onCustomersNav() { navigateTo("/fxml/customer.fxml", "Customers", btnInventoryNav); }
    @FXML private void onSuppliersNav() { navigateTo("/fxml/supplier.fxml", "Suppliers", btnInventoryNav); }
    @FXML private void onReports() { navigateTo("/fxml/report.fxml", "Reports", btnInventoryNav); }
    @FXML private void onSettings() {
        if (UserSession.getInstance().getCurrentRole() != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can access Settings.");
            return;
        }
        navigateTo("/fxml/settings.fxml", "Settings", btnInventoryNav);
    }
    @FXML private void onUsersNav() {
        if (UserSession.getInstance().getCurrentRole() != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can manage Users.");
            return;
        }
        navigateTo("/fxml/user_management.fxml", "Users Management", btnInventoryNav);
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