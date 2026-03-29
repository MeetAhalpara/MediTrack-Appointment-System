package com.example.healthappointment.dao;

import com.example.healthappointment.model.Patient;
import com.example.healthappointment.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PatientDAO — Data Access Object for the patients table.
 *
 * Provides CRUD operations for Patient records.
 * All SQL interactions go through DatabaseConnection.getInstance().
 */
public class PatientDAO {

    /**
     * Inserts a new patient and returns the generated patient_id.
     *
     * @param patient  Patient to insert (patient_id field is ignored on insert)
     * @return the newly assigned patient_id
     * @throws SQLException on any database error
     */
    public int insert(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (name, phone) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getPhone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    patient.setPatientId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Insert patient failed — no generated key returned.");
    }

    /**
     * Returns all patients ordered by name.
     */
    public List<Patient> findAll() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT patient_id, name, phone FROM patients ORDER BY name";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Finds a single patient by ID.
     */
    public Optional<Patient> findById(int id) throws SQLException {
        String sql = "SELECT patient_id, name, phone FROM patients WHERE patient_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Updates an existing patient record.
     */
    public void update(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET name = ?, phone = ? WHERE patient_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getPhone());
            ps.setInt(3, patient.getPatientId());
            ps.executeUpdate();
        }
    }

    /**
     * Deletes a patient and cascades to their appointments (CASCADE is set in schema).
     */
    public void delete(int patientId) throws SQLException {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Patient map(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("patient_id"),
                rs.getString("name"),
                rs.getString("phone")
        );
    }
}