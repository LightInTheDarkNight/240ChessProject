package dataaccess;

import chess.ChessGame;
import model.GameData;
import static server.WebException.*;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryGameDAO implements GameDAO {
    private final ConcurrentHashMap<Integer, GameData> gameDataList = new ConcurrentHashMap<>();
    private static int nextGameID = 1;

    @Override
    public boolean clear() {
        gameDataList.clear();
        return true;
    }

    @Override
    public boolean add(GameData game) {
        GameData old = gameDataList.putIfAbsent(game.gameID(), game);
        return old == null && gameDataList.get(game.gameID()).equals(game);
    }

    @Override
    public int newGame(GameData game) {
        while (gameDataList.get(nextGameID) != null) {
            nextGameID++;
        }
        GameData newGame = new GameData(nextGameID, game.whiteUsername(), game.blackUsername(), game.gameName(),
                game.game());
        gameDataList.put(nextGameID, newGame);
        nextGameID++;
        return newGame.gameID();
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

    @Override
    public boolean updateUsername(Integer gameID, ChessGame.TeamColor color, String newUsername)
            throws AlreadyTakenException {
        GameData game = gameDataList.get(gameID);
        if (game == null) {
            throw new RuntimeException("Game not in database.");
        }
        String old = switch (color) {
            case WHITE -> game.whiteUsername();
            case BLACK -> game.blackUsername();
        };
        if (old != null && newUsername != null) {
            throw new AlreadyTakenException();
        }
        if (newUsername == null && old == null){
            throw new RuntimeException("No player to leave the game.");
        }
        switch (color) {
            case WHITE -> gameDataList.put(game.gameID(), game.setWhitePlayer(newUsername));
            case BLACK -> gameDataList.put(game.gameID(), game.setBlackPlayer(newUsername));
        }
        String updated;
        return switch (color) {
            case WHITE:
                updated = gameDataList.get(gameID).whiteUsername();
                yield updated == null || updated.equals(newUsername);
            case BLACK:
                updated = gameDataList.get(gameID).blackUsername();
                yield updated == null || updated.equals(newUsername);
        };
    }

    @Override
    public boolean updateGame(Integer gameID, ChessGame game) {
        GameData old = gameDataList.get(gameID);
        if (old == null) {
            throw new RuntimeException("Game not in database.");
        }
        GameData newData = new GameData(gameID, old.whiteUsername(), old.blackUsername(), old.gameName(), game);
        gameDataList.put(gameID, newData);
        return newData.equals(gameDataList.get(gameID));
    }
}
