package com.supermarketpos.controller;

import com.supermarketpos.model.SyncLog;
import com.supermarketpos.service.GoogleSheetsService;
import com.supermarketpos.service.SyncService;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.util.DateUtil;
import com.supermarketpos.util.UserSession;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class GoogleSheetsController {

    @FXML private Label authStatusLabel;
    @FXML private Label lastSyncLabel;
    @FXML private TextField spreadsheetIdField;
    @FXML private TableView<SyncLog> syncHistoryTable;

    private final GoogleSheetsService sheetsService = new GoogleSheetsService();
    private final SyncService syncService = new SyncService();

    @FXML
    public void initialize() {
        updateAuthStatus();
        refreshHistory();
    }

    @FXML
    public void onLogin() {
        runInBackground(() -> {
            sheetsService.authenticate();
            Platform.runLater(() -> {
                updateAuthStatus();
                AlertUtil.showInfo("Google account connected successfully.");
            });
        }, "Google authentication failed");
    }

    @FXML
    public void onLogout() {
        sheetsService.logout();
        updateAuthStatus();
        AlertUtil.showInfo("Logged out of Google account.");
    }

    @FXML
    public void onCreateSpreadsheet() {
        TextInputDialog dialog = new TextInputDialog("SmartMart POS Data");
        dialog.setHeaderText("Enter a title for the new spreadsheet");
        dialog.showAndWait().ifPresent(title ->
                runInBackground(() -> {
                    String id = sheetsService.createSpreadsheet(title);
                    Platform.runLater(() -> {
                        spreadsheetIdField.setText(id);
                        AlertUtil.showInfo("Spreadsheet created: " + id);
                    });
                }, "Failed to create spreadsheet"));
    }

    @FXML
    public void onSelectSpreadsheet() {
        String id = spreadsheetIdField.getText();
        runInBackground(() -> {
            sheetsService.selectSpreadsheet(id);
            Platform.runLater(() -> AlertUtil.showInfo("Spreadsheet selected successfully."));
        }, "Failed to select spreadsheet");
    }

    @FXML
    public void onSyncProducts() {
        runInBackground(() -> {
            // Replace emptyList() with real data pulled from ProductService
            sheetsService.syncProducts(List.of());
            afterSync("Products synchronized.");
        }, "Product synchronization failed");
    }

    @FXML
    public void onSyncCustomers() {
        runInBackground(() -> {
            sheetsService.syncCustomers(List.of());
            afterSync("Customers synchronized.");
        }, "Customer synchronization failed");
    }

    @FXML
    public void onSyncPurchases() {
        runInBackground(() -> {
            sheetsService.syncPurchases(List.of());
            afterSync("Purchases synchronized.");
        }, "Purchase synchronization failed");
    }

    @FXML
    public void onSyncSales() {
        runInBackground(() -> {
            sheetsService.syncSales(List.of());
            afterSync("Sales synchronized.");
        }, "Sales synchronization failed");
    }

    @FXML
    public void onSyncInventory() {
        runInBackground(() -> {
            sheetsService.syncInventory(List.of());
            afterSync("Inventory synchronized.");
        }, "Inventory synchronization failed");
    }

    @FXML
    public void onSyncAll() {
        runInBackground(() -> {
            sheetsService.syncAll(List.of(), List.of(), List.of(), List.of(), List.of());
            afterSync("All data synchronized.");
        }, "Full synchronization failed");
    }

    @FXML
    public void onRefreshHistory() {
        refreshHistory();
    }

    private void afterSync(String message) {
        Platform.runLater(() -> {
            AlertUtil.showInfo(message);
            refreshHistory();
        });
    }

    private void refreshHistory() {
        try {
            List<SyncLog> history = syncService.getSyncHistory();
            syncHistoryTable.getItems().setAll(history);

            SyncLog last = syncService.getLastSync();
            lastSyncLabel.setText(last != null
                    ? DateUtil.format(last.getTimestamp()) + " (" + last.getStatus() + ")"
                    : "Never synced");
        } catch (Exception e) {
            AlertUtil.showError("Failed to load synchronization history.");
        }
    }

    private void updateAuthStatus() {
        authStatusLabel.setText(sheetsService.isAuthenticated()
                ? "Connected as: " + UserSession.getInstance().getUsername()
                : "Not connected");
    }

    private interface Task {
        void run() throws Exception;
    }

    private void runInBackground(Task task, String errorPrefix) {
        new Thread(() -> {
            try {
                task.run();
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError(errorPrefix + ": " + e.getMessage()));
            }
        }).start();
    }
}