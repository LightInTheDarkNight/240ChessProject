package dataaccess;

import model.UserData;

public interface UserDAO extends DAO<UserData, String>{

    @Override
    default UserData get(UserData user) throws DataAccessException {
        return user == null ? null : get(user.username());
    }

    @Override
    default boolean delete(UserData user) throws DataAccessException {
        return user == null || delete(user.username());
    }
}
