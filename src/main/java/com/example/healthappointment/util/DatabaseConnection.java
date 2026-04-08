package com.example.healthappointment.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static Connection connection;

    // Default production database
    private static String url =
            "jdbc:sqlite:" + System.getProperty("user.home") + "/appointments.db";

    /**
     * Allows tests to switch to a separate database.
     */
    public static void setDatabaseUrl(String newUrl) throws SQLException {
        closeConnection();
        url = newUrl;
    }

    /**
     * Resets database URL back to the default application database.
     */
    public static void resetToDefaultDatabase() throws SQLException {
        closeConnection();
        url = "jdbc:sqlite:" + System.getProperty("user.home") + "/appointments.db";
    }

    public static Connection getInstance() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url);

            // Enable foreign key support for SQLite
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static void initializeDatabase() {

        String patientsTable = """
            CREATE TABLE IF NOT EXISTS patients (
                patient_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT NOT NULL
            );
        """;

        String appointmentsTable = """
            CREATE TABLE IF NOT EXISTS appointments (
                appointment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER,
                date TEXT,
                time TEXT,
                doctor TEXT,
                reason TEXT,
                status TEXT,
                FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE
            );
        """;

        try (Connection conn = getInstance();
             Statement stmt = conn.createStatement()) {

            stmt.execute(patientsTable);
            stmt.execute(appointmentsTable);

            System.out.println("Database initialized successfully");

        } catch (Exception e) {
            System.out.println("Database initialization failed");
            e.printStackTrace();
        }
    }
}