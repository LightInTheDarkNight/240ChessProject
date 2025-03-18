package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.Connection;
import java.sql.SQLException;

public class DBAuthDAO implements AuthDAO {
    static {
        try{
        ChessDatabaseManager.createDatabase();
            try (Connection conn = ChessDatabaseManager.getConnection()){
                var createUserTable = conn.prepareStatement("""
                        CREATE TABLE IF NOT EXISTS `auth_data` (
                            `auth_token` char(36) NOT NULL,
                            `username` varchar(64) NOT NULL,
                            PRIMARY KEY (`auth_token`),
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


    @Override
    public boolean clear() throws DataAccessException {
        try (Connection conn = ChessDatabaseManager.getConnection();
             var deleteStatement = conn.prepareStatement("TRUNCATE TABLE auth_data")) {
            deleteStatement.executeUpdate();
            return true;
        }catch (SQLException e) {
            throw new DataAccessException("Error: credentials database clear failed: " + e.getMessage());
        }
    }

    @Override
    public boolean add(AuthData item) throws DataAccessException {
        try (Connection conn = ChessDatabaseManager.getConnection();
             var insertStatement = conn.prepareStatement(
                     "INSERT INTO auth_data (auth_token, username) VALUES(?, ?)")) {
            insertStatement.setString(1, item.authToken());
            insertStatement.setString(2, item.username());

            insertStatement.executeUpdate();
            return true;
        }catch (SQLException e) {
            if(e.getErrorCode()==1062){
                return false;
            }
            throw new DataAccessException("Error: user database insert failed");
        }
    }

    @Override
    public AuthData get(String authToken) throws DataAccessException {
        try (Connection conn = ChessDatabaseManager.getConnection();
             var queryStatement = conn.prepareStatement(
                     "SELECT auth_token, username FROM auth_data WHERE auth_token=?")) {
            queryStatement.setString(1, authToken);
            var results = queryStatement.executeQuery();
            if (!results.next()){
                return null;
            }
            return new AuthData(results.getString("auth_token"), results.getString("username"));
        }catch (SQLException e) {
            throw new DataAccessException("Error: user database select failed");
        }
    }

    @Override
    public boolean delete(String authToken) throws DataAccessException {
        try (Connection conn = ChessDatabaseManager.getConnection();
             var deleteStatement = conn.prepareStatement("DELETE FROM auth_data WHERE auth_token=?")) {
            deleteStatement.setString(1, authToken);
            deleteStatement.executeUpdate();
            return true;
        }catch (SQLException e) {
            throw new DataAccessException("Error: user database delete failed");
        }
    }
}
