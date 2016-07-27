package dekk.pw.pokemate.tasks;

import com.google.maps.model.LatLng;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Config;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by TimD on 7/21/2016.
 */
public class TaskController extends Thread {
    public static final double VARIANCE = Config.getRange();
    private final Context context;
    private static ArrayList<Task> tasks = new ArrayList<>();

    public TaskController(final Context context) {
        this.context = context;
        tasks.add(new Navigate(context, new LatLng(context.getLat().get() - VARIANCE, context.getLng().get() - VARIANCE),
                new LatLng(context.getLat().get() + VARIANCE, context.getLng().get() + VARIANCE)));

        tasks.add(new Update(context));
        tasks.add(new CatchPokemon(context));

        if (Config.isAutoEvolving()) {
            tasks.add(new EvolvePokemon(context));
        }

        tasks.add(new ReleasePokemon(context));
        tasks.add(new TagPokestop(context));

        if(Config.isEggsIncubating()) {
            tasks.add(new IncubateEgg(context));
        }

        if(Config.isEggsHatching()) {
            tasks.add(new HatchEgg(context));
        }

        if (Config.isDropItems()) {
            tasks.add(new DropItems(context));
        }
    }

    /**
     * This will execute all Tasks, then proceed to wait up to 5 seconds has passed.
     */
    public void run() {
        try {
            while (true) {
                tasks.forEach(Task::run);
                TimeUnit.SECONDS.sleep(5);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
