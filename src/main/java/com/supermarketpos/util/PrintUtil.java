package com.supermarketpos.util;

import javafx.concurrent.Task;

import javax.print.PrintException;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for sending HTML content to the default system printer.
 * Uses a temporary file + Desktop.open() as the most reliable
 * cross-platform approach for JavaFX desktop apps without a browser engine.
 *
 * For thermal receipt printers, replace the body of printHtml() with
 * an ESC/POS byte-stream implementation.
 */
public class PrintUtil {

    private static final Logger log = Logger.getLogger(PrintUtil.class.getName());

    private PrintUtil() {}

    /**
     * Writes HTML to a temp file and opens it in the system's default
     * browser/viewer which handles printing. Blocks until the process starts.
     */
    public static void printHtml(String html, String jobName) {
        try {
            Path tmp = Files.createTempFile("smartmart_receipt_", ".html");
            Files.writeString(tmp, html, StandardCharsets.UTF_8);
            tmp.toFile().deleteOnExit();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tmp.toFile());
                log.info("Receipt opened for printing: " + jobName);
            } else {
                throw new UnsupportedOperationException("Desktop API not supported on this platform.");
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Failed to create receipt temp file.", ex);
            throw new RuntimeException("Printer unavailable.", ex);
        }
    }
}