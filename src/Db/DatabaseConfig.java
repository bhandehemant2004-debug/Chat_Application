package Db;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConfig {

    private static String dbUrl = "jdbc:mysql://localhost:3306/chat_app_db?useSSL=false&allowPublicKeyRetrieval=true";
    private static String dbUser = "root";
    private static String dbPass = "root";

    static {
        try (FileInputStream fis = new FileInputStream("params.txt")) {
            Properties params = new Properties();
            params.load(fis);
            if (params.containsKey("db.url")) dbUrl = params.getProperty("db.url");
            if (params.containsKey("db.user")) dbUser = params.getProperty("db.user");
            if (params.containsKey("db.password")) dbPass = params.getProperty("db.password");
        } catch (Exception ignored) {}
    }

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }
}
