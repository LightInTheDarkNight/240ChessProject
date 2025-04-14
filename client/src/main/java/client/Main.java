package client;

import client.repl.GameplayOptions;
import client.repl.PostLoginOptions;
import client.repl.PreLoginOptions;
import client.repl.Repl;

import java.util.Scanner;
//import static client.repl.EscapeSequences.*;

public class Main {
    public static void main(String[] args) {
        var facade = new ServerFacade("http://localhost:8080");
        PreLoginOptions.setFacade(facade);
        Repl game = getGameplayMenu();
        Repl post = getPostLoginMenu(game);
        Repl pre = getPreLoginMenu(post);
        pre.accept(new Scanner(System.in), System.out);
    }
    private static Repl getPreLoginMenu(Repl postLogin){
        Repl out = new Repl("[Logged out] >>> ");
        out.setFunction("Login", PreLoginOptions.nextOnSuccess(PreLoginOptions::login, postLogin));
        out.setFunction("Register", PreLoginOptions.nextOnSuccess(PreLoginOptions::register, postLogin));
        return out;
    }

    private static Repl getPostLoginMenu(Repl gameplayMenu){
        Repl out = new Repl("[Logged in] >>> ", "Logged in! Please enter your next command:");
        out.addExitFunction("Logout", PostLoginOptions::logout);
        out.setFunction("CreateGame", PostLoginOptions::createGame);
        out.setFunction("ListGames", PostLoginOptions::listGames);
        out.setFunction("JoinGame", PostLoginOptions.nextOnSuccessFirstThrows(PostLoginOptions::joinGame,
                gameplayMenu));
        out.setFunction("ObserveGame", PostLoginOptions.nextOnSuccessFirstThrows(PostLoginOptions::observeGame,
                gameplayMenu));
        return out;
    }

    private static Repl getGameplayMenu(){
        Repl out = new Repl(GameplayOptions.prompt, "Game joined! please enter your next command:");
        out.addExitFunction("LeaveGame", GameplayOptions::leaveGame);
        out.setFunction("RedrawBoard", GameplayOptions::redrawBoard);
        out.setFunction("HighlightLegalMoves", GameplayOptions::highlightLegalMoves);
        out.setFunction("MakeMove",GameplayOptions::makeMove);
        out.setFunction("ResignGame", GameplayOptions::resignGame);
        return out;
    }
}