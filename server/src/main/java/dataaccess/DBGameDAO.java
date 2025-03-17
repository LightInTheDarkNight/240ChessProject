package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

import static java.sql.Statement.*;
public class DBGameDAO {
    static {
        try {
            ChessDatabaseManager.createDatabase();
                try (Connection conn = ChessDatabaseManager.getConnection()) {
                    var createUserTable = conn.prepareStatement("""
                        
                            CREATE TABLE `game_data` (
                           `gameid` int NOT NULL AUTO_INCREMENT,
                           `white_username` varchar(64) DEFAULT NULL,
                           `black_username` varchar(64) DEFAULT NULL,
                           `game_name` varchar(32) NOT NULL,
                           `game` json NOT NULL,
                           PRIMARY KEY (`gameid`),
                           KEY `white_username_idx` (`white_username`),
                           KEY `black_username_idx` (`black_username`),
                           CONSTRAINT `black_username` FOREIGN KEY (`black_username`)
                               REFERENCES `users` (`username`)
                               ON DELETE SET NULL
                               ON UPDATE CASCADE,
                           CONSTRAINT `white_username` FOREIGN KEY (`white_username`)
                               REFERENCES `users` (`username`)
                               ON DELETE SET NULL
                               ON UPDATE CASCADE
                         )""");
                    createUserTable.executeUpdate();
                }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException("User Table creation and initialization failed: " + e.getMessage());
        }
    }
}