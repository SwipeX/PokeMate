package dekk.pw.pokemate.tasks;

import com.google.maps.model.LatLng;
import dekk.pw.pokemate.Context;

import java.util.ArrayList;

/**
 * Created by TimD on 7/21/2016.
 */
public class TaskController extends Thread {
    private Context context;
    private static ArrayList<Task> tasks = new ArrayList<Task>();

    public TaskController(Context context) {
        this.context = context;
        tasks.add(new Navigate(context, new LatLng(context.getLat().get() + -.004, context.getLng().get() + -.004),
                new LatLng(context.getLat().get() + .004, context.getLng().get() + .004)));
        tasks.add(new Update());
        tasks.add(new CatchPokemon());
        tasks.add(new ReleasePokemon());
        tasks.add(new TagPokestop());
    }

    /**
     * This will execute all Tasks, then proceed to wait up to 5 seconds has passed.
     */
    public void run() {
        try {
            while (true) {
                for (Task task : tasks) {
                    task.run(context);
                }
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
