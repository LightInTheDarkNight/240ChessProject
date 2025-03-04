package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.UserData;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.*;

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

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.exception(WebException.class, this::errorHandler);
        Spark.exception(Exception.class, this::errorHandler);


        Spark.delete("/db", (req, res) -> {
            if(clearService.clearAll()){
                res.type(JSON);
                res.status(200);
            }
            else{
                throw new RuntimeException("Error: database clear failed");
            }
            return EMPTY;
        } );


        Spark.post("/user", (req, res) -> {
            UserData toCreate = serializer.fromJson(req.body(), UserData.class);
            AuthData auth = userService.register(toCreate);
            return successHandler(res, serializer.toJson(auth));
        });

        Spark.post("/session", (req, res) -> {
            UserData user = serializer.fromJson(req.body(), UserData.class);
            AuthData auth = userService.login(user);
            return successHandler(res, serializer.toJson(auth));
        });

        Spark.delete("/session", (req, res) -> {
            String authToken = req.headers("Authorization");
            boolean success = userService.logout(authToken);
            if(success){
                return successHandler(res, EMPTY);
            }
            throw new RuntimeException("Error: failed to delete authentication");
        });


        Spark.post("/game", (req, res) ->{
            userService.authenticate(req.headers("Authorization"));
            var out = gameService.createGame(serializer.fromJson(req.body(), GameService.CreateGameRequest.class));
            return successHandler(res, serializer.toJson(out));
        });

        Spark.awaitInitialization();
        return Spark.port();
    }



    public void stop() {
        Spark.stop();
        Spark.awaitStop();
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
        errorHandler(new WebException(e.getMessage()), req, res);
    }

    public static class WebException extends Exception{
        public int getStatusCode(){
            return 500;
        };
        WebException(String message){
            super(message);
        }
        public WebException(Exception e){
            super(e);
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

    private class BadRequestException extends WebException {
        public int getStatusCode(){
            return 400;
        }
        public BadRequestException(){
            super("Error: bad request");
        }
    }


}
