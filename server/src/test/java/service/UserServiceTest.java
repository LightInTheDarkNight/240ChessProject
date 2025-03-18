package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import server.Server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {
    private static final UserDAO USER_LIST = new MemoryUserDAO();
    private static final AuthDAO AUTH_LIST = new MemoryAuthDAO();
    private static final String[] USERNAMES = {"Johnathan", "Abraham", "Isaac", "Jacobugath",
            "YouTube", "Google", "Apple", "Mozart"};
    private static final String[] PASSWORDS = {"A", "B", "C", "D", "E", "F", "G", "H", "I",};
    private static final String[] EMAILS = {"b-dubs", "john", "tess", "quiz", "trix", "izet", "gorm", "temple"};
    private static final UserService service = new UserService(USER_LIST, AUTH_LIST);
    private static final String[] authTokens = new String[8];

    @BeforeEach
    void setup() throws Exception {
        USER_LIST.clear();
        AUTH_LIST.clear();
        for(int i = 0; i < 8; i ++){
            authTokens[i] = service.register(new UserData(USERNAMES[i], PASSWORDS[i], EMAILS[i])).authToken();
        }
    }

    @Test
    void registerTest() throws Exception {
        for(int i = 0; i < 8; i ++){
            assert authTokens[i] != null;
        }
    }

    @Test
    void registerTestThrows() {
        for(int i = 0; i < 8; i ++){
            int a = i;
            assertThrows(Server.AlreadyTakenException.class, () -> {
                service.register(new UserData(USERNAMES[a], PASSWORDS[a], EMAILS[a]));
            });
        }
    }

    @Test
    void authenticateTest() {
        for(int i = 0; i < 8; i ++){
            int a = i;
            assertDoesNotThrow(() -> {
                service.authenticate(authTokens[a]);
            });
        }
    }

    @Test
    void authenticateTestThrows() {
        assertThrows(Server.UnauthorizedRequestException.class, ()->{
            service.authenticate("NOT_AN_AUTH_TOKEN");
        });
    }

    @Test
    void getUsernameTest() {
        for(int i = 0; i < 8; i ++){
            int a = i;
            assertDoesNotThrow(() -> {
                assert service.getUsername(authTokens[a]).equals(USERNAMES[a]);
            });
        }
    }
    @Test
    void getUsernameTestThrows() {
        assertThrows(Server.UnauthorizedRequestException.class, ()->{
            service.authenticate("NOT_AN_AUTH_TOKEN");
        });
    }

    @Test
    void loginTestFailsUnregistered() {
        assertThrows(Server.UnauthorizedRequestException.class, ()->{
            service.login(new UserData("BOB_ISN'T_REGISTERED", "NOT_A_PASSWORD", null));
        });
    }
    @Test
    void loginTestFailsWrongPassword() {
        assertThrows(Server.UnauthorizedRequestException.class, ()->{
            service.login(new UserData("Johnathan", "NOT_A_PASSWORD", null));
        });
    }
    @Test
    void loginTestSuccess() throws Exception{
        for(int i = 0; i < 8; i ++){
            assert service.login(new UserData(USERNAMES[i], PASSWORDS[i], EMAILS[i])) != null;
        }
    }

    @Test
    void logoutTest() {
        assertDoesNotThrow(()->{
            for(int i = 0; i < 8; i ++){
                assert service.logout(authTokens[i]);
            }
        });
    }
    @Test
    void logoutTestThrows() throws DataAccessException {
        AUTH_LIST.clear();
        for(int i = 0; i < 8; i ++){
            int a = i;
            assertThrows(Server.UnauthorizedRequestException.class, ()->{
                service.logout(authTokens[a]);
            });
        }
    }
}