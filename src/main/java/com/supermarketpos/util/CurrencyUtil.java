package com.supermarketpos.util;

import java.text.DecimalFormat;

public final class CurrencyUtil {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.00");

    private CurrencyUtil() {
    }

    /** Formats an amount with the given symbol, clamping negatives to zero per business rules. */
    public static String format(double amount, String currencySymbol) {
        double safeAmount = Math.max(amount, 0);
        return currencySymbol + FORMAT.format(safeAmount);
    }
}