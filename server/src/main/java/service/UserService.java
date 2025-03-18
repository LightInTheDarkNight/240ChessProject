package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import server.Server.AlreadyTakenException;
import server.Server.UnauthorizedRequestException;

import java.util.UUID;

public class UserService {
    private final UserDAO users;
    private final AuthDAO credentials;

    public UserService(UserDAO users, AuthDAO auth) {
        this.users = users;
        this.credentials = auth;
    }

    /**
     * Retrieves just the username associated with the given authToken - convenience wrapper for the authenticate()
     * method.
     * @param authToken the authToken identifying the desired username.
     * @return the username associated with the given authToken.
     * @throws UnauthorizedRequestException if the given authToken is not in the credential database.
     * @throws DataAccessException if an error occurs accessing the credential database.
     */
    public String getUsername(String authToken) throws UnauthorizedRequestException, DataAccessException {
        return authenticate(authToken).username();
    }

    /**
     * Retrieves the AuthData associated with the given authToken, for username lookup, deletion, or something else.
     * @param authToken the identifier of the AuthData to retrieve.
     * @return the AuthData associated with the given authToken.
     * @throws UnauthorizedRequestException if the AuthToken is not in the attached credential database.
     * @throws DataAccessException if an error occurs accessing the credential database.
     */
    public AuthData authenticate(String authToken) throws UnauthorizedRequestException, DataAccessException {
        AuthData auth = credentials.get(authToken);
        if (auth == null) {
            throw new UnauthorizedRequestException();
        }
        return auth;
    }

    /**
     * Logs in the user represented by the passed in UserData. Email field is not required.
     * @param user the user to log in.
     * @return the AuthData that was created to represent the passed in user and added to the attached credential
     * database.
     * @throws UnauthorizedRequestException if the password from user is not the one associated with the username of
     * user in the database.
     * @throws DataAccessException if an error occurs accessing or updating the databases.
     */
    public AuthData login(UserData user) throws UnauthorizedRequestException, DataAccessException {
        var correct = users.get(user);
        if (correct == null || !BCrypt.checkpw(user.password(), correct.password())) {
            throw new UnauthorizedRequestException();
        }
        return createAuthData(correct.username());
    }

    private AuthData createAuthData(String username) throws DataAccessException {
        var auth = new AuthData(UUID.randomUUID().toString(), username);
        credentials.add(auth);
        return auth;
    }

    /**
     * Logs out the user represented by the passed in authToken.
     * @param authToken the authorization string representing a logged-in user.
     * @return whether the operation succeeded.
     * @throws UnauthorizedRequestException if the passed in authToken is not in the attached credential database.
     * @throws DataAccessException if an error occurs accessing or updating the credential database.
     */
    public boolean logout(String authToken) throws UnauthorizedRequestException, DataAccessException {
        return credentials.delete(authenticate(authToken));
    }

    /**
     * Registers a new user, logs them in, and returns the AuthData associated with logging them in. Throws an
     * AlreadyTakenException if the username is already in the attached user database. Throws a DataAccessException if
     * a database error occurs. Stores the password salted and hashed.
     * @param user the user to register's information.
     * @return the AuthData from logging in the newly registered user.
     * @throws AlreadyTakenException if user's username is already in the database.
     * @throws DataAccessException if an error occurs accessing or updating the database.
     */
    public AuthData register(UserData user) throws AlreadyTakenException, DataAccessException {
        String hashed = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        UserData hashedUser = new UserData(user.username(), hashed, user.email());
        if (!users.add(hashedUser)) {
            throw new AlreadyTakenException();
        }
        try {
            return login(user);
        } catch (UnauthorizedRequestException e) {
            throw new RuntimeException("Added user could not be logged in.", e);
        }
    }

}
