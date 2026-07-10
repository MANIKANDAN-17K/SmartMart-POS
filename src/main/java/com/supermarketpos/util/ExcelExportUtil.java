package com.supermarketpos.util;

import com.supermarketpos.report.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExcelExportUtil {

    private ExcelExportUtil() {}

    public static void export(String reportType, Object data, File file) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(reportType);

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            switch (reportType) {
                case "Daily Sales Report" -> writeDailySales(sheet, headerStyle, (DailySalesReport) data);
                case "Monthly Sales Report" -> writeMonthlySales(sheet, headerStyle, (MonthlySalesReport) data);
                case "Product Sales Report" -> writeProductSales(sheet, headerStyle, castList(data, ProductSalesReport.class));
                case "Purchase Report" -> writePurchase(sheet, headerStyle, castList(data, PurchaseReport.class));
                case "Profit Report" -> writeProfit(sheet, headerStyle, castList(data, ProfitReport.class));
                case "Stock Report" -> writeStock(sheet, headerStyle, castList(data, StockReport.class));
                default -> throw new IllegalArgumentException("Unknown report type: " + reportType);
            }

            autoSizeColumns(sheet);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }
    }

    private static void writeDailySales(Sheet sheet, CellStyle hs, DailySalesReport r) {
        String[] headers = {"Date", "Total Bills", "Total Sales", "Total GST", "Total Discount", "Avg Bill Value"};
        writeHeaders(sheet, hs, headers);
        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue(r.getDate().toString());
        row.createCell(1).setCellValue(r.getTotalBills());
        row.createCell(2).setCellValue(CurrencyUtil.format(r.getTotalSales()));
        row.createCell(3).setCellValue(CurrencyUtil.format(r.getTotalGst()));
        row.createCell(4).setCellValue(CurrencyUtil.format(r.getTotalDiscount()));
        row.createCell(5).setCellValue(CurrencyUtil.format(r.getAverageBillValue()));
    }

    private static void writeMonthlySales(Sheet sheet, CellStyle hs, MonthlySalesReport r) {
        String[] headers = {"Month", "Year", "Total Revenue", "Total Bills", "Avg Daily Sales"};
        writeHeaders(sheet, hs, headers);
        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue(r.getMonth());
        row.createCell(1).setCellValue(r.getYear());
        row.createCell(2).setCellValue(CurrencyUtil.format(r.getTotalRevenue()));
        row.createCell(3).setCellValue(r.getTotalBills());
        row.createCell(4).setCellValue(CurrencyUtil.format(r.getAverageDailySales()));
    }

    @SuppressWarnings("unchecked")
    private static void writeProductSales(Sheet sheet, CellStyle hs, List<ProductSalesReport> list) {
        String[] headers = {"Product ID", "Product Name", "Category", "Quantity Sold", "Revenue"};
        writeHeaders(sheet, hs, headers);
        int rowIdx = 1;
        for (ProductSalesReport r : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.getProductId());
            row.createCell(1).setCellValue(r.getProductName());
            row.createCell(2).setCellValue(r.getCategory());
            row.createCell(3).setCellValue(r.getQuantitySold());
            row.createCell(4).setCellValue(CurrencyUtil.format(r.getRevenue()));
        }
    }

    @SuppressWarnings("unchecked")
    private static void writePurchase(Sheet sheet, CellStyle hs, List<PurchaseReport> list) {
        String[] headers = {"Purchase ID", "Date", "Supplier", "Product", "Quantity", "Amount"};
        writeHeaders(sheet, hs, headers);
        int rowIdx = 1;
        for (PurchaseReport r : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.getPurchaseId());
            row.createCell(1).setCellValue(r.getPurchaseDate().toString());
            row.createCell(2).setCellValue(r.getSupplierName());
            row.createCell(3).setCellValue(r.getProductName());
            row.createCell(4).setCellValue(r.getQuantity());
            row.createCell(5).setCellValue(CurrencyUtil.format(r.getPurchaseAmount()));
        }
    }

    @SuppressWarnings("unchecked")
    private static void writeProfit(Sheet sheet, CellStyle hs, List<ProfitReport> list) {
        String[] headers = {"Date", "Revenue", "Cost", "Gross Profit", "Profit %"};
        writeHeaders(sheet, hs, headers);
        int rowIdx = 1;
        for (ProfitReport r : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.getDate().toString());
            row.createCell(1).setCellValue(CurrencyUtil.format(r.getRevenue()));
            row.createCell(2).setCellValue(CurrencyUtil.format(r.getCost()));
            row.createCell(3).setCellValue(CurrencyUtil.format(r.getGrossProfit()));
            row.createCell(4).setCellValue(String.format("%.2f%%", r.getProfitPercentage()));
        }
    }

    @SuppressWarnings("unchecked")
    private static void writeStock(Sheet sheet, CellStyle hs, List<StockReport> list) {
        String[] headers = {"Product ID", "Product Name", "Category", "Current Stock", "Reorder Level", "Status"};
        writeHeaders(sheet, hs, headers);
        int rowIdx = 1;
        for (StockReport r : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.getProductId());
            row.createCell(1).setCellValue(r.getProductName());
            row.createCell(2).setCellValue(r.getCategory());
            row.createCell(3).setCellValue(r.getCurrentStock());
            row.createCell(4).setCellValue(r.getReorderLevel());
            row.createCell(5).setCellValue(r.getStockStatus());
        }
    }

    private static void writeHeaders(Sheet sheet, CellStyle style, String[] headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private static void autoSizeColumns(Sheet sheet) {
        Row header = sheet.getRow(0);
        if (header == null) return;
        for (int i = 0; i < header.getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> castList(Object data, Class<T> clazz) {
        return (List<T>) data;
    }
}