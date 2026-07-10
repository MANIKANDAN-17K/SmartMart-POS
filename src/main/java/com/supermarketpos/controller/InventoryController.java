package com.supermarketpos.controller;

import com.supermarketpos.dao.ProductDao;
import com.supermarketpos.model.StockAdjustment;
import com.supermarketpos.model.StockMovement;
import com.supermarketpos.service.CategoryService;
import com.supermarketpos.service.InventoryService;
import com.supermarketpos.model.Category;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.util.DateUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class InventoryController {

    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label totalStockItemsLabel;
    @FXML
    private Label lowStockCountLabel;
    @FXML
    private Label outOfStockCountLabel;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Category> categoryFilter;
    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private TableView<ProductDao.InventoryRow> inventoryTable;
    @FXML
    private TableColumn<ProductDao.InventoryRow, String> barcodeColumn;
    @FXML
    private TableColumn<ProductDao.InventoryRow, String> skuColumn;
    @FXML
    private TableColumn<ProductDao.InventoryRow, String> nameColumn;
    @FXML
    private TableColumn<ProductDao.InventoryRow, String> categoryColumn;
    @FXML
    private TableColumn<ProductDao.InventoryRow, Number> currentStockColumn;
    @FXML
    private TableColumn<ProductDao.InventoryRow, Number> minStockColumn;
    @FXML
    private TableColumn<ProductDao.InventoryRow, String> statusColumn;
    @FXML
    private TableColumn<ProductDao.InventoryRow, String> lastUpdatedColumn;

    @FXML
    private ComboBox<String> adjustmentTypeDropdown;
    @FXML
    private TextField adjustmentQuantityField;
    @FXML
    private TextField reasonField;
    @FXML
    private TextArea remarksField;
    @FXML
    private Label selectedProductLabel;
    @FXML
    private Label currentStockLabel;

    @FXML
    private TableView<StockMovement> movementTable;
    @FXML
    private TableColumn<StockMovement, String> movementDateColumn;
    @FXML
    private TableColumn<StockMovement, String> movementProductColumn;
    @FXML
    private TableColumn<StockMovement, String> movementTypeColumn;
    @FXML
    private TableColumn<StockMovement, Number> movementQuantityColumn;
    @FXML
    private TableColumn<StockMovement, Number> movementPreviousColumn;
    @FXML
    private TableColumn<StockMovement, Number> movementCurrentColumn;
    @FXML
    private TableColumn<StockMovement, String> movementReferenceColumn;
    @FXML
    private TableColumn<StockMovement, String> movementPerformedByColumn;

    @FXML
    private Button refreshButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button adjustStockButton;
    @FXML
    private Button viewHistoryButton;
    @FXML
    private Button exportButton;
    @FXML
    private Button clearButton;

    private final InventoryService inventoryService = new InventoryService();
    private final CategoryService categoryService = new CategoryService();

    private final ObservableList<ProductDao.InventoryRow> inventoryList = FXCollections.observableArrayList();
    private final ObservableList<StockMovement> movementList = FXCollections.observableArrayList();

    private ProductDao.InventoryRow selectedRow;

    private static final String STATUS_ALL = "All";
    private static final String STATUS_IN_STOCK = "IN_STOCK";
    private static final String STATUS_LOW_STOCK = "LOW_STOCK";
    private static final String STATUS_OUT_OF_STOCK = "OUT_OF_STOCK";

    @FXML
    public void initialize() {
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        currentStockColumn.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        minStockColumn.setCellValueFactory(new PropertyValueFactory<>("minStockQuantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("stockStatus"));
        lastUpdatedColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getUpdatedAt() != null
                        ? cell.getValue().getUpdatedAt().toLocalDate().toString()
                        : ""));

        inventoryTable.setItems(inventoryList);
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedRow = newVal;
            populateAdjustmentPanel(newVal);
        });

        movementDateColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getMovementDate() != null
                        ? cell.getValue().getMovementDate().toString()
                        : ""));
        movementProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        movementTypeColumn.setCellValueFactory(new PropertyValueFactory<>("movementType"));
        movementQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        movementPreviousColumn.setCellValueFactory(new PropertyValueFactory<>("previousStock"));
        movementCurrentColumn.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        movementReferenceColumn.setCellValueFactory(new PropertyValueFactory<>("referenceNumber"));
        movementPerformedByColumn.setCellValueFactory(new PropertyValueFactory<>("performedBy"));
        movementTable.setItems(movementList);

        adjustmentTypeDropdown.setItems(FXCollections.observableArrayList(
                StockAdjustment.TYPE_INCREASE, StockAdjustment.TYPE_DECREASE));

        statusFilter.setItems(FXCollections.observableArrayList(
                STATUS_ALL, STATUS_IN_STOCK, STATUS_LOW_STOCK, STATUS_OUT_OF_STOCK));
        statusFilter.setValue(STATUS_ALL);

        loadCategoryFilter();
        loadInventory();
        loadSummary();
    }

    @FXML
    private void handleSearch() {
        try {
            inventoryList.setAll(inventoryService.searchInventory(searchField.getText()));
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to search inventory: " + e.getMessage());
        }
    }

    @FXML
    private void handleCategoryFilterChange() {
        Category selected = categoryFilter.getValue();
        try {
            if (selected == null) {
                loadInventory();
            } else {
                inventoryList.setAll(inventoryService.getInventoryByCategory(selected.getId()));
            }
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to filter inventory: " + e.getMessage());
        }
    }

    @FXML
    private void handleStatusFilterChange() {
        String status = statusFilter.getValue();
        try {
            if (status == null || STATUS_ALL.equals(status)) {
                loadInventory();
            } else {
                inventoryList.setAll(inventoryService.getInventoryByStatus(status));
            }
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to filter inventory: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdjustStock() {
        if (selectedRow == null) {
            AlertUtil.showWarning("No Selection", "Please select a product to adjust.");
            return;
        }
        String adjustmentType = adjustmentTypeDropdown.getValue();
        if (adjustmentType == null) {
            AlertUtil.showWarning("Validation Error", "Please select an adjustment type.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(adjustmentQuantityField.getText().trim());
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation Error", "Adjustment quantity must be a whole number.");
            return;
        }

        try {
            // TODO: replace with your actual UserSession accessor, e.g.
            // UserSession.getCurrentUser().getUsername()
            String performedBy = com.supermarketpos.util.UserSession.getInstance().getCurrentUsername();

            inventoryService.adjustStock(selectedRow.getProductId(), adjustmentType, quantity,
                    reasonField.getText(), remarksField.getText(), performedBy);

            AlertUtil.showInfo("Success", "Stock adjusted successfully.");
            clearAdjustmentForm();
            loadInventory();
            loadSummary();
        } catch (InventoryService.ValidationException e) {
            AlertUtil.showWarning("Validation Error", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to adjust stock: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewHistory() {
        if (selectedRow == null) {
            AlertUtil.showWarning("No Selection", "Please select a product to view history.");
            return;
        }
        try {
            movementList.setAll(inventoryService.getStockHistory(selectedRow.getProductId()));
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to load movement history: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        // Placeholder — CSV/Excel export is out of scope for this sprint's allowed
        // files.
        AlertUtil.showInfo("Export", "Export functionality will be added in a future sprint.");
    }

    @FXML
    private void handleClear() {
        clearAdjustmentForm();
        movementList.clear();
        inventoryTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleRefresh() {
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
            AlertUtil.showError("Database Error", "Unable to load categories: " + e.getMessage());
        }
    }

    private void loadInventory() {
        try {
            inventoryList.setAll(inventoryService.getCurrentInventory());
        } catch (SQLException e) {
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
        selectedProductLabel.setText("");
        currentStockLabel.setText("");
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) totalProductsLabel.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Dashboard");
        } catch (java.io.IOException e) {
            AlertUtil.showError("Navigation Error", "Could not load Dashboard.");
        }
    }
}