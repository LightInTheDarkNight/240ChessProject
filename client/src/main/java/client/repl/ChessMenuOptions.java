package client.repl;

import chess.ChessGame;
import client.ResponseException;
import client.ServerFacade;
import client.WebSocketFacade;

import java.io.PrintStream;
import java.util.*;

import static client.repl.EscapeSequences.*;

public class ChessMenuOptions {
    protected static ServerFacade facade = null;
    protected static WebSocketFacade socket = null;
    protected static String authToken = "";
    protected static ChessGame currentGame = null;
    protected static int currentGameID = 0;
    protected static ChessGame.TeamColor perspective = null;
    protected static String username = "";
    protected static final String WHITE_SQUARE_COLOR = SET_BG_COLOR_LIGHT_GREY;
    protected static final String BLACK_SQUARE_COLOR = SET_BG_COLOR_DARK_GREY;
    protected static final String WHITE_PIECE_COLOR = SET_TEXT_COLOR_WHITE;
    protected static final String BLACK_PIECE_COLOR = SET_TEXT_COLOR_BLACK;
    protected static final String HIGHLIGHT_SQUARE_COLOR = SET_BG_COLOR_YELLOW;

    public static void setFacade(ServerFacade server){
        facade = server;
    }

    protected static void checkFacade(String function){
        if(facade == null){
            throw new RuntimeException(
                    "Error: Static server facade variable not set before " + function + " method called");
        }
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
            }catch(InputMismatchException e){
                field = 0;
            }finally{
                in.nextLine();
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

}
