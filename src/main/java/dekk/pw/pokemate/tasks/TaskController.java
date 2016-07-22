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
        tasks.add(new Update());
        tasks.add(new CatchPokemon());
        tasks.add(new ReleasePokemon());
        tasks.add(new NavigatePokestop());
        tasks.add(new TagPokestop());
        tasks.add(new MoveRegion());
    }

    /**
     * This will execute all Tasks, then proceed to wait up to 5 seconds has passed.
     */
    public void run() {
        try {
            while (true) {
                long loopStart = System.currentTimeMillis();
                for (Task task : tasks) {
                    task.run(context);
                }
                Thread.sleep(5000 - Math.max(0, System.currentTimeMillis() - loopStart));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
