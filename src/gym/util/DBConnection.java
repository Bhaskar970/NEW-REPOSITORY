package gym.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton-style JDBC connection helper.
 * Edit DB_URL / USER / PASSWORD to match your MySQL setup.
 */
public class DBConnection {

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/gym_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";       // ← change if needed
    private static final String PASSWORD = "root";       // ← change if needed

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    /** Returns a fresh Connection (caller must close it). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    /** Quick smoke-test: run this main to verify connectivity. */
    public static void main(String[] args) {
        try (Connection c = getConnection()) {
            System.out.println("✅ Database connected successfully!");
            System.out.println("   Catalog: " + c.getCatalog());
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
        }
    }
}
