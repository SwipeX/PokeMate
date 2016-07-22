package dekk.pw.pokemate.tasks;

import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Walking;

/**
 * Created by TimD on 7/22/2016.
 */
public class MoveRegion implements Task {
    @Override
    public void run(Context context) {
        Walking.setLocation(true, context);
    }
}
