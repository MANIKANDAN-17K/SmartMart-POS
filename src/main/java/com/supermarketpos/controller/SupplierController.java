package com.supermarketpos.controller;

import com.supermarketpos.model.Supplier;
import com.supermarketpos.service.SupplierService;
import com.supermarketpos.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class SupplierController {

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

    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, String> codeColumn;
    @FXML private TableColumn<Supplier, String> nameColumn;
    @FXML private TableColumn<Supplier, String> contactColumn;
    @FXML private TableColumn<Supplier, String> mobileColumn;
    @FXML private TableColumn<Supplier, String> emailColumn;
    @FXML private TableColumn<Supplier, String> cityColumn;
    @FXML private TableColumn<Supplier, String> statusColumn;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button activateButton;
    @FXML private Button deactivateButton;
    @FXML private Button refreshButton;
    @FXML private Button purchaseHistoryButton;
    @FXML private Button clearButton;

    private final SupplierService supplierService = new SupplierService();
    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();
    private Supplier selectedSupplier;

    private static final String FILTER_ALL = "All";
    private static final String FILTER_ACTIVE = "Active";
    private static final String FILTER_INACTIVE = "Inactive";

    @FXML
    public void initialize() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("supplierCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        mobileColumn.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));

        supplierTable.setItems(supplierList);
        supplierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedSupplier = newVal;
            populateForm(newVal);
        });

        statusFilter.setItems(FXCollections.observableArrayList(FILTER_ALL, FILTER_ACTIVE, FILTER_INACTIVE));
        statusFilter.setValue(FILTER_ALL);

        loadSuppliers();
    }

    @FXML
    private void handleSearch() {
        try {
            supplierList.setAll(supplierService.searchSupplier(searchField.getText()));
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
            AlertUtil.showError("Database Error", "Unable to activate supplier: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeactivate() {
        if (selectedSupplier == null) {
            AlertUtil.showWarning("No Selection", "Please select a supplier to deactivate.");
            return;
        }
        boolean confirmed = AlertUtil.showConfirmation("Confirm Deactivation",
                "Inactive suppliers cannot be selected in the Purchase module. Continue?");
        if (!confirmed) return;

        try {
            supplierService.deactivateSupplier(selectedSupplier.getId());
            loadSuppliers();
        } catch (SQLException e) {
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
            AlertUtil.showWarning("No Selection", "Please select a supplier to view purchase history.");
            return;
        }
        // Placeholder only — Purchase module (future sprint) owns the real implementation.
        AlertUtil.showInfo("Purchase History",
                "Purchase history for '" + selectedSupplier.getSupplierName() +
                        "' will be available once the Purchase module is implemented.");
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void loadSuppliers() {
        try {
            supplierList.setAll(supplierService.getAllSuppliers());
        } catch (SQLException e) {
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
}