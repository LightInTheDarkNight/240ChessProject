package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import server.Server.UnauthorizedRequestException;
import server.Server.AlreadyTakenException;

import java.util.Objects;
import java.util.UUID;

public class UserService {
    private final UserDAO users;
    private final AuthDAO credentials;

    public UserService(UserDAO users, AuthDAO auth){
        this.users = users;
        this.credentials = auth;
    }

    public String getUsername(String authToken) throws UnauthorizedRequestException {
        return authenticate(authToken).username();
    }

    public AuthData authenticate(String authToken) throws UnauthorizedRequestException {
        AuthData auth = credentials.getAuthByToken(authToken);
        if(auth == null){
            throw new UnauthorizedRequestException();
        }
        return auth;
    }

    public AuthData login(UserData user) throws UnauthorizedRequestException {
        var correct = users.findUserData(user);
        if(correct == null || !Objects.equals(correct.password(), user.password())){
            throw new UnauthorizedRequestException();
        }
        return createAuthData(correct.username());
    }

    private AuthData createAuthData(String username) {
        var auth = new AuthData(UUID.randomUUID().toString(), username);
        credentials.addAuth(auth);
        return auth;
    }

    public boolean logout(String authToken) throws UnauthorizedRequestException {
        return credentials.deleteAuth(authenticate(authToken));
    }

    public AuthData register(UserData user) throws AlreadyTakenException {
        if (!users.addUser(user)){
            throw new AlreadyTakenException();
        }
        try{
            return login(user);
        } catch (UnauthorizedRequestException e) {
            throw new RuntimeException("Added user could not be logged in.", e);
        }
    }

}
