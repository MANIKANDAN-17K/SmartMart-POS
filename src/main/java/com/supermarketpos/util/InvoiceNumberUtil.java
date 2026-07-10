package com.supermarketpos.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique invoice numbers in the format:
 *   INV-YYYYMMDD-HHMMSS-NNNN
 * where NNNN is a per-JVM-session counter that resets daily.
 */
public class InvoiceNumberUtil {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final AtomicInteger counter = new AtomicInteger(1);

    private InvoiceNumberUtil() {}

    public static String generate() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("INV-%s-%04d", now.format(FMT), counter.getAndIncrement());
    }
}