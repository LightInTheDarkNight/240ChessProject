package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import service.*;
import spark.*;
import static service.GameService.*;

import java.util.Map;

public class Server {
    private static final UserDAO userDAO = new MemoryUserDAO();
    private static final GameDAO gameDAO = new MemoryGameDAO();
    private static final AuthDAO authDAO = new MemoryAuthDAO();
    private static final ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);
    private static final UserService userService = new UserService(userDAO, authDAO);
    private static final GameService gameService = new GameService(gameDAO);
    private static final Gson serializer = new Gson();
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


    private static Object registerHandler(Request req, Response res) throws AlreadyTakenException {
        UserData toCreate = serializer.fromJson(req.body(), UserData.class);
        AuthData auth = userService.register(toCreate);
        return successHandler(res, serializer.toJson(auth));
    }

    private static Object loginHandler(Request req, Response res) throws UnauthorizedRequestException {
        UserData user = serializer.fromJson(req.body(), UserData.class);
        AuthData auth = userService.login(user);
        return successHandler(res, serializer.toJson(auth));
    }

    private static Object logoutHandler(Request req, Response res) throws UnauthorizedRequestException {
        String authToken = req.headers(AUTH);
        boolean success = userService.logout(authToken);
        if(success){
            return successHandler(res, EMPTY);
        }
        throw new RuntimeException("Error: failed to delete authentication from database");
    }


    private static Object createGameHandler(Request req, Response res) throws UnauthorizedRequestException {
        userService.authenticate(req.headers(AUTH));
        var out = gameService.createGame(serializer.fromJson(req.body(), CreateGameRequest.class));
        return successHandler(res, serializer.toJson(out));
    }

    private static Object joinGameHandler(Request req, Response res) throws WebException {
        String username = userService.getUsername(req.headers(AUTH));
        GameService.JoinGameRequest joinReq = serializer.fromJson(req.body(), JoinGameRequest.class);
        boolean success;
        try {
            success = gameService.joinGame(username, joinReq);
            if(success){
                return successHandler(res, EMPTY);
            }
            throw new WebException("Error: failed to join valid game with valid username and open slot");
        } catch (RuntimeException e){
            throw new BadRequestException();
        }
    }

    private static Object listGamesHandler(Request req, Response res) throws UnauthorizedRequestException {
        userService.authenticate(req.headers(AUTH));
        return successHandler(res, serializer.toJson(Map.of("games", gameService.listGames())));
    }


    private static Object clearHandler(Request req, Response res){
        if(clearService.clearAll()){
            res.type(JSON);
            res.status(200);
        }
        else{
            throw new RuntimeException("Error: database clear failed");
        }
        return EMPTY;
    }


    private static String successHandler(Response res, String out){
        res.type(JSON);
        res.status(200);
        return out;
    }

    private void errorHandler(WebException e, Request req, Response res){
        var body = serializer.toJson(Map.of("message", e.getMessage()));
        res.type(JSON);
        res.status(e.getStatusCode());
        res.body(body);
    }

    private void errorHandler(Exception e, Request req, Response res){
        errorHandler(new WebException(e), req, res);
    }


    public static class WebException extends Exception{
        public int getStatusCode(){
            return 500;
        }
        WebException(String message){
            super(message);
        }
        public WebException(Exception e){
            super(e.getMessage());
        }
    }

    public static class UnauthorizedRequestException extends WebException {
        public int getStatusCode(){
            return 401;
        }
        public UnauthorizedRequestException(){
            super("Error: unauthorized");
        }
    }

    public static class AlreadyTakenException extends WebException {
        public int getStatusCode(){
            return 403;
        }
        public AlreadyTakenException(){
            super("Error: already taken");
        }
    }

    private static class BadRequestException extends WebException {
        public int getStatusCode(){
            return 400;
        }
        public BadRequestException(){
            super("Error: bad request");
        }
    }

}
