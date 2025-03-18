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

    public String getUsername(String authToken) throws UnauthorizedRequestException, DataAccessException {
        return authenticate(authToken).username();
    }

    public AuthData authenticate(String authToken) throws UnauthorizedRequestException, DataAccessException {
        AuthData auth = credentials.get(authToken);
        if (auth == null) {
            throw new UnauthorizedRequestException();
        }
        return auth;
    }

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

    public boolean logout(String authToken) throws UnauthorizedRequestException, DataAccessException {
        return credentials.delete(authenticate(authToken));
    }

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
