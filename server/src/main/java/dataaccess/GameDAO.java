package dataaccess;

import model.GameData;
import java.util.Collection;

public interface GameDAO {
    /**
     * Removes all data from the attached database.
     * @return true if successful, false otherwise.
     */
    boolean clear();

    /**
     * Adds the given game to the attached database.
     * @param game the game to add to the attached database.
     */
    void addGame(GameData game);

    /**
     * Retrieves a list of all games currently stored in the attached database.
     * @return a Collection of all the games in the database.
     */
    Collection<GameData> getGameList();

    /**
     * Retrieves the GameData object associated with the passed in gameID.
     * @param gameID the ID of the GameData to retrieve.
     * @return the GameData retrieved from the database.
     */
    GameData getGameByID(int gameID);

    /**
     * Removes the game with the given ID from the database.
     * @param gameID the ID of the GameData object to remove.
     * @return true if delete was successful; false otherwise.
     */
    boolean deleteGameByID(int gameID);

    /**
     * Deletes the old game with newGame's ID, and places newGame into the database instead.
     * @param newGame the new GameData to place in the database.
     * @return true if retrieving the game at newGame's ID yields a GameData object equal to newGame.
     */
    default boolean updateGame(GameData newGame){
        deleteGameByID(newGame.gameID());
        addGame(newGame);
        return getGameByID(newGame.gameID()).equals(newGame);
    }

    /**
     * Extracts the gameID from the passed in GameData object, and calls deleteGameByID with it.
     * @param gameData the GameData to extract the id from.
     * @return true if delete was successful; false otherwise.
     */
    default boolean deleteGame(GameData gameData){
        return deleteGameByID(gameData.gameID());
    }
}
