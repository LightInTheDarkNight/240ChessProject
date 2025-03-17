package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClearServiceTest {
    private static final UserDAO USER_LIST = new DBUserDAO();
    private static final GameDAO GAME_LIST = new MemoryGameDAO();
    private static final AuthDAO AUTH_LIST = new MemoryAuthDAO();
    private static final String[] USERNAMES = {"Johnathan", "Abraham", "Isaac", "Jacobugath",
            "YouTube", "Google", "Apple", "Mozart"};
    private static final String[] AUTH_TOKENS_AND_PASSWORDS = {"A", "B", "C", "D", "E", "F", "G", "H", "I",};
    private static final String[] EMAILS_AND_GAME_NAMES = {"b-dubs", "john", "tess", "quiz",
            "trix", "izet", "gorm", "temple"};
    private static ClearService service = new ClearService(USER_LIST, GAME_LIST, AUTH_LIST);

    @BeforeEach
    void setup() throws DataAccessException{
        USER_LIST.clear();
        GAME_LIST.clear();
        AUTH_LIST.clear();
        service = new ClearService(USER_LIST, GAME_LIST, AUTH_LIST);
    }

    void populateAuth() throws DataAccessException{


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

    void populateGames() throws DataAccessException{
        GameData[] games = new GameData[8];
        for(int i = 0; i < 8; i++) {
            games[i] = new GameData(i + 1, USERNAMES[i], USERNAMES[(i+2)%8], EMAILS_AND_GAME_NAMES[i], new ChessGame());
        }
        for(var item : games) {
            GAME_LIST.add(item);
        }
    }

    @Test
    void clearAll() throws DataAccessException{
        populateUsers();
        populateAuth();
        populateGames();
        assert service.clearAll();
        for(int i = 0; i < 8; i ++){
            assert USER_LIST.get(USERNAMES[i]) == null;
            assert AUTH_LIST.get(AUTH_TOKENS_AND_PASSWORDS[i]) == null;
            assert GAME_LIST.get(i) == null;
        }
    }
}