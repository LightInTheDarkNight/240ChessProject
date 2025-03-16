package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO extends DAO<GameData, Integer>{
    /**
     * Retrieves a list of all games currently stored in the attached database.
     *
     * @return a Collection of all the games in the database.
     */
    Collection<GameData> getGameList() throws DataAccessException;

    @Override
    default boolean delete(GameData game) throws DataAccessException {
        return game == null || delete(game.gameID());
    }

    @Override
    default GameData get(GameData game) throws DataAccessException {
        return game == null? null : get(game.gameID());
    }

    /**
     * Deletes the old game with newGame's ID, and places newGame into the database instead.
     *
     * @param newGame the new GameData to place in the database.
     * @return true if retrieving the game at newGame's ID yields a GameData object equal to newGame.
     */
    default boolean updateGame(GameData newGame) throws DataAccessException {
        boolean success = delete(newGame.gameID());
        success = success && add(newGame);
        return success && get(newGame.gameID()).equals(newGame);
    }
}
