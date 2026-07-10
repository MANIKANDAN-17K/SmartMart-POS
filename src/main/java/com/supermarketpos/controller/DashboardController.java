package com.supermarketpos.controller;

import com.supermarketpos.dao.BillDao;
import com.supermarketpos.dao.CustomerDao;
import com.supermarketpos.dao.ProductDao;
import com.supermarketpos.dao.SupplierDao;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DashboardController {

    private static final Logger LOGGER = LogManager.getLogger(DashboardController.class);

    @FXML private Label storeNameLabel;
    @FXML private Label loggedUserLabel;
    @FXML private Label roleLabel;
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

    @FXML private Button refreshButton;
    @FXML private Button logoutButton;
    @FXML private Button newBillButton;
    @FXML private Button productsButton;
    @FXML private Button inventoryButton;
    @FXML private Button purchasesButton;
    @FXML private Button reportsButton;
    @FXML private Button settingsButton;

    @FXML private Label appVersionLabel;

    private final AuthService authService = new AuthService();
    private final SettingsService settingsService = new SettingsService();
    private final BillDao billDao = new BillDao();
    private final ProductDao productDao = new ProductDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final SupplierDao supplierDao = new SupplierDao();

    private Timeline clockTimeline;
    private Timeline refreshTimeline;
    private String currencySymbol = "$";

    @FXML
    private void initialize() {
        invoiceColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        amountColumn.setCellValueFactory(data ->
                new SimpleStringProperty(CurrencyUtil.format(data.getValue().getAmount(), currencySymbol)));
        paymentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
        dateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(DateUtil.formatDisplay(data.getValue().getDate())));

        loadStoreSettings();
        loadUserInfo();
        startClock();
        loadDashboardData();
        startAutoRefresh();

        LOGGER.info("Dashboard opened");
    }

    private void loadStoreSettings() {
        try {
            StoreSettings settings = settingsService.getSettings();
            currencySymbol = settings.getCurrencySymbol();
            storeNameLabel.setText(settings.getStoreName());
            appVersionLabel.setText("v" + settings.getAppVersion());
        } catch (Exception e) {
            LOGGER.error("Failed to load store settings", e);
            storeNameLabel.setText("SmartMart POS");
            appVersionLabel.setText("v1.0.0");
        }
    }

    private void loadUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            loggedUserLabel.setText(currentUser.getUsername());
            roleLabel.setText(UserSession.getInstance().getCurrentRole().name());
        } else {
            loggedUserLabel.setText("Unknown");
            roleLabel.setText("-");
        }
    }

    private void startClock() {
        currentTimeLabel.setText(DateUtil.nowDisplay());
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> currentTimeLabel.setText(DateUtil.nowDisplay())));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
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

            todaySalesLabel.setText(CurrencyUtil.format(todaySales, currencySymbol));
            todayProfitLabel.setText(CurrencyUtil.format(todayProfit, currencySymbol));
            productsCountLabel.setText(String.valueOf(productCount));
            customersCountLabel.setText(String.valueOf(customerCount));
            suppliersCountLabel.setText(String.valueOf(supplierCount));
            lowStockLabel.setText(String.valueOf(lowStockCount));

            ObservableList<BillDao.RecentBill> recentBills =
                    FXCollections.observableArrayList(billDao.findRecent(5));
            recentBillsTable.setItems(recentBills);

            LOGGER.info("Dashboard refreshed");
        } catch (Exception e) {
            LOGGER.error("Dashboard load failed", e);
            AlertUtil.showError("Dashboard Error",
                    "Could not load dashboard data. Check your database connection and try again.");
        }
    }

    @FXML
    private void onRefresh() {
        loadDashboardData();
    }

    @FXML
    private void onLogout() {
        boolean confirmed = AlertUtil.showConfirm("Confirm Logout", "Are you sure you want to log out?");
        if (!confirmed) {
            return;
        }
        stopTimers();
        authService.logout();
        navigateToLogin();
    }

    private void navigateToLogin() {
        // Scene switching belongs in MainApp / a shared navigation helper, both
        // restricted files for this sprint. Wire this once that hook exists.
        LOGGER.info("User logged out, navigation to Login pending MainApp wiring");
    }

    private void stopTimers() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }

    @FXML private void onNewBill() { LOGGER.info("Quick action: New Bill (Billing module not yet wired)"); }
    @FXML private void onProducts() { LOGGER.info("Quick action: Products (Product module not yet wired)"); }
    @FXML private void onInventory() { LOGGER.info("Quick action: Inventory (Inventory module not yet wired)"); }
    @FXML private void onPurchases() { LOGGER.info("Quick action: Purchases (Purchase module not yet wired)"); }
    @FXML private void onReports() { LOGGER.info("Quick action: Reports (Report module not yet wired)"); }
    @FXML private void onSettings() { LOGGER.info("Quick action: Settings (Settings module not yet wired)"); }
}