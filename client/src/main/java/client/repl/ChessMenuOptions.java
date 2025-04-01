package client.repl;

import chess.ChessGame;
import web.ResponseException;
import web.ServerFacade;

import java.io.PrintStream;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;

import static client.repl.EscapeSequences.*;
import static client.repl.EscapeSequences.RESET_TEXT_COLOR;

public class ChessMenuOptions {
    protected static ServerFacade facade = null;
    protected static String authToken = "";
    protected static String username = "";
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
                default -> requiredMessage(out, "player color");
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
            out.print("[Y/n] :\n >>> ");
            field = in.nextLine().toLowerCase(Locale.ROOT).charAt(0);
            if(field == 'y'){
                return true;
            }else if(field == 'n'){
                return false;
            }else{
                out.println(SET_TEXT_COLOR_RED + "Sorry, your input was not recognized. Please try again."
                        + SET_TEXT_COLOR_BLUE);
                out.print("If you want to cancel, enter 'n', and then type 'exit' when prompted next if you don't " +
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
    protected static void exitMessage(PrintStream out){
        out.println(SET_TEXT_COLOR_BLUE + "Operation exited. Returning to menu." + RESET_TEXT_COLOR);
    }
    private static void requiredMessage(PrintStream out, String fieldName){
        out.println(SET_TEXT_COLOR_BLUE + "Sorry, " + fieldName +
                " is required. If you want to cancel the operation, please type 'exit'." + RESET_TEXT_COLOR);
    }
    private static void outOfRangeMessage(PrintStream out, String fieldName, int wrong, int high){
        out.println(SET_TEXT_COLOR_BLUE + "Sorry, " + wrong + " is not a valid option for " + fieldName + ". " +
                "Valid options lie between " + 1 + " and " + high + "inclusive. " +
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
