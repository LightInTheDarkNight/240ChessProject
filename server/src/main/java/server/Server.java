package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.UserData;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.Map;

import static service.GameService.CreateGameRequest;
import static service.GameService.JoinGameRequest;

public class Server {

    private static final ClearService CLEAR_SERVICE;
    private static final UserService USER_SERVICE;
    private static final GameService GAME_SERVICE;

    static {
        UserDAO userDAO = new MemoryUserDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        CLEAR_SERVICE = new ClearService(userDAO, gameDAO, authDAO);
        USER_SERVICE = new UserService(userDAO, authDAO);
        GAME_SERVICE = new GameService(gameDAO);
    }

    private static final Gson SERIALIZER = new Gson();
    private static final String JSON = "application/json";
    private static final String EMPTY = "{}";
    private static final String AUTH = "Authorization";

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.exception(WebException.class, this::errorHandler);
        Spark.exception(Exception.class, this::errorHandler);

        Spark.post("/user", Server::registerHandler);
        Spark.post("/session", Server::loginHandler);
        Spark.delete("/session", Server::logoutHandler);

        Spark.post("/game", Server::createGameHandler);
        Spark.put("/game", Server::joinGameHandler);
        Spark.get("/game", Server::listGamesHandler);

        Spark.delete("/db", Server::clearHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }


    private static Object registerHandler(Request req, Response res) throws WebException {
        UserData toCreate = SERIALIZER.fromJson(req.body(), UserData.class);
        if (toCreate.username() == null || toCreate.password() == null || toCreate.email() == null) {
            throw new BadRequestException();
        }
        AuthData auth = USER_SERVICE.register(toCreate);
        return successHandler(res, SERIALIZER.toJson(auth));
    }

    private static Object loginHandler(Request req, Response res) throws UnauthorizedRequestException {
        UserData user = SERIALIZER.fromJson(req.body(), UserData.class);
        AuthData auth = USER_SERVICE.login(user);
        return successHandler(res, SERIALIZER.toJson(auth));
    }

    private static Object logoutHandler(Request req, Response res) throws UnauthorizedRequestException {
        String authToken = req.headers(AUTH);
        boolean success = USER_SERVICE.logout(authToken);
        if (success) {
            return successHandler(res, EMPTY);
        }
        throw new RuntimeException("Error: failed to delete authentication from database");
    }


    private static Object createGameHandler(Request req, Response res) throws UnauthorizedRequestException {
        USER_SERVICE.authenticate(req.headers(AUTH));
        var out = GAME_SERVICE.createGame(SERIALIZER.fromJson(req.body(), CreateGameRequest.class));
        return successHandler(res, SERIALIZER.toJson(out));
    }

    private static Object joinGameHandler(Request req, Response res) throws WebException {
        String username = USER_SERVICE.getUsername(req.headers(AUTH));
        GameService.JoinGameRequest joinReq = SERIALIZER.fromJson(req.body(), JoinGameRequest.class);
        boolean success;
        try {
            success = GAME_SERVICE.joinGame(username, joinReq);
            if (success) {
                return successHandler(res, EMPTY);
            }
            throw new WebException("Error: failed to join valid game with valid username and open slot");
        } catch (RuntimeException e) {
            throw new BadRequestException();
        }
    }

    private static Object listGamesHandler(Request req, Response res) throws UnauthorizedRequestException {
        USER_SERVICE.authenticate(req.headers(AUTH));
        return successHandler(res, SERIALIZER.toJson(Map.of("games", GAME_SERVICE.listGames())));
    }


    private static Object clearHandler(Request req, Response res) {
        if (CLEAR_SERVICE.clearAll()) {
            res.type(JSON);
            res.status(200);
        } else {
            throw new RuntimeException("Error: database clear failed");
        }
        return EMPTY;
    }


    private static String successHandler(Response res, String out) {
        res.type(JSON);
        res.status(200);
        return out;
    }

    private void errorHandler(WebException e, Request req, Response res) {
        var body = SERIALIZER.toJson(Map.of("message", e.getMessage()));
        res.type(JSON);
        res.status(e.getStatusCode());
        res.body(body);
    }

    private void errorHandler(Exception e, Request req, Response res) {
        errorHandler(new WebException(e), req, res);
    }


    public static class WebException extends Exception {
        public int getStatusCode() {
            return 500;
        }

        WebException(String message) {
            super(message);
        }

        public WebException(Exception e) {
            super(e.getMessage());
        }
    }

    public static class UnauthorizedRequestException extends WebException {
        public int getStatusCode() {
            return 401;
        }

        public UnauthorizedRequestException() {
            super("Error: unauthorized");
        }
    }

    public static class AlreadyTakenException extends WebException {
        public int getStatusCode() {
            return 403;
        }

        public AlreadyTakenException() {
            super("Error: already taken");
        }
    }

    private static class BadRequestException extends WebException {
        public int getStatusCode() {
            return 400;
        }

        public BadRequestException() {
            super("Error: bad request");
        }
    }

}
