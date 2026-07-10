package com.supermarketpos.service;

import com.supermarketpos.dao.BillDao;
import com.supermarketpos.dao.ProductDao;
import com.supermarketpos.dao.PurchaseDao;
import com.supermarketpos.report.*;
import com.supermarketpos.util.ExcelExportUtil;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportService {

    private static final Logger LOGGER = Logger.getLogger(ReportService.class.getName());

    private final BillDao billDao = new BillDao();
    private final PurchaseDao purchaseDao = new PurchaseDao();
    private final ProductDao productDao = new ProductDao();

    public DailySalesReport getDailySalesReport(LocalDate date) {
        LOGGER.info("Report generated: Daily Sales Report for " + date);
        return billDao.getDailySalesReport(date);
    }

    public MonthlySalesReport getMonthlySalesReport(int month, int year) {
        LOGGER.info("Report generated: Monthly Sales Report for " + month + "/" + year);
        return billDao.getMonthlySalesReport(month, year);
    }

    public List<ProductSalesReport> getProductSalesReport(LocalDate startDate, LocalDate endDate,
                                                          String category, String productName) {
        LOGGER.info("Report generated: Product Sales Report from " + startDate + " to " + endDate);
        return billDao.getProductSalesReport(startDate, endDate, category, productName);
    }

    public List<PurchaseReport> getPurchaseReport(LocalDate startDate, LocalDate endDate, String supplier) {
        LOGGER.info("Report generated: Purchase Report from " + startDate + " to " + endDate);
        return purchaseDao.getPurchaseReport(startDate, endDate, supplier);
    }

    public List<ProfitReport> getProfitReport(LocalDate startDate, LocalDate endDate) {
        LOGGER.info("Report generated: Profit Report from " + startDate + " to " + endDate);
        return billDao.getProfitReport(startDate, endDate);
    }

    public List<StockReport> getStockReport(String category, String stockStatus) {
        LOGGER.info("Report generated: Stock Report");
        return productDao.getStockReport(category, stockStatus);
    }

    public List<String> getAllCategories() {
        return productDao.getAllCategories();
    }

    public List<String> getAllSupplierNames() {
        return purchaseDao.getAllSupplierNames();
    }

    public void exportToExcel(String reportType, Object data, File file) {
        try {
            ExcelExportUtil.export(reportType, data, file);
            LOGGER.info("Report exported: " + reportType + " to " + file.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Excel export failure: " + reportType, e);
            throw new RuntimeException("Excel export failed: " + e.getMessage(), e);
        }
    }
}