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
    public boolean addUser(UserData user) {
        return userDataList.putIfAbsent(user.username(), user) == null;
    }

    @Override
    public UserData getUserByUsername(String username) {
        return userDataList.get(username);
    }
}
