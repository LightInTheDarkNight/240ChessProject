package dataaccess;

import chess.ChessGame;
import model.GameData;
import server.Server.AlreadyTakenException;

import java.util.Collection;

public interface GameDAO extends DAO<GameData, Integer>{
    /**
     * Retrieves a list of all games currently stored in the attached database.
     *
     * @return a Collection of all the games in the database.
     */
    Collection<GameData> getGameList() throws DataAccessException;

    int newGame(GameData newGame) throws DataAccessException;

    @Override
    default boolean delete(GameData game) throws DataAccessException {
        return game == null || delete(game.gameID());
    }

    @Override
    default GameData get(GameData game) throws DataAccessException {
        return game == null? null : get(game.gameID());
    }

    /**
     * Updates the game with the given ID so the given team's username is set to the new one. Throws a Runtime Exception
     * if the game is not in the database.
     *
     *
     * @param gameID the id of the GameData to edit.
     * @param color the TeamColor of the username to update.
     * @param newUsername the username to update the GameData with.
     * @return true if retrieving the game at newGame's ID yields a GameData object with a username at the correct place
     * equal to the provided one.
     */
    boolean updateUsername(Integer gameID, ChessGame.TeamColor color, String newUsername)
            throws AlreadyTakenException, DataAccessException;
}
