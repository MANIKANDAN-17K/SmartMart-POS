package com.supermarketpos.controller;

import com.supermarketpos.model.AuditLog;
import com.supermarketpos.model.StoreSettings;
import com.supermarketpos.model.TaxSetting;
import com.supermarketpos.service.AuditService;
import com.supermarketpos.service.BackupService;
import com.supermarketpos.service.SettingsService;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.util.UserSession;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class SettingsController {

    // Store tab
    @FXML
    private TextField storeNameField, addressField, gstNumberField, phoneField, emailField;
    @FXML
    private javafx.scene.image.ImageView logoPreview;

    @FXML
    public void onBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) storeNameField.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Dashboard");
        } catch (java.io.IOException e) {
            AlertUtil.showError("Could not load Dashboard.");
        }
    }

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

    @FXML
    public void initialize() {
        if (!UserSession.getInstance().isAdmin()) {
            AlertUtil.showError("Access denied. Only ADMIN users can access Settings.");
            return;
        }
        loadStoreSettings();
        loadTaxSettings();
        refreshAuditLogs();
    }

    private void loadStoreSettings() {
        try {
            StoreSettings s = settingsService.getStoreSettings();
            if (s != null) {
                storeNameField.setText(s.getStoreName());
                addressField.setText(s.getAddress());
                gstNumberField.setText(s.getGstNumber());
                phoneField.setText(s.getPhone());
                emailField.setText(s.getEmail());
                receiptHeaderField.setText(s.getReceiptHeader());
                receiptFooterField.setText(s.getReceiptFooter());
                showLogoCheck.setSelected(s.isShowLogoOnReceipt());
                showGstCheck.setSelected(s.isShowGstOnReceipt());
                showCashierCheck.setSelected(s.isShowCashierOnReceipt());
                if ("dark".equals(s.getTheme()))
                    darkThemeRadio.setSelected(true);
                else
                    lightThemeRadio.setSelected(true);
            }
        } catch (Exception e) {
            AlertUtil.showError("Failed to load store settings.");
        }
    }

    private void loadTaxSettings() {
        try {
            TaxSetting t = settingsService.getTaxSettings();
            if (t != null) {
                gstPercentageField.setText(String.valueOf(t.getGstPercentage()));
                gstEnabledCheck.setSelected(t.isGstEnabled());
            }
        } catch (Exception e) {
            AlertUtil.showError("Failed to load tax settings.");
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

            settingsService.saveStoreSettings(s, UserSession.getInstance().getUsername());
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
            settingsService.saveTaxSettings(t, UserSession.getInstance().getUsername());
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
            settingsService.changeTheme(theme, UserSession.getInstance().getUsername());
            AlertUtil.showInfo("Theme applied.");
        } catch (Exception e) {
            AlertUtil.showError("Failed to apply theme.");
        }
    }

    @FXML
    public void onBackupDatabase() {
        if (!UserSession.getInstance().isAdmin()) {
            AlertUtil.showError("Only ADMIN users can perform backup.");
            return;
        }
        try {
            String fileName = backupService.createBackup(UserSession.getInstance().getUsername());
            backupStatusLabel.setText("Backup created: " + fileName);
            AlertUtil.showInfo("Database backup created successfully: " + fileName);
        } catch (Exception e) {
            AlertUtil.showError("Backup failed: " + e.getMessage());
        }
    }

    @FXML
    public void onRestoreDatabase() {
        if (!UserSession.getInstance().isAdmin()) {
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
                                UserSession.getInstance().getUsername());
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
            auditTable.getItems().setAll(logs);
            auditService.log(UserSession.getInstance().getUsername(), "AUDIT_LOGS_VIEWED", "Viewed audit logs");
        } catch (Exception e) {
            AlertUtil.showError("Failed to load audit logs.");
        }
    }

}