package com.supermarketpos.controller;

import com.supermarketpos.report.*;
import com.supermarketpos.service.ReportService;
import com.supermarketpos.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ReportController.class.getName());

    // ── Report type selector ──────────────────────────────────────
    @FXML private ComboBox<String> cmbReportType;

    // ── Filter panel ─────────────────────────────────────────────
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private DatePicker dpSingleDate;
    @FXML private ComboBox<String> cmbMonth;
    @FXML private ComboBox<Integer> cmbYear;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbSupplier;
    @FXML private ComboBox<String> cmbStockStatus;
    @FXML private TextField txtProductSearch;
    @FXML private TextField txtSearch;

    // ── Summary cards ─────────────────────────────────────────────
    @FXML private Label lblCard1Title;
    @FXML private Label lblCard1Value;
    @FXML private Label lblCard2Title;
    @FXML private Label lblCard2Value;
    @FXML private Label lblCard3Title;
    @FXML private Label lblCard3Value;
    @FXML private Label lblCard4Title;
    @FXML private Label lblCard4Value;
    @FXML private Label lblCard5Title;
    @FXML private Label lblCard5Value;

    // ── Report table ──────────────────────────────────────────────
    @FXML private TableView<ObservableList<String>> tblReport;
    @FXML private VBox reportContainer;

    // ── Toolbar buttons ───────────────────────────────────────────
    @FXML private Button btnGenerate;
    @FXML private Button btnExportExcel;
    @FXML private Button btnPrint;
    @FXML private Button btnRefresh;
    @FXML private Button btnClearFilters;

    private final ReportService reportService = new ReportService();
    private String currentReportType = "";
    private Object currentReportData = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupReportTypeCombo();
        setupYearCombo();
        setupMonthCombo();
        loadCategoriesAndSuppliers();
        setupStockStatusCombo();
        cmbReportType.getSelectionModel().selectFirst();
        onReportTypeChanged();
    }

    // ── Setup ─────────────────────────────────────────────────────

    private void setupReportTypeCombo() {
        cmbReportType.setItems(FXCollections.observableArrayList(
                "Daily Sales Report",
                "Monthly Sales Report",
                "Product Sales Report",
                "Purchase Report",
                "Profit Report",
                "Stock Report"
        ));
        cmbReportType.setOnAction(e -> onReportTypeChanged());
    }

    private void setupYearCombo() {
        ObservableList<Integer> years = FXCollections.observableArrayList();
        int thisYear = LocalDate.now().getYear();
        for (int y = thisYear; y >= thisYear - 5; y--) years.add(y);
        cmbYear.setItems(years);
        cmbYear.getSelectionModel().selectFirst();
    }

    private void setupMonthCombo() {
        ObservableList<String> months = FXCollections.observableArrayList();
        for (Month m : Month.values()) months.add(m.name());
        cmbMonth.setItems(months);
        cmbMonth.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);
    }

    private void loadCategoriesAndSuppliers() {
        ObservableList<String> cats = FXCollections.observableArrayList("All");
        cats.addAll(reportService.getAllCategories());
        cmbCategory.setItems(cats);
        cmbCategory.getSelectionModel().selectFirst();

        ObservableList<String> sups = FXCollections.observableArrayList("All");
        sups.addAll(reportService.getAllSupplierNames());
        cmbSupplier.setItems(sups);
        cmbSupplier.getSelectionModel().selectFirst();
    }

    private void setupStockStatusCombo() {
        cmbStockStatus.setItems(FXCollections.observableArrayList("All", "OK", "LOW", "OUT"));
        cmbStockStatus.getSelectionModel().selectFirst();
    }

    // ── Report type visibility ────────────────────────────────────

    private void onReportTypeChanged() {
        String type = cmbReportType.getValue();
        if (type == null) return;

        // Hide all filters first
        setVisible(dpSingleDate, false);
        setVisible(dpStartDate, false);
        setVisible(dpEndDate, false);
        setVisible(cmbMonth, false);
        setVisible(cmbYear, false);
        setVisible(cmbCategory, false);
        setVisible(cmbSupplier, false);
        setVisible(cmbStockStatus, false);
        setVisible(txtProductSearch, false);

        switch (type) {
            case "Daily Sales Report" -> setVisible(dpSingleDate, true);
            case "Monthly Sales Report" -> { setVisible(cmbMonth, true); setVisible(cmbYear, true); }
            case "Product Sales Report" -> {
                setVisible(dpStartDate, true); setVisible(dpEndDate, true);
                setVisible(cmbCategory, true); setVisible(txtProductSearch, true);
            }
            case "Purchase Report" -> {
                setVisible(dpStartDate, true); setVisible(dpEndDate, true); setVisible(cmbSupplier, true);
            }
            case "Profit Report" -> { setVisible(dpStartDate, true); setVisible(dpEndDate, true); }
            case "Stock Report" -> { setVisible(cmbCategory, true); setVisible(cmbStockStatus, true); }
        }

        clearTable();
        clearSummaryCards();
    }

    private void setVisible(Control control, boolean visible) {
        if (control != null) {
            control.setVisible(visible);
            control.setManaged(visible);
        }
    }

    // ── Generate ──────────────────────────────────────────────────

    @FXML
    private void onGenerate() {
        String type = cmbReportType.getValue();
        if (type == null) {
            AlertUtil.showWarning("No Report Selected", "Please select a report type.");
            return;
        }

        try {
            currentReportType = type;
            switch (type) {
                case "Daily Sales Report" -> generateDailySales();
                case "Monthly Sales Report" -> generateMonthlySales();
                case "Product Sales Report" -> generateProductSales();
                case "Purchase Report" -> generatePurchaseReport();
                case "Profit Report" -> generateProfitReport();
                case "Stock Report" -> generateStockReport();
            }
            LOGGER.info("Filters applied: " + type);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected exception during report generation", e);
            AlertUtil.showError("Error", "Failed to generate report: " + e.getMessage());
        }
    }

    private void generateDailySales() {
        LocalDate date = dpSingleDate.getValue() != null ? dpSingleDate.getValue() : LocalDate.now();
        DailySalesReport r = reportService.getDailySalesReport(date);
        currentReportData = r;
        showSummaryCard(0, "Total Bills", String.valueOf(r.getTotalBills()));
        showSummaryCard(1, "Total Sales", CurrencyUtil.format(r.getTotalSales()));
        showSummaryCard(2, "Total GST", CurrencyUtil.format(r.getTotalGst()));
        showSummaryCard(3, "Total Discount", CurrencyUtil.format(r.getTotalDiscount()));
        showSummaryCard(4, "Avg Bill Value", CurrencyUtil.format(r.getAverageBillValue()));
        buildTable(new String[]{"Date", "Total Bills", "Total Sales", "Total GST", "Total Discount", "Avg Bill Value"},
                List.of(FXCollections.observableArrayList(
                        DateUtil.format(r.getDate()),
                        String.valueOf(r.getTotalBills()),
                        CurrencyUtil.format(r.getTotalSales()),
                        CurrencyUtil.format(r.getTotalGst()),
                        CurrencyUtil.format(r.getTotalDiscount()),
                        CurrencyUtil.format(r.getAverageBillValue())
                )));
    }

    private void generateMonthlySales() {
        int month = cmbMonth.getSelectionModel().getSelectedIndex() + 1;
        int year = cmbYear.getValue() != null ? cmbYear.getValue() : LocalDate.now().getYear();
        MonthlySalesReport r = reportService.getMonthlySalesReport(month, year);
        currentReportData = r;
        showSummaryCard(0, "Total Revenue", CurrencyUtil.format(r.getTotalRevenue()));
        showSummaryCard(1, "Total Bills", String.valueOf(r.getTotalBills()));
        showSummaryCard(2, "Avg Daily Sales", CurrencyUtil.format(r.getAverageDailySales()));
        hideSummaryCards(3, 4);
        buildTable(new String[]{"Month", "Year", "Total Revenue", "Total Bills", "Avg Daily Sales"},
                List.of(FXCollections.observableArrayList(
                        cmbMonth.getValue(), String.valueOf(year),
                        CurrencyUtil.format(r.getTotalRevenue()),
                        String.valueOf(r.getTotalBills()),
                        CurrencyUtil.format(r.getAverageDailySales())
                )));
    }

    private void generateProductSales() {
        LocalDate start = dpStartDate.getValue() != null ? dpStartDate.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate end = dpEndDate.getValue() != null ? dpEndDate.getValue() : LocalDate.now();
        if (!ValidationUtil.isValidDateRange(start, end)) {
            AlertUtil.showWarning("Invalid Date Range", "Start date cannot be after end date."); return;
        }
        String cat = cmbCategory.getValue();
        String prod = txtProductSearch.getText();
        List<ProductSalesReport> list = reportService.getProductSalesReport(start, end, cat, prod);
        currentReportData = list;
        double totalRev = list.stream().mapToDouble(ProductSalesReport::getRevenue).sum();
        int totalQty = list.stream().mapToInt(ProductSalesReport::getQuantitySold).sum();
        showSummaryCard(0, "Total Products", String.valueOf(list.size()));
        showSummaryCard(1, "Total Qty Sold", String.valueOf(totalQty));
        showSummaryCard(2, "Total Revenue", CurrencyUtil.format(totalRev));
        hideSummaryCards(3, 4);
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
        for (ProductSalesReport r : list) {
            rows.add(FXCollections.observableArrayList(
                    String.valueOf(r.getProductId()), r.getProductName(), r.getCategory(),
                    String.valueOf(r.getQuantitySold()), CurrencyUtil.format(r.getRevenue())));
        }
        buildTable(new String[]{"Product ID", "Product Name", "Category", "Qty Sold", "Revenue"}, rows);
    }

    private void generatePurchaseReport() {
        LocalDate start = dpStartDate.getValue() != null ? dpStartDate.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate end = dpEndDate.getValue() != null ? dpEndDate.getValue() : LocalDate.now();
        if (!ValidationUtil.isValidDateRange(start, end)) {
            AlertUtil.showWarning("Invalid Date Range", "Start date cannot be after end date."); return;
        }
        String supplier = cmbSupplier.getValue();
        List<PurchaseReport> list = reportService.getPurchaseReport(start, end, supplier);
        currentReportData = list;
        double total = list.stream().mapToDouble(PurchaseReport::getPurchaseAmount).sum();
        showSummaryCard(0, "Total Purchases", String.valueOf(list.size()));
        showSummaryCard(1, "Total Amount", CurrencyUtil.format(total));
        hideSummaryCards(2, 3, 4);
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
        for (PurchaseReport r : list) {
            rows.add(FXCollections.observableArrayList(
                    String.valueOf(r.getPurchaseId()), DateUtil.format(r.getPurchaseDate()),
                    r.getSupplierName(), r.getProductName(),
                    String.valueOf(r.getQuantity()), CurrencyUtil.format(r.getPurchaseAmount())));
        }
        buildTable(new String[]{"Purchase ID", "Date", "Supplier", "Product", "Qty", "Amount"}, rows);
    }

    private void generateProfitReport() {
        LocalDate start = dpStartDate.getValue() != null ? dpStartDate.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate end = dpEndDate.getValue() != null ? dpEndDate.getValue() : LocalDate.now();
        if (!ValidationUtil.isValidDateRange(start, end)) {
            AlertUtil.showWarning("Invalid Date Range", "Start date cannot be after end date."); return;
        }
        List<ProfitReport> list = reportService.getProfitReport(start, end);
        currentReportData = list;
        double totalRev = list.stream().mapToDouble(ProfitReport::getRevenue).sum();
        double totalCost = list.stream().mapToDouble(ProfitReport::getCost).sum();
        double totalProfit = totalRev - totalCost;
        double pct = totalRev > 0 ? (totalProfit / totalRev) * 100 : 0;
        showSummaryCard(0, "Total Revenue", CurrencyUtil.format(totalRev));
        showSummaryCard(1, "Total Cost", CurrencyUtil.format(totalCost));
        showSummaryCard(2, "Gross Profit", CurrencyUtil.format(totalProfit));
        showSummaryCard(3, "Profit %", String.format("%.2f%%", pct));
        hideSummaryCards(4);
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
        for (ProfitReport r : list) {
            rows.add(FXCollections.observableArrayList(
                    DateUtil.format(r.getDate()), CurrencyUtil.format(r.getRevenue()),
                    CurrencyUtil.format(r.getCost()), CurrencyUtil.format(r.getGrossProfit()),
                    String.format("%.2f%%", r.getProfitPercentage())));
        }
        buildTable(new String[]{"Date", "Revenue", "Cost", "Gross Profit", "Profit %"}, rows);
    }

    private void generateStockReport() {
        String cat = cmbCategory.getValue();
        String status = cmbStockStatus.getValue();
        List<StockReport> list = reportService.getStockReport(cat, status);
        currentReportData = list;
        long outCount = list.stream().filter(r -> "OUT".equals(r.getStockStatus())).count();
        long lowCount = list.stream().filter(r -> "LOW".equals(r.getStockStatus())).count();
        long okCount  = list.stream().filter(r -> "OK".equals(r.getStockStatus())).count();
        showSummaryCard(0, "Total Products", String.valueOf(list.size()));
        showSummaryCard(1, "OK Stock", String.valueOf(okCount));
        showSummaryCard(2, "Low Stock", String.valueOf(lowCount));
        showSummaryCard(3, "Out of Stock", String.valueOf(outCount));
        hideSummaryCards(4);
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
        for (StockReport r : list) {
            rows.add(FXCollections.observableArrayList(
                    String.valueOf(r.getProductId()), r.getProductName(), r.getCategory(),
                    String.valueOf(r.getCurrentStock()), String.valueOf(r.getReorderLevel()), r.getStockStatus()));
        }
        buildTable(new String[]{"Product ID", "Product Name", "Category", "Current Stock", "Reorder Level", "Status"}, rows);
    }

    // ── Table builder ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void buildTable(String[] headers, List<ObservableList<String>> rows) {
        tblReport.getColumns().clear();
        for (int i = 0; i < headers.length; i++) {
            final int col = i;
            TableColumn<ObservableList<String>, String> tc = new TableColumn<>(headers[i]);
            tc.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(col)));
            tc.setPrefWidth(140);
            tblReport.getColumns().add(tc);
        }

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList(rows);
        FilteredList<ObservableList<String>> filtered = new FilteredList<>(data, p -> true);

        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                filtered.setPredicate(row -> {
                    if (newVal == null || newVal.isBlank()) return true;
                    String lower = newVal.toLowerCase();
                    return row.stream().anyMatch(cell -> cell.toLowerCase().contains(lower));
                });
            });
        }

        tblReport.setItems(filtered);
        if (rows.isEmpty()) {
            tblReport.setPlaceholder(new Label("No data found for the selected filters."));
        }
    }

    private void clearTable() {
        tblReport.getColumns().clear();
        tblReport.setItems(FXCollections.emptyObservableList());
    }

    // ── Summary cards helpers ─────────────────────────────────────

    private final Label[] cardTitles  = new Label[5];
    private final Label[] cardValues  = new Label[5];

    private void linkCardLabels() {
        cardTitles[0] = lblCard1Title; cardValues[0] = lblCard1Value;
        cardTitles[1] = lblCard2Title; cardValues[1] = lblCard2Value;
        cardTitles[2] = lblCard3Title; cardValues[2] = lblCard3Value;
        cardTitles[3] = lblCard4Title; cardValues[3] = lblCard4Value;
        cardTitles[4] = lblCard5Title; cardValues[4] = lblCard5Value;
    }

    private void showSummaryCard(int idx, String title, String value) {
        if (cardTitles[0] == null) linkCardLabels();
        cardTitles[idx].setText(title);
        cardValues[idx].setText(value);
        if (cardTitles[idx].getParent() != null) {
            cardTitles[idx].getParent().setVisible(true);
            cardTitles[idx].getParent().setManaged(true);
        }
    }

    private void hideSummaryCards(int... indices) {
        if (cardTitles[0] == null) linkCardLabels();
        for (int idx : indices) {
            if (cardTitles[idx].getParent() != null) {
                cardTitles[idx].getParent().setVisible(false);
                cardTitles[idx].getParent().setManaged(false);
            }
        }
    }

    private void clearSummaryCards() {
        if (cardTitles[0] == null) linkCardLabels();
        for (int i = 0; i < 5; i++) {
            if (cardTitles[i] != null) cardTitles[i].setText("");
            if (cardValues[i] != null) cardValues[i].setText("");
        }
    }

    // ── Export Excel ──────────────────────────────────────────────

    @FXML
    private void onExportExcel() {
        if (currentReportData == null) {
            AlertUtil.showWarning("No Data", "Generate a report before exporting."); return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Excel File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName(currentReportType.replace(" ", "_") + ".xlsx");
        File file = fc.showSaveDialog(btnExportExcel.getScene().getWindow());
        if (file == null) return;
        try {
            reportService.exportToExcel(currentReportType, currentReportData, file);
            AlertUtil.showInfo("Export Successful", "Report exported to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Excel export failure", e);
            AlertUtil.showError("Export Failed", "Could not export to Excel: " + e.getMessage());
        }
    }

    // ── Print ─────────────────────────────────────────────────────

    @FXML
    private void onPrint() {
        if (tblReport.getItems().isEmpty()) {
            AlertUtil.showWarning("No Data", "Generate a report before printing."); return;
        }
        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job == null) { AlertUtil.showError("Printer Error", "No printer available."); return; }
            if (job.showPrintDialog(btnPrint.getScene().getWindow())) {
                boolean printed = job.printPage(tblReport);
                if (printed) {
                    job.endJob();
                    LOGGER.info("Report printed: " + currentReportType);
                } else {
                    AlertUtil.showError("Print Failed", "Printing could not be completed.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Printer unavailable", e);
            AlertUtil.showError("Print Error", "Printer error: " + e.getMessage());
        }
    }

    // ── Refresh / Clear ───────────────────────────────────────────

    @FXML
    private void onRefresh() {
        onGenerate();
        LOGGER.info("Report refreshed: " + currentReportType);
    }

    @FXML
    private void onClearFilters() {
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        dpSingleDate.setValue(null);
        cmbCategory.getSelectionModel().selectFirst();
        cmbSupplier.getSelectionModel().selectFirst();
        cmbStockStatus.getSelectionModel().selectFirst();
        txtProductSearch.clear();
        if (txtSearch != null) txtSearch.clear();
        clearTable();
        clearSummaryCards();
        currentReportData = null;
    }
}