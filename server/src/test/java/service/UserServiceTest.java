package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static server.WebException.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {
    private static final UserDAO USER_LIST = new MemoryUserDAO();
    private static final AuthDAO AUTH_LIST = new MemoryAuthDAO();
    private static final String[] USERNAMES = {"Johnathan", "Abraham", "Isaac", "Jacobugath",
            "YouTube", "Google", "Apple", "Mozart"};
    private static final String[] PASSWORDS = {"A", "B", "C", "D", "E", "F", "G", "H", "I",};
    private static final String[] EMAILS = {"b-dubs", "john", "tess", "quiz", "trix", "izet", "gorm", "temple"};
    private static final UserService SERVICE = new UserService(USER_LIST, AUTH_LIST);
    private static final String[] AUTH_TOKENS = new String[8];

    @BeforeEach
    void setup() throws Exception {
        USER_LIST.clear();
        AUTH_LIST.clear();
        for(int i = 0; i < 8; i ++){
            AUTH_TOKENS[i] = SERVICE.register(new UserData(USERNAMES[i], PASSWORDS[i], EMAILS[i])).authToken();
        }
    }

    @Test
    void registerTest() {
        for(int i = 0; i < 8; i ++){
            assert AUTH_TOKENS[i] != null;
        }
    }

    @Test
    void registerTestThrows() {
        for(int i = 0; i < 8; i ++){
            int a = i;
            assertThrows(AlreadyTakenException.class, () ->
                    SERVICE.register(new UserData(USERNAMES[a], PASSWORDS[a], EMAILS[a])));
        }
    }

    @Test
    void authenticateTest() {
        for(int i = 0; i < 8; i ++){
            int a = i;
            assertDoesNotThrow(() -> {
                SERVICE.authenticate(AUTH_TOKENS[a]);
            });
        }
    }

    @Test
    void authenticateTestThrows() {
        assertThrows(UnauthorizedRequestException.class, ()-> SERVICE.authenticate("NOT_AN_AUTH_TOKEN"));
    }

    @Test
    void getUsernameTest() {
        for(int i = 0; i < 8; i ++){
            int a = i;
            assertDoesNotThrow(() -> {
                assert SERVICE.getUsername(AUTH_TOKENS[a]).equals(USERNAMES[a]);
            });
        }
    }
    @Test
    void getUsernameTestThrows() {
        assertThrows(UnauthorizedRequestException.class, ()-> SERVICE.authenticate("NOT_AN_AUTH_TOKEN"));
    }

    @Test
    void loginTestFailsUnregistered() {
        assertThrows(UnauthorizedRequestException.class, ()->
                SERVICE.login(new UserData("BOB_ISN'T_REGISTERED", "NOT_A_PASSWORD", null)) );
    }
    @Test
    void loginTestFailsWrongPassword() {
        assertThrows(UnauthorizedRequestException.class, ()->
                SERVICE.login(new UserData("Johnathan", "NOT_A_PASSWORD", null)));
    }
    @Test
    void loginTestSuccess() throws Exception{
        for(int i = 0; i < 8; i ++){
            assert SERVICE.login(new UserData(USERNAMES[i], PASSWORDS[i], EMAILS[i])) != null;
        }
    }

    @Test
    void logoutTest() {
        assertDoesNotThrow(()->{
            for(int i = 0; i < 8; i ++){
                assert SERVICE.logout(AUTH_TOKENS[i]);
            }
        });
    }
    @Test
    void logoutTestThrows() throws DataAccessException {
        AUTH_LIST.clear();
        for(int i = 0; i < 8; i ++){
            int a = i;
            assertThrows(UnauthorizedRequestException.class, ()-> SERVICE.logout(AUTH_TOKENS[a]));
        }
    }
}