package server;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static server.Server.GAME_SERVICE;
import static server.Server.USER_SERVICE;
import static server.WebException.*;
import static chess.ChessGame.TeamColor;

public class WebSocketHandler {
    private static final Gson SERIALIZER = new Gson();
    private static final Map<String, GameData> playerGameRetrieval = new ConcurrentHashMap<>();
    private static final Map<String, Session> sessionLookup = new ConcurrentHashMap<>();
    private static final Map<Integer, List<String>> affectedLookup = new HashMap<>();


    @OnWebSocketMessage
    public static void onMessage(Session session, String message) throws Exception {
        UserGameCommand received = SERIALIZER.fromJson(message, UserGameCommand.class);
        String username = "";
        try {
            username = USER_SERVICE.getUsername(received.authToken());
        } catch (UnauthorizedRequestException e) {
            session.getRemote().sendString(SERIALIZER.toJson(ServerMessage.error("Error: unauthorized.")));
        }
        int gameID = received.gameID();

        switch(received.commandType()){
            case CONNECT -> connect(session, username, gameID);
            case LEAVE -> leave(gameID, username);
            case RESIGN -> resign(gameID, username);
            case MAKE_MOVE -> {

            }
            case null, default -> throw new BadRequestException();
        }
    }

    private static void connect(Session session, String username, int gameID) throws IOException {
        sessionLookup.put(username, session);
        GameData game = getGameOrNotify(gameID, username);
        if(game == null){
            return;
        }
        affectedLookup.putIfAbsent(gameID, new ArrayList<>());
        affectedLookup.get(gameID).add(username);
        TeamColor color = getColor(username, game);

        if(color != null) {
            playerGameRetrieval.put(username, game);
            sendLoad(game, username);
        }
        notifyOthersJoin(game, username, color);
    }

    public static void notifyOthersJoin(GameData game, String username, TeamColor color) {
        ServerMessage message = ServerMessage.notification(username + " has joined the game as " +
                (color != null ? "the " + color + " player." : "an observer."));
        notifyList(getOthersAffected(game, username), message);
    }


    private static void leave(int gameID, String username) throws IOException {
        GameData game = getGameOrNotify(gameID, username);
        if(game == null){
            return;
        }
        TeamColor color = getColor(username, game);
        if(color!=null){
            try{
                GAME_SERVICE.leaveGame(gameID, color);
            } catch (DataAccessException e) {
                sendToUser(ServerMessage.error("Error: could not load game data."), username);
                return;
            } catch (AlreadyTakenException e) {
                sendToUser(ServerMessage.error("Error: something went very wrong (leaveGame)."), username);
                return;
            }
        }
        notifyOthersLeave(game, username, color);
        ServerMessage leaveMessage = ServerMessage.notification("You left the game.");
        sendToUser(leaveMessage, username);
        affectedLookup.get(gameID).remove(username);
        playerGameRetrieval.remove(username);
        Session remove = sessionLookup.remove(username);
        remove.close();
    }

    private static TeamColor getColor(String username, GameData game) {
        return username.equals(game.whiteUsername()) ? TeamColor.WHITE :
                username.equals(game.blackUsername()) ? TeamColor.BLACK : null;
    }

    private static void notifyOthersLeave(GameData game, String username, TeamColor color) {
        ServerMessage message = ServerMessage.notification(
                (color != null ? "The " + color + " player " : "The observer ") + username + " has left the game.");
        notifyList(getOthersAffected(game, username), message);
    }

    private static GameData getGameOrNotify(int gameID, String username) throws IOException {
        try{
            return GAME_SERVICE.getGame(gameID);
        } catch (DataAccessException e) {
            sendToUser(ServerMessage.error("Error: could not load game data."), username);
            return null;
        }
    }

    private static void resign(int gameID, String username) throws IOException {
        GameData game = getGameOrNotify(gameID, username);
        if(game == null){
            return;
        }
        TeamColor color = getColor(username, game);
        if(color == null){
            sendToUser(ServerMessage.error("Error: you can't resign from a game you aren't playing."),
                    username);
            return;
        }
        if(!resignGameOrNotify(game, username, color)){
            return;
        }
        notifyOthersResign(game, username, color);
        sendToUser(ServerMessage.notification("You resigned the game."), username);
    }

    private static void notifyOthersResign(GameData game, String username, TeamColor color) {
        ServerMessage message = ServerMessage.notification("The " + color + " player " + username + " has resigned.");
        List<String> others = getOthersAffected(game, username);
        notifyList(others, message);
    }

    private static boolean resignGameOrNotify(GameData game, String username, TeamColor side) throws IOException {
        game.game().resign(side);
        return updateGameOrNotify(game.gameID(), game.game(), username);
    }

    private static void makeMove(int gameID, String username, ChessMove move) throws IOException {
        GameData data = getGameOrNotify(gameID, username);
        if(data == null){
            return;
        }


    }

    private static TeamColor getColorOrNotify(String username, GameData game){return null;}


    private static boolean updateGameOrNotify(int gameID, ChessGame game, String username) throws IOException {
        try{
            return GAME_SERVICE.updateGame(gameID, game);
        } catch (DataAccessException e) {
            sendToUser(ServerMessage.error("Error: could not update game data."), username);
            return false;
        }
    }


    private static List<String> getOthersAffected(GameData game, String username) {
        List<String> toNotify = new ArrayList<>(affectedLookup.getOrDefault(game.gameID(), new ArrayList<>()));
        toNotify.remove(username);
        return toNotify;
    }

    private static void notifyList(List<String> toNotify, ServerMessage message) {
        for(String name: toNotify){
            try {
                sendToUser(message, name);
            } catch (IOException e) {
                System.err.println("IO Exception??");
            }
        }
    }

    private static void sendToUser(ServerMessage message, String username) throws IOException {
        Session out = sessionLookup.get(username);
        if(out.isOpen()){
            out.getRemote().sendString(SERIALIZER.toJson(message));
        }
    }

    public static void sendLoad(GameData game, String username) throws IOException {
        ServerMessage message = ServerMessage.load(SERIALIZER.toJson(game.game()));
        sendToUser(message, username);
    }




    
}
