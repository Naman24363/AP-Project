package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.Session;
import edu.univ.erp.util.Config;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Create a complete database backup using pg_dump
     * 
     * @param session   User session (must be admin)
     * @param backupDir Directory to save the backup file
     * @return Path to the created backup file
     * @throws IOException if backup fails
     */
    public static String backupDatabase(Session session, String backupDir) throws IOException, SQLException {
        AccessControl.mustBeAdmin(session);

        // Ensure backup directory exists
        File dir = new File(backupDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Failed to create backup directory: " + backupDir);
            }
        }

        String timestamp = LocalDateTime.now().format(dtf);
        String backupFilename = "univ_erp_backup_" + timestamp + ".sql";
        String backupPath = backupDir + File.separator + backupFilename;

        try {
            // Extract database credentials from config
            String erpUrl = Config.get("erp.url");
            String erpUser = Config.get("erp.user");
            String authUrl = Config.get("auth.url");
            String authUser = Config.get("auth.user");
            String dbPassword = Config.get("erp.password");

            // Extract database name from URL (jdbc:postgresql://localhost:5432/univ_erp)
            String erpDbName = extractDbName(erpUrl);
            String authDbName = extractDbName(authUrl);

            if (erpDbName == null || authDbName == null) {
                throw new IOException("Invalid database URL configuration");
            }

            // Create backup file
            FileWriter fw = new FileWriter(backupPath);
            try (PrintWriter pw = new PrintWriter(fw)) {
                pw.println("-- University ERP Complete Database Backup");
                pw.println("-- Generated: " + LocalDateTime.now());
                pw.println("-- Auth Database: " + authDbName);
                pw.println("-- ERP Database: " + erpDbName);
                pw.println();

                // Backup auth database
                pw.println("-- ===== AUTH DATABASE BACKUP =====");
                backupDatabase(erpUser, dbPassword, authDbName, pw);

                pw.println();
                pw.println("-- ===== ERP DATABASE BACKUP =====");
                // Backup ERP database
                backupDatabase(erpUser, dbPassword, erpDbName, pw);
            }

            return backupPath;
        } catch (Exception e) {
            throw new IOException("Database backup failed: " + e.getMessage(), e);
        }
    }

    /**
     * Restore database from backup file
     * 
     * @param session    User session (must be admin)
     * @param backupFile Path to the backup file
     * @throws IOException if restore fails
     */
    public static void restoreDatabase(Session session, String backupFile) throws IOException, SQLException {
        AccessControl.mustBeAdmin(session);

        File file = new File(backupFile);
        if (!file.exists()) {
            throw new IOException("Backup file not found: " + backupFile);
        }

        try {
            String erpUrl = Config.get("erp.url");
            String erpUser = Config.get("erp.user");
            String authUrl = Config.get("auth.url");
            String dbPassword = Config.get("erp.password");

            String erpDbName = extractDbName(erpUrl);
            String authDbName = extractDbName(authUrl);

            if (erpDbName == null || authDbName == null) {
                throw new IOException("Invalid database URL configuration");
            }

            // Read and execute backup file
            StringBuilder sqlContent = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sqlContent.append(line).append("\n");
                }
            }

            restoreDatabaseContent(erpUser, dbPassword, erpDbName, authDbName, sqlContent.toString());
        } catch (Exception e) {
            throw new IOException("Database restore failed: " + e.getMessage(), e);
        }
    }

    private static void backupDatabase(String user, String password, String dbName, PrintWriter pw)
            throws IOException {
        try {
            // Use pg_dump to backup the database
            ProcessBuilder pb = new ProcessBuilder("pg_dump", "-U", user, dbName);
            pb.environment().put("PGPASSWORD", password);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    pw.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("pg_dump failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            throw new IOException("Failed to backup database " + dbName + ": " + e.getMessage(), e);
        }
    }

    private static void restoreDatabaseContent(String user, String password, String erpDbName, String authDbName,
            String sqlContent) throws IOException {
        try {
            // Use psql to restore the databases
            ProcessBuilder pb = new ProcessBuilder("psql", "-U", user, erpDbName);
            pb.environment().put("PGPASSWORD", password);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (OutputStream os = process.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    BufferedWriter bw = new BufferedWriter(osw)) {
                bw.write(sqlContent);
                bw.flush();
            }

            StringBuilder errors = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errors.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("psql restore failed with exit code: " + exitCode + "\n" + errors.toString());
            }
        } catch (Exception e) {
            throw new IOException("Failed to restore database: " + e.getMessage(), e);
        }
    }

    private static String extractDbName(String jdbcUrl) {
        try {
            // Format: jdbc:postgresql://host:port/dbname
            return jdbcUrl.substring(jdbcUrl.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return null;
        }
    }
}
