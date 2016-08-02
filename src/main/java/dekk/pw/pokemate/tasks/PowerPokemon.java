package dekk.pw.pokemate.tasks;

import dekk.pw.pokemate.Context;

/**
 * Created by Kyle on 8/2/2016.
 */
public class PowerPokemon extends Task implements Runnable {

    PowerPokemon(final Context context) {
        super(context);
    }

    @Override
    public void run() {

        try {

        } catch(Exception ex) {

        } finally {
            context.addTask(new PowerPokemon(context));
        }

    }
}
