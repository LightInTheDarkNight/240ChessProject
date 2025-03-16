package dataaccess;

import model.AuthData;

public interface AuthDAO extends DAO<AuthData, String>{

    @Override
    default AuthData get(AuthData auth) throws DataAccessException {
        return auth == null? null : get(auth.authToken());
    }

    @Override
    default boolean delete(AuthData auth) throws DataAccessException {
        return auth == null || delete(auth.authToken());
    }
}
