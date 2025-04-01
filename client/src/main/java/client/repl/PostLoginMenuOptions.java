package client.repl;

import chess.ChessGame;
import model.GameData;
import web.ResponseException;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Scanner;

import static client.repl.EscapeSequences.*;
import static java.lang.Integer.max;

public class PostLoginMenuOptions extends ChessMenuOptions {
    private static final String TAKEN = "is already playing as that color in that game.";
    private static GameData[] gamesLastRetrieval = new GameData[0];
    public static void logout(Scanner in, PrintStream out){
        try {
            facade.logout(authToken);
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
            final String COLUMN_DIV = "  |";
            final int DIV_WIDTH = COLUMN_DIV.length();
            final String PREFIX = "      |";
            final String ID_HEADER = "  Game ID:" + COLUMN_DIV;
            final String NAME_HEADER = "Game Name:" + COLUMN_DIV;
            final String WHITE_HEADER = "Playing White:" + COLUMN_DIV;
            final String BLACK_HEADER = "Playing Black:" + COLUMN_DIV;
            final String ABSENT = SET_TEXT_COLOR_GREEN + "Available!" + SET_TEXT_COLOR_BLUE;
            final int ID_COLUMN_WIDTH = ID_HEADER.length() - DIV_WIDTH;
            int longestBlack = BLACK_HEADER.length() - DIV_WIDTH;
            int longestWhite = WHITE_HEADER.length() - DIV_WIDTH;
            int longestName = BLACK_HEADER.length() - DIV_WIDTH;

            for(var game:games){
                String black = game.blackUsername();
                String white = game.whiteUsername();
                longestBlack = max(longestBlack, black == null? 0 : black.length());
                longestWhite = max(longestWhite, white == null? 0 : white.length());
                longestName = max(longestName, game.gameName().length());
            }

            //padding in front
            longestName += 2;
            longestWhite += 2;
            longestBlack += 2;

            //Prefix spaces + 1 for prefix | + columns + divs
            final String HORIZONTAL_RULE = "      " + "-".repeat(1 + 4 * DIV_WIDTH + ID_COLUMN_WIDTH + longestName
                    + longestWhite + longestBlack) + "\n";


            StringBuilder listText = new StringBuilder(SET_TEXT_COLOR_BLUE + "Active games on the server:\n");
            listText.append(HORIZONTAL_RULE);
            listText.append(PREFIX);
            listText.append(ID_HEADER);
            listText.append(padTo(longestName, NAME_HEADER));
            listText.append(padTo(longestWhite, WHITE_HEADER));
            listText.append(padTo(longestBlack, BLACK_HEADER));
            listText.append(HORIZONTAL_RULE);
            for(int i = 0; i < gamesLastRetrieval.length;){
                GameData game = gamesLastRetrieval[i];
                i++;
                listText.append(PREFIX);
                listText.append(padTo(ID_COLUMN_WIDTH, ""+i));
                listText.append(COLUMN_DIV);

                listText.append(padTo(longestName, game.gameName()));
                listText.append(COLUMN_DIV);

                String white = game.whiteUsername();
                listText.append(padTo(longestWhite, white == null? ABSENT : white));
                listText.append(COLUMN_DIV);

                String black = game.blackUsername();
                listText.append(padTo(longestBlack, black == null? ABSENT : black));
                listText.append(COLUMN_DIV);
                listText.append("\n");
            }
            listText.append(HORIZONTAL_RULE);
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
        String empty = "no one";
        out.printf("The game you have requested to " + action + " is number %1$n. Its name is %2$s. White is being " +
                        "played by %3$s, and Black is being played by %4$s. Is this the game you meant?", gameID, name,
                white == null? empty: white, black == null? empty : black);
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
