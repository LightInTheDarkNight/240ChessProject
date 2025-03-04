package dataaccess;

import model.AuthData;

public interface AuthDAO {
    /**
     * Removes all data from the attached database.
     *
     * @return true if successful, false otherwise
     */
    boolean clear();

    /**
     * Adds the passed-in AuthData to the list of valid credentials
     *
     * @param authorization the authorization to add
     */
    void addAuth(AuthData authorization);

    /**
     * Retrieves and returns the AuthData associated with the given authToken string;
     * returns null if the token is invalid.
     *
     * @param authToken the String representing the authToken to validate.
     * @return the AuthData associated with the AuthToken, or null if there is none.
     */
    AuthData getAuthByToken(String authToken);

    /**
     * Removes the authorization passed in from the list of valid credentials. Returns a boolean representing the
     * success of the deletion.
     *
     * @param authorization the AuthData to remove from the list of credentials.
     * @return true if the operation succeeded; false otherwise.
     */
    boolean deleteAuth(AuthData authorization);
}
