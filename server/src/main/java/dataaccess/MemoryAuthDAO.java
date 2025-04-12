package dataaccess;

import model.AuthData;

import java.util.concurrent.ConcurrentHashMap;

public class MemoryAuthDAO implements AuthDAO {
    private final ConcurrentHashMap<String, AuthData> authDataList = new ConcurrentHashMap<>();

    @Override
    public boolean clear() {
        authDataList.clear();
        return true;
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
