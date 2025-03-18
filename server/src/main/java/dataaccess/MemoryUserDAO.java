package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final HashMap<String, UserData> userDataList = new HashMap<>();

    @Override
    public boolean clear() {
        userDataList.clear();
        return userDataList.isEmpty();
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
