package com.supermarketpos.report;

public class MonthlySalesReport {
    private int month;
    private int year;
    private double totalRevenue;
    private int totalBills;
    private double averageDailySales;

    public MonthlySalesReport() {}

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public int getTotalBills() { return totalBills; }
    public void setTotalBills(int totalBills) { this.totalBills = totalBills; }

    public double getAverageDailySales() { return averageDailySales; }
    public void setAverageDailySales(double averageDailySales) { this.averageDailySales = averageDailySales; }
}