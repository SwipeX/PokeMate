package dekk.pw.pokemate.tasks;

import dekk.pw.pokemate.Context;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Andrew Sidhu on 8/1/2016.
 */
public class ConsoleGUIUpdate extends Task implements Runnable {

    ConsoleGUIUpdate(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        Set set = context.getConsoleStrings().entrySet();
        Iterator i = set.iterator();

        // Clears old console output. (Probably won't work on windows)
        System.out.print("\033[H\033[2J");
        System.out.print("Console GUI: [" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]\n");

        context.getConsoleStrings().forEach( (key,value) -> {
            if (value.isEmpty()) {
                System.out.println(key + ":");
            } else {
                System.out.printf("%-15.15s %-30s\n", "\t" + key, value);
            }
        });
        context.addTask(new ConsoleGUIUpdate(context));
    }
}
