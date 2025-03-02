package dataaccess;

import model.AuthData;

public interface AuthDAO {
    public boolean clear();
    public void addAuth(AuthData authorization);
    public AuthData getAuthByToken(String authToken);
    public boolean deleteAuth(AuthData authorization);
}
