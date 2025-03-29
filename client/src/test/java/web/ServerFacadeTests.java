package web;

import chess.ChessGame;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import service.ClearService;
import service.GameService;

import java.lang.reflect.Field;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ServerFacadeTests {

    private static Server server;
    private static String URL;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        URL = "http://localhost:" + port;
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setUp(){
        facade = new ServerFacade(URL);
    }

    @AfterEach
    public void cleanUp() throws NoSuchFieldException, IllegalAccessException, DataAccessException {
        //reset server contents
        Field clearService = server.getClass().getDeclaredField("CLEAR_SERVICE");
        clearService.setAccessible(true);
        ((ClearService) clearService.get(server)).clearAll();
    }

    @Test
    public void registerSuccess() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");
        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
        });
    }

    @Test
    public void registerFail() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");

        assertDoesNotThrow(() -> facade.register(user));
        assertThrows(ResponseException.class, () -> facade.register(user));
    }

    @Test
    public void logoutSuccess() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");
        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
            facade.logout(response.authToken());
        });
    }

    @Test
    public void logoutFail() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");

        //never valid
        assertThrows(ResponseException.class, () -> facade.logout(""));

        assertThrows(ResponseException.class, () -> facade.logout("Fake"));

        //previously valid
        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
            facade.logout(response.authToken());
            assertThrows(ResponseException.class, () -> facade.logout(response.authToken()));
        });
    }

    @Test
    public void loginSuccess() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");
        UserData withoutEmail = new UserData("Jethro", "password", null);
        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
            facade.logout(response.authToken());
            response = facade.login(user);
            assert response != null;
            facade.logout(response.authToken());
            response = facade.login(withoutEmail);
            assert response != null;
            facade.logout(response.authToken());
        });
    }

    @Test
    public void loginFail() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");
        UserData missingPassword = new UserData("Jethro", null, null);
        UserData emptyPassword = new UserData("Jethro", "", null);
        UserData badUser = new UserData("Jethro", "imposter", null);

        //Before registration
        assertThrows(ResponseException.class, () -> facade.login(user));

        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
            facade.logout(response.authToken());
        });

        //Wrong Password
        assertThrows(ResponseException.class, () -> facade.login(badUser));
        //Missing Password
        assertThrows(ResponseException.class, () -> facade.login(missingPassword));
        //Empty Password
        assertThrows(ResponseException.class, () -> facade.login(emptyPassword));
    }

    @Test
    public void createGameSuccess() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");
        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
            int id = facade.createGame(response.authToken(), "newGame");
            assert id > 0;
            testForGamePresence(id);
        });
    }

    @Test
    public void createGameFail() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");

        //never valid
        assertThrows(ResponseException.class, () -> facade.createGame("", "doesn't work"));

        assertThrows(ResponseException.class, () -> facade.createGame("fake", "doesn't work"));


        //previously valid
        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
            facade.logout(response.authToken());
            assertThrows(ResponseException.class, () -> facade.createGame(response.authToken(), "Unauthorized"));
        });
    }

    @Test
    public void listGamesSuccess(){
        UserData user = new UserData("John Lock", "Rousseau", "you thought");
        String[] gameNames = new String[]{"Game1", "Game2", "Game3", "Game4", "Game5"};
        assertDoesNotThrow(() -> {
            String token = facade.register(user).authToken();
            for(String name:gameNames){
                facade.createGame(token, name);
            }
            Collection<GameData> gameList = facade.listGames(token);
            assert gameList != null;
            assert gameList.size() == gameNames.length;
            for(String name:gameNames){
                boolean found = false;
                for(GameData game:gameList){
                    if (game.gameName().equals(name)) {
                        found = true;
                        break;
                    }
                }
                assert found;
            }
        });
    }
    @Test
    public void listGamesThrows(){
        UserData user = new UserData("John Lock", "Rousseau", "you thought");
        String[] gameNames = new String[]{"Game1", "Game2", "Game3", "Game4", "Game5"};
        assertDoesNotThrow(() -> {
            String token = facade.register(user).authToken();
            for(String name:gameNames){
                assertThrows(ResponseException.class, () -> facade.createGame("", name));
            }
            for(String name:gameNames){
                facade.createGame(token, name);
            }
            assertThrows(ResponseException.class, () -> facade.listGames(""));

        });
    }

    @Test
    public void playGameSuccess(){
        UserData user = new UserData("John Lock", "Rousseau", "you thought");
        UserData userB = new UserData("Washington", "USA", "VivaLaVida");
        String[] gameNames = new String[]{"Game1", "Game2", "Game3", "Game4", "Game5"};
        assertDoesNotThrow(() -> {
            String token = facade.register(user).authToken();
            int gameID = facade.createGame(token, "TestGame");
            facade.playGame(token, ChessGame.TeamColor.BLACK, gameID);
            testForGamePlayer(user.username(), ChessGame.TeamColor.BLACK, gameID);

            String tokenB = facade.register(userB).authToken();
            facade.playGame(tokenB, ChessGame.TeamColor.WHITE, gameID);
            testForGamePlayer(userB.username(), ChessGame.TeamColor.WHITE, gameID);

            gameID = facade.createGame(token, "TestGame2");
            facade.playGame(token, ChessGame.TeamColor.WHITE, gameID);
            testForGamePlayer(user.username(), ChessGame.TeamColor.WHITE, gameID);

            for(String name:gameNames){
                gameID = facade.createGame(token, name);
                facade.playGame(token, ChessGame.TeamColor.WHITE, gameID);
                facade.playGame(tokenB, ChessGame.TeamColor.BLACK, gameID);
                testForGamePlayer(user.username(), ChessGame.TeamColor.WHITE, gameID);
                testForGamePlayer(userB.username(), ChessGame.TeamColor.BLACK, gameID);
            }
        });
    }

    @Test
    public void playGameThrows(){
        UserData user = new UserData("John Lock", "Rousseau", "you thought");
        UserData userB = new UserData("Washington", "USA", "VivaLaVida");
        assertDoesNotThrow(() -> {
            String token = facade.register(user).authToken();
            int gameID = facade.createGame(token, "TestGame");
            facade.playGame(token, ChessGame.TeamColor.BLACK, gameID);
            testForGamePlayer(user.username(), ChessGame.TeamColor.BLACK, gameID);

            String tokenB = facade.register(userB).authToken();
            {
                //Unauthorized
                assertThrows(Exception.class, () -> facade.playGame("", ChessGame.TeamColor.WHITE, gameID));
                //No Such Game
                assertThrows(Exception.class, () -> facade.playGame(tokenB, ChessGame.TeamColor.WHITE, gameID *2));
                //No Color provided
                assertThrows(Exception.class, () -> facade.playGame(tokenB, null, gameID));
                //Already Taken
                assertThrows(Exception.class, () -> facade.playGame(tokenB, ChessGame.TeamColor.BLACK, gameID));
            }
        });
    }

    private static void testForGamePresence(int... gameIDs) {
        assertDoesNotThrow(() -> {
            Field gameService = server.getClass().getDeclaredField("GAME_SERVICE");
            gameService.setAccessible(true);
            for(int id:gameIDs){
                assert ((GameService) gameService.get(server)).getGame(id) != null;
            }
        });
    }

    private static void testForGamePlayer(String username, ChessGame.TeamColor color, int gameID) {
        assertDoesNotThrow(() -> {
            Field gameService = server.getClass().getDeclaredField("GAME_SERVICE");
            gameService.setAccessible(true);
            GameData game = ((GameService) gameService.get(server)).getGame(gameID);
            assert game != null;
            assert username.equals(color == ChessGame.TeamColor.WHITE? game.whiteUsername() : game.blackUsername());
        });
    }
}
