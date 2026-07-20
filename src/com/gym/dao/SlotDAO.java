package com.gym.dao;

import com.gym.model.Slot;
import com.gym.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Access Object for the `slots` table.
 */
public class SlotDAO {

    // ── Create ────────────────────────────────────────────────────────

    /** Inserts a new slot. */
    public boolean addSlot(Slot slot) {
        String sql = "INSERT INTO slots (slot_name, start_time, end_time, max_capacity, slot_date) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, slot.getSlotName());
            ps.setString(2, slot.getStartTime());
            ps.setString(3, slot.getEndTime());
            ps.setInt   (4, slot.getMaxCapacity());
            ps.setString(5, slot.getSlotDate());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[SlotDAO] addSlot error: " + e.getMessage());
            return false;
        }
    }

    // ── Read ──────────────────────────────────────────────────────────

    /**
     * Returns all slots for a given date, including the current booking count.
     */
    public List<Slot> getSlotsByDate(String date) {
        List<Slot> list = new ArrayList<>();
        String sql = """
                SELECT s.*,
                       COUNT(b.id) AS current_bookings
                FROM   slots s
                LEFT JOIN bookings b
                       ON b.slot_id = s.id AND b.status = 'confirmed'
                WHERE  s.slot_date = ?
                GROUP BY s.id
                ORDER BY s.start_time
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[SlotDAO] getSlotsByDate error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all slots (admin use), enriched with live booking counts.
     */
    public List<Slot> getAllSlots() {
        List<Slot> list = new ArrayList<>();
        String sql = """
                SELECT s.*,
                       COUNT(b.id) AS current_bookings
                FROM   slots s
                LEFT JOIN bookings b
                       ON b.slot_id = s.id AND b.status = 'confirmed'
                GROUP BY s.id
                ORDER BY s.slot_date, s.start_time
                """;
        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[SlotDAO] getAllSlots error: " + e.getMessage());
        }
        return list;
    }

    /** Fetches a single slot by id, including current booking count. */
    public Slot getSlotById(int id) {
        String sql = """
                SELECT s.*,
                       COUNT(b.id) AS current_bookings
                FROM   slots s
                LEFT JOIN bookings b
                       ON b.slot_id = s.id AND b.status = 'confirmed'
                WHERE  s.id = ?
                GROUP BY s.id
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[SlotDAO] getSlotById error: " + e.getMessage());
        }
        return null;
    }

    // ── Update ────────────────────────────────────────────────────────

    /** Updates a slot's capacity. */
    public boolean updateSlotCapacity(int slotId, int newCapacity) {
        String sql = "UPDATE slots SET max_capacity = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, newCapacity);
            ps.setInt(2, slotId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[SlotDAO] updateSlotCapacity error: " + e.getMessage());
            return false;
        }
    }

    // ── Delete ────────────────────────────────────────────────────────

    /** Deletes a slot (cascades to bookings via FK). */
    public boolean deleteSlot(int id) {
        String sql = "DELETE FROM slots WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[SlotDAO] deleteSlot error: " + e.getMessage());
            return false;
        }
    }

    // ── Helper ────────────────────────────────────────────────────────

    private Slot mapRow(ResultSet rs) throws SQLException {
        Slot s = new Slot();
        s.setId(rs.getInt("id"));
        s.setSlotName(rs.getString("slot_name"));
        s.setStartTime(rs.getString("start_time"));
        s.setEndTime(rs.getString("end_time"));
        s.setMaxCapacity(rs.getInt("max_capacity"));
        s.setSlotDate(rs.getString("slot_date"));
        s.setCurrentBookings(rs.getInt("current_bookings"));
        return s;
    }
}
