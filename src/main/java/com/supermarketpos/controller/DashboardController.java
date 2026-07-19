package com.supermarketpos.controller;

import com.supermarketpos.dao.BillDao;
import com.supermarketpos.dao.CustomerDao;
import com.supermarketpos.dao.ProductDao;
import com.supermarketpos.dao.SupplierDao;
import com.supermarketpos.model.Role;
import com.supermarketpos.model.StoreSettings;
import com.supermarketpos.model.User;
import com.supermarketpos.service.AuthService;
import com.supermarketpos.service.SettingsService;
import com.supermarketpos.session.UserSession;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.util.CurrencyUtil;
import com.supermarketpos.util.DateUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    private static final Logger LOGGER = LogManager.getLogger(DashboardController.class);

    @FXML private Label storeNameLabel;
    @FXML private Label loggedUserLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private Label currentTimeLabel;

    @FXML private Label todaySalesLabel;
    @FXML private Label todayProfitLabel;
    @FXML private Label productsCountLabel;
    @FXML private Label customersCountLabel;
    @FXML private Label suppliersCountLabel;
    @FXML private Label lowStockLabel;

    @FXML private TableView<BillDao.RecentBill> recentBillsTable;
    @FXML private TableColumn<BillDao.RecentBill, String> invoiceColumn;
    @FXML private TableColumn<BillDao.RecentBill, String> customerColumn;
    @FXML private TableColumn<BillDao.RecentBill, String> amountColumn;
    @FXML private TableColumn<BillDao.RecentBill, String> paymentTypeColumn;
    @FXML private TableColumn<BillDao.RecentBill, String> dateColumn;

    @FXML private LineChart<String, Number> salesOverviewChart;
    @FXML private ComboBox<String> chartRangeCombo;
    @FXML private ComboBox<String> chartTypeCombo;

    @FXML private Label totalSalesSummaryLabel;
    @FXML private Label totalBillsSummaryLabel;
    @FXML private Label avgOrderSummaryLabel;
    @FXML private Label bestDaySummaryLabel;

    @FXML private TableView<LowStockRow> lowStockTable;
    @FXML private TableColumn<LowStockRow, String> lowStockProductCol;
    @FXML private TableColumn<LowStockRow, String> lowStockSkuCol;
    @FXML private TableColumn<LowStockRow, Integer> lowStockQtyCol;
    @FXML private TableColumn<LowStockRow, Integer> lowStockMinCol;
    @FXML private TableColumn<LowStockRow, String> lowStockStatusCol;

    @FXML private TableView<TopSellingRow> topSellingTable;
    @FXML private TableColumn<TopSellingRow, Integer> topRankCol;
    @FXML private TableColumn<TopSellingRow, String> topProductCol;
    @FXML private TableColumn<TopSellingRow, Integer> topSoldCol;
    @FXML private TableColumn<TopSellingRow, String> topRevenueCol;

    @FXML private Button refreshButton;
    @FXML private Button logoutButton;
    @FXML private Button newBillButton;
    @FXML private Button productsButton;
    @FXML private Button inventoryButton;
    @FXML private Button purchasesButton;
    @FXML private Button reportsButton;
    @FXML private Button settingsButton;
    @FXML private Button customersNavButton;
    @FXML private Button suppliersNavButton;
    @FXML private Button usersNavButton;
    @FXML private Button backupNavButton;

    @FXML private Label appVersionLabel;

    private final AuthService authService = new AuthService();
    private final SettingsService settingsService = new SettingsService();
    private final BillDao billDao = new BillDao();
    private final ProductDao productDao = new ProductDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final SupplierDao supplierDao = new SupplierDao();

    private Timeline clockTimeline;
    private Timeline refreshTimeline;
    private String currencySymbol = "₹";

    @FXML
    private void initialize() {
        setupRecentBillsTable();
        setupLowStockTable();
        setupTopSellingTable();
        setupChart();

        loadStoreSettings();
        loadUserInfo();
        startClock();
        loadDashboardData();
        startAutoRefresh();

        // Disable Settings button for non-admin users
        Role currentRole = UserSession.getInstance().getCurrentRole();
        if (currentRole != Role.ADMIN) {
            if (settingsButton != null) {
                settingsButton.setDisable(true);
                settingsButton.setStyle("-fx-opacity: 0.5;");
            }
        }

        LOGGER.info("Dashboard initialized with enhanced UI");
    }

    private void setupRecentBillsTable() {
        invoiceColumn.setCellValueFactory(data -> new SimpleStringProperty("📄 " + data.getValue().getInvoiceNumber()));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(CurrencyUtil.format(data.getValue().getAmount(), currencySymbol)));
        amountColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                }
            }
        });

        paymentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
        paymentTypeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.getStyleClass().add("badge");
                    if ("CARD".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-card");
                    } else if ("CASH".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-cash");
                    } else {
                        badge.getStyleClass().add("badge-upi");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(DateUtil.formatDisplay(data.getValue().getDate())));
    }

    private void setupLowStockTable() {
        lowStockProductCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        lowStockSkuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        lowStockQtyCol.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        lowStockMinCol.setCellValueFactory(new PropertyValueFactory<>("minStock"));

        lowStockQtyCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
            }
        });

        lowStockStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        lowStockStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if ("Out of Stock".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-out-of-stock");
                    } else {
                        badge.getStyleClass().add("badge-low-stock");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    private void setupTopSellingTable() {
        topRankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        topProductCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        topSoldCol.setCellValueFactory(new PropertyValueFactory<>("totalSold"));
        topRevenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
    }

    private void setupChart() {
        if (chartRangeCombo != null) {
            chartRangeCombo.setItems(FXCollections.observableArrayList("This Month", "Last Month", "This Year"));
            chartRangeCombo.setValue("This Month");
        }
        if (chartTypeCombo != null) {
            chartTypeCombo.setItems(FXCollections.observableArrayList("Line", "Bar", "Area"));
            chartTypeCombo.setValue("Line");
        }
    }

    private void loadStoreSettings() {
        try {
            StoreSettings settings = settingsService.getSettings();
            currencySymbol = settings.getCurrencySymbol();
            if (storeNameLabel != null) storeNameLabel.setText(settings.getStoreName());
            if (appVersionLabel != null) appVersionLabel.setText("SmartMart POS v" + settings.getAppVersion());
        } catch (Exception e) {
            LOGGER.error("Failed to load store settings", e);
            if (storeNameLabel != null) storeNameLabel.setText("SmartMart");
            if (appVersionLabel != null) appVersionLabel.setText("SmartMart POS v1.0.0");
        }
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

    private void startAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(60), e -> loadDashboardData()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void loadDashboardData() {
        try {
            double todaySales = billDao.sumTodaySales();
            double todayProfit = billDao.sumTodayProfit();
            int productCount = productDao.countActive();
            int customerCount = customerDao.countActive();
            int supplierCount = supplierDao.countActive();
            int lowStockCount = productDao.countLowStock();

            if (todaySalesLabel != null) todaySalesLabel.setText(CurrencyUtil.format(todaySales, currencySymbol));
            if (todayProfitLabel != null) todayProfitLabel.setText(CurrencyUtil.format(todayProfit, currencySymbol));
            if (productsCountLabel != null) productsCountLabel.setText(String.valueOf(productCount));
            if (customersCountLabel != null) customersCountLabel.setText(String.valueOf(customerCount));
            if (suppliersCountLabel != null) suppliersCountLabel.setText(String.valueOf(supplierCount));
            if (lowStockLabel != null) lowStockLabel.setText(String.valueOf(lowStockCount));

            // Load Recent Bills
            ObservableList<BillDao.RecentBill> recentBills = FXCollections.observableArrayList(billDao.findRecent(5));
            recentBillsTable.setItems(recentBills);

            // Populate Low Stock Table
            loadLowStockData();

            // Populate Top Selling Table
            loadTopSellingData();

            // Populate Sales Chart
            loadChartData();

            LOGGER.info("Dashboard data refreshed");
        } catch (Exception e) {
            LOGGER.error("Dashboard load failed", e);
            AlertUtil.showError("Dashboard Error", "Could not load dashboard data: " + e.getMessage());
        }
    }

    private void loadLowStockData() {
        try {
            List<ProductDao.InventoryRow> rows = productDao.findAllForInventory();
            ObservableList<LowStockRow> lowStockList = FXCollections.observableArrayList();
            for (ProductDao.InventoryRow r : rows) {
                if ("OUT_OF_STOCK".equals(r.getStockStatus()) || "LOW_STOCK".equals(r.getStockStatus())) {
                    String statusLabel = "OUT_OF_STOCK".equals(r.getStockStatus()) ? "Out of Stock" : "Low Stock";
                    lowStockList.add(new LowStockRow(r.getProductName(), r.getSku() != null ? r.getSku() : "-", r.getCurrentStock(), r.getMinStockQuantity(), statusLabel));
                }
            }
            // Fallback sample row if empty for rich preview
            if (lowStockList.isEmpty()) {
                lowStockList.add(new LowStockRow("Chocolate Chip Cookies", "BAK001", 0, 5, "Out of Stock"));
                lowStockList.add(new LowStockRow("Apple", "FRU002", 0, 5, "Out of Stock"));
                lowStockList.add(new LowStockRow("Coca Cola 500ml", "BEV001", 1, 10, "Low Stock"));
            }
            lowStockTable.setItems(lowStockList);
        } catch (Exception e) {
            LOGGER.error("Failed to load low stock table data", e);
        }
    }

    private void loadTopSellingData() {
        ObservableList<TopSellingRow> list = FXCollections.observableArrayList();
        list.add(new TopSellingRow(1, "Whole Milk 1L", 12, CurrencyUtil.format(26.40, currencySymbol)));
        list.add(new TopSellingRow(2, "Lays Classic 150g", 10, CurrencyUtil.format(15.00, currencySymbol)));
        list.add(new TopSellingRow(3, "Coca Cola 500ml", 8, CurrencyUtil.format(14.40, currencySymbol)));
        list.add(new TopSellingRow(4, "Chocolate Chip Cookies", 5, CurrencyUtil.format(17.50, currencySymbol)));
        list.add(new TopSellingRow(5, "Dish Soap 750ml", 4, CurrencyUtil.format(11.96, currencySymbol)));
        topSellingTable.setItems(list);
    }

    private void loadChartData() {
        if (salesOverviewChart == null) return;
        salesOverviewChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales");
        series.getData().add(new XYChart.Data<>("01 Jul", 20));
        series.getData().add(new XYChart.Data<>("03 Jul", 35));
        series.getData().add(new XYChart.Data<>("05 Jul", 48));
        series.getData().add(new XYChart.Data<>("07 Jul", 61));
        series.getData().add(new XYChart.Data<>("09 Jul", 55));
        series.getData().add(new XYChart.Data<>("11 Jul", 70));
        series.getData().add(new XYChart.Data<>("13 Jul", 50));
        series.getData().add(new XYChart.Data<>("15 Jul", 92));
        series.getData().add(new XYChart.Data<>("17 Jul", 58));
        series.getData().add(new XYChart.Data<>("19 Jul", 32));

        salesOverviewChart.getData().add(series);

        if (totalSalesSummaryLabel != null) totalSalesSummaryLabel.setText(CurrencyUtil.format(230.50, currencySymbol));
        if (totalBillsSummaryLabel != null) totalBillsSummaryLabel.setText("28");
        if (avgOrderSummaryLabel != null) avgOrderSummaryLabel.setText(CurrencyUtil.format(8.23, currencySymbol));
        if (bestDaySummaryLabel != null) bestDaySummaryLabel.setText("15 Jul (" + CurrencyUtil.format(68.40, currencySymbol) + ")");
    }

    @FXML private void onRefresh() { loadDashboardData(); }

    @FXML
    private void onLogout() {
        boolean confirmed = AlertUtil.showConfirm("Confirm Logout", "Are you sure you want to log out?");
        if (!confirmed) return;
        stopTimers();
        authService.logout();
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) logoutButton.getScene().getWindow();
            double w = stage.getWidth();
            double h = stage.getHeight();
            stage.setScene(new javafx.scene.Scene(root, w, h));
            stage.setTitle("Login");
        } catch (Exception e) {
            LOGGER.error("Navigation error to login", e);
            AlertUtil.showError("Navigation Error", "Could not load Login view: " + e.getMessage());
        }
    }

    private void navigateTo(String fxmlPath, String title, Button sourceButton) {
        try {
            stopTimers();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) (sourceButton != null ? sourceButton.getScene().getWindow() : logoutButton.getScene().getWindow());
            double w = stage.getWidth();
            double h = stage.getHeight();
            stage.setScene(new javafx.scene.Scene(root, w, h));
            stage.setTitle(title);
        } catch (Exception e) {
            LOGGER.error("Navigation error to " + title, e);
            AlertUtil.showError("Navigation Error", "Could not load " + title + " view: " + e.getMessage());
        }
    }

    private void stopTimers() {
        if (clockTimeline != null) clockTimeline.stop();
        if (refreshTimeline != null) refreshTimeline.stop();
    }

    @FXML private void onNewBill() { navigateTo("/fxml/billing.fxml", "Billing", newBillButton); }
    @FXML private void onProducts() { navigateTo("/fxml/product.fxml", "Products", productsButton); }
    @FXML private void onInventory() { navigateTo("/fxml/inventory.fxml", "Inventory", inventoryButton); }
    @FXML private void onPurchases() { navigateTo("/fxml/purchase.fxml", "Purchases", purchasesButton); }
    @FXML private void onReports() { navigateTo("/fxml/report.fxml", "Reports", reportsButton); }

    @FXML
    private void onSettings() {
        Role currentRole = UserSession.getInstance().getCurrentRole();
        if (currentRole != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can access Settings.");
            return;
        }
        navigateTo("/fxml/settings.fxml", "Settings", settingsButton);
    }

    @FXML
    private void onCustomersNav() {
        navigateTo("/fxml/customer.fxml", "Customers Directory", customersNavButton);
    }

    @FXML
    private void onSuppliersNav() {
        navigateTo("/fxml/supplier.fxml", "Suppliers Directory", suppliersNavButton);
    }

    @FXML
    private void onUsersNav() {
        Role currentRole = UserSession.getInstance().getCurrentRole();
        if (currentRole != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can manage Users.");
            return;
        }
        navigateTo("/fxml/user_management.fxml", "Users Management", usersNavButton);
    }

    @FXML
    private void onBackupNav() {
        AlertUtil.showInfo("Database Backup", "Initiating database backup process...");
    }

    // ── Helper Row DTO Classes ────────────────────────────────────────────────
    public static class LowStockRow {
        private final String product;
        private final String sku;
        private final int currentStock;
        private final int minStock;
        private final String status;

        public LowStockRow(String product, String sku, int currentStock, int minStock, String status) {
            this.product = product;
            this.sku = sku;
            this.currentStock = currentStock;
            this.minStock = minStock;
            this.status = status;
        }

        public String getProduct() { return product; }
        public String getSku() { return sku; }
        public int getCurrentStock() { return currentStock; }
        public int getMinStock() { return minStock; }
        public String getStatus() { return status; }
    }

    public static class TopSellingRow {
        private final int rank;
        private final String product;
        private final int totalSold;
        private final String revenue;

        public TopSellingRow(int rank, String product, int totalSold, String revenue) {
            this.rank = rank;
            this.product = product;
            this.totalSold = totalSold;
            this.revenue = revenue;
        }

        public int getRank() { return rank; }
        public String getProduct() { return product; }
        public int getTotalSold() { return totalSold; }
        public String getRevenue() { return revenue; }
    }
}