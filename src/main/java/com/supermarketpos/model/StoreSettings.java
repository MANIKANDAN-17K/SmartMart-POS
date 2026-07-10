package com.supermarketpos.model;

public class StoreSettings {
    private int id;
    private String storeName;
    private String currencySymbol;
    private double taxRate;
    private String theme;
    private String appVersion;

    public StoreSettings() {
    }

    public StoreSettings(int id, String storeName, String currencySymbol, double taxRate,
                         String theme, String appVersion) {
        this.id = id;
        this.storeName = storeName;
        this.currencySymbol = currencySymbol;
        this.taxRate = taxRate;
        this.theme = theme;
        this.appVersion = appVersion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
}