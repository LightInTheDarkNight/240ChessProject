package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import server.Server.AlreadyTakenException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class DBGameDAO implements GameDAO {
    private static final Gson serializer = new Gson();

    static {
        try {
            DatabaseManager.createDatabase();
            try (Connection conn = DatabaseManager.getConnection()) {
                var createUserTable = conn.prepareStatement("""
                        
                           CREATE TABLE IF NOT EXISTS `game_data` (
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
            throw new RuntimeException("Game table creation and initialization failed: " + e.getMessage());
        }
    }

    public boolean clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             var deleteStatement = conn.prepareStatement("TRUNCATE TABLE game_data")) {
            deleteStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Error: game database clear failed: " + e.getMessage());
        }
    }

    public boolean add(GameData data) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             var insertStatement = conn.prepareStatement(
                     "INSERT INTO game_data (gameid, white_username, black_username, game_name, game) " +
                             "VALUES(?, ?, ?, ?, ?)",
                     RETURN_GENERATED_KEYS)) {
            insertStatement.setInt(1, data.gameID());
            insertStatement.setString(2, data.whiteUsername());
            insertStatement.setString(3, data.blackUsername());
            insertStatement.setString(4, data.gameName());
            insertStatement.setString(5, serializer.toJson(data.game()));

            insertStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                return false;
            }
            throw new DataAccessException("Error: game database insert failed");
        }
    }

    public int newGame(GameData data) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             var insertStatement = conn.prepareStatement(
                     "INSERT INTO game_data (white_username, black_username, game_name, game) " +
                             "VALUES(?, ?, ?, ?)",
                     RETURN_GENERATED_KEYS)) {
            insertStatement.setString(1, data.whiteUsername());
            insertStatement.setString(2, data.blackUsername());
            insertStatement.setString(3, data.gameName());
            insertStatement.setString(4, serializer.toJson(data.game()));

            insertStatement.executeUpdate();
            var resultSet = insertStatement.getGeneratedKeys();
            var ID = 0;
            if (resultSet.next()) {
                ID = resultSet.getInt(1);
            }
            return ID;
        } catch (SQLException e) {
            throw new DataAccessException("Error: game database insert failed");
        }
    }

    @Override
    public GameData get(Integer gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             var queryStatement = conn.prepareStatement(
                     "SELECT gameid, white_username, black_username, game_name, game FROM game_data WHERE gameid=?")) {
            queryStatement.setInt(1, gameID);
            var results = queryStatement.executeQuery();
            if (!results.next()) {
                return null;
            }
            return new GameData(results.getInt("gameid"), results.getString("white_username"),
                    results.getString("black_username"), results.getString("game_name"),
                    serializer.fromJson(results.getString("game"), ChessGame.class));
        } catch (SQLException e) {
            throw new DataAccessException("Error: game database select failed");
        }
    }

    @Override
    public boolean delete(Integer gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             var deleteStatement = conn.prepareStatement("DELETE FROM game_data WHERE gameid=?")) {
            deleteStatement.setInt(1, gameID);
            deleteStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Error: game database delete failed");
        }
    }

    @Override
    public Collection<GameData> getGameList() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             var listStatement = conn.prepareStatement("SELECT * FROM game_data")) {
            var results = listStatement.executeQuery();

            while (results.next()) {
                games.add(new GameData(results.getInt("gameid"), results.getString("white_username"),
                        results.getString("black_username"), results.getString("game_name"),
                        serializer.fromJson(results.getString("game"), ChessGame.class)));
            }

            return games;
        } catch (SQLException e) {
            throw new DataAccessException("Error: game database select failed");
        }
    }

    @Override
    public boolean updateUsername(Integer gameID, ChessGame.TeamColor color, String newUsername)
            throws AlreadyTakenException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {

            String columnName = switch (color) {
                case WHITE -> "white_username";
                case BLACK -> "black_username";
            };
            var queryStatement = conn.prepareStatement("SELECT " + columnName + " FROM game_data WHERE gameid=?");
            var updateStatement = conn.prepareStatement("UPDATE game_data SET " + columnName + "=? WHERE gameid=?");
            queryStatement.setInt(1, gameID);

            var results = queryStatement.executeQuery();
            if (!results.next()) {
                throw new RuntimeException("Game not in database");
            }
            if (results.getString(1) != null) {
                throw new AlreadyTakenException();
            }

            updateStatement.setString(1, newUsername);
            updateStatement.setInt(2, gameID);

            updateStatement.executeUpdate();

            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Error: game database select failed");
        }
    }
}