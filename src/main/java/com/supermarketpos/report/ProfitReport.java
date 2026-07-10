package com.supermarketpos.report;

import java.time.LocalDate;

public class ProfitReport {
    private LocalDate date;
    private double revenue;
    private double cost;
    private double grossProfit;
    private double profitPercentage;

    public ProfitReport() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public double getGrossProfit() { return grossProfit; }
    public void setGrossProfit(double grossProfit) { this.grossProfit = grossProfit; }

    public double getProfitPercentage() { return profitPercentage; }
    public void setProfitPercentage(double profitPercentage) { this.profitPercentage = profitPercentage; }
}