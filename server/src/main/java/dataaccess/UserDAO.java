package dataaccess;

import model.UserData;

public interface UserDAO {
    /**
     * Removes all data from the attached database.
     *
     * @return true if successful, false otherwise.
     */
    boolean clear();

    /**
     * Adds the given user to the attached database.
     *
     * @param user the user to add to the database
     * @return true if the username was free and the update successful; false if username was taken
     */
    boolean addUser(UserData user);

    /**
     * Retrieves the UserData object in the database associated with the given username.
     *
     * @param username the username of the UserData object to find and retrieve
     * @return the UserData object retrieved, or null if it doesn't exist.
     */
    UserData getUserByUsername(String username);

    /**
     * Finds the UserData object in the database associated with the passed in object's username field.
     *
     * @param user the user whose username should be used to find the corresponding UserData object
     * @return the UserData object retrieved, or null if it doesn't exist.
     */
    default UserData getUserByUsername(UserData user) {
        return user == null ? null : getUserByUsername(user.username());
    }
}
