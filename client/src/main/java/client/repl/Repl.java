package client.repl;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.BiConsumer;
import static client.repl.EscapeSequences.*;

public class Repl implements BiConsumer<Scanner, PrintStream>{
    private final HashMap<String, BiConsumer<Scanner, PrintStream>> functions = new HashMap<>();
    private String stopKeyword = "exit";
    private String helpKeyword = "help";
    private String welcomeMessage = "Welcome! please enter your commands below.";
    private String prompt = ">>> ";

    public Repl(String stop, String help, String welcome, String prompt){
        stopKeyword = stop;
        helpKeyword = help;
        welcomeMessage = welcome;
        this.prompt = prompt;
    }

    public void accept(Scanner in, PrintStream out){
        out.println(welcomeMessage);
        while(true){
            out.print(prompt);
            String option = in.next();
            var function = functions.get(option.toLowerCase(Locale.ROOT));
            if(stopKeyword.equals(option)){
                break;
            }
            if(helpKeyword.equals(option)){
                helpFunction(in, out);
            }else if(function == null){
                errorHandler(in, out);
            }else {
                function.accept(in, out);
            }
        }
    }

    public void add(String key, BiConsumer<Scanner, PrintStream> function){
        functions.put(key, function);
    }
    public void alias(String alias, String key){
        add(alias, functions.get(key));
    }

    private void errorHandler(Scanner in, PrintStream out){
        if (in.hasNext()) {
            in.nextLine();
        }
        out.println(SET_TEXT_COLOR_RED + "Unrecognized command. Type " + helpKeyword + " to see your command options.");
        out.println("Capitalization is ignored.");

    }
    private void helpFunction(Scanner in, PrintStream out){
        out.println("Valid Menu Options include: ");
        int i = 1;
        for(String key:functions.keySet()){
            out.println("   " + i + ". " + key);
        }
    }
}
