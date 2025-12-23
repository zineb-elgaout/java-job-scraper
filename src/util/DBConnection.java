package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // charge le driver MySQL

            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3307/job_scraper",
                "root",
                ""
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
