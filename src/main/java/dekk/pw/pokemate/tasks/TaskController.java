package dekk.pw.pokemate.tasks;

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
        tasks.add(new RefreshData());
        tasks.add(new CatchPokemon());
        tasks.add(new MoveLocation());
        tasks.add(new ReleasePokemon());
        tasks.add(new TagPokestop());
    }

    public void run() {
        try {
            while (true) {
                for (Task task : tasks) {
                    task.run(context);
                    Thread.sleep(500);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
