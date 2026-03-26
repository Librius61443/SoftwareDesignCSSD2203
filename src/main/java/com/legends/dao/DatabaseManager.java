package com.legends.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    
    // 1. Private static instance of the class (Singleton Pattern)
    private static DatabaseManager instance;
    private Connection connection;

    // Database connection credentials
    // Note: In a production environment, these should be loaded from an application.properties file or environment variables.
    private static final String URL = "jdbc:mysql://localhost:3306/legends_db"; 
    private static final String USERNAME = "root"; 
    private static final String PASSWORD = "password"; 

    // 2. Private constructor prevents instantiation from other classes
    private DatabaseManager() {
        try {
            // Initialize the MySQL connection
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Database connection established successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Falling back to local in-memory storage.");
        }
    }

    // 3. Public static method to get the single, global instance
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Method to get the actual database connection for your DAOs to use
    public Connection getConnection() {
        return connection;
    }
    
    // Good practice: Method to close the connection when the application shuts down
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Failed to close the database connection.");
            }
        }
    }
}
