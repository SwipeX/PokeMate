package dekk.pw.pokemate.tasks;

import com.google.maps.model.LatLng;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Config;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by TimD on 7/21/2016.
 */
public class TaskController extends Thread {
    public static final double VARIANCE = Config.getRange();
    private final Context context;
    private static ArrayList<Task> tasks = new ArrayList<>();

    public TaskController(final Context context) {
        this.context = context;
    }

    /**
     * This will execute all Tasks, then proceed to wait up to 5 seconds has passed.
     */
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(6);


        executor.submit(new Navigate(context,
            new LatLng(context.getLat().get() - VARIANCE, context.getLng().get() - VARIANCE),
            new LatLng(context.getLat().get() + VARIANCE, context.getLng().get() + VARIANCE)));


        executor.submit(new Update(context));
        executor.submit(new CatchPokemon(context));
        executor.submit(new ReleasePokemon(context));
        executor.submit(new TagPokestop(context));

        if (Config.isAutoEvolving()) executor.submit(new EvolvePokemon(context));
        if (Config.isEggsIncubating()) executor.submit(new IncubateEgg(context));
        if (Config.isEggsHatching()) executor.submit(new HatchEgg(context));
        if (Config.isDropItems()) executor.submit(new DropItems(context));

    }
}

