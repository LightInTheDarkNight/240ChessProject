package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static server.Server.GAME_SERVICE;
import static server.Server.USER_SERVICE;
import static server.WebException.*;
import static chess.ChessGame.TeamColor;

@WebSocket
public class WebSocketHandler {
    private static final Gson SERIALIZER = new Gson();
    private static final Map<String, Session> sessionLookup = new ConcurrentHashMap<>();
    private static final Map<Integer, List<String>> affectedLookup = new ConcurrentHashMap<>();
    private static final Map<String, Integer> userToCurrentGameLookup = new ConcurrentHashMap<>();


    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        UserGameCommand received = SERIALIZER.fromJson(message, UserGameCommand.class);
        String username;
        try {
            username = USER_SERVICE.getUsername(received.authToken());
        } catch (UnauthorizedRequestException e) {
            session.getRemote().sendString(SERIALIZER.toJson(ServerMessage.error("Error: unauthorized.")));
            return;
        }
        int gameID = received.gameID();

        switch(received.commandType()){
            case CONNECT -> connect(session, username, gameID);
            case LEAVE -> leave(gameID, username);
            case RESIGN -> resign(gameID, username);
            case MAKE_MOVE -> makeMove(gameID, username, received.move());
            case null, default -> throw new BadRequestException();
        }
    }

    private static void connect(Session session, String username, int gameID) throws IOException {
        sessionLookup.put(username, session);
        Integer old = userToCurrentGameLookup.put(username, gameID);
        if(old != null){
            var oldTwo = affectedLookup.get(old);
            if(oldTwo != null){
                oldTwo.remove(username);
            }
        }
        GameData game = getGameOrNotify(gameID, username);
        if(game == null){
            return;
        }
        affectedLookup.putIfAbsent(gameID, new ArrayList<>());
        affectedLookup.get(gameID).add(username);
        TeamColor color = getColor(username, game);
        sendToUser(ServerMessage.load(SERIALIZER.toJson(game.game())), username);
        notifyOthersJoin(gameID, username, color);
    }

    public static void notifyOthersJoin(int gameID, String username, TeamColor color) {
        ServerMessage message = ServerMessage.notification(username + " has joined the game as " +
                (color != null ? "the " + color + " player." : "an observer."));
        sendToList(getOthersAffected(gameID, username), message);
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
        notifyOthersLeave(gameID, username, color);
//        sendToUser(ServerMessage.notification("You left the game."), username);
        affectedLookup.get(gameID).remove(username);
        sessionLookup.remove(username).close();
        userToCurrentGameLookup.remove(username);
    }

    private static TeamColor getColor(String username, GameData game) {
        return username.equals(game.whiteUsername()) ? TeamColor.WHITE :
                username.equals(game.blackUsername()) ? TeamColor.BLACK : null;
    }

    private static void notifyOthersLeave(int gameID, String username, TeamColor color) {
        ServerMessage message = ServerMessage.notification(
                (color != null ? "The " + color + " player " : "The observer ") + username + " has left the game.");
        sendToList(getOthersAffected(gameID, username), message);
    }

    private static GameData getGameOrNotify(int gameID, String username) throws IOException {
        try{
            GameData data = GAME_SERVICE.getGame(gameID);
            if(data == null){
                sendToUser(ServerMessage.error("Error: no game with that ID in database."), username);
            }
            return data;
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
        TeamColor color = getColorOrNotify(username, game);
        if(color == null){
            return;
        }
        if(!resignGameOrNotify(game, username, color)){
            return;
        }

        sendToList(affectedLookup.get(gameID), ServerMessage.notification(
                "The " + color + " player, " + username + ", has resigned."));

    }

    private static boolean resignGameOrNotify(GameData game, String username, TeamColor side) throws IOException {
        try {
            game.game().resign(side);
        } catch (InvalidMoveException e) {
            sendToUser(ServerMessage.error("Error: Cannot resign after game is over."), username);
            return false;
        }
        return updateGameOrNotify(game.gameID(), game.game(), username);
    }

    private static void makeMove(int gameID, String username, ChessMove move) throws IOException {
        GameData data = getGameOrNotify(gameID, username);
        if(data == null){
            return;
        }
        TeamColor color = getColorOrNotify(username, data);
        if(color == null){
            return;
        }
        if(data.game().getTeamTurn() != color){
            sendToUser(ServerMessage.error("Error: cannot move for opponent."), username);
            return;
        }
        try {
            data.game().makeMove(move);
        } catch (InvalidMoveException e) {
            sendToUser(ServerMessage.error("Error: invalid move."), username);
            return;
        }
        if(!updateGameOrNotify(data.gameID(), data.game(), username)){
            return;
        }

        List<String> allClients = affectedLookup.get(gameID);
        sendToList(allClients, ServerMessage.load(SERIALIZER.toJson(data.game())));
        notifyOthersMove(gameID, username, color, move);

        var status = data.game().getStatus();
        String checkmate = "### Checkmate! ###";
        String check = "+++ Check +++";
        switch(status){
            case WHITE_WON, BLACK_WON -> sendToList(allClients, ServerMessage.notification(checkmate));
            case WHITE_IN_CHECK, BLACK_IN_CHECK -> sendToList(allClients, ServerMessage.notification(check));
        }
    }

    private static void notifyOthersMove(int gameID, String username, TeamColor color, ChessMove move) {
        ServerMessage message = ServerMessage.notification(username + " (" + color + ") made the move " + move);
        sendToList(getOthersAffected(gameID, username), message);
    }

    private static TeamColor getColorOrNotify(String username, GameData game) throws IOException {
        TeamColor color = getColor(username, game);
        if(color == null){
            sendToUser(ServerMessage.error("Error: you aren't playing this game."), username);
        }
        return color;
    }


    private static boolean updateGameOrNotify(int gameID, ChessGame game, String username) throws IOException {
        try{
            return GAME_SERVICE.updateGame(gameID, game);
        } catch (DataAccessException e) {
            sendToUser(ServerMessage.error("Error: could not update game data."), username);
            return false;
        }
    }


    private static List<String> getOthersAffected(int gameID, String username) {
        List<String> toNotify = new ArrayList<>(affectedLookup.getOrDefault(gameID, new ArrayList<>()));
        toNotify.remove(username);
        return toNotify;
    }

    private static void sendToList(List<String> toNotify, ServerMessage message) {
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

}
