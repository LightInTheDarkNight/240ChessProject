package service;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import dataaccess.GameDAO;
import model.GameData;
import server.Server.AlreadyTakenException;

import java.util.Collection;

public class GameService {
    private final GameDAO games;
    private static int nextGameID = 0;

    public GameService(GameDAO games) {
        this.games = games;
    }

    public CreateGameResponse createGame(CreateGameRequest game) {
        games.addGame(new GameData(nextGameID, null, null, game.gameName, new ChessGame()));
        return new CreateGameResponse(nextGameID++);
    }

    public record CreateGameRequest(String gameName) {
    }

    public record CreateGameResponse(int gameID) {
    }

    public GameData getGame(int id) {
        return games.getGameByID(id);
    }

    public boolean joinGame(String username, JoinGameRequest req) throws AlreadyTakenException {
        if (username == null || req.playerColor == null) {
            throw new RuntimeException("username or color were null");
        }

        GameData original = getGame(req.gameID);
        if (original == null) {
            throw new RuntimeException("game not in database");
        }

        GameData out;

        if (req.playerColor() == TeamColor.WHITE && original.whiteUsername() == null) {
            out = original.setWhitePlayer(username);
        } else if (req.playerColor() == TeamColor.BLACK && original.blackUsername() == null) {
            out = original.setBlackPlayer(username);
        } else {
            throw new AlreadyTakenException();
        }
        return games.updateGame(out);
    }

    public record JoinGameRequest(TeamColor playerColor, int gameID) {
    }

    public Collection<GameData> listGames() {
        return games.getGameList();
    }
}
