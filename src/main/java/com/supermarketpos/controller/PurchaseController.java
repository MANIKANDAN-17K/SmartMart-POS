package com.supermarketpos.controller;

import com.supermarketpos.dao.PurchaseDao;
import com.supermarketpos.model.Role;
import com.supermarketpos.model.User;
import com.supermarketpos.report.PurchaseReport;
import com.supermarketpos.service.AuthService;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PurchaseController {

    private static final Logger LOGGER = LogManager.getLogger(PurchaseController.class);

    // Header & User Profile Controls
    @FXML private Label loggedUserLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private Label currentTimeLabel;

    // KPI Cards
    @FXML private Label lblTotalPurchases;
    @FXML private Label lblActiveSuppliers;
    @FXML private Label lblTotalExpenses;

    // Filter controls
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cmbSupplier;

    // Purchase Table
    @FXML private TableView<PurchaseReport> purchaseTable;
    @FXML private TableColumn<PurchaseReport, Integer> colPurchaseId;
    @FXML private TableColumn<PurchaseReport, String> colPurchaseDate;
    @FXML private TableColumn<PurchaseReport, String> colSupplier;
    @FXML private TableColumn<PurchaseReport, String> colProduct;
    @FXML private TableColumn<PurchaseReport, Integer> colQty;
    @FXML private TableColumn<PurchaseReport, Double> colAmount;

    // Buttons
    @FXML private Button refreshButton;
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

    private final PurchaseDao purchaseDao = new PurchaseDao();
    private final AuthService authService = new AuthService();
    private final ObservableList<PurchaseReport> purchasesList = FXCollections.observableArrayList();

    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadUserInfo();
        startClock();

        dpStartDate.setValue(LocalDate.now().minusMonths(1));
        dpEndDate.setValue(LocalDate.now());

        loadSuppliers();
        onSearch();
        LOGGER.info("PurchaseController initialized");
    }

    private void setupTableColumns() {
        colPurchaseId.setCellValueFactory(new PropertyValueFactory<>("purchaseId"));
        colPurchaseDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPurchaseDate() != null ?
                        data.getValue().getPurchaseDate().toString() : ""));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("purchaseAmount"));

        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : CurrencyUtil.format(val));
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

    private void loadSuppliers() {
        ObservableList<String> sups = FXCollections.observableArrayList("All");
        sups.addAll(purchaseDao.getAllSupplierNames());
        cmbSupplier.setItems(sups);
        cmbSupplier.getSelectionModel().selectFirst();
    }

    @FXML
    private void onSearch() {
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();
        String supplier = cmbSupplier.getValue();

        if (start == null || end == null) {
            AlertUtil.showWarning("Filter Error", "Please select start and end dates.");
            return;
        }

        List<PurchaseReport> list = purchaseDao.getPurchaseReport(start, end, supplier);
        purchasesList.setAll(list);
        purchaseTable.setItems(purchasesList);
        updateKpiSummary();
    }

    private void updateKpiSummary() {
        int totalOrders = purchasesList.size();
        double totalExpense = purchasesList.stream()
                .mapToDouble(PurchaseReport::getPurchaseAmount)
                .sum();
        long uniqueSuppliers = purchasesList.stream()
                .map(PurchaseReport::getSupplierName)
                .distinct()
                .count();

        if (lblTotalPurchases != null) lblTotalPurchases.setText(String.valueOf(totalOrders));
        if (lblActiveSuppliers != null) lblActiveSuppliers.setText(String.valueOf(uniqueSuppliers));
        if (lblTotalExpenses != null) lblTotalExpenses.setText(CurrencyUtil.format(totalExpense));
    }

    // Navigation Handlers
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

    @FXML private void onDashboard() { navigateTo("/fxml/dashboard.fxml", "Dashboard", btnPurchasesNav); }
    @FXML private void onNewBill() { navigateTo("/fxml/billing.fxml", "Billing", btnPurchasesNav); }
    @FXML private void onProducts() { navigateTo("/fxml/product.fxml", "Products", btnPurchasesNav); }
    @FXML private void onInventory() { navigateTo("/fxml/inventory.fxml", "Inventory", btnPurchasesNav); }
    @FXML private void onPurchases() { onSearch(); }
    @FXML private void onCustomersNav() { navigateTo("/fxml/customer.fxml", "Customers", btnPurchasesNav); }
    @FXML private void onSuppliersNav() { navigateTo("/fxml/supplier.fxml", "Suppliers", btnPurchasesNav); }
    @FXML private void onReports() { navigateTo("/fxml/report.fxml", "Reports", btnPurchasesNav); }
    @FXML private void onSettings() {
        if (UserSession.getInstance().getCurrentRole() != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can access Settings.");
            return;
        }
        navigateTo("/fxml/settings.fxml", "Settings", btnPurchasesNav);
    }
    @FXML private void onUsersNav() {
        if (UserSession.getInstance().getCurrentRole() != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can manage Users.");
            return;
        }
        navigateTo("/fxml/user_management.fxml", "Users Management", btnPurchasesNav);
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

    @FXML
    private void onBackToDashboard() {
        onDashboard();
    }
}
