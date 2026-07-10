package com.supermarketpos.service;

import com.supermarketpos.dao.SettingsDao;
import com.supermarketpos.model.StoreSettings;

public class SettingsService {

    private final SettingsDao settingsDao;

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
            defaults.setCurrencySymbol("$");
            defaults.setTaxRate(0.0);
            defaults.setTheme("light");
            defaults.setAppVersion("1.0.0");
            return defaults;
        });
    }

    public StoreSettings updateSettings(StoreSettings settings) {
        return settingsDao.update(settings);
    }
}