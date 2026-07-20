package com.gym.util;

import com.gym.model.Booking;
import com.gym.model.Slot;
import com.gym.model.User;

import java.util.List;

/**
 * Lightweight JSON serialiser — no external libraries needed.
 * All responses from GymServer are built using these helpers.
 */
public class JsonUtil {

    // ── Primitives ────────────────────────────────────────────────────

    public static String ok(String message) {
        return "{\"success\":true,\"message\":\"" + esc(message) + "\"}";
    }

    public static String error(String message) {
        return "{\"success\":false,\"message\":\"" + esc(message) + "\"}";
    }

    // ── User ──────────────────────────────────────────────────────────

    public static String userToJson(User u) {
        return "{" +
               "\"id\":"       + u.getId()                   + "," +
               "\"name\":\""   + esc(u.getName())            + "\"," +
               "\"email\":\""  + esc(u.getEmail())           + "\"," +
               "\"phone\":\""  + esc(u.getPhone())           + "\"," +
               "\"role\":\""   + esc(u.getRole())            + "\"," +
               "\"createdAt\":\""+ esc(nvl(u.getCreatedAt()))+ "\"" +
               "}";
    }

    public static String usersToJson(List<User> users) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            sb.append(userToJson(users.get(i)));
            if (i < users.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    // ── Slot ──────────────────────────────────────────────────────────

    public static String slotToJson(Slot s) {
        return "{" +
               "\"id\":"              + s.getId()                   + "," +
               "\"slotName\":\""      + esc(s.getSlotName())        + "\"," +
               "\"startTime\":\""     + esc(s.getStartTime())       + "\"," +
               "\"endTime\":\""       + esc(s.getEndTime())         + "\"," +
               "\"maxCapacity\":"     + s.getMaxCapacity()          + "," +
               "\"slotDate\":\""      + esc(s.getSlotDate())        + "\"," +
               "\"currentBookings\":" + s.getCurrentBookings()      + "," +
               "\"availableSeats\":"  + s.getAvailableSeats()       + "," +
               "\"isFull\":"          + s.isFull()                  +
               "}";
    }

    public static String slotsToJson(List<Slot> slots) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < slots.size(); i++) {
            sb.append(slotToJson(slots.get(i)));
            if (i < slots.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    // ── Booking ───────────────────────────────────────────────────────

    public static String bookingToJson(Booking b) {
        return "{" +
               "\"id\":"         + b.getId()                    + "," +
               "\"userId\":"     + b.getUserId()                + "," +
               "\"slotId\":"     + b.getSlotId()                + "," +
               "\"status\":\""   + esc(b.getStatus())           + "\"," +
               "\"bookedAt\":\""  + esc(nvl(b.getBookedAt()))   + "\"," +
               "\"userName\":\""  + esc(nvl(b.getUserName()))   + "\"," +
               "\"slotName\":\""  + esc(nvl(b.getSlotName()))   + "\"," +
               "\"slotDate\":\""  + esc(nvl(b.getSlotDate()))   + "\"," +
               "\"startTime\":\""  + esc(nvl(b.getStartTime())) + "\"," +
               "\"endTime\":\""    + esc(nvl(b.getEndTime()))   + "\"" +
               "}";
    }

    public static String bookingsToJson(List<Booking> bookings) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bookings.size(); i++) {
            sb.append(bookingToJson(bookings.get(i)));
            if (i < bookings.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    /** Escapes characters that would break JSON string values. */
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /** Null-safe string fallback. */
    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
