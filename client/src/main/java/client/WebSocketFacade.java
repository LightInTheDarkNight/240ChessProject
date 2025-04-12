package client;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

public class WebSocketFacade extends Endpoint {
    private final Session session;

    public WebSocketFacade(String serverUrl, MessageHandler listener) throws Exception {
        URI uri = new URI(serverUrl.replace("http", "ws") + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(listener);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }

    public void connectToGame(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, null));
    }

    public void leaveGame(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID, null));
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move));
    }

    public void resignGame(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID, null));
    }

    public void sendCommand(UserGameCommand command) throws IOException {
        session.getBasicRemote().sendText(new Gson().toJson(command));
    }
}
