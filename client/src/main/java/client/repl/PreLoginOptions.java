package client.repl;
import model.UserData;
import client.ResponseException;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.BiConsumer;

import static client.repl.EscapeSequences.*;
public class PreLoginOptions extends ChessMenuOptions{
    private static final String TAKEN = "already has that username.";

    public static void login(Scanner in, PrintStream out){
        checkFacade("login");
        try {
            UserData user = getUser(in, out, false);
            if(user == null){
                return;
            }
            authToken = facade.login(user).authToken();
            out.println(SET_TEXT_COLOR_BLUE + "Login successful! Opening user menu." + RESET_TEXT_COLOR);
        } catch (ResponseException e) {
            handleError(out, e, TAKEN);
        }
    }

    public static void register(Scanner in, PrintStream out){
        checkFacade("register");
        try {
            UserData user = getUser(in, out, true);
            if(user == null){
                return;
            }
            authToken = facade.register(user).authToken();
            out.println(SET_TEXT_COLOR_BLUE + "Registration and login successful! Opening user menu." + RESET_TEXT_COLOR);
        } catch (ResponseException e) {
            handleError(out, e, TAKEN);
        }
    }

    public static BiConsumer<Scanner, PrintStream> nextOnSuccess(BiConsumer<Scanner, PrintStream> first,
                                                                 BiConsumer<Scanner, PrintStream> next){
        return (Scanner in, PrintStream out) -> {
            first.accept(in, out);
            if (!authToken.isBlank()){
                next.accept(in, out);
            }
        };
    }

    private static UserData getUser(Scanner in, PrintStream out, boolean requireEmail){
        String username = getField(in, out, "your username");
        if(username.isBlank()){
            exitMessage(out);
            PreLoginOptions.username = "";
            return null;
        }
        PreLoginOptions.username = username;
        String password = getField(in, out, "your password");
        if(password.isBlank()){
            exitMessage(out);
            PreLoginOptions.username = "";
            return null;
        }
        if(!requireEmail){
            return new UserData(username, password, null);
        }

        String email = getField(in, out, "your email");
        if(email.isBlank()){
            exitMessage(out);
            PreLoginOptions.username = "";
            return null;
        }
        return new UserData(username, password, email);
    }

}
