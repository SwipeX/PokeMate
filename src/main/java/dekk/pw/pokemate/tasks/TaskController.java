package dekk.pw.pokemate.tasks;

import com.google.maps.model.LatLng;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;

import java.util.ArrayList;

/**
 * Created by TimD on 7/21/2016.
 */
public class TaskController extends Thread {
    private static final double VARIANCE = Config.getRange();
    private final Context context;
    private static final ArrayList<Task> tasks = new ArrayList<>();

    public TaskController(final Context context) {
        this.context = context;
        tasks.add(new Update(context));

        tasks.add(new Navigate(context, new LatLng(context.getLat().get() - VARIANCE, context.getLng().get() - VARIANCE),
            new LatLng(context.getLat().get() + VARIANCE, context.getLng().get() + VARIANCE)));


        tasks.add(new CatchPokemon(context));

        if (Config.isAutoEvolving()) {
            tasks.add(new EvolvePokemon(context));
        }

        tasks.add(new ReleasePokemon(context));
        //tasks.add(new TagPokestop(context));

        if(Config.isEggsIncubating()) {
            tasks.add(new IncubateEgg(context));
        }

        if(Config.isEggsHatching()) {
            tasks.add(new HatchEgg(context));
        }

        if (Config.isDropItems()) {
            tasks.add(new DropItems(context));
        }

        if (Config.isConsoleNotification()) {
            tasks.add(new ConsoleGUIUpdate(context));
        }
    }

    /**
     * This will execute all Tasks, then proceed to wait up to 5 seconds has passed.
     */
    public void run() {

        tasks.forEach(context::addTask);

    }
}
