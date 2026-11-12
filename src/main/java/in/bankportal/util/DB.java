package in.bankportal.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DB {
    private DB() {}
    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
    public static Connection getConnection() throws SQLException {
        String url = env("BANK_DB_URL", "jdbc:mysql://localhost:3306/bank_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        String user = env("BANK_DB_USER", "root");
        String password = env("BANK_DB_PASSWORD", "manager");
        return DriverManager.getConnection(url, user, password);
    }
}
