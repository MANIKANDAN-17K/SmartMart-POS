package com.supermarketpos.model;

public class StoreSettings {
    private int id = 1;
    private String storeName = "SmartMart POS";
    private String currencySymbol = "$";
    private double taxRate = 0.0;
    private String theme = "light";
    private String appVersion = "1.0.0";
    private String address = "";
    private String gstNumber = "";
    private String phone = "";
    private String email = "";
    private String logoPath = "";
    private String receiptHeader = "";
    private String receiptFooter = "";
    private boolean showLogoOnReceipt = false;
    private boolean showGstOnReceipt = false;
    private boolean showCashierOnReceipt = false;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getReceiptHeader() {
        return receiptHeader;
    }

    public void setReceiptHeader(String receiptHeader) {
        this.receiptHeader = receiptHeader;
    }

    public String getReceiptFooter() {
        return receiptFooter;
    }

    public void setReceiptFooter(String receiptFooter) {
        this.receiptFooter = receiptFooter;
    }

    public boolean isShowLogoOnReceipt() {
        return showLogoOnReceipt;
    }

    public void setShowLogoOnReceipt(boolean showLogoOnReceipt) {
        this.showLogoOnReceipt = showLogoOnReceipt;
    }

    public boolean isShowGstOnReceipt() {
        return showGstOnReceipt;
    }

    public void setShowGstOnReceipt(boolean showGstOnReceipt) {
        this.showGstOnReceipt = showGstOnReceipt;
    }

    public boolean isShowCashierOnReceipt() {
        return showCashierOnReceipt;
    }

    public void setShowCashierOnReceipt(boolean showCashierOnReceipt) {
        this.showCashierOnReceipt = showCashierOnReceipt;
    }
}