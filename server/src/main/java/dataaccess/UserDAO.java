package dataaccess;

import model.UserData;

public interface UserDAO {
    public boolean clear();
    public void addUser(UserData user);
    public UserData findUserData(String username);
    public default UserData findUserData(UserData user){
        return findUserData(user.username());
    }
}
