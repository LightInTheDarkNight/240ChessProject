package client.repl;

import chess.ChessGame;
import model.GameData;
import web.ResponseException;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Scanner;

import static client.repl.EscapeSequences.*;
import static java.lang.Integer.max;

public class PostLoginOptions extends ChessMenuOptions {
    private static final String TAKEN = "is already playing as that color in that game.";
    private static GameData[] gamesLastRetrieval = new GameData[0];

    public static void logout(Scanner in, PrintStream out){
        try {
            facade.logout(authToken);
            authToken = "";
            out.println(SET_TEXT_COLOR_BLUE + "Logout successful! Returning to primary menu." + RESET_TEXT_COLOR);
        } catch (ResponseException e) {
            handleError(out, e, TAKEN);
        }
    }

    public static void createGame(Scanner in, PrintStream out){
        String gameName = getField(in, out, "your desired game name");
        if(gameName.isBlank()){
            return;
        }
        try {
            facade.createGame(authToken, gameName);
            out.println(SET_TEXT_COLOR_BLUE + "Game creation successful!" + RESET_TEXT_COLOR);
        } catch (ResponseException e) {
            handleError(out, e, TAKEN);
        }
    }

    public static void listGames(Scanner in, PrintStream out){
        try {
            Collection<GameData> games = facade.listGames(authToken);
            gamesLastRetrieval = games.toArray(GameData[]::new);
            final String COLUMN_DIV = "  " + TABLE_VL + "  ";
            final String PREFIX = "      " + TABLE_VL;
            final String ID_HEADER = "  Game ID:";
            final String NAME_HEADER = "Game Name:";
            final String WHITE_HEADER = "Playing White:";
            final String BLACK_HEADER = "Playing Black:";
            final String ABSENT = SET_TEXT_COLOR_GREEN + "Available!" + SET_TEXT_COLOR_BLUE;
            final int ID_COLUMN_WIDTH = ID_HEADER.length();
            int longestBlack = BLACK_HEADER.length();
            int longestWhite = WHITE_HEADER.length();
            int longestName = BLACK_HEADER.length();

            for(var game:games){
                String black = game.blackUsername();
                String white = game.whiteUsername();
                longestBlack = max(longestBlack, black == null? 0 : black.length());
                longestWhite = max(longestWhite, white == null? 0 : white.length());
                longestName = max(longestName, game.gameName().length());
            }

            String[][] pieces = new String[games.size()+1][4];
            pieces[0][0] = ID_HEADER;
            pieces[0][1] = padTo(longestName, NAME_HEADER);
            pieces[0][2] = padTo(longestWhite, WHITE_HEADER);
            pieces[0][3] = padTo(longestBlack, BLACK_HEADER);
            for(int i = 0; i < gamesLastRetrieval.length;){
                GameData game = gamesLastRetrieval[i];
                i++;
                pieces[i][0] = (padTo(ID_COLUMN_WIDTH, ""+i));

                pieces[i][1] = padTo(longestName, game.gameName());

                String white = game.whiteUsername();
                pieces[i][2] = padTo(longestWhite, white == null? ABSENT : white);

                String black = game.blackUsername();
                pieces[i][3] = padTo(longestBlack, black == null? ABSENT : black);
            }

            final String ID_HL = TABLE_HL.repeat(ID_HEADER.length() + 2);
            final String NAME_HL = TABLE_HL.repeat(longestName + 4);
            final String WHITE_HL = TABLE_HL.repeat(longestWhite + 4);
            final String BLACK_HL = TABLE_HL.repeat(longestBlack + 4);
            final String TOP_HR = "      " + TABLE_TL_CORNER + ID_HL + TABLE_DOWN_JOIN + NAME_HL + TABLE_DOWN_JOIN
                    + WHITE_HL + TABLE_DOWN_JOIN + BLACK_HL + TABLE_TR_CORNER + "\n";
            final String MID_HR = "      " + TABLE_RIGHT_JOIN + ID_HL + TABLE_CROSS_JOIN + NAME_HL + TABLE_CROSS_JOIN
                    + WHITE_HL + TABLE_CROSS_JOIN + BLACK_HL + TABLE_LEFT_JOIN + "\n";
            final String LOW_HR = "      " + TABLE_BL_CORNER + ID_HL + TABLE_UP_JOIN + NAME_HL + TABLE_UP_JOIN
                    + WHITE_HL + TABLE_UP_JOIN + BLACK_HL + TABLE_BR_CORNER + "\n";

            StringBuilder listText = new StringBuilder(SET_TEXT_COLOR_BLUE + "Active games on the server:\n");
            listText.append(TOP_HR).append(MID_HR);

            for(String[] row:pieces){
                listText.append(PREFIX);
                for(String piece:row){
                    listText.append(piece).append(COLUMN_DIV);
                }
                listText.append("\n").append(MID_HR);
            }
            listText.append(LOW_HR);
            out.println(listText);
        } catch (ResponseException e) {
            handleError(out, e, TAKEN);
        }
    }

    public static void joinGame(Scanner in, PrintStream out){
        GameData game = getAndConfirmGame(in, out, true);
        if(game == null){
            exitMessage(out);
            return;
        }
        ChessGame.TeamColor color = getColor(in, out);
        if(color == null){
            exitMessage(out);
            return;
        }
        out.println(SET_TEXT_COLOR_BLUE + "Attempting to join game " + game.gameName() + "...." );
        try{
            facade.playGame(authToken, color, game.gameID());
            currentGame = game.game();
            //until functionality added (next phase)
            perspective = color.other();
            drawBoard(out);
            //Assuming this will stay
            perspective = color;
            drawBoard(out);
        }catch(ResponseException e){
            handleError(out, e, TAKEN);
        }
    }

    public static void observeGame(Scanner in, PrintStream out){

        GameData game = getAndConfirmGame(in, out, false);
        if(game == null){
            exitMessage(out);
            return;
        }
        ChessGame.TeamColor color = getColor(in, out);
        if(color == null){
            exitMessage(out);
            return;
        }
        out.println(SET_TEXT_COLOR_BLUE + "Attempting to observe game " + game.gameName() + "...." );
        try{
            facade.observeGame(authToken, color, game.gameID());
            currentGame = game.game();
            //until functionality added (next phase)
            perspective = color.other();
            drawBoard(out);
            //Assuming this will stay
            perspective = color;
            drawBoard(out);
        }catch(ResponseException e){
            handleError(out, e, TAKEN);
        }
    }

    private static GameData getAndConfirmGame(Scanner in, PrintStream out, boolean play){
        String action = play ? "play" : "observe";
        int gameID = getInt(in, out, "the id of the game you want to observe", gamesLastRetrieval.length+1);
        if(gameID == -1){
            return null;
        }
        GameData game = gamesLastRetrieval[gameID-1];
        String name = game.gameName();
        String white = game.whiteUsername();
        String black = game.blackUsername();
        String empty = "No One";
        out.printf(SET_TEXT_COLOR_BLUE + """
                        The game you have requested to %s is number %d. Its name is %s.
                        White is being played by %s, and Black is being played by %s.
                        Is this the game you meant?
                        """, action, gameID, name, white == null? empty: white, black == null? empty : black);
        if(!confirm(in, out)){
            exitMessage(out);
            return null;
        }
        return game;
    }

    private static String padTo(int size, String toPad){
        int reps = size - toPad.length();
        return " ".repeat(max(0, reps)) + toPad;
    }
}
