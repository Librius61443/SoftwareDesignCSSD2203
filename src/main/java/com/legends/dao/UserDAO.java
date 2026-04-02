package com.legends.dao;

import com.legends.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserDAO {

    private static final Map<String, User> IN_MEMORY_USERS = new ConcurrentHashMap<>();

    public boolean createUser(User user) {
        String query = "INSERT INTO users (username, password, wins, losses) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) {
            return createUserInMemory(user);
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setInt(3, user.getWins());
            stmt.setInt(4, user.getLosses());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return createUserInMemory(user);
        }
    }

    public User authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) {
            return authenticateInMemory(username, password);
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return hydrateUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return authenticateInMemory(username, password);
        }
        return null;
    }

    public User getUser(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) {
            return getUserInMemory(username);
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return hydrateUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return getUserInMemory(username);
        }
        return null;
    }

    public boolean updateStats(User user) {
        String query = "UPDATE users SET wins = ?, losses = ? WHERE username = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) {
            return updateStatsInMemory(user);
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getWins());
            stmt.setInt(2, user.getLosses());
            stmt.setString(3, user.getUsername());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return updateStatsInMemory(user);
        }
    }

    private boolean createUserInMemory(User user) {
        if (IN_MEMORY_USERS.containsKey(user.getUsername())) {
            return false;
        }
        IN_MEMORY_USERS.put(user.getUsername(), cloneUser(user));
        return true;
    }

    private User authenticateInMemory(String username, String password) {
        User user = IN_MEMORY_USERS.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return cloneUser(user);
        }
        return null;
    }

    private User getUserInMemory(String username) {
        User user = IN_MEMORY_USERS.get(username);
        return user == null ? null : cloneUser(user);
    }

    private boolean updateStatsInMemory(User user) {
        if (!IN_MEMORY_USERS.containsKey(user.getUsername())) {
            return false;
        }
        IN_MEMORY_USERS.put(user.getUsername(), cloneUser(user));
        return true;
    }

    private User hydrateUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("username"),
            rs.getString("password"),
            rs.getInt("wins"),
            rs.getInt("losses")
        );
    }

    private User cloneUser(User user) {
        return new User(user.getUsername(), user.getPassword(), user.getWins(), user.getLosses());
    }

    public void resetInMemoryStore() {
        IN_MEMORY_USERS.clear();
    }
}
