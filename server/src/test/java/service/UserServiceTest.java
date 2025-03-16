package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {
    private static final UserDAO USER_LIST = new MemoryUserDAO();
    private static final AuthDAO AUTH_LIST = new MemoryAuthDAO();
    private static final String[] USERNAMES = {"Johnathan", "Abraham", "Isaac", "Jacobugath",
            "YouTube", "Google", "Apple", "Mozart"};
    private static final String[] AUTH_TOKENS_AND_PASSWORDS = {"A", "B", "C", "D", "E", "F", "G", "H", "I",};
    private static final String[] EMAILS_AND_GAME_NAMES = {"b-dubs", "john", "tess", "quiz",
            "trix", "izet", "gorm", "temple"};
    private static UserService service = new UserService(USER_LIST, AUTH_LIST);

    @BeforeEach
    void setup() throws DataAccessException {
        USER_LIST.clear();
        AUTH_LIST.clear();
        service = new UserService(USER_LIST, AUTH_LIST);
    }

    void populateAuth() throws DataAccessException {
        AuthData[] credentials = new AuthData[8];
        for(int i = 0; i < 8; i++) {
            credentials[i] = new AuthData(AUTH_TOKENS_AND_PASSWORDS[i], USERNAMES[i]);
        }
        for(var item : credentials) {
            AUTH_LIST.add(item);
        }
    }

    void populateUsers() throws DataAccessException{
        UserData[] users = new UserData[8];
        for(int i = 0; i < 8; i++) {
            users[i] = new UserData(USERNAMES[i], AUTH_TOKENS_AND_PASSWORDS[i], EMAILS_AND_GAME_NAMES[i]);
        }
        for(var item : users) {
            USER_LIST.add(item);
        }
    }

    @Test
    void getUsernameTest() {

    }

    @Test
    void authenticateTest() {
    }

    @Test
    void loginTest() {
    }

    @Test
    void logoutTest() {
    }

    @Test
    void registerTest() {
    }
}