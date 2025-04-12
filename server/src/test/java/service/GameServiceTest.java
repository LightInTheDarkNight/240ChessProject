package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import dataaccess.*;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static server.WebException.*;

class GameServiceTest {
    private static final GameDAO GAME_LIST = new MemoryGameDAO();
    private static final UserDAO USER_LIST = new MemoryUserDAO();
    private static final AuthDAO AUTH_LIST = new MemoryAuthDAO();
    private static final boolean testingWithDatabase = GAME_LIST.getClass().equals(DBGameDAO.class);
    private static final UserService users = new UserService(USER_LIST, AUTH_LIST);
    private static final ClearService clear = new ClearService(USER_LIST, GAME_LIST, AUTH_LIST);
    private static GameService service = new GameService(GAME_LIST);
    private static final String[] A_FEW_NAMES = {"jackhammer", "johnathan", "coffee cup", "Jackson 5", "SQL sucks"};

    @BeforeAll
    static void primary() {
        for(String name: A_FEW_NAMES){
            UserData user = new UserData(name, "password", "nothing");
            assertDoesNotThrow(() -> users.register(user));
        }
    }

    @AfterAll
    static void terminal() {
        assertDoesNotThrow(clear::clearAll);
    }

    @BeforeEach
    void setup() throws DataAccessException {
        assert GAME_LIST.clear();
        service = new GameService(GAME_LIST);
    }

    private static List<Integer> populateGames() throws DataAccessException {
        List<Integer> idResults = new ArrayList<>();
        for (var name : GameServiceTest.A_FEW_NAMES) {
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
        List<Integer> gameIDs = populateGames();
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
        List<Integer> gameIDs = populateGames();
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
            service.joinGame(A_FEW_NAMES[0], new GameService.JoinGameRequest(ChessGame.TeamColor.BLACK, id));
            service.joinGame(A_FEW_NAMES[1], new GameService.JoinGameRequest(ChessGame.TeamColor.WHITE, id));
        } catch (AlreadyTakenException e) {
            throw new RuntimeException("Something got really screwed up");
        }
        GameData game = service.getGame(id);
        assert game.blackUsername().equals(A_FEW_NAMES[0]);
        assert game.whiteUsername().equals(A_FEW_NAMES[1]);
    }

    @Test
    void negativeJoinGameTest() throws DataAccessException {
        assertThrows(RuntimeException.class, () ->
                service.joinGame(A_FEW_NAMES[0], new GameService.JoinGameRequest(ChessGame.TeamColor.BLACK, 5)));
        int id = service.createGame(new GameService.CreateGameRequest("test")).gameID();
        try {
            service.joinGame(A_FEW_NAMES[0], new GameService.JoinGameRequest(ChessGame.TeamColor.BLACK, id));
            service.joinGame(A_FEW_NAMES[1], new GameService.JoinGameRequest(ChessGame.TeamColor.WHITE, id));
        } catch (AlreadyTakenException e) {
            throw new RuntimeException("Something got really screwed up");
        }
        assertThrows(AlreadyTakenException.class, () ->
                service.joinGame(A_FEW_NAMES[0], new GameService.JoinGameRequest(ChessGame.TeamColor.BLACK, id)));
        assertThrows(AlreadyTakenException.class, () ->
                service.joinGame(A_FEW_NAMES[0], new GameService.JoinGameRequest(ChessGame.TeamColor.WHITE, id)));
    }

    @Test
    void updateGameTest() throws DataAccessException {
        List<Integer> gameIDs = populateGames();
        ChessMove firstMove = new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1));
        ChessGame before = new ChessGame();
        ChessGame after = new ChessGame();
        assertDoesNotThrow(()->after.makeMove(firstMove));

        for (int id : gameIDs) {
            GameData current = service.getGame(id);
            assert current.game().equals(before);

            assertDoesNotThrow(()->current.game().makeMove(firstMove));

            assert current.game().equals(after);
            if(testingWithDatabase){

                assert !(current.equals(service.getGame(id)));
                assert !(current.game().equals(service.getGame(id).game()));
            }

            assertDoesNotThrow(() -> service.updateGame(id, current.game()));

            assert current.equals(service.getGame(id));
            assert current.game().equals(service.getGame(id).game());
        }
    }

    @Test
    void updateGameThrows() throws DataAccessException {
        int invalidID = populateGames().size() + 1;
        ChessMove firstMove = new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1));
        ChessGame after = new ChessGame();
        assertDoesNotThrow(()->after.makeMove(firstMove));
        if(testingWithDatabase){
            assertThrows(DataAccessException.class, () -> service.updateGame(invalidID, after));
        } else{
            assertThrows(RuntimeException.class, () -> service.updateGame(invalidID, after));
        }

    }

    @Test
    void leaveGameTest() throws DataAccessException {
        int id = service.createGame(new GameService.CreateGameRequest("test")).gameID();
        try {
            service.joinGame(A_FEW_NAMES[0], new GameService.JoinGameRequest(ChessGame.TeamColor.BLACK, id));
            service.joinGame(A_FEW_NAMES[1], new GameService.JoinGameRequest(ChessGame.TeamColor.WHITE, id));
        } catch (AlreadyTakenException e) {
            throw new RuntimeException("Something got really screwed up");
        }
        assertDoesNotThrow(() -> service.leaveGame(id, ChessGame.TeamColor.BLACK));
        assert service.getGame(id).blackUsername() == null;
        assertDoesNotThrow(() -> service.leaveGame(id, ChessGame.TeamColor.WHITE));
        assert service.getGame(id).whiteUsername() == null;
    }

    @Test
    void leaveGameThrows() throws DataAccessException {
        int id = service.createGame(new GameService.CreateGameRequest("test")).gameID();
        Class<? extends Exception> exceptionClass = testingWithDatabase? DataAccessException.class :
                RuntimeException.class;
        //invalid id
        assertThrows(exceptionClass, () -> service.leaveGame(id + 1, ChessGame.TeamColor.BLACK));
        assertThrows(exceptionClass, () -> service.leaveGame(id + 1, ChessGame.TeamColor.WHITE));
        //never joined
        assertThrows(RuntimeException.class, () -> service.leaveGame(id, ChessGame.TeamColor.BLACK));
        assertThrows(RuntimeException.class, () -> service.leaveGame(id, ChessGame.TeamColor.WHITE));
    }
}