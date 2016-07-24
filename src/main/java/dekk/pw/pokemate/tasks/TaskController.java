package dekk.pw.pokemate.tasks;

import com.google.maps.model.LatLng;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Config;

import java.util.ArrayList;

/**
 * Created by TimD on 7/21/2016.
 */
public class TaskController extends Thread {
    public static final double VARIANCE = .004;
    private Context context;
    private static ArrayList<Task> tasks = new ArrayList<Task>();

    public TaskController(Context context) {
        this.context = context;
        tasks.add(new Navigate(context, new LatLng(context.getLat().get() - VARIANCE, context.getLng().get() - VARIANCE),
                new LatLng(context.getLat().get() + VARIANCE, context.getLng().get() + VARIANCE)));
        tasks.add(new Update());
        tasks.add(new CatchPokemon());
        tasks.add(new ReleasePokemon());
        tasks.add(new EvolvePokemon());
        tasks.add(new TagPokestop());
        tasks.add(new HatchEgg());
        if(Config.isDropItems()) {
            tasks.add(new DropItems());
        }
    }

    /**
     * This will execute all Tasks, then proceed to wait up to 5 seconds has passed.
     */
    public void run() {
        try {
            while (true) {
                tasks.forEach(t -> t.run(context));
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
