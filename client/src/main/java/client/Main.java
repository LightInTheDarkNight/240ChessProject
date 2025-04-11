package client;

import client.repl.PostLoginOptions;
import client.repl.PreLoginOptions;
import client.repl.Repl;

import java.util.Scanner;
//import static client.repls.EscapeSequences.*;

public class Main {
    //private String authToken;
    public static void main(String[] args) {
        var facade = new ServerFacade("http://localhost:8080");
        PreLoginOptions.setFacade(facade);
        Repl post = getPostLoginMenu();
        Repl pre = getPreLoginMenu(post);
        pre.accept(new Scanner(System.in), System.out);
    }
    private static Repl getPreLoginMenu(Repl postLogin){
        Repl out = new Repl("[Logged out] >>> ");
        out.setFunction("Login", PreLoginOptions.nextOnSuccess(PreLoginOptions::login, postLogin));
        out.setFunction("Register", PreLoginOptions.nextOnSuccess(PreLoginOptions::register, postLogin));
        return out;
    }

    private static Repl getPostLoginMenu(){
        Repl out = new Repl("[Logged in] >>> ", "Logged in! Please enter your next command:");
        out.addExitFunction("Logout", PostLoginOptions::logout);
        out.setFunction("CreateGame", PostLoginOptions::createGame);
        out.setFunction("ListGames", PostLoginOptions::listGames);
        out.setFunction("JoinGame", PostLoginOptions::joinGame);
        out.setFunction("ObserveGame", PostLoginOptions::observeGame);
        return out;
    }
}