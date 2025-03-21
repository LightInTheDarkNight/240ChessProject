package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;

import java.util.Collection;
import java.util.HashSet;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GameServiceTest {
    private static final GameDAO GAME_LIST = new MemoryGameDAO();
    private static GameService service = new GameService(GAME_LIST);
    private static final String[] A_FEW_NAMES = {"jackhammer", "johnathan", "coffee cup", "Jackson 5", "SQL sucks"};

    @BeforeEach
    void setup() throws DataAccessException {
        assert GAME_LIST.clear();
        service = new GameService(GAME_LIST);
    }

    private static HashSet<Integer> populateGames(String[] names) throws DataAccessException {
        HashSet<Integer> idResults = new HashSet<>();
        for (var name : names) {
            GameService.CreateGameResponse response = service.createGame(new GameService.CreateGameRequest(name));
            boolean success = idResults.add(response.gameID());
            assert success;
        }
        return idResults;
    }


    @Test
    void createGameTest() throws DataAccessException {
        boolean success = service.createGame(new GameService.CreateGameRequest(A_FEW_NAMES[0])) != null;
        assert success;
    }

    @Test
    void listGamesTest() throws DataAccessException {
        HashSet<Integer> gameIDs = populateGames(A_FEW_NAMES);
        Collection<GameData> games = service.listGames();
        assert gameIDs.size() == A_FEW_NAMES.length;
        assert games.size() == gameIDs.size();
    }

    @Test
    void emptyListGamesTest() throws DataAccessException {
        assert service.listGames().isEmpty();
    }

    @Test
    void getGameTest() throws DataAccessException {
        HashSet<Integer> gameIDs = populateGames(A_FEW_NAMES);
        for (int id : gameIDs) {
            assert service.getGame(id) != null;
        }
    }

    @Test
    void invalidGetGameTest() throws DataAccessException {
        int invalidID = RandomGenerator.getDefault().nextInt();
        assert service.getGame(invalidID) == null;
    }

    @Test
    void joinGameTest() throws DataAccessException {
        int id = service.createGame(new GameService.CreateGameRequest("test")).gameID();
        try {
            service.joinGame("JohnCena", new GameService.JoinGameRequest(ChessGame.TeamColor.BLACK, id));
            service.joinGame("Johan", new GameService.JoinGameRequest(ChessGame.TeamColor.WHITE, id));
        } catch (Server.AlreadyTakenException e) {
            throw new RuntimeException("Something got really screwed up");
        }
        GameData game = service.getGame(id);
        assert game.blackUsername().equals("JohnCena");
        assert game.whiteUsername().equals("Johan");
    }

    @Test
    void negativeJoinGameTest() throws DataAccessException {
        assertThrows(RuntimeException.class, () ->
                service.joinGame("JohnCena", new GameService.JoinGameRequest(ChessGame.TeamColor.BLACK, 5)));
        int id = service.createGame(new GameService.CreateGameRequest("test")).gameID();
        try {
            service.joinGame("JohnCena", new GameService.JoinGameRequest(ChessGame.TeamColor.BLACK, id));
            service.joinGame("Johan", new GameService.JoinGameRequest(ChessGame.TeamColor.WHITE, id));
        } catch (Server.AlreadyTakenException e) {
            throw new RuntimeException("Something got really screwed up");
        }
        assertThrows(Server.AlreadyTakenException.class, () ->
                service.joinGame("JohnCena", new GameService.JoinGameRequest(ChessGame.TeamColor.BLACK, id)));
        assertThrows(Server.AlreadyTakenException.class, () ->
                service.joinGame("JohnCena", new GameService.JoinGameRequest(ChessGame.TeamColor.WHITE, id)));
    }
}