package websocket.commands;
import chess.ChessMove;

/**
 * Represents a command a user can send the server over a websocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public record UserGameCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move) {
    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }
}
