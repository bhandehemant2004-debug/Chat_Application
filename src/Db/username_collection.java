package Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class username_collection {

    public static boolean add(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null) {
            System.err.println("[Db Engine] Registration rejected: username or password empty");
            return false;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.executeUpdate();
                System.out.println("[Db Engine - MySQL] Successfully registered user in MySQL: " + username);
                return true;
            }
        } catch (Exception e) {
            System.err.println("[Db Engine - MySQL Error] Failed to register user '" + username + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkPassword(String username, String password) {
        if (username == null || password == null) return false;

        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String dbPassword = rs.getString("password");
                        boolean match = dbPassword.equals(password);
                        System.out.println("[Db Engine - MySQL] Password match for '" + username + "': " + match);
                        return match;
                    } else {
                        System.err.println("[Db Engine - MySQL] User not found: " + username);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Db Engine - MySQL Error] Password check failed for '" + username + "': " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static int add_user(String username, String password) {
        return add(username, password) ? 1 : 0;
    }

    public static int check_user(String username, String password) {
        return checkPassword(username, password) ? 1 : 0;
    }
}
