package com.gym.dao;

import com.gym.model.User;
import com.gym.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Access Object for the `users` table.
 * All database operations for User are centralised here.
 */
public class UserDAO {

    // ── Create ────────────────────────────────────────────────────────

    /**
     * Inserts a new user into the database.
     *
     * @return true if the row was inserted successfully.
     */
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (name, email, phone, password, role) VALUES (?, ?, ?, ?, 'member')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPassword());   // plain-text for demo; hash in production

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] registerUser error: " + e.getMessage());
            return false;
        }
    }

    // ── Read ──────────────────────────────────────────────────────────

    /**
     * Validates login credentials.
     *
     * @return the matching User object, or null if credentials are wrong.
     */
    public User loginUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] loginUser error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Looks up a user by primary key.
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getUserById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns every user (admin use).
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllUsers error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Checks whether an email is already registered.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] emailExists error: " + e.getMessage());
        }
        return false;
    }

    // ── Update ────────────────────────────────────────────────────────

    /**
     * Updates the user's profile fields (name, phone).
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, phone = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getPhone());
            ps.setInt(3, user.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updateUser error: " + e.getMessage());
            return false;
        }
    }

    // ── Delete ────────────────────────────────────────────────────────

    /**
     * Removes a user (and cascades to their bookings).
     */
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] deleteUser error: " + e.getMessage());
            return false;
        }
    }

    // ── Helper ────────────────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setCreatedAt(rs.getString("created_at"));
        return u;
    }
}
