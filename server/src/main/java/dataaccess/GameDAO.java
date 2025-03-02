package dataaccess;

import model.GameData;
import java.util.Collection;

public interface GameDAO {
    public boolean clear();
    public void addGame(GameData game);
    public Collection<GameData> getGameList();
    public GameData getGameByID(int gameID);
    public boolean deleteGameByID(int gameID);
    public default boolean deleteGame(GameData gameData){
        return deleteGameByID(gameData.gameID());
    }
}
