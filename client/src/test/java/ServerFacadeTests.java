import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import service.ClearService;

import java.lang.reflect.Field;


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
        Assertions.assertDoesNotThrow(() -> {
            AuthData response = facade.register(user);
            assert response != null;
        });
    }

    @Test
    public void registerFail() {
        UserData user = new UserData("Jethro", "password", "example@aol.com");

        Assertions.assertDoesNotThrow(() -> facade.register(user));
        Assertions.assertThrows(ResponseException.class, () -> facade.register(user));
    }

}
