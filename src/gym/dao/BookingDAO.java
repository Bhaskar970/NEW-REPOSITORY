package gym.dao;

import gym.model.Booking;
import gym.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    /**
     * Book a slot for a user.
     * Returns:
     *   1  = success
     *   0  = slot full
     *  -1  = already booked by this user
     *  -2  = SQL error
     */
    public int bookSlot(int userId, int slotId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Lock & check capacity
            String checkSql = "SELECT available_spots FROM slot_availability WHERE slot_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, slotId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt("available_spots") <= 0) {
                        conn.rollback();
                        return 0; // full
                    }
                }
            }

            // 2. Insert booking
            String insertSql = "INSERT INTO bookings (user_id, slot_id, status) VALUES (?, ?, 'confirmed')";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, slotId);
                ps.executeUpdate();
            }

            conn.commit();
            return 1;

        } catch (SQLIntegrityConstraintViolationException e) {
            if (conn != null) conn.rollback();
            return -1; // duplicate booking
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) { conn.setAutoCommit(true); conn.close(); }
        }
    }

    /** Cancel a booking (only by the owning user). */
    public boolean cancelBooking(int bookingId, int userId) throws SQLException {
        String sql = "UPDATE bookings SET status='cancelled' WHERE id=? AND user_id=? AND status='confirmed'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /** All confirmed bookings for a specific user (with slot details). */
    public List<Booking> getBookingsByUser(int userId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql =
            "SELECT b.id, b.user_id, b.slot_id, b.booked_on, b.status, " +
            "       s.slot_name, s.slot_date, s.start_time, s.end_time " +
            "FROM bookings b " +
            "JOIN slots s ON b.slot_id = s.id " +
            "WHERE b.user_id = ? " +
            "ORDER BY s.slot_date DESC, s.start_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** All bookings – admin view. */
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql =
            "SELECT b.id, b.user_id, b.slot_id, b.booked_on, b.status, " +
            "       u.name AS user_name, " +
            "       s.slot_name, s.slot_date, s.start_time, s.end_time " +
            "FROM bookings b " +
            "JOIN users u  ON b.user_id  = u.id " +
            "JOIN slots s  ON b.slot_id  = s.id " +
            "ORDER BY b.booked_on DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Booking bk = mapRow(rs);
                bk.setUserName(rs.getString("user_name"));
                list.add(bk);
            }
        }
        return list;
    }

    /** Check if user already has a confirmed booking in a slot. */
    public boolean alreadyBooked(int userId, int slotId) throws SQLException {
        String sql = "SELECT id FROM bookings WHERE user_id=? AND slot_id=? AND status='confirmed'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── Row mapper ────────────────────────────────────────────

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setSlotId(rs.getInt("slot_id"));
        b.setBookedOn(rs.getTimestamp("booked_on"));
        b.setStatus(rs.getString("status"));
        b.setSlotName(rs.getString("slot_name"));
        b.setSlotDate(rs.getString("slot_date"));
        b.setStartTime(rs.getString("start_time"));
        b.setEndTime(rs.getString("end_time"));
        return b;
    }
}
