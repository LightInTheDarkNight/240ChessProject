package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO{
    private HashMap<String, UserData> userDataList = new HashMap<>();

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
    public UserData findUserData(String username) {
        return userDataList.get(username);
    }
}
