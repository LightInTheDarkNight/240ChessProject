import com.google.gson.Gson;
import jdk.jshell.spi.ExecutionControl;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {
    private final String serverUrl;
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";
    private static final String PUT = "PUT";

    public ServerFacade(String url) throws ResponseException {
        serverUrl = url;
    }
    public String register(UserData user) throws ResponseException {
        String path = "/user";

        return "";
    }
    public String login() throws ResponseException {
        String path = "/session";
        return "";
    }
    public String logout() throws ResponseException {
        String path = "/session";
        return "";
    }
    public String createGame() throws ResponseException {
        String path = "/game";
        return "";
    }
    public String playGame() throws ResponseException {
        String path = "/game";
        return "";
    }
    public String listGames() throws ResponseException {
        String path = "/game";
        return "";
    }
    public String observeGame() throws ResponseException {
        String path = "/game";
        return "";
    }
    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass)
            throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

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
    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
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
