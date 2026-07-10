package com.supermarketpos.service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {

    private final AuditService auditService = new AuditService();
    private static final String DB_NAME = "smartmart_pos";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "yourpassword";
    private static final String BACKUP_DIR = "backups";

    public String createBackup(String username) throws Exception {
        Files.createDirectories(Paths.get(BACKUP_DIR));

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = DB_NAME + "_backup_" + timestamp + ".sql";
        Path backupPath = Paths.get(BACKUP_DIR, fileName);

        if (Files.exists(backupPath)) {
            throw new IOException("Backup file already exists.");
        }

        ProcessBuilder pb = new ProcessBuilder(
                "mysqldump", "-u", DB_USER, "-p" + DB_PASSWORD, DB_NAME
        );
        pb.redirectOutput(backupPath.toFile());
        pb.redirectErrorStream(false);

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            Files.deleteIfExists(backupPath);
            throw new IOException("Backup failed. mysqldump exited with code " + exitCode);
        }

        auditService.log(username, "BACKUP_CREATED", "Backup file: " + fileName);
        return fileName;
    }

    public void restoreBackup(String filePath, String username) throws Exception {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("Invalid backup file: file does not exist.");
        }

        ProcessBuilder pb = new ProcessBuilder(
                "mysql", "-u", DB_USER, "-p" + DB_PASSWORD, DB_NAME
        );
        pb.redirectInput(path.toFile());
        pb.redirectErrorStream(false);

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Restore failed. mysql exited with code " + exitCode);
        }

        auditService.log(username, "DATABASE_RESTORED", "Restored from: " + filePath);
    }
}