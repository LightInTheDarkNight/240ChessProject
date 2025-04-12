package dataaccess;

import model.UserData;

import java.util.concurrent.ConcurrentHashMap;

public class MemoryUserDAO implements UserDAO {
    private final ConcurrentHashMap<String, UserData> userDataList = new ConcurrentHashMap<>();

    @Override
    public boolean clear() {
        userDataList.clear();
        return true;
    }

    @Override
    public boolean add(UserData user) {
        return userDataList.putIfAbsent(user.username(), user) == null;
    }

    @Override
    public UserData get(String username) {
        return userDataList.get(username);
    }

    @Override
    public boolean delete(String username) {
        userDataList.remove(username);
        return userDataList.get(username) == null;
    }
}
