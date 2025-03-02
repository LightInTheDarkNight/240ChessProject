package model;

import chess.ChessGame;

import java.util.Objects;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

    public GameData setWhitePlayer(String whitePlayer){
        return new GameData(gameID, whitePlayer, blackUsername, gameName, game);
    }

    public GameData setBlackPlayer(String blackPlayer){
        return new GameData(gameID, whiteUsername, blackPlayer, gameName, game);
    }
}
