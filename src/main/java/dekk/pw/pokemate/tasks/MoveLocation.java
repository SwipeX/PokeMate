package dekk.pw.pokemate.tasks;

import com.google.common.geometry.S2LatLng;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Walking;

/**
 * Created by TimD on 7/21/2016.
 */
public class MoveLocation implements Task {
    private static final int MOVE_TIMEOUT = 5000;
    private static long lastMove = System.currentTimeMillis();

    public void run(Context context) {
        if (!context.isWalking()) {
            try {
                if (context.getApi().getMap().getCatchablePokemon().size() == 0 &&
                        context.getMapObjects().getPokestops().stream().filter(Pokestop::canLoot).count() == 0) {
                    System.out.println("No pokemon or usable pokestops remain in this area. Moving.");
                    if (System.currentTimeMillis() - lastMove > MOVE_TIMEOUT) {
                        double lat = context.getApi().getLatitude();
                        double lon = context.getApi().getLongitude();
                        Walking.walk(S2LatLng.fromDegrees(context.getApi().getLatitude() + Walking.getSmallRandom(),
                                context.getApi().getLongitude() + Walking.getSmallRandom()), context);
                        System.out.println(lat + " - " + lon + " -> " + context.getApi().getLatitude() + " - " + context.getApi().getLongitude());
                        resetTime();
                    }
                }
            } catch (LoginFailedException | RemoteServerException e) {
                e.printStackTrace();
            }
        }
    }

    public static void resetTime() {
        lastMove = System.currentTimeMillis();
    }
}
