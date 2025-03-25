import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import service.ClearService;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ServerFacadeTests {

    private static Server server;
    private static String URL;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        URL = "http://localhost:" + port;
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setUp(){
        facade = new ServerFacade(URL);
    }

    @AfterEach
    public void cleanUp() throws NoSuchFieldException, IllegalAccessException, DataAccessException {
        //reset server contents
        Field clearService = server.getClass().getDeclaredField("CLEAR_SERVICE");
        clearService.setAccessible(true);
        ((ClearService) clearService.get(server)).clearAll();
    }

    @Test
    public void registerSuccess() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");
        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
        });
    }

    @Test
    public void registerFail() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");

        assertDoesNotThrow(() -> facade.register(user));
        assertThrows(ResponseException.class, () -> facade.register(user));
    }

    @Test
    public void logoutSuccess() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");
        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
            facade.logout(response.authToken());
        });
    }

    @Test
    public void logoutFail() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");
        String authToken = "";

        //never valid
        assertThrows(ResponseException.class, () -> facade.logout(authToken));

        //previously valid
        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
            facade.logout(response.authToken());
            assertThrows(ResponseException.class, ()-> facade.logout(response.authToken()));
        });
    }

    @Test
    public void loginSuccess() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");
        UserData withoutEmail = new UserData("Jethro", "password", null);
        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
            facade.logout(response.authToken());
            response = facade.login(user);
            assert response != null;
            facade.logout(response.authToken());
            response = facade.login(withoutEmail);
            assert response != null;
            facade.logout(response.authToken());
        });
    }

    @Test
    public void loginFail() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");
        UserData missingPassword = new UserData("Jethro", null, null);
        UserData emptyPassword = new UserData("Jethro", "", null);
        UserData badUser = new UserData("Jethro", "imposter", null);

        //Before registration
        assertThrows(ResponseException.class, () -> facade.login(user));

        assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
            facade.logout(response.authToken());
        });

        //Wrong Password
        assertThrows(ResponseException.class, () -> facade.login(badUser));
        //Missing Password
        assertThrows(ResponseException.class, () -> facade.login(missingPassword));
        //Empty Password
        assertThrows(ResponseException.class, () -> facade.login(emptyPassword));
    }

}
