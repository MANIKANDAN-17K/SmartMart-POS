package com.supermarketpos.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for saving HTML receipts/reports to local files.
 * For development, files are saved to a receipts/ directory.
 * Later, this can be swapped for actual printer integration.
 */
public class PrintUtil {

    private static final Logger log = Logger.getLogger(PrintUtil.class.getName());
    private static final String RECEIPTS_DIR = "receipts";
    private static final DateTimeFormatter FILE_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private PrintUtil() {}

    /**
     * Saves HTML content to a file in the receipts/ directory.
     * Returns the absolute path of the saved file.
     *
     * @param html    the HTML content to save
     * @param jobName a descriptive name for the receipt (used in the filename)
     * @return the absolute path of the saved file
     */
    public static String printHtml(String html, String jobName) {
        try {
            Path receiptsDir = Paths.get(RECEIPTS_DIR);
            if (!Files.exists(receiptsDir)) {
                Files.createDirectories(receiptsDir);
            }

            String sanitizedName = jobName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String timestamp = LocalDateTime.now().format(FILE_DATE_FMT);
            String fileName = sanitizedName + "_" + timestamp + ".html";

            Path filePath = receiptsDir.resolve(fileName);
            Files.writeString(filePath, html, StandardCharsets.UTF_8);

            String absolutePath = filePath.toAbsolutePath().toString();
            log.info("Receipt saved to file: " + absolutePath);
            return absolutePath;
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Failed to save receipt file.", ex);
            throw new RuntimeException("Could not save receipt file.", ex);
        }
    }

    /**
     * Saves HTML report content to a file in the receipts/ directory.
     * Returns the absolute path of the saved file.
     *
     * @param html       the HTML content to save
     * @param reportName a descriptive name for the report
     * @return the absolute path of the saved file
     */
    public static String saveReport(String html, String reportName) {
        return printHtml(html, "Report_" + reportName);
    }
}