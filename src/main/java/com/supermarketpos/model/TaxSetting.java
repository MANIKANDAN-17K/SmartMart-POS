package com.supermarketpos.model;

public class TaxSetting {
    private int id;
    private double gstPercentage;
    private boolean gstEnabled;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getGstPercentage() { return gstPercentage; }
    public void setGstPercentage(double gstPercentage) { this.gstPercentage = gstPercentage; }
    public boolean isGstEnabled() { return gstEnabled; }
    public void setGstEnabled(boolean gstEnabled) { this.gstEnabled = gstEnabled; }
}