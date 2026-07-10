package com.supermarketpos.report;

import java.time.LocalDate;

public class DailySalesReport {
    private LocalDate date;
    private int totalBills;
    private double totalSales;
    private double totalGst;
    private double totalDiscount;
    private double averageBillValue;

    public DailySalesReport() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getTotalBills() { return totalBills; }
    public void setTotalBills(int totalBills) { this.totalBills = totalBills; }

    public double getTotalSales() { return totalSales; }
    public void setTotalSales(double totalSales) { this.totalSales = totalSales; }

    public double getTotalGst() { return totalGst; }
    public void setTotalGst(double totalGst) { this.totalGst = totalGst; }

    public double getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(double totalDiscount) { this.totalDiscount = totalDiscount; }

    public double getAverageBillValue() { return averageBillValue; }
    public void setAverageBillValue(double averageBillValue) { this.averageBillValue = averageBillValue; }
}