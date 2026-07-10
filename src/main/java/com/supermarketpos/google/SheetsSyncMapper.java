package com.supermarketpos.google;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts POS domain rows (as Object[] from a ResultSet-mapped list, or your model getters)
 * into Sheets-friendly List<List<Object>> rows. Extend each method to pull real fields
 * from your existing Product/Customer/Purchase/Sale/Inventory model classes.
 */
public class SheetsSyncMapper {

    public static List<List<Object>> mapProducts(List<Object[]> products) {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(List.of("ID", "Name", "Category", "Price", "Stock")); // header row
        for (Object[] p : products) {
            rows.add(List.of(p[0], p[1], p[2], p[3], p[4]));
        }
        return rows;
    }

    public static List<List<Object>> mapCustomers(List<Object[]> customers) {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(List.of("ID", "Name", "Phone", "Email"));
        for (Object[] c : customers) {
            rows.add(List.of(c[0], c[1], c[2], c[3]));
        }
        return rows;
    }

    public static List<List<Object>> mapPurchases(List<Object[]> purchases) {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(List.of("ID", "Supplier", "Date", "Total"));
        for (Object[] p : purchases) {
            rows.add(List.of(p[0], p[1], p[2], p[3]));
        }
        return rows;
    }

    public static List<List<Object>> mapSales(List<Object[]> sales) {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(List.of("ID", "Customer", "Date", "Total"));
        for (Object[] s : sales) {
            rows.add(List.of(s[0], s[1], s[2], s[3]));
        }
        return rows;
    }

    public static List<List<Object>> mapInventory(List<Object[]> inventory) {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(List.of("Product ID", "Product Name", "Quantity"));
        for (Object[] i : inventory) {
            rows.add(List.of(i[0], i[1], i[2]));
        }
        return rows;
    }
}