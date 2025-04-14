package client.repl;

import java.io.PrintStream;
import java.util.*;
import java.util.function.BiConsumer;
import static client.repl.EscapeSequences.*;

public class Repl implements BiConsumer<Scanner, PrintStream>{
    private final HashMap<String, BiConsumer<Scanner, PrintStream>> functions = new HashMap<>();
    private String welcomeMessage = "Welcome! Please enter your commands below.";
    private String prompt = ">>> ";
    private final Set<String> exitWords = new HashSet<>();
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

    public void accept(Scanner in, PrintStream out){
        out.println(SET_TEXT_COLOR_BLUE + welcomeMessage + RESET_TEXT_COLOR);

        while(true){
            out.print(SET_TEXT_COLOR_BLUE + prompt + RESET_TEXT_COLOR);
            String option = in.next().toLowerCase(Locale.ROOT);
            in.nextLine();
            var function = functions.get(option);
            if(function == null){
                errorHandler(out);
            }else {
                function.accept(in, out);
                if(exitWords.contains(option)){
                    break;
                }
            }
        }
    }

    public void setFunction(String key, BiConsumer<Scanner, PrintStream> function){
        functions.put(key, function);
        alias(key.toLowerCase(Locale.ROOT), key);
    }

    public void addExitFunction(String key, BiConsumer<Scanner, PrintStream> function){
        exitWords.add(key);
        setFunction(key, function);
        if(!key.equalsIgnoreCase("Exit")){
            setFunction("Exit", function);
        }
    }

    public void alias(String alias, String key){
        functions.put(alias, functions.get(key));
        if(exitWords.contains(key)){
            exitWords.add(alias);
        }
    }

    private void errorHandler(PrintStream out){
        out.println(SET_TEXT_COLOR_RED + "Unrecognized command. Type 'help' to see your command options.");
        out.println("Capitalization is ignored." + RESET_TEXT_COLOR);

    }

    private void getHelp(Scanner in, PrintStream out){
        out.println("Valid Menu Options include: ");
        removeDigits();
        Set<String> keys = new HashSet<>(functions.keySet());
        for(String key: functions.keySet()){
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
        functions.keySet().removeIf((x)-> DIGITS.contains(x.charAt(0)));
        exitWords.removeIf((x)-> DIGITS.contains(x.charAt(0)));
    }
}
