package client;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";
    private static final String PUT = "PUT";

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData register(UserData user) throws ResponseException {
        String path = "/user";
        return makeRequest(POST, path, null, user, AuthData.class);
    }

    public AuthData login(UserData user) throws ResponseException {
        String path = "/session";
        return makeRequest(POST, path, null, user, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        String path = "/session";
        makeRequest(DELETE, path, authToken, null, null);
    }

    public int createGame(String authToken, String gameName) throws ResponseException {
        String path = "/game";
        TypeToken<HashMap<String, Integer>> type = new TypeToken<>(){};
        return makeRequestTypeToken(POST, path, authToken, Map.of("gameName", gameName), type).get("gameID");
    }

    public void playGame(String authToken, ChessGame.TeamColor color, int gameID) throws ResponseException {
        String path = "/game";
        makeRequest(PUT, path, authToken, Map.of("playerColor", color, "gameID", gameID),
                null);
    }

    public Collection<GameData> listGames(String authToken) throws ResponseException {
        String path = "/game";
        TypeToken<HashMap<String, Collection<GameData>>> type = new TypeToken<>(){};
        return makeRequestTypeToken(GET, path, authToken, null, type).get("games");
    }

    public void observeGame(String authToken, ChessGame.TeamColor perspective, int gameID) throws ResponseException{
        if(gameID < 1){
            throw new ResponseException(400, "Error: Bad request");
        }
    }

    private <T> T makeRequest(String method, String path, String auth, Object request, Class<T> responseClass)
            throws ResponseException {
        return makeRequestTypeToken(method, path, auth, request,
                responseClass == null? null : TypeToken.get(responseClass));
    }

    private <T> T makeRequestTypeToken(String method, String path, String auth, Object request,
                                       TypeToken<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if(auth != null){
                http.setRequestProperty("Authorization", auth);
            }
            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        int status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(status, respErr);
                }
            }

            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, TypeToken<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
