package client.repl;
import model.UserData;
import web.ResponseException;
import web.ServerFacade;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Scanner;

import static client.repl.EscapeSequences.*;
public class OuterMenuOptions {
    public static ServerFacade facade = null;
    public static String authToken = "";
    public void setFacade(ServerFacade server){
        facade = server;
    }
    public static void login(Scanner in, PrintStream out){
        if(facade == null){
            throw new RuntimeException("Error: Static server facade variable not set before login method called");
        }

        try {
            authToken = facade.login(getUser(in, out, false)).authToken();
        } catch (ResponseException e) {
            handleError(out, e);
        }
    }
    public static void register(Scanner in, PrintStream out){
        if(facade == null){
            throw new RuntimeException("Error: Static server facade variable not set before register method called");
        }

        try {
            authToken = facade.login(getUser(in, out, true)).authToken();
        } catch (ResponseException e) {
            handleError(out, e);
        }
    }

    private static UserData getUser(Scanner in, PrintStream out, boolean requireEmail){
        String username = getField(in, out, "username");
        if(username.isBlank()){
            exitMessage(out);
            return null;
        }
        String password = getField(in, out, "password");
        if(password.isBlank()){
            exitMessage(out);
            return null;
        }
        if(!requireEmail){
            return new UserData(username, password, null);
        }
        String email = getField(in, out, "email");
        if(email.isBlank()){
            exitMessage(out);
            return null;
        }
        return new UserData(username, password, email);
    }

    private static String getField(Scanner in, PrintStream out, String fieldName){
        boolean repeat = false;
        String field;
        do{
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

    private static void exitMessage(PrintStream out){
        out.println(SET_TEXT_COLOR_BLUE + "Operation exited. Returning to menu." + RESET_TEXT_COLOR);
    }
    private static void requiredMessage(PrintStream out, String fieldName){
        out.println(SET_TEXT_COLOR_BLUE + "Sorry, " + fieldName +
                " is a required field. If you want to cancel the operation, please type 'exit'." + RESET_TEXT_COLOR);
    }
    private static void prompt(PrintStream out, String fieldName){
        out.print(SET_TEXT_COLOR_BLUE + "Please enter your " + fieldName + ": \n" + "[" + fieldName + "] >>> " +
                RESET_TEXT_COLOR);
    }

    private static void handleError(PrintStream out, ResponseException e){
        out.print(SET_TEXT_COLOR_RED);
        switch(e.getStatusCode()){
            case 400 -> out.println("Sorry, somehow invalid input was sent to the server. Please try again.");
            case 401 -> out.println("Invalid username or password. Please try again.");
            case 403 -> out.println("Sorry, someone already has that username. Please try again.");
            default -> out.print("Sorry, a server error occurred. Please try again.");
        }
        out.print(RESET_TEXT_COLOR);
    }
}
