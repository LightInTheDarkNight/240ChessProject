package dataaccess;

import model.UserData;

public interface UserDAO {
    /**
     * Removes all data from the attached database.
     * @return true if successful, false otherwise.
     */
    public boolean clear();

    /**
     * Adds the given user to the attached database.
     * @param user the user to add to the database
     * @return true if the username was free and the update successful; false if username was taken
     */
    public boolean addUser(UserData user);
    public UserData findUserData(String username);
    public default UserData findUserData(UserData user){
        return user == null? null : findUserData(user.username());
    }
}
