package com.supermarketpos.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private DateUtil() {
    }

    public static String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DT_FMT) : "";
    }

    public static String formatDate(LocalDate d) {
        return d != null ? d.format(DATE_FMT) : "";
    }

    public static String now() {
        return LocalDateTime.now().format(DT_FMT);
    }

    public static String nowDisplay() {
        return LocalDateTime.now().format(DT_FMT);
    }

    public static String format(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "";
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DT_FMT) : "";
    }

    public static LocalDate parse(String text) {
        return (text != null && !text.isBlank()) ? LocalDate.parse(text, DATE_FMT) : null;
    }

    public static String formatDisplay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DT_FMT) : "";
    }

    public static String formatDisplay(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "";
    }
}