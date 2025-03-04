package service;

import dataaccess.GameDAO;
import model.GameData;

public class GameService {
    private final GameDAO games;
    private static int lastGameID = 0;
    public GameService(GameDAO games){
        this.games = games;
    }

    public int createGame(GameData game){
        return 0;
    }
}
