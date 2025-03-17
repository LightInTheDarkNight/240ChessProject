package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

public class DBAuthDAO {
    static {
        try{
        ChessDatabaseManager.createDatabase();
            try (Connection conn = ChessDatabaseManager.getConnection()){
                var createUserTable = conn.prepareStatement("""
                        CREATE TABLE `auth_data` (
                            `authstring` char(36) NOT NULL,
                            `username` varchar(64) NOT NULL,
                            PRIMARY KEY (`authstring`),
                            KEY `username_idx` (`username`),
                            CONSTRAINT `username` FOREIGN KEY (`username`)
                                REFERENCES `users` (`username`)
                                ON DELETE CASCADE
                                ON UPDATE CASCADE
                        )""");
                createUserTable.executeUpdate();
            }
        }catch (DataAccessException | SQLException e) {
            throw new RuntimeException("User Table creation and initialization failed: " + e.getMessage());
        }
    }
}
