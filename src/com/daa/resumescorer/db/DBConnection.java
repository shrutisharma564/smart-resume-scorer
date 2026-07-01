package com.daa.resumescorer.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central place that opens JDBC connections to MySQL.
 * Reads db.properties (url / user / password) from the project root.
 *
 * NOTE: we intentionally do NOT call Class.forName("com.mysql.cj.jdbc.Driver")
 * because JDBC 4+ drivers register themselves automatically via the
 * java.sql.Driver service loader, as long as mysql-connector-j-*.jar
 * is on the classpath.
 */
public class DBConnection {

    private static Properties props;

    private static synchronized Properties loadProps() {
        if (props != null) return props;
        props = new Properties();
        try (InputStream in = new FileInputStream("db.properties")) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("Could not read db.properties: " + e.getMessage());
        }
        return props;
    }

    /** Opens a brand new connection. Caller is responsible for closing it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        Properties p = loadProps();
        String url  = p.getProperty("db.url");
        String user = p.getProperty("db.user");
        String pass = p.getProperty("db.password");
        return DriverManager.getConnection(url, user, pass);
    }

    /** Quick test used by the login screen / main() to fail fast with a clear message. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("DB connection failed: " + e.getMessage());
            return false;
        }
    }
}
