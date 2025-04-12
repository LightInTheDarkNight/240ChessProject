package websocket.messages;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public record ServerMessage(ServerMessageType serverMessageType, String message, String errorMessage, String game) {
    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }
    public static ServerMessage error(String errorMessage){
        return new ServerMessage(ServerMessageType.ERROR, null, errorMessage, null);
    }
    public static ServerMessage notification(String message){
        return new ServerMessage(ServerMessageType.NOTIFICATION, message, null, null);
    }
    public static ServerMessage load(String game){
        return new ServerMessage(ServerMessageType.LOAD_GAME, null, null, game);
    }
}
