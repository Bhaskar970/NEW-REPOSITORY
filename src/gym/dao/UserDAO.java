package gym.dao;

import gym.model.User;
import gym.util.DBConnection;
import gym.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User operations.
 */
public class UserDAO {

    // ── Register ──────────────────────────────────────────────

    /**
     * Register a new member.
     * @return generated user id, or -1 if email already exists.
     */
    public int register(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, phone, password, role) VALUES (?, ?, ?, ?, 'member')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, PasswordUtil.hash(user.getPassword()));

            int rows = ps.executeUpdate();
            if (rows == 0) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            return -1; // duplicate email
        }
        return -1;
    }

    // ── Login ─────────────────────────────────────────────────

    /**
     * Validate credentials.
     * @return User object on success, null on failure.
     */
    public User login(String email, String plainPassword) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtil.verify(plainPassword, storedHash)) {
                        return mapRow(rs);
                    }
                }
            }
        }
        return null;
    }

    // ── Find ──────────────────────────────────────────────────

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── All Members (admin) ───────────────────────────────────

    public List<User> getAllMembers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role='member' ORDER BY joined_on DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── Row mapper ────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setJoinedOn(rs.getTimestamp("joined_on"));
        return u;
    }
}
