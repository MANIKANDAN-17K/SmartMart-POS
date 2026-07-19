package com.supermarketpos.service;

import com.supermarketpos.dao.SettingsDao;
import com.supermarketpos.model.StoreSettings;
import com.supermarketpos.model.TaxSetting;
import com.supermarketpos.util.ValidationUtil;
import com.supermarketpos.util.ThemeUtil;

import java.sql.SQLException;

public class SettingsService {

    private final SettingsDao settingsDao;
    private final AuditService auditService = new AuditService();

    public SettingsService() {
        this.settingsDao = new SettingsDao();
    }

    public SettingsService(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }

    /** Falls back to sane defaults if store_settings has no row yet. */
    public StoreSettings getSettings() {
        return settingsDao.findSettings().orElseGet(() -> {
            StoreSettings defaults = new StoreSettings();
            defaults.setStoreName("SmartMart POS");
            defaults.setCurrencySymbol("₹");
            defaults.setTaxRate(0.0);
            defaults.setTheme("light");
            defaults.setAppVersion("1.0.0");
            return defaults;
        });
    }

    public StoreSettings getStoreSettings() throws SQLException {
        return settingsDao.getStoreSettings();
    }

    public void saveStoreSettings(StoreSettings s, String username) throws Exception {
        if (!ValidationUtil.isNotEmpty(s.getStoreName()))
            throw new IllegalArgumentException("Store name is required.");
        if (!ValidationUtil.isValidEmail(s.getEmail()))
            throw new IllegalArgumentException("Invalid email address.");
        if (!ValidationUtil.isValidPhone(s.getPhone()))
            throw new IllegalArgumentException("Invalid phone number.");
        if (!ValidationUtil.isValidGstNumber(s.getGstNumber()))
            throw new IllegalArgumentException("Invalid GST number.");

        settingsDao.saveStoreSettings(s);
        auditService.log(username, "STORE_SETTINGS_UPDATED", "Store settings updated");
    }

    public void changeTheme(String theme, String username) throws Exception {
        if (theme == null || (!theme.equals("light") && !theme.equals("dark")))
            throw new IllegalArgumentException("Theme selection required.");
        settingsDao.saveTheme(theme);
        ThemeUtil.applyTheme(theme);
        auditService.log(username, "THEME_CHANGED", "Theme changed to " + theme);
    }

    public TaxSetting getTaxSettings() throws SQLException {
        return settingsDao.getTaxSettings();
    }

    public void saveTaxSettings(TaxSetting t, String username) throws Exception {
        if (t.getGstPercentage() < 0 || t.getGstPercentage() > 100)
            throw new IllegalArgumentException("GST percentage must be between 0 and 100.");
        settingsDao.saveTaxSettings(t);
        auditService.log(username, "TAX_SETTINGS_UPDATED", "GST set to " + t.getGstPercentage() + "%");
    }

    public StoreSettings updateSettings(StoreSettings settings) {
        return settingsDao.update(settings);
    }
}