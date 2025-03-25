import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ResponseException extends Exception {
    private final int statusCode;
    public ResponseException(int code, String message){
        super(message);
        statusCode = code;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static ResponseException fromJson(int code, InputStream stream) {
        var map = new Gson().fromJson(new InputStreamReader(stream), HashMap.class);
        String message = map.get("message").toString();
        return new ResponseException(code, message);
    }
}
