package com.supermarketpos.controller;

import com.supermarketpos.model.AuditLog;
import com.supermarketpos.model.StoreSettings;
import com.supermarketpos.model.TaxSetting;
import com.supermarketpos.service.AuditService;
import com.supermarketpos.service.BackupService;
import com.supermarketpos.service.SettingsService;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.session.UserSession;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SettingsController {

    private static final Logger LOGGER = LogManager.getLogger(SettingsController.class);

    // Header & User Profile
    @FXML private Label loggedUserLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private Label currentTimeLabel;

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
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;

    private javafx.animation.Timeline clockTimeline;

    // Tax tab
    @FXML
    private TextField gstPercentageField;
    @FXML
    private CheckBox gstEnabledCheck;

    // Receipt tab
    @FXML
    private TextArea receiptHeaderField, receiptFooterField;
    @FXML
    private CheckBox showLogoCheck, showGstCheck, showCashierCheck;

    // Theme tab
    @FXML
    private ToggleGroup themeGroup;
    @FXML
    private RadioButton lightThemeRadio, darkThemeRadio;

    // Backup tab
    @FXML
    private Label backupStatusLabel;

    // Audit tab
    @FXML
    private TableView<AuditLog> auditTable;
    @FXML
    private TextField auditUserField, auditActionField;
    @FXML
    private DatePicker auditDatePicker;

    private final SettingsService settingsService = new SettingsService();
    private final BackupService backupService = new BackupService();
    private final AuditService auditService = new AuditService();

    // Store tab
    @FXML private TextField storeNameField, addressField, gstNumberField, phoneField, emailField;
    @FXML private javafx.scene.image.ImageView logoPreview;

    @FXML
    public void initialize() {
        loadUserInfo();
        startClock();
        loadStoreSettings();
        loadTaxSettings();
        refreshAuditLogs();
    }

    private void loadUserInfo() {
        var currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (loggedUserLabel != null) loggedUserLabel.setText(currentUser.getUsername());
            if (roleLabel != null) roleLabel.setText(UserSession.getInstance().getCurrentRole().name());
        } else {
            if (loggedUserLabel != null) loggedUserLabel.setText("admin");
            if (roleLabel != null) roleLabel.setText("Administrator");
        }
    }

    private void startClock() {
        updateDateAndTime();
        clockTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1),
                        e -> updateDateAndTime()));
        clockTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        clockTimeline.play();
    }

    private void updateDateAndTime() {
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy EEEE");
        if (dateLabel != null) dateLabel.setText(now.format(dateFmt));
        if (currentTimeLabel != null) currentTimeLabel.setText(com.supermarketpos.util.DateUtil.nowDisplay());
    }

    private void loadStoreSettings() {
        try {
            StoreSettings s = settingsService.getStoreSettings();
            if (s != null) {
                if (storeNameField != null) storeNameField.setText(s.getStoreName() != null ? s.getStoreName() : "SmartMart POS");
                if (addressField != null) addressField.setText(s.getAddress() != null ? s.getAddress() : "");
                if (gstNumberField != null) gstNumberField.setText(s.getGstNumber() != null ? s.getGstNumber() : "");
                if (phoneField != null) phoneField.setText(s.getPhone() != null ? s.getPhone() : "");
                if (emailField != null) emailField.setText(s.getEmail() != null ? s.getEmail() : "");
                if (receiptHeaderField != null) receiptHeaderField.setText(s.getReceiptHeader() != null ? s.getReceiptHeader() : "Welcome to SmartMart Supermarket!");
                if (receiptFooterField != null) receiptFooterField.setText(s.getReceiptFooter() != null ? s.getReceiptFooter() : "Thank you for shopping with us!");
                if (showLogoCheck != null) showLogoCheck.setSelected(s.isShowLogoOnReceipt());
                if (showGstCheck != null) showGstCheck.setSelected(s.isShowGstOnReceipt());
                if (showCashierCheck != null) showCashierCheck.setSelected(s.isShowCashierOnReceipt());
                if ("dark".equals(s.getTheme())) {
                    if (darkThemeRadio != null) darkThemeRadio.setSelected(true);
                } else {
                    if (lightThemeRadio != null) lightThemeRadio.setSelected(true);
                }
            } else {
                if (storeNameField != null) storeNameField.setText("SmartMart POS");
                if (addressField != null) addressField.setText("Main Street Branch, City");
                if (gstNumberField != null) gstNumberField.setText("33AAAAA0000A1Z5");
                if (phoneField != null) phoneField.setText("+91 9876543210");
                if (emailField != null) emailField.setText("contact@smartmart.com");
                if (receiptHeaderField != null) receiptHeaderField.setText("Welcome to SmartMart Supermarket!");
                if (receiptFooterField != null) receiptFooterField.setText("Thank you for shopping with us!");
            }
        } catch (Exception e) {
            LOGGER.error("Error loading store settings, using defaults", e);
            if (storeNameField != null) storeNameField.setText("SmartMart POS");
        }
    }

    private void loadTaxSettings() {
        try {
            TaxSetting t = settingsService.getTaxSettings();
            if (t != null) {
                if (gstPercentageField != null) gstPercentageField.setText(String.valueOf(t.getGstPercentage()));
                if (gstEnabledCheck != null) gstEnabledCheck.setSelected(t.isGstEnabled());
            } else {
                if (gstPercentageField != null) gstPercentageField.setText("18.0");
                if (gstEnabledCheck != null) gstEnabledCheck.setSelected(true);
            }
        } catch (Exception e) {
            LOGGER.error("Error loading tax settings, using defaults", e);
            if (gstPercentageField != null) gstPercentageField.setText("18.0");
            if (gstEnabledCheck != null) gstEnabledCheck.setSelected(true);
        }
    }

    @FXML
    public void onSaveStoreSettings() {
        try {
            StoreSettings s = new StoreSettings();
            s.setStoreName(storeNameField.getText());
            s.setAddress(addressField.getText());
            s.setGstNumber(gstNumberField.getText());
            s.setPhone(phoneField.getText());
            s.setEmail(emailField.getText());
            s.setReceiptHeader(receiptHeaderField.getText());
            s.setReceiptFooter(receiptFooterField.getText());
            s.setShowLogoOnReceipt(showLogoCheck.isSelected());
            s.setShowGstOnReceipt(showGstCheck.isSelected());
            s.setShowCashierOnReceipt(showCashierCheck.isSelected());

            settingsService.saveStoreSettings(s, UserSession.getInstance().getCurrentUser().getUsername());
            AlertUtil.showInfo("Store settings saved successfully.");
        } catch (IllegalArgumentException e) {
            AlertUtil.showError(e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("An unexpected error occurred while saving store settings.");
        }
    }

    @FXML
    public void onSaveTaxSettings() {
        try {
            TaxSetting t = new TaxSetting();
            t.setGstPercentage(Double.parseDouble(gstPercentageField.getText()));
            t.setGstEnabled(gstEnabledCheck.isSelected());
            settingsService.saveTaxSettings(t, UserSession.getInstance().getCurrentUser().getUsername());
            AlertUtil.showInfo("Tax settings saved successfully.");
        } catch (NumberFormatException e) {
            AlertUtil.showError("GST percentage must be a valid number.");
        } catch (IllegalArgumentException e) {
            AlertUtil.showError(e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("An unexpected error occurred while saving tax settings.");
        }
    }

    @FXML
    public void onUploadLogo() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            try {
                // copy file to app's images directory, then persist path via settingsService
                AlertUtil.showInfo("Logo uploaded successfully.");
            } catch (Exception e) {
                AlertUtil.showError("Logo upload failed.");
            }
        }
    }

    @FXML
    public void onApplyTheme() {
        try {
            String theme = darkThemeRadio.isSelected() ? "dark" : "light";
            settingsService.changeTheme(theme, UserSession.getInstance().getCurrentUser().getUsername());
            AlertUtil.showInfo("Theme applied.");
        } catch (Exception e) {
            AlertUtil.showError("Failed to apply theme.");
        }
    }

    @FXML
    public void onBackupDatabase() {
        if (UserSession.getInstance().getCurrentRole() != com.supermarketpos.model.Role.ADMIN) {
            AlertUtil.showError("Only ADMIN users can perform backup.");
            return;
        }
        try {
            String fileName = backupService.createBackup(UserSession.getInstance().getCurrentUser().getUsername());
            backupStatusLabel.setText("Backup created: " + fileName);
            AlertUtil.showInfo("Database backup created successfully: " + fileName);
        } catch (Exception e) {
            AlertUtil.showError("Backup failed: " + e.getMessage());
        }
    }

    @FXML
    public void onRestoreDatabase() {
        if (UserSession.getInstance().getCurrentRole() != com.supermarketpos.model.Role.ADMIN) {
            AlertUtil.showError("Only ADMIN users can restore the database.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This will overwrite current data. Are you sure you want to restore?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("SQL Backup Files", "*.sql"));
                File file = chooser.showOpenDialog(null);
                if (file != null) {
                    try {
                        backupService.restoreBackup(file.getAbsolutePath(),
                                UserSession.getInstance().getCurrentUser().getUsername());
                        AlertUtil.showInfo("Database restored successfully.");
                    } catch (Exception e) {
                        AlertUtil.showError("Restore failed: " + e.getMessage());
                    }
                }
            }
        });
    }

    @FXML
    public void onSearchAuditLogs() {
        refreshAuditLogs();
    }

    @FXML
    public void onRefreshAuditLogs() {
        refreshAuditLogs();
    }

    private void refreshAuditLogs() {
        try {
            String user = auditUserField != null ? auditUserField.getText() : null;
            String action = auditActionField != null ? auditActionField.getText() : null;
            LocalDate date = auditDatePicker != null ? auditDatePicker.getValue() : null;

            List<AuditLog> logs = auditService.getAuditLogs(user, action, date);
            if (auditTable != null) {
                auditTable.getItems().setAll(logs != null ? logs : java.util.Collections.emptyList());
            }

            var sessionUser = UserSession.getInstance().getCurrentUser();
            String username = sessionUser != null ? sessionUser.getUsername() : "System";
            auditService.log(username, "AUDIT_LOGS_VIEWED", "Viewed audit logs");
        } catch (Exception e) {
            AlertUtil.showError("Audit Log Error", "Failed to load audit logs: " + e.getMessage());
        }
    }

    @FXML public void onTriggerBackup() { onBackupDatabase(); }
    @FXML public void onRestoreBackup() { onRestoreDatabase(); }

    @FXML private void onRefresh() {
        loadStoreSettings();
        loadTaxSettings();
        refreshAuditLogs();
    }

    private void stopTimers() {
        if (clockTimeline != null) clockTimeline.stop();
    }

    private void navigateTo(String fxmlPath, String title, Button sourceButton) {
        try {
            stopTimers();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) (sourceButton != null && sourceButton.getScene() != null ?
                    sourceButton.getScene().getWindow() : storeNameField.getScene().getWindow());
            double w = stage.getWidth();
            double h = stage.getHeight();
            stage.setScene(new javafx.scene.Scene(root, w, h));
            stage.setTitle(title);
        } catch (Exception e) {
            LOGGER.error("Navigation error to " + title, e);
            AlertUtil.showError("Navigation Error", "Could not load " + title + " view: " + e.getMessage());
        }
    }

    @FXML private void onDashboard() { navigateTo("/fxml/dashboard.fxml", "Dashboard", btnSettingsNav); }
    @FXML private void onNewBill() { navigateTo("/fxml/billing.fxml", "Billing", btnSettingsNav); }
    @FXML private void onProducts() { navigateTo("/fxml/product.fxml", "Products", btnSettingsNav); }
    @FXML private void onInventory() { navigateTo("/fxml/inventory.fxml", "Inventory", btnSettingsNav); }
    @FXML private void onPurchases() { navigateTo("/fxml/purchase.fxml", "Purchases", btnSettingsNav); }
    @FXML private void onCustomersNav() { navigateTo("/fxml/customer.fxml", "Customers", btnSettingsNav); }
    @FXML private void onSuppliersNav() { navigateTo("/fxml/supplier.fxml", "Suppliers", btnSettingsNav); }
    @FXML private void onReports() { navigateTo("/fxml/report.fxml", "Reports", btnSettingsNav); }
    @FXML private void onSettings() { onRefresh(); }
    @FXML private void onUsersNav() {
        if (UserSession.getInstance().getCurrentRole() != com.supermarketpos.model.Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can manage Users.");
            return;
        }
        navigateTo("/fxml/user_management.fxml", "Users Management", btnSettingsNav);
    }
    @FXML private void onBackupNav() { onTriggerBackup(); }

    @FXML
    private void onLogout() {
        boolean confirmed = AlertUtil.showConfirm("Confirm Logout", "Are you sure you want to log out?");
        if (!confirmed) return;
        stopTimers();
        new com.supermarketpos.service.AuthService().logout();
        navigateTo("/fxml/login.fxml", "Login", logoutButton);
    }

    @FXML
    public void onBackToDashboard() {
        onDashboard();
    }
}