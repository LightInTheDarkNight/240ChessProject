package service;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import server.Server.AlreadyTakenException;

import java.util.Collection;

public class GameService {
    private final GameDAO games;

    public GameService(GameDAO games) {
        this.games = games;
    }

    public CreateGameResponse createGame(CreateGameRequest game) throws DataAccessException {
        int id = games.newGame(new GameData(0, null, null, game.gameName, new ChessGame()));
        if (id != 0) {
            return new CreateGameResponse(id);
        } else {
            return null;
        }
    }

    public record CreateGameRequest(String gameName) {
    }

    public record CreateGameResponse(int gameID) {
    }

    public GameData getGame(int id) throws DataAccessException {
        return games.get(id);
    }

    public boolean joinGame(String username, JoinGameRequest req) throws AlreadyTakenException, DataAccessException {
        if (username == null || req.playerColor == null) {
            throw new RuntimeException("username or color were null");
        }

        return games.updateUsername(req.gameID, req.playerColor, username);
    }

    public record JoinGameRequest(TeamColor playerColor, int gameID) {
    }

    public Collection<GameData> listGames() throws DataAccessException {
        return games.getGameList();
    }
}
