package com.gym.dao;

import com.gym.model.Booking;
import com.gym.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Access Object for the `bookings` table.
 *
 * Key design decision: bookSlot() uses a database-level transaction +
 * SELECT … FOR UPDATE to prevent race conditions when two users try to
 * book the last seat simultaneously.
 */
public class BookingDAO {

    // ── Create ────────────────────────────────────────────────────────

    /**
     * Books a slot for a user.
     *
     * Returns a result code:
     *   0 → success
     *   1 → slot is full (overcrowding prevention)
     *   2 → user already has an active booking for this slot
     *  -1 → database error
     */
    public int bookSlot(int userId, int slotId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);                      // begin transaction

            // 1. Lock the slot row so concurrent requests serialise here
            String lockSql = """
                    SELECT max_capacity,
                           (SELECT COUNT(*) FROM bookings
                            WHERE slot_id = ? AND status = 'confirmed') AS booked
                    FROM   slots
                    WHERE  id = ?
                    FOR UPDATE
                    """;
            int maxCapacity, booked;
            try (PreparedStatement ps = con.prepareStatement(lockSql)) {
                ps.setInt(1, slotId);
                ps.setInt(2, slotId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return -1;
                    }
                    maxCapacity = rs.getInt("max_capacity");
                    booked      = rs.getInt("booked");
                }
            }

            // 2. Check capacity
            if (booked >= maxCapacity) {
                con.rollback();
                return 1;          // slot full
            }

            // 3. Check for duplicate booking
            String dupSql = "SELECT id FROM bookings WHERE user_id = ? AND slot_id = ? AND status = 'confirmed'";
            try (PreparedStatement ps = con.prepareStatement(dupSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, slotId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        con.rollback();
                        return 2;   // already booked
                    }
                }
            }

            // 4. Insert booking
            String insertSql = "INSERT INTO bookings (user_id, slot_id, status) VALUES (?, ?, 'confirmed')";
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, slotId);
                ps.executeUpdate();
            }

            con.commit();
            return 0;              // success

        } catch (SQLException e) {
            System.err.println("[BookingDAO] bookSlot error: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return -1;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ── Read ──────────────────────────────────────────────────────────

    /**
     * Returns all bookings for a specific user (with joined slot details).
     */
    public List<Booking> getBookingsByUser(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = """
                SELECT b.*, s.slot_name, s.slot_date, s.start_time, s.end_time, u.name AS user_name
                FROM   bookings b
                JOIN   slots    s ON b.slot_id  = s.id
                JOIN   users    u ON b.user_id  = u.id
                WHERE  b.user_id = ?
                ORDER BY b.booked_at DESC
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] getBookingsByUser error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all bookings in the system (admin view).
     */
    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = """
                SELECT b.*, s.slot_name, s.slot_date, s.start_time, s.end_time, u.name AS user_name
                FROM   bookings b
                JOIN   slots    s ON b.slot_id  = s.id
                JOIN   users    u ON b.user_id  = u.id
                ORDER BY b.booked_at DESC
                """;
        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[BookingDAO] getAllBookings error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns confirmed bookings for a specific slot (admin use).
     */
    public List<Booking> getBookingsBySlot(int slotId) {
        List<Booking> list = new ArrayList<>();
        String sql = """
                SELECT b.*, s.slot_name, s.slot_date, s.start_time, s.end_time, u.name AS user_name
                FROM   bookings b
                JOIN   slots    s ON b.slot_id  = s.id
                JOIN   users    u ON b.user_id  = u.id
                WHERE  b.slot_id = ? AND b.status = 'confirmed'
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] getBookingsBySlot error: " + e.getMessage());
        }
        return list;
    }

    // ── Update (Cancel) ───────────────────────────────────────────────

    /**
     * Cancels a booking (sets status = 'cancelled').
     * Only the owning user or an admin should call this.
     */
    public boolean cancelBooking(int bookingId) {
        String sql = "UPDATE bookings SET status = 'cancelled' WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[BookingDAO] cancelBooking error: " + e.getMessage());
            return false;
        }
    }

    // ── Helper ────────────────────────────────────────────────────────

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setSlotId(rs.getInt("slot_id"));
        b.setStatus(rs.getString("status"));
        b.setBookedAt(rs.getString("booked_at"));
        b.setUserName(rs.getString("user_name"));
        b.setSlotName(rs.getString("slot_name"));
        b.setSlotDate(rs.getString("slot_date"));
        b.setStartTime(rs.getString("start_time"));
        b.setEndTime(rs.getString("end_time"));
        return b;
    }
}
