package service;

import chess.ChessGame;
import dataaccess.GameDAO;
import model.GameData;
import server.Server;

import java.util.Collection;

public class GameService {
    private final GameDAO games;
    private static int nextGameID = 0;

    public GameService(GameDAO games){
        this.games = games;
    }

    public CreateGameResponse createGame(CreateGameRequest game){
        games.addGame(new GameData(nextGameID, null, null, game.gameName, new ChessGame()));
        return new CreateGameResponse(nextGameID++);
    }
    public record CreateGameRequest(String gameName){}
    public record CreateGameResponse(int gameID){}

    public GameData getGame(int id){
        return games.getGameByID(id);
    }

    public boolean joinGame(String username, JoinGameRequest req) throws Server.AlreadyTakenException {
        if(username == null){
            throw new RuntimeException("username was null");
        }

        GameData original = getGame(req.gameID);
        if (original == null) {
            throw new RuntimeException("game not in database");
        }

        GameData out;
        String whitePlayer = original.whiteUsername();
        String blackPlayer = original.blackUsername();

        if(req.playerColor() == ChessGame.TeamColor.WHITE && whitePlayer == null){
            out = original.setWhitePlayer(username);
        } else if (req.playerColor() == ChessGame.TeamColor.BLACK && blackPlayer == null) {
            out = original.setBlackPlayer(username);
        } else {
            throw new Server.AlreadyTakenException();
        }
        return games.updateGame(out);
    }
    public record JoinGameRequest(ChessGame.TeamColor playerColor, int gameID){}

    public Collection<GameData> listGames(){
        return games.getGameList();
    }
}
