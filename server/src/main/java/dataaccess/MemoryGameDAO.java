package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> gameDataList = new HashMap<>();

    @Override
    public boolean clear() {
        gameDataList.clear();
        return gameDataList.isEmpty();
    }

    @Override
    public boolean add(GameData game) {
        GameData old = gameDataList.putIfAbsent(game.gameID(), game);
        return old == null && gameDataList.get(game.gameID()).equals(game);
    }

    @Override
    public GameData get(Integer gameID) {
        return gameDataList.get(gameID);
    }

    @Override
    public Collection<GameData> getGameList() {
        return gameDataList.values().stream().toList();
    }

    @Override
    public boolean delete(Integer gameID) {
        gameDataList.remove(gameID);
        return gameDataList.get(gameID) == null;
    }
}
