package com.supermarketpos.service;

import com.supermarketpos.google.GoogleAuthHandler;
import com.supermarketpos.google.SheetsApiClient;
import com.supermarketpos.google.SheetsSyncMapper;
import com.supermarketpos.model.SyncLog;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GoogleSheetsService {

    private final GoogleAuthHandler authHandler = new GoogleAuthHandler();
    private SheetsApiClient apiClient;
    private String spreadsheetId;
    private final SyncService syncService = new SyncService();

    // Prevents multiple simultaneous synchronizations, per validation rules
    private final AtomicBoolean syncInProgress = new AtomicBoolean(false);

    public boolean isAuthenticated() {
        return authHandler.isAuthenticated();
    }

    public void authenticate() throws Exception {
        if (!isInternetAvailable()) {
            throw new IllegalStateException("Internet connection is required for Google authentication.");
        }
        authHandler.login();
        apiClient = new SheetsApiClient(authHandler);
    }

    public void logout() {
        authHandler.logout();
        apiClient = null;
        spreadsheetId = null;
    }

    public String createSpreadsheet(String title) throws Exception {
        requireAuthenticated();
        spreadsheetId = apiClient.createSpreadsheet(title);
        return spreadsheetId;
    }

    public void selectSpreadsheet(String spreadsheetId) throws Exception {
        requireAuthenticated();
        if (spreadsheetId == null || spreadsheetId.isBlank()) {
            throw new IllegalArgumentException("Spreadsheet selection is required.");
        }
        apiClient.ensureWorksheetsExist(spreadsheetId);
        this.spreadsheetId = spreadsheetId;
    }

    public void syncProducts(List<Object[]> products) throws Exception {
        runSync("PRODUCTS", () -> apiClient.appendRows(
                spreadsheetId, "Products", SheetsSyncMapper.mapProducts(products)));
    }

    public void syncCustomers(List<Object[]> customers) throws Exception {
        runSync("CUSTOMERS", () -> apiClient.appendRows(
                spreadsheetId, "Customers", SheetsSyncMapper.mapCustomers(customers)));
    }

    public void syncPurchases(List<Object[]> purchases) throws Exception {
        runSync("PURCHASES", () -> apiClient.appendRows(
                spreadsheetId, "Purchases", SheetsSyncMapper.mapPurchases(purchases)));
    }

    public void syncSales(List<Object[]> sales) throws Exception {
        runSync("SALES", () -> apiClient.appendRows(
                spreadsheetId, "Sales", SheetsSyncMapper.mapSales(sales)));
    }

    public void syncInventory(List<Object[]> inventory) throws Exception {
        runSync("INVENTORY", () -> apiClient.appendRows(
                spreadsheetId, "Inventory", SheetsSyncMapper.mapInventory(inventory)));
    }

    public void syncAll(List<Object[]> products, List<Object[]> customers, List<Object[]> purchases,
                        List<Object[]> sales, List<Object[]> inventory) throws Exception {
        runSync("ALL", () -> {
            apiClient.appendRows(spreadsheetId, "Products", SheetsSyncMapper.mapProducts(products));
            apiClient.appendRows(spreadsheetId, "Customers", SheetsSyncMapper.mapCustomers(customers));
            apiClient.appendRows(spreadsheetId, "Purchases", SheetsSyncMapper.mapPurchases(purchases));
            apiClient.appendRows(spreadsheetId, "Sales", SheetsSyncMapper.mapSales(sales));
            apiClient.appendRows(spreadsheetId, "Inventory", SheetsSyncMapper.mapInventory(inventory));
        });
    }

    private interface SyncAction {
        void run() throws Exception;
    }

    /**
     * Runs a sync action safely: never lets a failure touch local data,
     * always logs the result, and prevents overlapping syncs.
     */
    private void runSync(String syncType, SyncAction action) throws Exception {
        requireAuthenticated();
        if (spreadsheetId == null) {
            throw new IllegalStateException("Please select or create a spreadsheet first.");
        }
        if (!syncInProgress.compareAndSet(false, true)) {
            throw new IllegalStateException("A synchronization is already in progress.");
        }
        if (!isInternetAvailable()) {
            syncInProgress.set(false);
            syncService.recordLog(new SyncLog(syncType, "FAILED", "Internet unavailable."));
            throw new IllegalStateException("Internet connection unavailable. Sync aborted; local data is unaffected.");
        }

        try {
            action.run();
            syncService.recordLog(new SyncLog(syncType, "SUCCESS", syncType + " synchronized successfully."));
        } catch (Exception e) {
            // Failure never affects local database - we only logged read data, nothing was written locally
            syncService.recordLog(new SyncLog(syncType, "FAILED", e.getMessage()));
            throw e;
        } finally {
            syncInProgress.set(false);
        }
    }

    private void requireAuthenticated() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Google account authentication is required before synchronization.");
        }
    }

    private boolean isInternetAvailable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("8.8.8.8", 53), 2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}