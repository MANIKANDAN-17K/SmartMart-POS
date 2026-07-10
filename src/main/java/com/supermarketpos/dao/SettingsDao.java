package com.supermarketpos.dao;

import com.supermarketpos.model.StoreSettings;
import com.supermarketpos.model.TaxSetting;
import com.supermarketpos.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SettingsDao {

    private static final String FIND_SETTINGS_SQL = "SELECT * FROM settings LIMIT 1";
    private static final String UPDATE_SETTINGS_SQL = "UPDATE settings SET store_name=?, currency_symbol=?, tax_rate=?, theme=?, app_version=? WHERE id=?";

    public Optional<StoreSettings> findSettings() {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(FIND_SETTINGS_SQL);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch store settings", e);
        }
    }

    public StoreSettings getStoreSettings() throws SQLException {
        return findSettings().orElse(null);
    }

    public StoreSettings update(StoreSettings settings) {
        try (Connection conn = DBConnection.getConnection();
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
        StoreSettings s = new StoreSettings();
        s.setId(rs.getInt("id"));
        s.setStoreName(rs.getString("store_name"));
        s.setAddress(rs.getString("address"));
        s.setGstNumber(rs.getString("gst_number"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setLogoPath(rs.getString("logo_path"));
        s.setReceiptHeader(rs.getString("receipt_header"));
        s.setReceiptFooter(rs.getString("receipt_footer"));
        s.setShowLogoOnReceipt(rs.getBoolean("show_logo_on_receipt"));
        s.setShowGstOnReceipt(rs.getBoolean("show_gst_on_receipt"));
        s.setShowCashierOnReceipt(rs.getBoolean("show_cashier_on_receipt"));
        s.setTheme(rs.getString("theme"));
        s.setAppVersion(rs.getString("app_version"));
        s.setCurrencySymbol(rs.getString("currency_symbol"));
        s.setTaxRate(rs.getDouble("tax_rate"));
        return s;
    }

    public void saveStoreSettings(StoreSettings s) throws SQLException {
        String sql = "UPDATE settings SET store_name=?, address=?, gst_number=?, phone=?, email=?, " +
                "logo_path=?, receipt_header=?, receipt_footer=?, show_logo_on_receipt=?, " +
                "show_gst_on_receipt=?, show_cashier_on_receipt=? WHERE id = 1";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getStoreName());
            ps.setString(2, s.getAddress());
            ps.setString(3, s.getGstNumber());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getEmail());
            ps.setString(6, s.getLogoPath());
            ps.setString(7, s.getReceiptHeader());
            ps.setString(8, s.getReceiptFooter());
            ps.setBoolean(9, s.isShowLogoOnReceipt());
            ps.setBoolean(10, s.isShowGstOnReceipt());
            ps.setBoolean(11, s.isShowCashierOnReceipt());
            ps.executeUpdate();
        }
    }

    public void saveTheme(String theme) throws SQLException {
        String sql = "UPDATE settings SET theme=? WHERE id = 1";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, theme);
            ps.executeUpdate();
        }
    }

    public TaxSetting getTaxSettings() throws SQLException {
        String sql = "SELECT * FROM tax_settings WHERE id = 1";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                TaxSetting t = new TaxSetting();
                t.setId(rs.getInt("id"));
                t.setGstPercentage(rs.getDouble("gst_percentage"));
                t.setGstEnabled(rs.getBoolean("gst_enabled"));
                return t;
            }
            return null;
        }
    }

    public void saveTaxSettings(TaxSetting t) throws SQLException {
        String sql = "UPDATE tax_settings SET gst_percentage=?, gst_enabled=? WHERE id = 1";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, t.getGstPercentage());
            ps.setBoolean(2, t.isGstEnabled());
            ps.executeUpdate();
        }
    }
}