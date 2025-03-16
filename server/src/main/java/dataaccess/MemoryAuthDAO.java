package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> authDataList = new HashMap<>();

    @Override
    public boolean clear() {
        authDataList.clear();
        return authDataList.isEmpty();
    }

    @Override
    public boolean add(AuthData auth) {
        AuthData old = authDataList.putIfAbsent(auth.authToken(), auth);
        return old == null && authDataList.get(auth.authToken()) == auth;
    }

    @Override
    public AuthData get(String authToken) {
        return authDataList.get(authToken);
    }

    @Override
    public boolean delete(String token) {
        authDataList.remove(token);
        return authDataList.get(token) == null;
    }
}
