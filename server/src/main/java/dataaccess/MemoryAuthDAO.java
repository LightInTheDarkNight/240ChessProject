package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO{
    private final HashMap<String, AuthData> authDataList = new HashMap<>();

    @Override
    public boolean clear() {
        authDataList.clear();
        return authDataList.isEmpty();
    }

    @Override
    public void addAuth(AuthData authorization) {
        authDataList.put(authorization.authToken(), authorization);
    }

    @Override
    public AuthData getAuthByToken(String authToken) {
        return authDataList.get(authToken);
    }

    @Override
    public boolean deleteAuth(AuthData authorization) {
        String token = authorization.authToken();
        authDataList.remove(token);
        return authDataList.get(token) == null;
    }
}
