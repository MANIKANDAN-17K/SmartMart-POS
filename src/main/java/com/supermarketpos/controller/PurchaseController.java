package com.supermarketpos.controller;

import com.supermarketpos.dao.PurchaseDao;
import com.supermarketpos.report.PurchaseReport;
import com.supermarketpos.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;

public class PurchaseController {

    @FXML
    private TableView<PurchaseReport> purchaseTable;
    @FXML
    private TableColumn<PurchaseReport, Integer> colPurchaseId;
    @FXML
    private TableColumn<PurchaseReport, String> colPurchaseDate;
    @FXML
    private TableColumn<PurchaseReport, String> colSupplier;
    @FXML
    private TableColumn<PurchaseReport, String> colProduct;
    @FXML
    private TableColumn<PurchaseReport, Integer> colQty;
    @FXML
    private TableColumn<PurchaseReport, Double> colAmount;

    @FXML
    private DatePicker dpStartDate;
    @FXML
    private DatePicker dpEndDate;
    @FXML
    private ComboBox<String> cmbSupplier;

    private final PurchaseDao purchaseDao = new PurchaseDao();
    private final ObservableList<PurchaseReport> purchasesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colPurchaseId.setCellValueFactory(new PropertyValueFactory<>("purchaseId"));
        colPurchaseDate
                .setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPurchaseDate().toString()));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("purchaseAmount"));

        dpStartDate.setValue(LocalDate.now().minusMonths(1));
        dpEndDate.setValue(LocalDate.now());

        loadSuppliers();
        onSearch();
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
    }

    @FXML
    private void onBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) dpStartDate.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Dashboard");
        } catch (java.io.IOException e) {
            AlertUtil.showError("Could not load Dashboard.");
        }
    }
}
