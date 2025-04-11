package client.repl;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import client.ResponseException;
import client.ServerFacade;

import java.io.PrintStream;
import java.util.*;

import static client.repl.EscapeSequences.*;
import static client.repl.EscapeSequences.BLACK_PAWN;

public class ChessMenuOptions {
    protected static ServerFacade facade = null;
    protected static String authToken = "";
    protected static ChessGame currentGame = null;
    protected static ChessGame.TeamColor perspective = null;
    protected static final String WHITE_SQUARE_COLOR = SET_BG_COLOR_LIGHT_GREY;
    protected static final String BLACK_SQUARE_COLOR = SET_BG_COLOR_DARK_GREY;
    protected static final String WHITE_PIECE_COLOR = SET_TEXT_COLOR_WHITE;
    protected static final String BLACK_PIECE_COLOR = SET_TEXT_COLOR_BLACK;

    public static void setFacade(ServerFacade server){
        facade = server;
    }

    protected static String getField(Scanner in, PrintStream out, String fieldName){
        boolean repeat;
        String field;
        do{
            repeat = false;
            prompt(out, fieldName);
            field = in.nextLine();
            if("exit".equals(field.toLowerCase(Locale.ROOT))){
                return "";
            }
            if(field.isBlank()){
                repeat = true;
                requiredMessage(out, fieldName);
            }
        }while(repeat);
        return field;
    }

    protected static ChessGame.TeamColor getColor(Scanner in, PrintStream out){
        String field;
        do{
            out.print(SET_TEXT_COLOR_BLUE + "Please enter the color you want to play/observe as: \n" +
                    "[WHITE/w/1 or BLACK/b/2] >>> " + RESET_TEXT_COLOR);
            field = in.nextLine().toLowerCase(Locale.ROOT);
            switch (field) {
                case "exit" -> {
                    return null;
                }
                case "white", "w", "1" -> {
                    return ChessGame.TeamColor.WHITE;
                }
                case "black", "b", "2" -> {
                    return ChessGame.TeamColor.BLACK;
                }
                default -> {
                    requiredMessage(out, "player color");
                    out.println(SET_TEXT_COLOR_BLUE + "Options are WHITE/W/w/1 or BLACK/b/2" + RESET_TEXT_COLOR);
                }
            }
        }while(true);
    }

    protected static int getInt(Scanner in, PrintStream out, String fieldName, int high){
        boolean repeat;
        int field;
        do{
            repeat = false;
            prompt(out, fieldName);
            try{
                field = in.nextInt();
                in.nextLine();
            }catch(InputMismatchException e){
                field = 0;
            }
            if(field == -1){
                return -1;
            }
            if(field < 1 || field > high){
                repeat = true;
                outOfRangeMessage(out, fieldName, field, high);
            }
        }while(repeat);
        return field;
    }

    protected static boolean confirm(Scanner in, PrintStream out){
        char field;
        do{
            out.print(SET_TEXT_COLOR_BLUE + "[Y/n] >>> " + RESET_TEXT_COLOR);
            String input =  in.nextLine().toLowerCase(Locale.ROOT);
            field = input.isBlank()? ' ' : input.charAt(0);
            if(field == 'y'){
                return true;
            }else if(field == 'n'){
                return false;
            }else{
                out.println(SET_TEXT_COLOR_RED + "Sorry, your input was not recognized. Please try again."
                        + SET_TEXT_COLOR_BLUE);
                out.println("If you want to cancel, enter 'n', and then type 'exit' when prompted next if you don't " +
                        "exit automatically.");
            }
        }while(true);
    }

    protected static void checkFacade(String function){
        if(facade == null){
            throw new RuntimeException(
                    "Error: Static server facade variable not set before " + function + " method called");
        }
    }

    protected static void drawBoard(PrintStream out){
        if(currentGame == null){
            throw new RuntimeException("Error: current game is not set.");
        }
        if(perspective == null){
            throw new RuntimeException("Error: game perspective is not set.");
        }
        List<String> rows = new ArrayList<>(Arrays.asList(getRowStrings()));
        if(perspective == ChessGame.TeamColor.BLACK){
            rows = rows.reversed();
        }
        for(String row:rows){
            out.print(row);
        }
    }

    private static String[] getRowStrings(){
        String[][][] squares = getSquares();
        String[] rows = new String[10];
        for(int i = 0; i < 10; i++){
            rows[i] = getRowString(squares[i]);
        }
        return rows;
    }

    private static String[][][] getSquares(){
        String[][][] out = new String[10][10][];
        String[] rowLabels = new String[]{ONE_LABEL, TWO_LABEL, THREE_LABEL, FOUR_LABEL, FIVE_LABEL, SIX_LABEL,
                SEVEN_LABEL, EIGHT_LABEL};
        String[] columnLabels = new String[]{A_LABEL, B_LABEL, C_LABEL, D_LABEL, E_LABEL, F_LABEL, G_LABEL, H_LABEL};
        for(int i = 8; i > 0; i --){
            String[] row = getLabelStrings(rowLabels[i-1]);
            String[] col = getLabelStrings(columnLabels[i-1]);
            out[i][0] = out[i][9] = row;
            out[0][i] = out[9][i] = col;
        }
        out[0][0] = out[0][9] = out[9][0] = out[9][9] = getLabelStrings(EMPTY);
        for(int i = 1; i < 9; i ++){
            for(int j = 1; j < 9; j ++){
                String squareColor = BLACK_SQUARE_COLOR; // Dark square
                if((i + j) % 2 == 0){
                    squareColor = WHITE_SQUARE_COLOR; // Light square
                }
                out[i][j] = getSquareStrings(squareColor, currentGame.getBoard().getPiece(new ChessPosition(i, j)));
            }
        }
        return out;
    }

    private static String[] getLabelStrings(String label){
        return new String[]{SET_BG_COLOR_BLACK + EMPTY, SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + label,
                SET_BG_COLOR_BLACK + EMPTY};
    }

    private static String[] getSquareStrings(String squareColor, ChessPiece piece){
        String pieceRep = squareColor + getPieceString(piece);
        String rowBorder = squareColor + EMPTY;
        return new String[]{rowBorder, pieceRep, rowBorder};
    }

    private static String getPieceString(ChessPiece piece){
        if(piece == null){
            return EMPTY;
        }
        String[] blackStrings = new String[]{
                BLACK_KING, BLACK_QUEEN, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK, BLACK_PAWN};
        String[] whiteStrings = new String[]{
                WHITE_KING, WHITE_QUEEN, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK, WHITE_PAWN};
        for(int i = 0; i < blackStrings.length; i++){
            blackStrings[i] = BLACK_PIECE_COLOR + blackStrings[i];
            whiteStrings[i] = WHITE_PIECE_COLOR + whiteStrings[i];
        }
        String[] toUse = switch(piece.getTeamColor()){
            case WHITE -> whiteStrings;
            case BLACK -> blackStrings;
        };
        return toUse[piece.getPieceType().ordinal()];

    }

    private static String getRowString(String[][] squares){
        StringBuilder[] builders = new StringBuilder[]{
                new StringBuilder(), new StringBuilder(), new StringBuilder()};
        for (String[] square : squares) {
            for (int j = 0; j < 3; j++) {
                builders[j].append(square[j]);
            }
        }
        for(StringBuilder builder:builders){
            builder.append("\n");
        }
        return "" + builders[0] + builders[1] + builders[2];
    }

    protected static void exitMessage(PrintStream out){
        out.println(SET_TEXT_COLOR_BLUE + "Operation exited. Returning to menu." + RESET_TEXT_COLOR);
    }

    private static void requiredMessage(PrintStream out, String fieldName){
        out.println(SET_TEXT_COLOR_BLUE + "Sorry, " + fieldName +
                " is required. If you want to cancel the operation, please type 'exit'." + RESET_TEXT_COLOR);
    }

    private static void outOfRangeMessage(PrintStream out, String fieldName, int wrong, int high){
        out.println(SET_TEXT_COLOR_BLUE + "Sorry, " + wrong + " is not a valid option for " + fieldName + ". " +
                "Valid options lie between " + 1 + " and " + high + " inclusive. " +
                "If you want to cancel the operation, please type '-1'." + RESET_TEXT_COLOR);
    }

    protected static void prompt(PrintStream out, String fieldName){
        out.print(SET_TEXT_COLOR_BLUE + "Please enter " + fieldName + ": \n" + "[" + fieldName + "] >>> " +
                RESET_TEXT_COLOR);
    }

    protected static void handleError(PrintStream out, ResponseException e, String taken){
        out.print(SET_TEXT_COLOR_RED);
        switch(e.getStatusCode()){
            case 400 -> out.println("Sorry, somehow invalid input was sent to the server. Please try again.");
            case 401 -> out.println("Invalid username or password. Please try again.");
            case 403 -> out.println("Sorry, someone " + taken + " Please try again.");
            default -> out.print("Sorry, a server error occurred. Please try again.");
        }
        out.print(RESET_TEXT_COLOR);
    }
}
