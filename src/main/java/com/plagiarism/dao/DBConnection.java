package com.plagiarism.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens JDBC connections. Settings come from environment variables, with local defaults. */
public final class DBConnection {

    private static final String URL = env("DB_URL", "jdbc:mysql://localhost:3306/plagiarism_db"
            + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8");
    private static final String USER = "root";
    private static final String PASSWORD = "Meer26@Chitkara";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() { }

  public static Connection get() throws SQLException {
    System.out.println("===== DB CONNECTION TEST =====");
    System.out.println("USER: " + USER);
    System.out.println("PASSWORD PROVIDED: "
            + (PASSWORD != null && !PASSWORD.isEmpty()));

    Connection c = DriverManager.getConnection(URL, USER, PASSWORD);

    System.out.println("MYSQL CONNECTION SUCCESSFUL!");
    return c;
}

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }
}
