package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.net.URISyntaxException;
import java.net.URI;

@Slf4j
public class SQLDatabaseEngine extends DatabaseEngine {
    @Override
    String search(String text) throws Exception {
        //Write your code here
        String result = null;
        try {
            Connection connection = getConnection();

            // Make query
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM harry WHERE LOWER(?) LIKE CONCAT('%', LOWER(keyword), '%')");
            stmt.setString(1, text);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            String keyword = rs.getString(1);
            result = rs.getString(2) + " (" + (rs.getInt(3) + 1) + " hits)";

            // Update hit count
            if (text.toLowerCase().contains(keyword.toLowerCase())) {
                rs.close();
                stmt.close();

                stmt = connection.prepareStatement("UPDATE harry SET hit_total = hit_total + 1 WHERE keyword = ?");
                stmt.setString(1, keyword);
                
                rs = stmt.executeQuery();

                // Will only update hit_total if a file is changed
                if (rs.getString(1) != "UPDATE 1") {
                    throw new Exception("NOT UPDATED");
                }
            }

            rs.close();
            stmt.close();
            connection.close();

            return result;
        } catch (Exception e) {
            log.info("Exception occured: {}", e.toString());
        }
        return result;
    }


    private Connection getConnection() throws URISyntaxException, SQLException {
        Connection connection;
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() +  "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

        log.info("Username: {} Password: {}", username, password);
        log.info ("dbUrl: {}", dbUrl);

        connection = DriverManager.getConnection(dbUrl, username, password);

        return connection;
    }

}
