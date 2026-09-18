package com.sameneed.config;

import com.sameneed.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {

    private static DatabaseConfig instance;
    private final String url;
    private final String username;
    private final String password;

    private DatabaseConfig() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new DatabaseException("db.properties not found on classpath", null);
            }
            props.load(in);
        } catch (IOException e) {
            throw new DatabaseException("Failed to load db.properties", e);
        }
        String driver = props.getProperty("db.driver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("JDBC driver not found: " + driver, e);
        }
        this.url = props.getProperty("db.url");
        this.username = props.getProperty("db.username");
        this.password = props.getProperty("db.password");
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to obtain database connection", e);
        }
    }

    public static Connection openConnection() {
        return getInstance().getConnection();
    }
}
