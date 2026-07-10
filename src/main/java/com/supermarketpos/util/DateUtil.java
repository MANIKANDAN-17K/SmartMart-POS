package com.supermarketpos.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter DT_FMT  = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private DateUtil() {}

    public static String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DT_FMT) : "";
    }

    public static String formatDate(LocalDate d) {
        return d != null ? d.format(DATE_FMT) : "";
    }

    public static String now() {
        return LocalDateTime.now().format(DT_FMT);
    }
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private DateUtil() {}

    public static String format(LocalDate date) {
        return date != null ? date.format(DISPLAY_FORMAT) : "";
    }

    public static LocalDate parse(String text) {
        return (text != null && !text.isBlank()) ? LocalDate.parse(text, DISPLAY_FORMAT) : null;
    }
}