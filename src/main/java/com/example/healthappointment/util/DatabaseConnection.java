package com.example.healthappointment.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseConnection — singleton utility that manages a single SQLite connection.
 *
 * Design notes (per proposal Section 6):
 *  • Uses SQLite so the application is self-contained (no external server).
 *  • The database file (health_appointments.db) is created in the user's home
 *    directory so it persists between runs on every OS.
 *  • Schema initialization is idempotent — safe to call on every startup.
 *  • Foreign-key support is enabled per the normalized schema discussed in the
 *    proposal's "Improved Database Design" section.
 */
public class DatabaseConnection {

    private static final String DB_URL =
            "jdbc:sqlite:" + System.getProperty("user.home") + "/health_appointments.db";

    private static Connection instance;

    private DatabaseConnection() {}

    /**
     * Returns (and lazily initialises) the single shared Connection.
     * Throws RuntimeException on failure so callers don't need to handle
     * checked exceptions for every query.
     */
    public static Connection getInstance() {
        try {
            if (instance == null || instance.isClosed()) {
                instance = DriverManager.getConnection(DB_URL);
                instance.setAutoCommit(true);
                enableForeignKeys(instance);
                initSchema(instance);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open database: " + e.getMessage(), e);
        }
        return instance;
    }

    /** Enable SQLite foreign-key enforcement (off by default in SQLite). */
    private static void enableForeignKeys(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
    }

    /**
     * Creates the Patients and Appointments tables if they do not already exist.
     * The schema matches the normalised two-table design from the proposal.
     */
    private static void initSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {

            // Patients table
            st.execute("""
                CREATE TABLE IF NOT EXISTS patients (
                    patient_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    name         TEXT    NOT NULL,
                    phone        TEXT    NOT NULL
                )
                """);

            // Appointments table — patient_id is a foreign key to patients
            st.execute("""
                CREATE TABLE IF NOT EXISTS appointments (
                    appointment_id  INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id      INTEGER NOT NULL REFERENCES patients(patient_id) ON DELETE CASCADE,
                    date            TEXT    NOT NULL,
                    time            TEXT    NOT NULL,
                    doctor          TEXT    NOT NULL,
                    reason          TEXT    NOT NULL,
                    status          TEXT    NOT NULL DEFAULT 'Scheduled'
                )
                """);
        }
    }

    /** Closes the connection — call on application exit. */
    public static void close() {
        if (instance != null) {
            try {
                instance.close();
            } catch (SQLException ignored) {}
            instance = null;
        }
    }
}