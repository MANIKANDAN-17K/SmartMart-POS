package com.supermarketpos.dao;

import com.supermarketpos.database.DatabaseInitializer;
import com.supermarketpos.model.StoreSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SettingsDao {

    private static final String FIND_SETTINGS_SQL = "SELECT * FROM store_settings LIMIT 1";
    private static final String UPDATE_SETTINGS_SQL =
            "UPDATE store_settings SET store_name=?, currency_symbol=?, tax_rate=?, theme=?, app_version=? WHERE id=?";

    public Optional<StoreSettings> findSettings() {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_SETTINGS_SQL);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch store settings", e);
        }
    }

    public StoreSettings update(StoreSettings settings) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SETTINGS_SQL)) {
            ps.setString(1, settings.getStoreName());
            ps.setString(2, settings.getCurrencySymbol());
            ps.setDouble(3, settings.getTaxRate());
            ps.setString(4, settings.getTheme());
            ps.setString(5, settings.getAppVersion());
            ps.setInt(6, settings.getId());
            ps.executeUpdate();
            return settings;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update store settings", e);
        }
    }

    private StoreSettings mapRow(ResultSet rs) throws SQLException {
        return new StoreSettings(
                rs.getInt("id"),
                rs.getString("store_name"),
                rs.getString("currency_symbol"),
                rs.getDouble("tax_rate"),
                rs.getString("theme"),
                rs.getString("app_version")
        );
    }
}