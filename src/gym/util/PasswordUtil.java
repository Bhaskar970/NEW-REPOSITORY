package gym.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Simple SHA-256 + salt password utility.
 * (For production, swap in BCrypt via the jbcrypt library.)
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Hash a plain-text password and return "salt:hash". */
    public static String hash(String plainPassword) {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String hash = sha256(salt + plainPassword);
        return salt + ":" + hash;
    }

    /** Verify a plain-text password against a stored "salt:hash" value. */
    public static boolean verify(String plainPassword, String stored) {
        if (stored == null || !stored.contains(":")) return false;
        String[] parts = stored.split(":", 2);
        String salt = parts[0];
        String expectedHash = parts[1];
        return sha256(salt + plainPassword).equals(expectedHash);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
