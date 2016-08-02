package dekk.pw.pokemate.tasks;

import com.google.maps.model.LatLng;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
        tasks.add(new ReleasePokemon(context));

        if (Config.isAutoEvolving()) {
            tasks.add(new EvolvePokemon(context));
        }

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

        if(Config.isUsingOrdinaryIncense()) {
            tasks.add(new IncenseItems(context));
        }
    }

    /**
     * This will execute all Tasks, then proceed to wait up to 5 seconds has passed.
     */
    public void run() {

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    new ConsoleGUIUpdate(context).run();
                } catch (Exception e) {
                    System.out.println("Rate Limit Exceeded");
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
        tasks.forEach(context::addTask);

    }
}
