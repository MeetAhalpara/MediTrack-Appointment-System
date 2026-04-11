package com.example.healthappointment.dao;

import com.example.healthappointment.model.Appointment;
import com.example.healthappointment.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AppointmentDAO — Data Access Object for the appointments table.
 *
 * Every query JOINs patients so that the controller never needs a second
 * round-trip to fetch the patient name / phone for display.
 *
 * Search and filter support (proposal Section 9):
 *  • searchByNameOrDoctor(String keyword) — real-time text filter
 *  • filterByDateAndStatus(LocalDate, String) — compound filter
 */
public class AppointmentDAO {

    // Full SELECT used by every read method — JOIN brings patient name + phone
    private static final String SELECT_ALL = """
            SELECT a.appointment_id,
                   a.patient_id,
                   p.name  AS patient_name,
                   p.phone AS phone,
                   a.date,
                   a.time,
                   a.doctor,
                   a.reason,
                   a.status
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            """;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Inserts a new appointment.
     *
     * @param appt Appointment to insert (appointment_id is ignored — DB assigns it)
     * @return generated appointment_id
     */
    public int insert(Appointment appt) throws SQLException {
        String sql = """
                INSERT INTO appointments (patient_id, date, time, doctor, reason, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = DatabaseConnection.getInstance()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, appt.getPatientId());
            ps.setString(2, appt.getDate().toString());
            ps.setString(3, appt.getTime());
            ps.setString(4, appt.getDoctor());
            ps.setString(5, appt.getReason());
            ps.setString(6, appt.getStatus() != null ? appt.getStatus() : "Scheduled");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    appt.setAppointmentId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Insert appointment failed — no generated key returned.");
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    /** Returns all appointments ordered by date then time. */
    /** Returns all appointments ordered by appointment_id (insertion order). */
    public List<Appointment> findAll() throws SQLException {
        String sql = SELECT_ALL + " ORDER BY a.appointment_id ASC";
        return query(sql);
    }

    /** Finds a single appointment by its ID. */
    public Optional<Appointment> findById(int id) throws SQLException {
        String sql = SELECT_ALL + " WHERE a.appointment_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Case-insensitive LIKE search across patient name and doctor name.
     * Used by the real-time search bar (proposal Section 9).
     *
     * @param keyword partial text typed in the search field
     */
    public List<Appointment> searchByNameOrDoctor(String keyword) throws SQLException {
        String like = "%" + keyword + "%";
        String sql  = SELECT_ALL +
                " WHERE LOWER(p.name) LIKE LOWER(?) OR LOWER(a.doctor) LIKE LOWER(?)" +
                " ORDER BY a.date, a.time";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            return queryPs(ps);
        }
    }

    /**
     * Filters appointments by date and/or status.
     * Pass null for a parameter to skip that filter criterion.
     */
    public List<Appointment> filterByDateAndStatus(LocalDate date, String status)
            throws SQLException {
        StringBuilder sql   = new StringBuilder(SELECT_ALL + " WHERE 1=1");
        List<Object>  params = new ArrayList<>();

        if (date != null) {
            sql.append(" AND a.date = ?");
            params.add(date.toString());
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND a.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY a.date, a.time");

        try (PreparedStatement ps = DatabaseConnection.getInstance()
                .prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return queryPs(ps);
        }
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /** Updates an existing appointment record. */
    public void update(Appointment appt) throws SQLException {
        String sql = """
                UPDATE appointments
                SET patient_id = ?, date = ?, time = ?, doctor = ?, reason = ?, status = ?
                WHERE appointment_id = ?
                """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, appt.getPatientId());
            ps.setString(2, appt.getDate().toString());
            ps.setString(3, appt.getTime());
            ps.setString(4, appt.getDoctor());
            ps.setString(5, appt.getReason());
            ps.setString(6, appt.getStatus());
            ps.setInt(7, appt.getAppointmentId());
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    /** Deletes an appointment by its ID. */
    public void delete(int appointmentId) throws SQLException {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<Appointment> query(String sql) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private List<Appointment> queryPs(PreparedStatement ps) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Appointment map(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getInt("appointment_id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getString("phone"),
                LocalDate.parse(rs.getString("date")),
                rs.getString("time"),
                rs.getString("doctor"),
                rs.getString("reason"),
                rs.getString("status")
        );
    }
}