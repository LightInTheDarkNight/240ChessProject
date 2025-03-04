package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class ClearService {
    private final UserDAO users;
    private final GameDAO games;
    private final AuthDAO credentials;

    public ClearService(UserDAO users, GameDAO games, AuthDAO auth) {
        this.users = users;
        this.games = games;
        this.credentials = auth;
    }

    public boolean clearAll() {
        return users.clear() && games.clear() && credentials.clear();
    }
}
