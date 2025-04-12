package server;

import chess.ChessGame;
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
            case CONNECT:
                connect(session, username, gameID);
                break;
            case LEAVE:
                leave(gameID, username);
                break;
            case RESIGN:
                break;
            case MAKE_MOVE:
                break;
            case null, default:
                throw new BadRequestException();
        }
    }

    private static void connect(Session session, String username, int gameID) throws IOException {
        sessionLookup.put(username, session);
        GameData game;
        try{
            game = GAME_SERVICE.getGame(gameID);
        } catch (DataAccessException e) {
            sendToUser(ServerMessage.error("Error: could not load game data."), username);
            return;
        }
        affectedLookup.putIfAbsent(gameID, new ArrayList<>());
        affectedLookup.get(gameID).add(username);
        ChessGame.TeamColor color = null;
        String otherPlayer = "";
        
        if(username.equals(game.blackUsername())) {
            color = ChessGame.TeamColor.BLACK;
            otherPlayer = game.whiteUsername();
        }
        else if (username.equals(game.whiteUsername())) {
            color = ChessGame.TeamColor.WHITE;
            otherPlayer = game.blackUsername();
        }
        boolean player = !otherPlayer.isEmpty();

        if(player) {
            playerGameRetrieval.put(username, game);
            sendLoad(game, username);
        }
        notifyOthersJoin(game, player, username, color);
    }

    public static void notifyOthersJoin(GameData game, boolean player, String username, ChessGame.TeamColor color) {
        ServerMessage message = ServerMessage.notification(username + " has joined the game as " +
                (player ? "the " + color + " player." : "an observer."));
        notifyList(getOthersAffected(game, username), message);
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

    private static List<String> getOthersAffected(GameData game, String username) {
        List<String> toNotify = new ArrayList<>(affectedLookup.getOrDefault(game.gameID(), new ArrayList<>()));
        toNotify.remove(username);
        return toNotify;
    }

    public static void sendLoad(GameData game, String username) throws IOException {
        ServerMessage message = ServerMessage.load(SERIALIZER.toJson(game.game()));
        sendToUser(message, username);
    }

    private static void leave(int gameID, String username) throws IOException {
        boolean player = playerGameRetrieval.get(username) != null;
        GameData game;
        try{
            game = GAME_SERVICE.getGame(gameID);
        } catch (DataAccessException e) {
            sendToUser(ServerMessage.error("Error: could not load game data."), username);
            return;
        }
        ChessGame.TeamColor color = null;
        if(player){
            color = username.equals(game.whiteUsername())? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
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
        notifyOthersLeave(game, player, username, color);
        ServerMessage leaveMessage = ServerMessage.notification("You left the game.");
        sendToUser(leaveMessage, username);
        affectedLookup.get(gameID).remove(username);
        playerGameRetrieval.remove(username);
        Session remove = sessionLookup.remove(username);
        remove.close();
    }

    private static void notifyOthersLeave(GameData game, boolean player, String username, ChessGame.TeamColor color) {
        ServerMessage message = ServerMessage.notification((player ? "The " + color + " player " : "The observer ") +
                username + " has left the game.");
        List<String> others = getOthersAffected(game, username);
        notifyList(others, message);
    }


    
}
