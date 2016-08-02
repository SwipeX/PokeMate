package dekk.pw.pokemate.tasks;

import dekk.pw.pokemate.Context;

/**
 * Created by TimD on 7/21/2016.
 */
public abstract class Task implements Runnable {

    static long APIStartTime;
    static long APIElapsedTime;

    final Context context;

    Task(final Context context) {
        this.context = context;
    }


}
