package gym.dao;

import gym.model.Slot;
import gym.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SlotDAO {

    /** All available slots for a specific date, with live counts. */
    public List<Slot> getSlotsByDate(String date) throws SQLException {
        List<Slot> list = new ArrayList<>();
        String sql = "SELECT * FROM slot_availability WHERE slot_date = ? ORDER BY start_time";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** All slots for the next N days. */
    public List<Slot> getUpcomingSlots(int days) throws SQLException {
        List<Slot> list = new ArrayList<>();
        String sql = "SELECT * FROM slot_availability " +
                     "WHERE slot_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                     "ORDER BY slot_date, start_time";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Fetch a single slot by id (from base table). */
    public Slot findById(int slotId) throws SQLException {
        String sql = "SELECT * FROM slot_availability WHERE slot_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Check if a slot still has capacity. */
    public boolean hasCapacity(int slotId) throws SQLException {
        String sql = "SELECT available_spots FROM slot_availability WHERE slot_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("available_spots") > 0;
            }
        }
        return false;
    }

    // ── Row mapper ────────────────────────────────────────────

    private Slot mapRow(ResultSet rs) throws SQLException {
        Slot s = new Slot();
        s.setId(rs.getInt("slot_id"));
        s.setSlotName(rs.getString("slot_name"));
        s.setSlotDate(rs.getDate("slot_date"));
        s.setStartTime(rs.getTime("start_time"));
        s.setEndTime(rs.getTime("end_time"));
        s.setMaxCapacity(rs.getInt("max_capacity"));
        s.setBookedCount(rs.getInt("booked_count"));
        s.setAvailableSpots(rs.getInt("available_spots"));
        return s;
    }
}
