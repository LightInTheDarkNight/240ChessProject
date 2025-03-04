package server;

import com.google.gson.Gson;
import dataaccess.*;
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

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.exception(WebException.class, this::errorHandler);
        Spark.exception(Exception.class, this::errorHandler);
        Spark.delete("/db", (req, res) -> {
            if(clearService.clearAll()){
                res.type("application/json");
                res.status(200);
            }
            else{
                throw new RuntimeException("Error: database clear failed");
            }
            return "{}";
        } );

        Spark.awaitInitialization();
        return Spark.port();
    }



    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void errorHandler(WebException e, Request req, Response res){
        var body = new Gson().toJson(Map.of("message", e.getMessage()));
        res.type("application/json");
        res.status(e.getStatusCode());
        res.body(body);
    }

    private void errorHandler(Exception e, Request req, Response res){
        errorHandler(new WebException(e), req, res);
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
