package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.SQLException;

public class DBUserDAO implements UserDAO{
    static {
        try{
            ChessDatabaseManager.createDatabase();
            try (Connection conn = ChessDatabaseManager.getConnection()){
                var createUserTable = conn.prepareStatement("""
                        CREATE TABLE IF NOT EXISTS `users` (
                          `username` varchar(64) NOT NULL,
                          `email` varchar(64) NOT NULL,
                          `password` varchar(64) NOT NULL,
                          PRIMARY KEY (`username`)
                        )""");
                createUserTable.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException("Error: User table creation and initialization failed.");
        }
    }

    public boolean clear() throws DataAccessException {
        try (Connection conn = ChessDatabaseManager.getConnection();
             var deleteStatement = conn.prepareStatement("DELETE FROM users")) {
            deleteStatement.executeUpdate();
            return true;
        }catch (SQLException e) {
            throw new DataAccessException("Error: user database clear failed: " + e.getMessage());
        }
    }

    public boolean add(UserData user) throws DataAccessException {
        try (Connection conn = ChessDatabaseManager.getConnection();
             var insertStatement = conn.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES(?, ?, ?)")) {
            insertStatement.setString(1, user.username());
            insertStatement.setString(2, user.email());
            insertStatement.setString(3, user.password());

            insertStatement.executeUpdate();
            return true;
        }catch (SQLException e) {
            if(e.getErrorCode()==1062){
                return false;
            }
            throw new DataAccessException("Error: user database insert failed");
        }
    }

    public UserData get(String username) throws DataAccessException {
        try (Connection conn = ChessDatabaseManager.getConnection();
             var queryStatement = conn.prepareStatement(
                     "SELECT username, password, email FROM users WHERE username=?")) {
            queryStatement.setString(1, username);
            var results = queryStatement.executeQuery();
            if (!results.next()){
                return null;
            }
            return new UserData(results.getString("username"), results.getString("password"),
                    results.getString("email"));
//            var resultSet = preparedStatement.getGeneratedKeys();
//            var ID = 0;
//            if (resultSet.next()) {
//                ID = resultSet.getInt(1);
//            }
        }catch (SQLException e) {
            throw new DataAccessException("Error: user database select failed");
        }
    }

    public boolean delete(String username) throws DataAccessException {
        try (Connection conn = ChessDatabaseManager.getConnection();
             var deleteStatement = conn.prepareStatement("DELETE FROM users WHERE username=?")) {
            deleteStatement.setString(1, username);
            deleteStatement.executeUpdate();
            return true;
        }catch (SQLException e) {
            throw new DataAccessException("Error: user database delete failed");
        }
    }
}
