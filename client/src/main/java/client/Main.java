package client;

import chess.ChessGame;
import chess.ChessPiece;
//import static client.repls.EscapeSequences.*;

public class Main {
    //private String authToken;
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
    }
}