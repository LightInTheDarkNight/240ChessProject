package service;

import chess.ChessGame;
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
    void setup() {
        assert GAME_LIST.clear();
        service = new GameService(GAME_LIST);
    }

    private static HashSet<Integer> populateGames(String[] names) {
        HashSet<Integer> idResults = new HashSet<>();
        for (var name : names) {
            GameService.CreateGameResponse response = service.createGame(new GameService.CreateGameRequest(name));
            boolean success = idResults.add(response.gameID());
            assert success;
        }
        return idResults;
    }


    @Test
    void createGameTest() {
        HashSet<Integer> results = populateGames(A_FEW_NAMES);
        assert A_FEW_NAMES.length == results.size();
    }

    @Test
    void listGameTest() {
        HashSet<Integer> gameIDs = populateGames(A_FEW_NAMES);
        Collection<GameData> games = service.listGames();
        assert gameIDs.size() == A_FEW_NAMES.length;
        assert games.size() == gameIDs.size();
    }

    @Test
    void getGameTest() {
        HashSet<Integer> gameIDs = populateGames(A_FEW_NAMES);
        for (int id : gameIDs) {
            assert service.getGame(id) != null;
        }
        int invalidID = RandomGenerator.getDefault().nextInt() + A_FEW_NAMES.length;
        assert service.getGame(invalidID) == null;
    }

    @Test
    void joinGameTest() {
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
        assertThrows(Server.AlreadyTakenException.class, () ->
                service.joinGame("JohnCena", new GameService.JoinGameRequest(ChessGame.TeamColor.BLACK, id)));
        assertThrows(Server.AlreadyTakenException.class, () ->
                service.joinGame("JohnCena", new GameService.JoinGameRequest(ChessGame.TeamColor.WHITE, id)));
    }
}