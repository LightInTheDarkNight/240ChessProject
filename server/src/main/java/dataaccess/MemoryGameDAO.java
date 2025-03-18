package dataaccess;

import chess.ChessGame;
import model.GameData;
import server.Server.AlreadyTakenException;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> gameDataList = new HashMap<>();
    private static int nextGameID = 1;

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
        if (old != null) {
            throw new AlreadyTakenException();
        }
        switch (color) {
            case WHITE -> gameDataList.put(game.gameID(), game.setWhitePlayer(newUsername));
            case BLACK -> gameDataList.put(game.gameID(), game.setBlackPlayer(newUsername));
        }
        return switch (color) {
            case WHITE -> gameDataList.get(gameID).whiteUsername().equals(newUsername);
            case BLACK -> gameDataList.get(gameID).blackUsername().equals(newUsername);
        };
    }
}
