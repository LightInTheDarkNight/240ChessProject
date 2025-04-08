package client.repl;

import java.io.PrintStream;
import java.util.*;
import java.util.function.BiConsumer;
import static client.repl.EscapeSequences.*;

public class Repl implements BiConsumer<Scanner, PrintStream>{
    private final HashMap<String, BiConsumer<Scanner, PrintStream>> FUNCTIONS = new HashMap<>();
    private String welcomeMessage = "Welcome! Please enter your commands below.";
    private String prompt = ">>> ";
    private final Set<String> EXIT_WORDS = new HashSet<>();
    private static final Set<Character> DIGITS = new HashSet<>(List.of('1','2','3','4','5','6','7','8','9','0'));

    public Repl(){
        addExitFunction("Exit", (x,y)->{});
        setFunction("Help", this::getHelp);
    }
    public Repl(String prompt){
        this();
        this.prompt = prompt;
    }
    public Repl(String prompt, String welcome){
        this(prompt);
        welcomeMessage = welcome;
    }
    public Repl(String stop, String help, String welcome, String prompt){
        this(prompt, welcome);
        alias(stop, "Exit");
        alias(help, "Help");
    }

    public void accept(Scanner in, PrintStream out){
        out.println(SET_TEXT_COLOR_BLUE + welcomeMessage + RESET_TEXT_COLOR);

        while(true){
            out.print(SET_TEXT_COLOR_BLUE + prompt + RESET_TEXT_COLOR);
            String option = in.next().toLowerCase(Locale.ROOT);
            in.nextLine();
            var function = FUNCTIONS.get(option);
            if(function == null){
                errorHandler(out);
            }else {
                function.accept(in, out);
                if(EXIT_WORDS.contains(option)){
                    break;
                }
            }
        }
    }

    public void setFunction(String key, BiConsumer<Scanner, PrintStream> function){
        FUNCTIONS.put(key, function);
        alias(key.toLowerCase(Locale.ROOT), key);
    }

    public void addExitFunction(String key, BiConsumer<Scanner, PrintStream> function){
        EXIT_WORDS.add(key.toLowerCase(Locale.ROOT));
        setFunction(key, function);
        if(EXIT_WORDS.size() == 2){
            setFunction("Exit", function);
        }
    }

    public void alias(String alias, String key){
        FUNCTIONS.put(alias, FUNCTIONS.get(key));
        if(EXIT_WORDS.contains(key)){
            EXIT_WORDS.add(alias);
        }
    }

    private void errorHandler(PrintStream out){
        out.println(SET_TEXT_COLOR_RED + "Unrecognized command. Type help to see your command options.");
        out.println("Capitalization is ignored." + RESET_TEXT_COLOR);

    }

    private void getHelp(Scanner in, PrintStream out){
        out.println("Valid Menu Options include: ");
        removeDigits();
        Set<String> keys = new HashSet<>(FUNCTIONS.keySet());
        for(String key: FUNCTIONS.keySet()){
            String lower = key.toLowerCase(Locale.ROOT);
            if(!lower.equals(key)){
                keys.remove(lower);
            }
        }
        int i = 1;
        out.println("   " + i + ". Help");
        alias("" + i, "Help");
        i++;
        for(String key:keys){
            if(!key.equals("Help") && !key.equals("Exit")){
                out.println("   " + i + ". " + key);
                alias("" + i, key);
                i++;
            }
        }
        out.println("   " + i + ". Exit");
        alias("" + i, "Exit");
    }

    private void removeDigits(){
        FUNCTIONS.keySet().removeIf((x)-> DIGITS.contains(x.charAt(0)));
        EXIT_WORDS.removeIf((x)-> DIGITS.contains(x.charAt(0)));
    }
}
