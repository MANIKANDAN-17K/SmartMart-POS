package com.supermarketpos.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtil {

    private static final Locale INDIA = new Locale("en", "IN");
    private static final NumberFormat FORMAT = NumberFormat.getCurrencyInstance(INDIA);

    private CurrencyUtil() {
    }

    /** Returns "₹1,234.56" style string. */
    public static String format(BigDecimal amount) {
        if (amount == null)
            return FORMAT.format(BigDecimal.ZERO);
        return FORMAT.format(amount);
    }

    /** Safe parse — returns ZERO on null or empty input. */
    public static BigDecimal parse(String value) {
        if (value == null || value.isBlank())
            return BigDecimal.ZERO;
        try {
            String clean = value.replaceAll("[^\\d.]", "");
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public static String format(double amount) {
        return FORMAT.format(amount);
    }

    public static String format(double amount, String symbol) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return symbol + nf.format(amount);
    }

    public static String format(BigDecimal amount, String symbol) {
        if (amount == null)
            amount = BigDecimal.ZERO;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return symbol + nf.format(amount);
    }
}